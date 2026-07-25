package com.pedrogavazzi.controleestudos.ui.materiadetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.Materia
import com.pedrogavazzi.controleestudos.data.StudyRepository
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

    fun agendarAula(aula: Aula, dataHoraMillis: Long?) {
        viewModelScope.launch { repository.agendarAula(aula, dataHoraMillis) }
    }

    fun reagendarAula(aula: Aula, novaDataHoraMillis: Long) {
        viewModelScope.launch { repository.reagendarAula(aula, novaDataHoraMillis) }
    }

    fun agendarEmLote(
        dataHoraInicialMillis: Long,
        intervaloDias: Int,
        quantidade: Int,
        apenasDiasUteis: Boolean,
        aoConcluir: (idsAgendados: List<Long>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val ids = repository.agendarEmLote(materiaId, dataHoraInicialMillis, intervaloDias, quantidade, apenasDiasUteis)
            aoConcluir(ids)
        }
    }

    /** Desfaz um agendamento em lote recente (volta as aulas indicadas para "sem data"). */
    fun desfazerAgendamentoEmLote(aulaIds: List<Long>) {
        viewModelScope.launch { repository.desfazerAgendamentoEmLote(aulaIds) }
    }

    fun adicionarAula() {
        viewModelScope.launch { repository.adicionarAula(materiaId) }
    }

    fun excluirAula(aula: Aula, aoConcluir: () -> Unit = {}) {
        viewModelScope.launch {
            repository.excluirAula(aula)
            aoConcluir()
        }
    }

    /** Desfaz a exclusão de uma aula recente, restaurando ela exatamente como estava. */
    fun restaurarAula(aula: Aula) {
        viewModelScope.launch { repository.restaurarAula(aula) }
    }

    fun renomearAula(aula: Aula, novoNome: String?) {
        viewModelScope.launch { repository.renomearAula(aula, novoNome) }
    }

    fun marcarConclusao(aula: Aula, concluida: Boolean) {
        viewModelScope.launch { repository.marcarConclusao(aula, concluida) }
    }

    fun salvarObservacao(aula: Aula, observacao: String) {
        viewModelScope.launch { repository.salvarObservacao(aula, observacao) }
    }

    fun salvarLink(aula: Aula, link: String) {
        viewModelScope.launch { repository.salvarLink(aula, link) }
    }

    fun salvarAnotacaoCaderno(aula: Aula, anotacoes: String) {
        viewModelScope.launch { repository.salvarAnotacaoCaderno(aula, anotacoes) }
    }
}
