package com.pedrogavazzi.controleestudos.data

import android.content.Context
import com.pedrogavazzi.controleestudos.notifications.AlarmScheduler
import kotlinx.coroutines.flow.Flow

/**
 * Ponto único de acesso aos dados do app (matérias e aulas) e responsável por manter
 * os alarmes de notificação sincronizados sempre que uma aula é criada, reagendada,
 * concluída, ou quando as preferências globais de notificação mudam.
 */
class StudyRepository(context: Context, private val preferencias: PreferenciasApp) {

    private val db = AppDatabase.getInstance(context)
    private val materiaDao = db.materiaDao()
    private val aulaDao = db.aulaDao()
    private val alarmScheduler = AlarmScheduler(context)

    // ---------- Matérias ----------

    fun observarMaterias(): Flow<List<Materia>> = materiaDao.observarTodas()

    fun observarMateria(id: Long): Flow<Materia?> = materiaDao.observarPorId(id)

    suspend fun buscarMateria(id: Long): Materia? = materiaDao.buscarPorId(id)

    /** Cria uma nova matéria já gerando as N aulas (numeradas 1..N) sem data definida. */
    suspend fun criarMateria(nome: String, totalAulas: Int, corHex: String): Long {
        val materia = Materia(nome = nome, totalAulas = totalAulas, corHex = corHex)
        val materiaId = materiaDao.inserir(materia)
        val novasAulas = (1..totalAulas).map { numero ->
            Aula(materiaId = materiaId, numero = numero)
        }
        if (novasAulas.isNotEmpty()) aulaDao.inserirTodas(novasAulas)
        return materiaId
    }

    /**
     * Atualiza nome/cor da matéria e, se o número de aulas mudou, ajusta a lista por
     * QUANTIDADE (não mais comparando contra o maior número existente) — como aulas avulsas
     * podem ser adicionadas/excluídas individualmente, a numeração pode ter buracos, e a
     * lógica antiga (baseada em "numero > novoTotalAulas") podia duplicar ou apagar a aula
     * errada nesse caso. Ao diminuir, prioriza remover aulas ainda "vazias" (sem data,
     * observação, caderno ou conclusão) antes de qualquer aula com conteúdo do usuário.
     */
    suspend fun atualizarMateria(materia: Materia, novoTotalAulas: Int) {
        val aulasAtuais = aulaDao.buscarTodasDaMateriaSuspend(materia.id)
        val totalAtual = aulasAtuais.size
        materiaDao.atualizar(materia.copy(totalAulas = novoTotalAulas))

        if (novoTotalAulas > totalAtual) {
            val maiorNumero = aulasAtuais.maxOfOrNull { it.numero } ?: 0
            val novasAulas = (1..(novoTotalAulas - totalAtual)).map { indice ->
                Aula(materiaId = materia.id, numero = maiorNumero + indice)
            }
            aulaDao.inserirTodas(novasAulas)
        } else if (novoTotalAulas < totalAtual) {
            val quantidadeARemover = totalAtual - novoTotalAulas
            val candidatasARemover = aulasAtuais.sortedWith(
                compareBy<Aula> { aulaTemConteudo(it) }.thenByDescending { it.numero }
            )
            val aRemover = candidatasARemover.take(quantidadeARemover)
            aRemover.forEach { alarmScheduler.cancelar(it) }
            aRemover.forEach { aulaDao.excluir(it) }
        }
    }

    /** Uma aula "vazia" (sem data, observação, anotações do caderno ou conclusão) pode ser
     *  removida com segurança primeiro ao diminuir o número de aulas de uma matéria. */
    private fun aulaTemConteudo(aula: Aula): Boolean =
        aula.dataHoraMillis != null || aula.observacao.isNotBlank() ||
            aula.anotacoesCaderno.isNotBlank() || aula.concluida || aula.nomePersonalizado != null

    suspend fun excluirMateria(materia: Materia) {
        aulaDao.buscarTodasDaMateriaSuspend(materia.id).forEach { alarmScheduler.cancelar(it) }
        materiaDao.excluir(materia)
    }

