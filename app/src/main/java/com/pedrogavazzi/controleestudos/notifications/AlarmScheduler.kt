package com.pedrogavazzi.controleestudos.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pedrogavazzi.controleestudos.data.Aula

/**
 * Agenda e cancela os alarmes exatos (AlarmManager) que disparam a notificação de cada aula,
 * usando as preferências globais de som/vibração e a antecedência definida nas Configurações
 * (não mais opções por aula).
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun agendar(
        aula: Aula,
        nomeMateria: String,
        horarioDispararMillis: Long,
        somAtivado: Boolean,
        vibracaoAtivada: Boolean
    ) {
        val pendingIntent = criarPendingIntent(aula, nomeMateria, somAtivado, vibracaoAtivada)

        val podeAgendarExato = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

        try {
            if (podeAgendarExato) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    horarioDispararMillis,
                    pendingIntent
                )
            } else {
                // Sem permissão para alarmes exatos: usa alarme aproximado como alternativa.
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    horarioDispararMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, horarioDispararMillis, pendingIntent)
        }
    }

    fun cancelar(aula: Aula) {
        val pendingIntent = criarPendingIntent(aula, "", somAtivado = true, vibracaoAtivada = true)
        alarmManager.cancel(pendingIntent)
    }

    fun podeAgendarAlarmesExatos(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }

    private fun criarPendingIntent(
        aula: Aula,
        nomeMateria: String,
        somAtivado: Boolean,
        vibracaoAtivada: Boolean
    ): PendingIntent {
        val intent = Intent(context, AulaAlarmReceiver::class.java).apply {
            putExtra(AulaAlarmReceiver.EXTRA_AULA_ID, aula.id)
            putExtra(AulaAlarmReceiver.EXTRA_NUMERO_AULA, aula.numero)
            putExtra(AulaAlarmReceiver.EXTRA_NOME_MATERIA, nomeMateria)
            putExtra(AulaAlarmReceiver.EXTRA_SOM_ATIVADO, somAtivado)
            putExtra(AulaAlarmReceiver.EXTRA_VIBRACAO_ATIVADA, vibracaoAtivada)
        }
        // A chave do PendingIntent precisa ser igual entre agendar/cancelar da mesma aula,
        // então usamos só o id da aula — os extras (som/vibração) podem mudar sem quebrar isso.
        return PendingIntent.getBroadcast(
            context,
            aula.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
