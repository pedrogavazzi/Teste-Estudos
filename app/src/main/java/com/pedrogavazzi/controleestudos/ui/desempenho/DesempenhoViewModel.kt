package com.pedrogavazzi.controleestudos.ui.desempenho

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import com.pedrogavazzi.controleestudos.data.StudyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * O painel já teve um resumo por matéria (progresso, percentual, atrasadas) — removido
 * porque duplicava exatamente o que o card de cada matéria já mostra na aba Matérias. A
 * única informação que não existe em nenhum outro lugar do app é o total de aulas
 * cadastradas, então é só isso que essa tela mostra agora.
 */
class DesempenhoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository = (application as ControleEstudosApp).repository

    val totalAulas: StateFlow<Int> =
        repository.observarTodasAsAulas()
            .map { it.size }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )
}
