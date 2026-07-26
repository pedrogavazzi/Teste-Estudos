package com.pedrogavazzi.controleestudos.ui.favoritos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.Materia
import com.pedrogavazzi.controleestudos.data.StudyRepository
import com.pedrogavazzi.controleestudos.ui.agenda.AulaComMateria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Aba Favoritos: aulas marcadas pelo aluno (o "estrela" no cabeçalho de cada aula) para
 * revisão rápida depois — pensada pra achar direto o que precisa reler antes de uma prova,
 * sem ter que lembrar em qual matéria estava.
 */
class FavoritosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository = (application as ControleEstudosApp).repository

    val favoritas: StateFlow<List<AulaComMateria>> =
        combine(repository.observarTodasAsAulas(), repository.observarMaterias()) { aulas, materias ->
            val materiasPorId: Map<Long, Materia> = materias.associateBy { it.id }
            aulas.filter { it.favorita }
                .mapNotNull { aula ->
                    val materia = materiasPorId[aula.materiaId] ?: return@mapNotNull null
                    AulaComMateria(aula, materia.nome, materia.corHex)
                }
                .sortedBy { it.nomeMateria.lowercase() }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun desmarcarFavorita(aula: Aula) {
        viewModelScope.launch { repository.definirFavorita(aula, false) }
    }
}
