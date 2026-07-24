package com.pedrogavazzi.controleestudos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "configuracoes_app")

class PreferenciasApp(private val context: Context) {

    companion object {
        val KEY_NOTIFICACOES_ATIVADAS = booleanPreferencesKey("notificacoes_ativadas")
        val KEY_MODO_ESCURO = booleanPreferencesKey("modo_escuro")
    }

    val notificacoesAtivadas: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_NOTIFICACOES_ATIVADAS] ?: true
        }

    val modoEscuro: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_MODO_ESCURO] ?: false
        }

    suspend fun setNotificacoesAtivadas(ativadas: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICACOES_ATIVADAS] = ativadas
        }
    }

    suspend fun setModoEscuro(ativo: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MODO_ESCURO] = ativo
        }
    }
}
