package com.pedrogavazzi.controleestudos.data

import android.content.Context
import com.pedrogavazzi.controleestudos.notifications.AlarmScheduler
import java.util.Calendar
import kotlinx.coroutines.flow.Flow

/**
 * Ponto único de acesso aos dados do app (matérias e aulas) e responsável por manter
 * os alarmes de notificação sincronizados sempre que uma aula é criada, reagendada,
 * concluída ou tem o alerta desativado.
 */
class StudyRepository(context: Context) {

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
     * Atualiza nome/cor da matéria e, se o número de aulas mudou, ajusta a lista:
     * cria aulas novas no final (sem data) ou remove as aulas excedentes a partir do final.
     */
    suspend fun atualizarMateria(materia: Materia, novoTotalAulas: Int) {
        val totalAtual = aulaDao.contarAulasDaMateria(materia.id)
        materiaDao.atualizar(materia.copy(totalAulas = novoTotalAulas))

        if (novoTotalAulas > totalAtual) {
            val novasAulas = (totalAtual + 1..novoTotalAulas).map { numero ->
                Aula(materiaId = materia.id, numero = numero)
            }
            aulaDao.inserirTodas(novasAulas)
        } else if (novoTotalAulas < totalAtual) {
            // Cancela alarmes das aulas que serão removidas antes de excluir do banco.
            aulaDao.buscarTodasDaMateriaSuspend(materia.id)
                .filter { it.numero > novoTotalAulas }
                .forEach { alarmScheduler.cancelar(it) }
            removerAulasExcedentes(materia.id, novoTotalAulas)
        }
    }

    private suspend fun removerAulasExcedentes(materiaId: Long, novoTotalAulas: Int) {
        aulaDao.removerAulasAcimaDoNumero(materiaId, novoTotalAulas)
    }

    suspend fun excluirMateria(materia: Materia) {
        aulaDao.buscarTodasDaMateriaSuspend(materia.id).forEach { alarmScheduler.cancelar(it) }
        materiaDao.excluir(materia)
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
     * ainda sem data, espaçadas por [intervaloDias] dias corridos (qualquer número, escolhido
     * pelo usuário), sempre no mesmo horário de [dataHoraInicialMillis]. Se [apenasDiasUteis]
     * estiver ativo, qualquer data que caia em sábado ou domingo é empurrada para a
     * segunda-feira seguinte — cada data é calculada a partir da anterior já ajustada, para
     * que duas aulas nunca caiam empurradas no mesmo dia.
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

        val calendario = Calendar.getInstance().apply { timeInMillis = dataHoraInicialMillis }
        if (apenasDiasUteis) ajustarParaProximoDiaUtil(calendario)

        aulasParaAgendar.forEachIndexed { indice, aula ->
            if (indice > 0) {
                calendario.add(Calendar.DAY_OF_YEAR, intervaloDias.coerceAtLeast(1))
                if (apenasDiasUteis) ajustarParaProximoDiaUtil(calendario)
            }
            val atualizada = aula.copy(dataHoraMillis = calendario.timeInMillis)
            aulaDao.atualizar(atualizada)
            sincronizarAlarme(atualizada)
        }
    }

    private fun ajustarParaProximoDiaUtil(calendario: Calendar) {
        while (calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendario.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendario.add(Calendar.DAY_OF_YEAR, 1)
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

    suspend fun definirAlerta(aula: Aula, ativado: Boolean) {
        val atualizada = aula.copy(alertaAtivado = ativado)
        aulaDao.atualizar(atualizada)
        sincronizarAlarme(atualizada)
    }

    /** Define a forma de notificação (som, vibração ou ambos) e reprograma o alerta se necessário. */
    suspend fun definirTipoAlerta(aula: Aula, tipoAlerta: TipoAlerta) {
        val atualizada = aula.copy(tipoAlerta = tipoAlerta)
        aulaDao.atualizar(atualizada)
        sincronizarAlarme(atualizada)
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

    private suspend fun sincronizarAlarme(aula: Aula) {
        if (aula.alertaAtivado && !aula.concluida && aula.dataHoraMillis != null &&
            aula.dataHoraMillis > System.currentTimeMillis()
        ) {
            val materia = materiaDao.buscarPorId(aula.materiaId)
            alarmScheduler.agendar(aula, materia?.nome ?: "Matéria")
        } else {
            alarmScheduler.cancelar(aula)
        }
    }

    /** Reagenda todos os alarmes pendentes; usado após reiniciar o aparelho. */
    suspend fun reagendarTodosOsAlarmes() {
        val materiasPorId = materiaDao.buscarTodasSuspend().associateBy { it.id }
        aulaDao.buscarTodasSuspend()
            .filter { it.alertaAtivado && !it.concluida && it.dataHoraMillis != null && it.dataHoraMillis > System.currentTimeMillis() }
            .forEach { aula ->
                val nomeMateria = materiasPorId[aula.materiaId]?.nome ?: "Matéria"
                alarmScheduler.agendar(aula, nomeMateria)
            }
    }
}