    /** Busca todas as matérias com suas aulas de uma vez, para exportar um resumo em texto. */
    suspend fun buscarTudoParaExportacao(): List<Pair<Materia, List<Aula>>> {
        val materias = materiaDao.buscarTodasSuspend()
        return materias.map { materia -> materia to aulaDao.buscarTodasDaMateriaSuspend(materia.id).sortedBy { it.numero } }
    }

    // ---------- Aulas ----------

    fun observarAulasDaMateria(materiaId: Long): Flow<List<Aula>> =
        aulaDao.observarPorMateria(materiaId)

    fun observarTodasAsAulas(): Flow<List<Aula>> = aulaDao.observarTodas()

    fun observarAula(id: Long): Flow<Aula?> = aulaDao.observarPorId(id)

    /** Adiciona uma aula avulsa ao final da matéria (numerada automaticamente), sem data definida. */
    suspend fun adicionarAula(materiaId: Long): Aula {
        val aulasAtuais = aulaDao.buscarTodasDaMateriaSuspend(materiaId)
        val proximoNumero = (aulasAtuais.maxOfOrNull { it.numero } ?: 0) + 1
        val novaAula = Aula(materiaId = materiaId, numero = proximoNumero)
        val id = aulaDao.inserir(novaAula)
        materiaDao.buscarPorId(materiaId)?.let { materia ->
            materiaDao.atualizar(materia.copy(totalAulas = aulasAtuais.size + 1))
        }
        return novaAula.copy(id = id)
    }

    /** Define um nome customizado para a aula, ou volta ao padrão "Aula N" se [novoNome] for vazio. */
    suspend fun renomearAula(aula: Aula, novoNome: String?) {
        aulaDao.atualizar(aula.copy(nomePersonalizado = novoNome?.trim()?.takeIf { it.isNotBlank() }))
    }

    /** Define ou altera a data/horário de uma aula e reprograma o alerta, se ativado. */
    suspend fun agendarAula(aula: Aula, novaDataHoraMillis: Long?) {
        val atualizada = aula.copy(dataHoraMillis = novaDataHoraMillis)
        aulaDao.atualizar(atualizada)
        sincronizarAlarme(atualizada)
    }

    /** Reagenda uma aula não concluída para nova data/horário, incrementando o contador. */
    suspend fun reagendarAula(aula: Aula, novaDataHoraMillis: Long) {
        val atualizada = aula.copy(
            dataHoraMillis = novaDataHoraMillis,
            vezesReagendada = aula.vezesReagendada + 1,
            concluida = false,
            concluidaEmMillis = null
        )
        aulaDao.atualizar(atualizada)
        sincronizarAlarme(atualizada)
    }

    /**
     * Agenda várias aulas de uma matéria de uma vez, em sequência a partir da primeira aula
     * ainda sem data, espaçadas por [intervaloDias] dias (qualquer número, escolhido pelo
     * usuário), sempre no mesmo horário de [dataHoraInicialMillis]. Se [apenasDiasUteis]
     * estiver ativo, o intervalo conta só dias de segunda a sexta — sábado e domingo são
     * pulados na própria contagem (não é "soma dias corridos e empurra depois", que contaria
     * o fim de semana como se fosse dia útil e podia até fazer duas aulas caírem juntas).
     */
    suspend fun agendarEmLote(
        materiaId: Long,
        dataHoraInicialMillis: Long,
        intervaloDias: Int,
        quantidade: Int,
        apenasDiasUteis: Boolean
    ) {
        val aulasParaAgendar = aulaDao.buscarTodasDaMateriaSuspend(materiaId)
            .filter { it.dataHoraMillis == null }
            .sortedBy { it.numero }
            .take(quantidade)

        val datas = AgendamentoUtil.calcularDatas(
            dataHoraInicialMillis, intervaloDias, aulasParaAgendar.size, apenasDiasUteis
        )

        aulasParaAgendar.zip(datas).forEach { (aula, data) ->
            val atualizada = aula.copy(dataHoraMillis = data)
            aulaDao.atualizar(atualizada)
            sincronizarAlarme(atualizada)
        }
    }

