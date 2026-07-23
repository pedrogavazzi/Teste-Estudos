package com.pedrogavazzi.controleestudos.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pedrogavazzi.controleestudos.data.TipoAlerta

/** Recebe o disparo do AlarmManager no horário exato da aula e mostra a notificação. */
class AulaAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_AULA_ID = "extra_aula_id"
        const val EXTRA_NUMERO_AULA = "extra_numero_aula"
        const val EXTRA_NOME_MATERIA = "extra_nome_materia"
        const val EXTRA_TIPO_ALERTA = "extra_tipo_alerta"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val aulaId = intent.getLongExtra(EXTRA_AULA_ID, -1L)
        if (aulaId == -1L) return
        val numeroAula = intent.getIntExtra(EXTRA_NUMERO_AULA, 0)
        val nomeMateria = intent.getStringExtra(EXTRA_NOME_MATERIA) ?: "Matéria"
        val tipoAlerta = runCatching {
            TipoAlerta.valueOf(intent.getStringExtra(EXTRA_TIPO_ALERTA) ?: "")
        }.getOrDefault(TipoAlerta.SOM_E_VIBRACAO)

        NotificationHelper.exibirNotificacaoDeAula(context, aulaId, nomeMateria, numeroAula, tipoAlerta)
    }
}
