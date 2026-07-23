package com.pedrogavazzi.controleestudos.ui.materiadetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.Materia
import com.pedrogavazzi.controleestudos.data.StudyRepository
import com.pedrogavazzi.controleestudos.data.TipoAlerta
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MateriaDetailViewModel(
    private val repository: StudyRepository,
    private val materiaId: Long
) : ViewModel() {

    val materia: StateFlow<Materia?> =
        repository.observarMateria(materiaId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val aulas: StateFlow<List<Aula>> =
        repository.observarAulasDaMateria(materiaId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agendarAula(aula: Aula, dataHoraMillis: Long) {
        viewModelScope.launch { repository.agendarAula(aula, dataHoraMillis) }
    }

    fun reagendarAula(aula: Aula, novaDataHoraMillis: Long) {
        viewModelScope.launch { repository.reagendarAula(aula, novaDataHoraMillis) }
    }

    fun agendarEmLote(dataHoraInicialMillis: Long, intervaloDias: Int, quantidade: Int, apenasDiasUteis: Boolean) {
        viewModelScope.launch {
            repository.agendarEmLote(materiaId, dataHoraInicialMillis, intervaloDias, quantidade, apenasDiasUteis)
        }
    }

    fun adicionarAula() {
        viewModelScope.launch { repository.adicionarAula(materiaId) }
    }

    fun excluirAula(aula: Aula) {
        viewModelScope.launch { repository.excluirAula(aula) }
    }

    fun renomearAula(aula: Aula, novoNome: String?) {
        viewModelScope.launch { repository.renomearAula(aula, novoNome) }
    }

    fun marcarConclusao(aula: Aula, concluida: Boolean) {
        viewModelScope.launch { repository.marcarConclusao(aula, concluida) }
    }

    fun definirAlerta(aula: Aula, ativado: Boolean) {
        viewModelScope.launch { repository.definirAlerta(aula, ativado) }
    }

    fun definirTipoAlerta(aula: Aula, tipoAlerta: TipoAlerta) {
        viewModelScope.launch { repository.definirTipoAlerta(aula, tipoAlerta) }
    }

    fun salvarObservacao(aula: Aula, observacao: String) {
        viewModelScope.launch { repository.salvarObservacao(aula, observacao) }
    }

    fun salvarAnotacaoCaderno(aula: Aula, anotacoes: String) {
        viewModelScope.launch { repository.salvarAnotacaoCaderno(aula, anotacoes) }
    }
}
