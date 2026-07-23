package com.pedrogavazzi.controleestudos

import android.app.Application
import com.pedrogavazzi.controleestudos.data.StudyRepository
import com.pedrogavazzi.controleestudos.notifications.NotificationHelper

class ControleEstudosApp : Application() {

    val repository: StudyRepository by lazy { StudyRepository(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.criarCanalNotificacao(this)
    }
}
