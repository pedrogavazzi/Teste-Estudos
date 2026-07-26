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

class AgendaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository = (application as ControleEstudosApp).repository

    private val _mostrarSoAtrasadas = MutableStateFlow(false)
    val mostrarSoAtrasadas: StateFlow<Boolean> = _mostrarSoAtrasadas.asStateFlow()

    private val aulasComMateria =
        combine(repository.observarTodasAsAulas(), repository.observarMaterias()) { aulas, materias ->
            val materiasPorId: Map<Long, Materia> = materias.associateBy { it.id }
            aulas.mapNotNull { aula ->
                val materia = materiasPorId[aula.materiaId] ?: return@mapNotNull null
                AulaComMateria(aula, materia.nome, materia.corHex)
            }
        }

    /** Aulas com data marcada, ainda não concluídas — a lista principal da Agenda. Quando o
     *  filtro "só atrasadas" está ligado, mostra só as que já passaram do fim do dia agendado. */
    val aulasAgendadas: StateFlow<List<AulaComMateria>> =
        combine(aulasComMateria, _mostrarSoAtrasadas) { itens, soAtrasadas ->
            itens
                .filter { it.aula.dataHoraMillis != null && !it.aula.concluida }
                .filter { !soAtrasadas || it.aula.statusAtual() == StatusAula.ATRASADA }
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

    /** Se existe alguma aula com data marcada, SEM aplicar o filtro "só atrasadas" — usado só
     *  pra decidir se o botão do filtro deve aparecer. Precisa ser independente do resultado
     *  filtrado: senão, ligar o filtro e não ter nenhuma atrasada faz a lista (e o próprio
     *  botão do filtro) sumir, sem nenhum jeito de desligá-lo de novo. */
    val temAulasComData: StateFlow<Boolean> =
        aulasComMateria.map { itens -> itens.any { it.aula.dataHoraMillis != null && !it.aula.concluida } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    fun alternarFiltroAtrasadas() {
        _mostrarSoAtrasadas.value = !_mostrarSoAtrasadas.value
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
