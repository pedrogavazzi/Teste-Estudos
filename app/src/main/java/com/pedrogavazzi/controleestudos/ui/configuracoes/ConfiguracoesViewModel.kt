package com.pedrogavazzi.controleestudos.ui.configuracoes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import com.pedrogavazzi.controleestudos.data.ExportadorPdf
import com.pedrogavazzi.controleestudos.data.PreferenciasApp
import com.pedrogavazzi.controleestudos.data.StudyRepository
import com.pedrogavazzi.controleestudos.data.TemaApp
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfiguracoesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ControleEstudosApp
    private val preferencias: PreferenciasApp = app.preferencias
    private val repository: StudyRepository = app.repository

    val tema: StateFlow<TemaApp> = preferencias.tema
    val usarCorDinamica: StateFlow<Boolean> = preferencias.usarCorDinamica
    val notificacoesAtivadas: StateFlow<Boolean> = preferencias.notificacoesAtivadas
    val somAtivado: StateFlow<Boolean> = preferencias.somAtivado
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

    fun definirMinutosAntecedencia(minutos: Int) {
        preferencias.definirMinutosAntecedencia(minutos)
        reagendarAlarmes()
    }

    /** Reagenda todos os alarmes pendentes para valer imediatamente com a nova preferência. */
    private fun reagendarAlarmes() {
        viewModelScope.launch { repository.reagendarTodosOsAlarmes() }
    }

    /**
     * Gera um PDF com o resumo de todas as matérias e aulas — incluindo o texto do caderno
     * de cada aula que tiver anotação de verdade (cadernos vazios ficam de fora) — e devolve
     * o arquivo pronto pra compartilhar. Roda em uma thread de I/O porque desenhar o PDF e
     * escrever no disco não deve travar a tela.
     */
    fun gerarPdfExportacao(aoConcluir: (File) -> Unit) {
        viewModelScope.launch {
            val dados = repository.buscarTudoParaExportacao()
            val arquivo = withContext(Dispatchers.IO) {
                ExportadorPdf.gerar(app, dados)
            }
            aoConcluir(arquivo)
        }
    }
}
