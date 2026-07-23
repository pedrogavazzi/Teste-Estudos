package com.pedrogavazzi.controleestudos.ui.caderno

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.data.Materia
import com.pedrogavazzi.controleestudos.data.StudyRepository
import com.pedrogavazzi.controleestudos.ui.agenda.AulaComMateria
import java.util.Calendar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun inicioDoDiaMillis(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun fimDoDiaMillis(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.timeInMillis

data class EstadoCaderno(
    val emAndamento: List<AulaComMateria> = emptyList(),
    val aulasFeitas: List<AulaComMateria> = emptyList()
)

/**
 * Caderno com as aulas agendadas para hoje. Assim que uma anotação é salva para uma aula,
 * ela sai da lista "em andamento" e passa para "aulas feitas" — mas continua acessível e
 * editável ali, sem perder o que já foi escrito.
 */
class CadernoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository = (application as ControleEstudosApp).repository

    val estado: StateFlow<EstadoCaderno> =
        combine(repository.observarTodasAsAulas(), repository.observarMaterias()) { aulas, materias ->
            val materiasPorId: Map<Long, Materia> = materias.associateBy { it.id }
            val inicio = inicioDoDiaMillis()
            val fim = fimDoDiaMillis()
            val aulasDeHoje = aulas
                .filter { it.dataHoraMillis != null && it.dataHoraMillis in inicio..fim }
                .sortedBy { it.dataHoraMillis }
                .mapNotNull { aula ->
                    val materia = materiasPorId[aula.materiaId] ?: return@mapNotNull null
                    AulaComMateria(aula, materia.nome, materia.corHex)
                }
            EstadoCaderno(
                emAndamento = aulasDeHoje.filter { it.aula.anotacoesCaderno.isBlank() },
                aulasFeitas = aulasDeHoje.filter { it.aula.anotacoesCaderno.isNotBlank() }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EstadoCaderno()
        )

    fun salvarAnotacao(aula: Aula, anotacoes: String) {
        viewModelScope.launch { repository.salvarAnotacaoCaderno(aula, anotacoes) }
    }

    fun marcarConclusao(aula: Aula, concluida: Boolean) {
        viewModelScope.launch { repository.marcarConclusao(aula, concluida) }
    }
}
