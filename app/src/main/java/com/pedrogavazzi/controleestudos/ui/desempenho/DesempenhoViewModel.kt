package com.pedrogavazzi.controleestudos.ui.desempenho

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.Materia
import com.pedrogavazzi.controleestudos.data.StatusAula
import com.pedrogavazzi.controleestudos.data.StudyRepository
import com.pedrogavazzi.controleestudos.data.statusAtual
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DesempenhoMateria(
    val materia: Materia,
    val totalAulas: Int,
    val aulasConcluidas: Int,
    val aulasAtrasadas: Int
) {
    val percentual: Float
        get() = if (totalAulas == 0) 0f else aulasConcluidas / totalAulas.toFloat()
}

data class DesempenhoGeral(
    val totalAulas: Int,
    val aulasConcluidas: Int,
    val aulasAtrasadas: Int,
    val porMateria: List<DesempenhoMateria>
) {
    val percentual: Float
        get() = if (totalAulas == 0) 0f else aulasConcluidas / totalAulas.toFloat()
}

/**
 * Recalcula automaticamente o desempenho (geral e por matéria) sempre que qualquer
 * matéria, número de aulas, conclusão ou reagendamento muda — pois observa os fluxos
 * reativos do Room diretamente.
 */
class DesempenhoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository = (application as ControleEstudosApp).repository

    val desempenho: StateFlow<DesempenhoGeral> =
        combine(repository.observarMaterias(), repository.observarTodasAsAulas()) { materias, aulas ->
            val agora = System.currentTimeMillis()
            val aulasPorMateria: Map<Long, List<Aula>> = aulas.groupBy { it.materiaId }

            val porMateria = materias.map { materia ->
                val aulasDaMateria = aulasPorMateria[materia.id].orEmpty()
                DesempenhoMateria(
                    materia = materia,
                    totalAulas = aulasDaMateria.size,
                    aulasConcluidas = aulasDaMateria.count { it.concluida },
                    aulasAtrasadas = aulasDaMateria.count { it.statusAtual(agora) == StatusAula.ATRASADA }
                )
                // Quem tem mais aulas atrasadas sobe primeiro (é quem mais precisa de atenção);
                // entre empates, quem está com menor percentual concluído; por fim, ordem alfabética.
                // Antes disso, a lista era ordenada só pelo tamanho da matéria, o que escondia
                // matérias pequenas e atrasadas no fim da lista.
            }.sortedWith(
                compareByDescending<DesempenhoMateria> { it.aulasAtrasadas }
                    .thenBy { it.percentual }
                    .thenBy { it.materia.nome.lowercase() }
            )

            DesempenhoGeral(
                totalAulas = aulas.size,
                aulasConcluidas = aulas.count { it.concluida },
                aulasAtrasadas = aulas.count { it.statusAtual(agora) == StatusAula.ATRASADA },
                porMateria = porMateria
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DesempenhoGeral(0, 0, 0, emptyList())
        )
}
