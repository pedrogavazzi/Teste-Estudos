package com.pedrogavazzi.controleestudos.ui.materias

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.Materia
import com.pedrogavazzi.controleestudos.data.StudyRepository
import com.pedrogavazzi.controleestudos.ui.components.TAMANHO_MAXIMO_NOME_MATERIA
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MateriaComProgresso(
    val materia: Materia,
    val aulasConcluidas: Int,
    val totalAulas: Int
) {
    val percentual: Float
        get() = if (totalAulas == 0) 0f else aulasConcluidas / totalAulas.toFloat()
}

class MateriasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository = (application as ControleEstudosApp).repository

    val materiasComProgresso: StateFlow<List<MateriaComProgresso>> =
        combine(repository.observarMaterias(), repository.observarTodasAsAulas()) { materias, aulas ->
            val aulasPorMateria: Map<Long, List<Aula>> = aulas.groupBy { it.materiaId }
            materias.map { materia ->
                val aulasDaMateria = aulasPorMateria[materia.id].orEmpty()
                MateriaComProgresso(
                    materia = materia,
                    aulasConcluidas = aulasDaMateria.count { it.concluida },
                    totalAulas = aulasDaMateria.size
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun criarMateria(nome: String, totalAulas: Int, corHex: String) {
        if (nome.isBlank() || totalAulas <= 0) return
        viewModelScope.launch {
            repository.criarMateria(nome.trim().take(TAMANHO_MAXIMO_NOME_MATERIA), totalAulas, corHex)
        }
    }

    fun atualizarMateria(materia: Materia, novoNome: String, novoTotalAulas: Int, novaCor: String) {
        if (novoNome.isBlank() || novoTotalAulas <= 0) return
        viewModelScope.launch {
            repository.atualizarMateria(
                materia.copy(nome = novoNome.trim().take(TAMANHO_MAXIMO_NOME_MATERIA), corHex = novaCor),
                novoTotalAulas
            )
        }
    }

    fun excluirMateria(materia: Materia) {
        viewModelScope.launch {
            repository.excluirMateria(materia)
        }
    }
}
