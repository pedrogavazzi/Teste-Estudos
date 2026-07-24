package com.pedrogavazzi.controleestudos.ui.configuracoes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import com.pedrogavazzi.controleestudos.data.PreferenciasApp
import com.pedrogavazzi.controleestudos.data.StudyRepository
import com.pedrogavazzi.controleestudos.data.TemaApp
import com.pedrogavazzi.controleestudos.data.nomeExibido
import com.pedrogavazzi.controleestudos.ui.components.formatarDataHora
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConfiguracoesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ControleEstudosApp
    private val preferencias: PreferenciasApp = app.preferencias
    private val repository: StudyRepository = app.repository

    val tema: StateFlow<TemaApp> = preferencias.tema
    val usarCorDinamica: StateFlow<Boolean> = preferencias.usarCorDinamica
    val notificacoesAtivadas: StateFlow<Boolean> = preferencias.notificacoesAtivadas
    val somAtivado: StateFlow<Boolean> = preferencias.somAtivado
    val vibracaoAtivada: StateFlow<Boolean> = preferencias.vibracaoAtivada
    val minutosAntecedencia: StateFlow<Int> = preferencias.minutosAntecedencia

    fun definirTema(tema: TemaApp) {
        preferencias.definirTema(tema)
    }

    fun definirUsarCorDinamica(ativo: Boolean) {
        preferencias.definirUsarCorDinamica(ativo)
    }

    fun definirNotificacoesAtivadas(ativo: Boolean) {
        preferencias.definirNotificacoesAtivadas(ativo)
        reagendarAlarmes()
    }

    fun definirSomAtivado(ativo: Boolean) {
        preferencias.definirSomAtivado(ativo)
        reagendarAlarmes()
    }

    fun definirVibracaoAtivada(ativo: Boolean) {
        preferencias.definirVibracaoAtivada(ativo)
        reagendarAlarmes()
    }

    fun definirMinutosAntecedencia(minutos: Int) {
        preferencias.definirMinutosAntecedencia(minutos)
        reagendarAlarmes()
    }

    /** Reagenda todos os alarmes pendentes para valer imediatamente com a nova preferência. */
    private fun reagendarAlarmes() {
        viewModelScope.launch { repository.reagendarTodosOsAlarmes() }
    }

    /** Monta um resumo em texto simples de todas as matérias e aulas, para exportar/compartilhar
     *  como um backup manual — já que o app não tem exportação nenhuma além do backup
     *  automático do Android. [aoConcluir] recebe o texto pronto. */
    fun gerarTextoExportacao(aoConcluir: (String) -> Unit) {
        viewModelScope.launch {
            val dados = repository.buscarTudoParaExportacao()
            val texto = buildString {
                appendLine("Controle de Estudos — exportação de dados")
                appendLine("Gerado em ${formatarDataHora(System.currentTimeMillis())}")
                appendLine()
                if (dados.isEmpty()) {
                    appendLine("Nenhuma matéria cadastrada ainda.")
                }
                dados.forEach { (materia, aulas) ->
                    val concluidas = aulas.count { it.concluida }
                    appendLine("${materia.nome} — $concluidas de ${aulas.size} aulas concluídas")
                    aulas.forEach { aula ->
                        val dataTexto = aula.dataHoraMillis?.let { formatarDataHora(it) } ?: "sem data definida"
                        val statusTexto = if (aula.concluida) "concluída" else "pendente"
                        appendLine("  • ${aula.nomeExibido()} — $dataTexto ($statusTexto)")
                        if (aula.observacao.isNotBlank()) {
                            appendLine("      Observação: ${aula.observacao}")
                        }
                    }
                    appendLine()
                }
            }
            aoConcluir(texto)
        }
    }
}