    suspend fun marcarConclusao(aula: Aula, concluida: Boolean) {
        val atualizada = aula.copy(
            concluida = concluida,
            concluidaEmMillis = if (concluida) System.currentTimeMillis() else null
        )
        aulaDao.atualizar(atualizada)
        if (concluida) {
            alarmScheduler.cancelar(atualizada)
        } else {
            sincronizarAlarme(atualizada)
        }
    }

    suspend fun salvarObservacao(aula: Aula, observacao: String) {
        aulaDao.atualizar(aula.copy(observacao = observacao))
    }

    /** Salva as anotações longas do caderno de aula (independentes da observação curta). */
    suspend fun salvarAnotacaoCaderno(aula: Aula, anotacoes: String) {
        aulaDao.atualizar(aula.copy(anotacoesCaderno = anotacoes))
    }

    suspend fun excluirAula(aula: Aula) {
        alarmScheduler.cancelar(aula)
        aulaDao.excluir(aula)
        materiaDao.buscarPorId(aula.materiaId)?.let { materia ->
            materiaDao.atualizar(materia.copy(totalAulas = aulaDao.contarAulasDaMateria(aula.materiaId)))
        }
    }

    /**
     * Agenda ou cancela o alarme de uma aula com base nas preferências GLOBAIS de notificação
     * (não mais por aula) — se notificações estiverem desligadas nas Configurações, nenhuma
     * aula dispara alerta, mesmo com data futura. Se a antecedência configurada for maior que
     * o tempo restante até a aula (ex.: antecedência de 60 min mas a aula é daqui a 30 min),
     * o alerta dispara o quanto antes em vez de simplesmente não ser agendado.
     */
    private suspend fun sincronizarAlarme(aula: Aula) {
        val horario = aula.dataHoraMillis
        val agora = System.currentTimeMillis()
        if (preferencias.notificacoesAtivadas.value && !aula.concluida && horario != null && horario > agora) {
            val horarioComAntecedencia = horario - preferencias.minutosAntecedencia.value * 60_000L
            val horarioDisparo = if (horarioComAntecedencia > agora) horarioComAntecedencia else agora
            val materia = materiaDao.buscarPorId(aula.materiaId)
            alarmScheduler.agendar(
                aula = aula,
                nomeMateria = materia?.nome ?: "Matéria",
                horarioDispararMillis = horarioDisparo,
                somAtivado = preferencias.somAtivado.value,
                vibracaoAtivada = preferencias.vibracaoAtivada.value
            )
        } else {
            alarmScheduler.cancelar(aula)
        }
    }

    /** Reagenda todos os alarmes pendentes conforme as preferências atuais; usado após
     *  reiniciar o aparelho ou quando o usuário muda uma configuração de notificação. */
    suspend fun reagendarTodosOsAlarmes() {
        val materiasPorId = materiaDao.buscarTodasSuspend().associateBy { it.id }
        if (!preferencias.notificacoesAtivadas.value) {
            aulaDao.buscarTodasSuspend().forEach { alarmScheduler.cancelar(it) }
            return
        }
        val antecedenciaMillis = preferencias.minutosAntecedencia.value * 60_000L
        val agora = System.currentTimeMillis()
        aulaDao.buscarTodasSuspend()
            .filter { it.dataHoraMillis != null && !it.concluida }
            .forEach { aula ->
                if (aula.dataHoraMillis!! > agora) {
                    val horarioComAntecedencia = aula.dataHoraMillis - antecedenciaMillis
                    val horarioDisparo = if (horarioComAntecedencia > agora) horarioComAntecedencia else agora
                    val nomeMateria = materiasPorId[aula.materiaId]?.nome ?: "Matéria"
                    alarmScheduler.agendar(
                        aula = aula,
                        nomeMateria = nomeMateria,
                        horarioDispararMillis = horarioDisparo,
                        somAtivado = preferencias.somAtivado.value,
                        vibracaoAtivada = preferencias.vibracaoAtivada.value
                    )
                } else {
                    alarmScheduler.cancelar(aula)
                }
            }
    }
}
