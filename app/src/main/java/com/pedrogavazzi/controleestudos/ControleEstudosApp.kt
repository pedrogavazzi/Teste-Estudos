package com.pedrogavazzi.controleestudos

import android.app.Application
import com.pedrogavazzi.controleestudos.data.PreferenciasApp
import com.pedrogavazzi.controleestudos.data.StudyRepository
import com.pedrogavazzi.controleestudos.notifications.NotificationHelper

class ControleEstudosApp : Application() {

    val preferencias: PreferenciasApp by lazy { PreferenciasApp(this) }
    val repository: StudyRepository by lazy { StudyRepository(this, preferencias) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.criarCanaisNotificacao(this)
    }
}
