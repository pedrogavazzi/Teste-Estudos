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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

fun inicioDoDiaMillis(base: Long = System.currentTimeMillis()): Long = Calendar.getInstance().apply {
    timeInMillis = base
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun fimDoDia(base: Long): Long = Calendar.getInstance().apply {
    timeInMillis = base
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
 * Caderno das aulas de um dia — por padrão hoje, mas o usuário pode navegar para qualquer
 * outro dia (histórico), não só "hoje". Assim que uma anotação é salva para uma aula, ela
 * sai da lista "em andamento" e passa para "aulas feitas" — mas continua acessível e
 * editável ali, sem perder o que já foi escrito.
 */
class CadernoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository = (application as ControleEstudosApp).repository

    private val _dataSelecionada = MutableStateFlow(inicioDoDiaMillis())
    val dataSelecionada: StateFlow<Long> = _dataSelecionada.asStateFlow()

    val estado: StateFlow<EstadoCaderno> =
        combine(repository.observarTodasAsAulas(), repository.observarMaterias(), _dataSelecionada) { aulas, materias, inicio ->
            val materiasPorId: Map<Long, Materia> = materias.associateBy { it.id }
            val fim = fimDoDia(inicio)
            val aulasDoDia = aulas
                .filter { it.dataHoraMillis != null && it.dataHoraMillis in inicio..fim }
                .sortedBy { it.dataHoraMillis }
                .mapNotNull { aula ->
                    val materia = materiasPorId[aula.materiaId] ?: return@mapNotNull null
                    AulaComMateria(aula, materia.nome, materia.corHex)
                }
            EstadoCaderno(
                emAndamento = aulasDoDia.filter { !temAnotacaoReal(it.aula.anotacoesCaderno) },
                aulasFeitas = aulasDoDia.filter { temAnotacaoReal(it.aula.anotacoesCaderno) }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EstadoCaderno()
        )

    fun irParaHoje() {
        _dataSelecionada.value = inicioDoDiaMillis()
    }

    fun diaAnterior() {
        _dataSelecionada.value = Calendar.getInstance().apply {
            timeInMillis = _dataSelecionada.value
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis
    }

    fun diaSeguinte() {
        _dataSelecionada.value = Calendar.getInstance().apply {
            timeInMillis = _dataSelecionada.value
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
    }

    fun selecionarData(millis: Long) {
        _dataSelecionada.value = inicioDoDiaMillis(millis)
    }

    fun marcarConclusao(aula: Aula, concluida: Boolean) {
        viewModelScope.launch { repository.marcarConclusao(aula, concluida) }
    }
}

/** Uma anotação só conta como "feita" se houver texto de verdade — o texto serializado
 *  nunca fica totalmente vazio depois que a tela do caderno é aberta, pois guarda o
 *  cabeçalho de estilos mesmo sem conteúdo digitado. */
private fun temAnotacaoReal(anotacoesCaderno: String): Boolean =
    CadernoSerializer.temConteudo(anotacoesCaderno)
