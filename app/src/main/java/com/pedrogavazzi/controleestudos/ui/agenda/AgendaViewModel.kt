package com.pedrogavazzi.controleestudos.ui.agenda

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.Materia
import com.pedrogavazzi.controleestudos.data.StudyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AulaComMateria(
    val aula: Aula,
    val nomeMateria: String,
    val corHex: String
)

class AgendaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository = (application as ControleEstudosApp).repository

    val aulasAgendadas: StateFlow<List<AulaComMateria>> =
        combine(repository.observarTodasAsAulas(), repository.observarMaterias()) { aulas, materias ->
            val materiasPorId: Map<Long, Materia> = materias.associateBy { it.id }
            aulas
                .filter { it.dataHoraMillis != null }
                .sortedBy { it.dataHoraMillis }
                .mapNotNull { aula ->
                    val materia = materiasPorId[aula.materiaId] ?: return@mapNotNull null
                    AulaComMateria(aula, materia.nome, materia.corHex)
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun marcarConclusao(aula: Aula, concluida: Boolean) {
        viewModelScope.launch { repository.marcarConclusao(aula, concluida) }
    }

    fun reagendarAula(aula: Aula, novaDataHoraMillis: Long) {
        viewModelScope.launch { repository.reagendarAula(aula, novaDataHoraMillis) }
    }

    fun agendarAula(aula: Aula, novaDataHoraMillis: Long) {
        viewModelScope.launch { repository.agendarAula(aula, novaDataHoraMillis) }
    }
}
