package com.pedrogavazzi.controleestudos.ui.configuracoes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import com.pedrogavazzi.controleestudos.data.PreferenciasApp
import com.pedrogavazzi.controleestudos.data.StudyRepository
import com.pedrogavazzi.controleestudos.data.TemaApp
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConfiguracoesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ControleEstudosApp
    private val preferencias: PreferenciasApp = app.preferencias
    private val repository: StudyRepository = app.repository

    val tema: StateFlow<TemaApp> = preferencias.tema
    val notificacoesAtivadas: StateFlow<Boolean> = preferencias.notificacoesAtivadas
    val somAtivado: StateFlow<Boolean> = preferencias.somAtivado
    val vibracaoAtivada: StateFlow<Boolean> = preferencias.vibracaoAtivada
    val minutosAntecedencia: StateFlow<Int> = preferencias.minutosAntecedencia

    fun definirTema(tema: TemaApp) {
        preferencias.definirTema(tema)
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
}
