package com.pedrogavazzi.controleestudos.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Tema visual do app. */
enum class TemaApp { CLARO, ESCURO, SISTEMA }

/** Opções de antecedência do alerta, em minutos antes do horário agendado da aula. */
val OPCOES_ANTECEDENCIA_MINUTOS = listOf(0, 5, 10, 15, 30, 45, 60)

/**
 * Preferências globais do app (tema, notificações, som, vibração, antecedência do alerta),
 * salvas em SharedPreferences e expostas como StateFlow para a UI reagir automaticamente.
 * Substitui as antigas configurações por aula — agora tudo é definido uma vez, na aba
 * Configurações, para não poluir a tela de cada aula.
 */
class PreferenciasApp(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(NOME_ARQUIVO, Context.MODE_PRIVATE)

    private val _tema = MutableStateFlow(
        runCatching { TemaApp.valueOf(prefs.getString(CHAVE_TEMA, null) ?: TemaApp.SISTEMA.name) }
            .getOrDefault(TemaApp.SISTEMA)
    )
    val tema: StateFlow<TemaApp> = _tema.asStateFlow()

    private val _notificacoesAtivadas = MutableStateFlow(prefs.getBoolean(CHAVE_NOTIFICACOES, true))
    val notificacoesAtivadas: StateFlow<Boolean> = _notificacoesAtivadas.asStateFlow()

    private val _somAtivado = MutableStateFlow(prefs.getBoolean(CHAVE_SOM, true))
    val somAtivado: StateFlow<Boolean> = _somAtivado.asStateFlow()

    private val _vibracaoAtivada = MutableStateFlow(prefs.getBoolean(CHAVE_VIBRACAO, true))
    val vibracaoAtivada: StateFlow<Boolean> = _vibracaoAtivada.asStateFlow()

    private val _minutosAntecedencia = MutableStateFlow(prefs.getInt(CHAVE_ANTECEDENCIA, 0))
    val minutosAntecedencia: StateFlow<Int> = _minutosAntecedencia.asStateFlow()

    fun definirTema(novo: TemaApp) {
        _tema.value = novo
        prefs.edit().putString(CHAVE_TEMA, novo.name).apply()
    }

    fun definirNotificacoesAtivadas(ativo: Boolean) {
        _notificacoesAtivadas.value = ativo
        prefs.edit().putBoolean(CHAVE_NOTIFICACOES, ativo).apply()
    }

    fun definirSomAtivado(ativo: Boolean) {
        _somAtivado.value = ativo
        prefs.edit().putBoolean(CHAVE_SOM, ativo).apply()
    }

    fun definirVibracaoAtivada(ativo: Boolean) {
        _vibracaoAtivada.value = ativo
        prefs.edit().putBoolean(CHAVE_VIBRACAO, ativo).apply()
    }

    fun definirMinutosAntecedencia(minutos: Int) {
        _minutosAntecedencia.value = minutos
        prefs.edit().putInt(CHAVE_ANTECEDENCIA, minutos).apply()
    }

    private companion object {
        const val NOME_ARQUIVO = "preferencias_app"
        const val CHAVE_TEMA = "tema"
        const val CHAVE_NOTIFICACOES = "notificacoes_ativadas"
        const val CHAVE_SOM = "som_ativado"
        const val CHAVE_VIBRACAO = "vibracao_ativada"
        const val CHAVE_ANTECEDENCIA = "minutos_antecedencia"
    }
}
