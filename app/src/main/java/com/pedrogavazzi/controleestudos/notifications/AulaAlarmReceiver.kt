package com.pedrogavazzi.controleestudos.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Recebe o disparo do AlarmManager no horário (já com antecedência aplicada) e mostra a notificação. */
class AulaAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_AULA_ID = "extra_aula_id"
        const val EXTRA_NUMERO_AULA = "extra_numero_aula"
        const val EXTRA_NOME_MATERIA = "extra_nome_materia"
        const val EXTRA_SOM_ATIVADO = "extra_som_ativado"
        const val EXTRA_VIBRACAO_ATIVADA = "extra_vibracao_ativada"

        private val PADRAO_VIBRACAO = longArrayOf(0, 500, 250, 500)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val aulaId = intent.getLongExtra(EXTRA_AULA_ID, -1L)
        if (aulaId == -1L) return
        val numeroAula = intent.getIntExtra(EXTRA_NUMERO_AULA, 0)
        val nomeMateria = intent.getStringExtra(EXTRA_NOME_MATERIA) ?: "Matéria"
        val somAtivado = intent.getBooleanExtra(EXTRA_SOM_ATIVADO, true)
        val vibracaoAtivada = intent.getBooleanExtra(EXTRA_VIBRACAO_ATIVADA, true)

        NotificationHelper.exibirNotificacaoDeAula(context, aulaId, nomeMateria, numeroAula, somAtivado, vibracaoAtivada)

        // Vibração disparada diretamente pela API do sistema — mais confiável do que depender
        // só do padrão de vibração configurado no canal de notificação (que pode ser ignorado
        // por alguns fabricantes/versões do Android).
        if (vibracaoAtivada) {
            vibrar(context)
        }
    }

    private fun vibrar(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator?.hasVibrator() != true) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(PADRAO_VIBRACAO, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(PADRAO_VIBRACAO, -1)
        }
    }
}
