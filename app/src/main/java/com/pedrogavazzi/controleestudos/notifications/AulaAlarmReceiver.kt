package com.pedrogavazzi.controleestudos.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Recebe o disparo do AlarmManager no horário (já com antecedência aplicada) e mostra a notificação. */
class AulaAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_AULA_ID = "extra_aula_id"
        const val EXTRA_NUMERO_AULA = "extra_numero_aula"
        const val EXTRA_NOME_MATERIA = "extra_nome_materia"
        const val EXTRA_SOM_ATIVADO = "extra_som_ativado"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val aulaId = intent.getLongExtra(EXTRA_AULA_ID, -1L)
        if (aulaId == -1L) return
        val numeroAula = intent.getIntExtra(EXTRA_NUMERO_AULA, 0)
        val nomeMateria = intent.getStringExtra(EXTRA_NOME_MATERIA) ?: "Matéria"
        val somAtivado = intent.getBooleanExtra(EXTRA_SOM_ATIVADO, true)

        NotificationHelper.exibirNotificacaoDeAula(context, aulaId, nomeMateria, numeroAula, somAtivado)
    }
}
