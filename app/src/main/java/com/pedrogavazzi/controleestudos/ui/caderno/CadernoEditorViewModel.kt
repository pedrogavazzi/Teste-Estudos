package com.pedrogavazzi.controleestudos.ui.caderno

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.Materia
import com.pedrogavazzi.controleestudos.data.StudyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EstadoCadernoEditor(
    val aula: Aula? = null,
    val materia: Materia? = null,
    val carregando: Boolean = true
)

class CadernoEditorViewModel(
    private val repository: StudyRepository,
    aulaId: Long
) : ViewModel() {

    val estado: StateFlow<EstadoCadernoEditor> =
        combine(repository.observarAula(aulaId), repository.observarMaterias()) { aula, materias ->
            val materia = aula?.let { atual -> materias.firstOrNull { it.id == atual.materiaId } }
            EstadoCadernoEditor(aula = aula, materia = materia, carregando = false)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EstadoCadernoEditor()
        )

    fun salvarAnotacoes(aula: Aula, anotacoesSerializadas: String) {
        viewModelScope.launch { repository.salvarAnotacaoCaderno(aula, anotacoesSerializadas) }
    }

    fun marcarConclusao(aula: Aula, concluida: Boolean) {
        viewModelScope.launch { repository.marcarConclusao(aula, concluida) }
    }
}
