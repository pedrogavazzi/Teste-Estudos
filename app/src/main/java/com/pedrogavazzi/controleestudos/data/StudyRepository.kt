package com.pedrogavazzi.controleestudos.data

import android.content.Context
import com.pedrogavazzi.controleestudos.notifications.AlarmScheduler
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

    suspend fun salvarObservacao(aula: Aula, observacao: String) {
        aulaDao.atualizar(aula.copy(observacao = observacao))
    }

    suspend fun excluirAula(aula: Aula) {
        alarmScheduler.cancelar(aula)
        aulaDao.excluir(aula)
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
