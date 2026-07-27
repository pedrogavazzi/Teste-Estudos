package com.pedrogavazzi.controleestudos.ui.agenda

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.Materia
import com.pedrogavazzi.controleestudos.data.StatusAula
import com.pedrogavazzi.controleestudos.data.StudyRepository
import com.pedrogavazzi.controleestudos.data.statusAtual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AulaComMateria(
    val aula: Aula,
    val nomeMateria: String,
    val corHex: String
)

/** As três formas de olhar pra Agenda — sempre uma de cada vez (uma aula concluída não pode
 *  também estar atrasada, por exemplo, então não faz sentido combinar mais de um filtro). */
enum class FiltroAgenda { PENDENTES, ATRASADAS, CONCLUIDAS }

class AgendaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository = (application as ControleEstudosApp).repository

    private val _filtro = MutableStateFlow(FiltroAgenda.PENDENTES)
    val filtro: StateFlow<FiltroAgenda> = _filtro.asStateFlow()

    private val aulasComMateria =
        combine(repository.observarTodasAsAulas(), repository.observarMaterias()) { aulas, materias ->
            val materiasPorId: Map<Long, Materia> = materias.associateBy { it.id }
            aulas.mapNotNull { aula ->
                val materia = materiasPorId[aula.materiaId] ?: return@mapNotNull null
                AulaComMateria(aula, materia.nome, materia.corHex)
            }
        }

    /** Aulas com data marcada, filtradas conforme [filtro] — pendentes (agendada, ainda não
     *  atrasada), atrasadas, ou concluídas (essas três cobrem juntas toda aula com data). */
    val aulasAgendadas: StateFlow<List<AulaComMateria>> =
        combine(aulasComMateria, _filtro) { itens, filtro ->
            itens
                .filter { it.aula.dataHoraMillis != null }
                .filter { item ->
                    when (filtro) {
                        FiltroAgenda.PENDENTES -> item.aula.statusAtual() == StatusAula.AGENDADA
                        FiltroAgenda.ATRASADAS -> item.aula.statusAtual() == StatusAula.ATRASADA
                        FiltroAgenda.CONCLUIDAS -> item.aula.statusAtual() == StatusAula.CONCLUIDA
                    }
                }
                .sortedBy { it.aula.dataHoraMillis }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Aulas que ainda não têm nenhuma data marcada — ficavam invisíveis na Agenda antes,
     *  só apareciam entrando na matéria específica. */
    val aulasSemData: StateFlow<List<AulaComMateria>> =
        aulasComMateria.map { itens ->
            itens.filter { it.aula.dataHoraMillis == null && !it.aula.concluida }
                .sortedBy { it.nomeMateria.lowercase() }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Se existe alguma aula com data marcada, de QUALQUER status — usado só pra decidir se a
     *  fileira de filtros deve aparecer. Não pode considerar só o filtro atual: senão, trocar
     *  pra "Concluídas" sem nenhuma concluída ainda escondia a própria fileira de filtros. */
    val temAulasComData: StateFlow<Boolean> =
        aulasComMateria.map { itens -> itens.any { it.aula.dataHoraMillis != null } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    fun selecionarFiltro(novo: FiltroAgenda) {
        _filtro.value = novo
    }

    fun marcarConclusao(aula: Aula, concluida: Boolean) {
        viewModelScope.launch { repository.marcarConclusao(aula, concluida) }
    }

    fun reagendarAula(aula: Aula, novaDataHoraMillis: Long) {
        viewModelScope.launch { repository.reagendarAula(aula, novaDataHoraMillis) }
    }

    fun agendarAula(aula: Aula, novaDataHoraMillis: Long) {
        viewModelScope.launch { repository.agendarAula(aula, novaDataHoraMillis) }
    }

    fun salvarLink(aula: Aula, link: String) {
        viewModelScope.launch { repository.salvarLink(aula, link) }
    }
}
