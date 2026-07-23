package com.pedrogavazzi.controleestudos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pedrogavazzi.controleestudos.MainActivity
import com.pedrogavazzi.controleestudos.R
import com.pedrogavazzi.controleestudos.data.TipoAlerta
import android.app.PendingIntent
import android.content.Intent

object NotificationHelper {

    private const val CANAL_SOM = "canal_aulas_som"
    private const val CANAL_VIBRACAO = "canal_aulas_vibracao"
    private const val CANAL_SOM_E_VIBRACAO = "canal_aulas_som_vibracao"

    private val padraoVibracao = longArrayOf(0, 500, 250, 500)

    /**
     * Cria um canal de notificação por forma de alerta (som, vibração ou ambos), já que a
     * partir do Android 8 o som/vibração de um canal não pode mais ser alterado depois de criado.
     */
    fun criarCanalNotificacao(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val atributosSom = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val somPadrao = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val canalSom = NotificationChannel(CANAL_SOM, "Alertas de aulas (som)", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notificações de horário das aulas — apenas som"
            enableVibration(false)
            setSound(somPadrao, atributosSom)
        }
        val canalVibracao = NotificationChannel(CANAL_VIBRACAO, "Alertas de aulas (vibração)", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notificações de horário das aulas — apenas vibração"
            enableVibration(true)
            vibrationPattern = padraoVibracao
            setSound(null, null)
        }
        val canalSomEVibracao = NotificationChannel(CANAL_SOM_E_VIBRACAO, "Alertas de aulas (som e vibração)", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notificações de horário das aulas — som e vibração"
            enableVibration(true)
            vibrationPattern = padraoVibracao
            setSound(somPadrao, atributosSom)
        }

        manager.createNotificationChannel(canalSom)
        manager.createNotificationChannel(canalVibracao)
        manager.createNotificationChannel(canalSomEVibracao)
    }

    private fun canalParaTipoAlerta(tipoAlerta: TipoAlerta): String = when (tipoAlerta) {
        TipoAlerta.SOM -> CANAL_SOM
        TipoAlerta.VIBRACAO -> CANAL_VIBRACAO
        TipoAlerta.SOM_E_VIBRACAO -> CANAL_SOM_E_VIBRACAO
    }

    fun exibirNotificacaoDeAula(
        context: Context,
        aulaId: Long,
        nomeMateria: String,
        numeroAula: Int,
        tipoAlerta: TipoAlerta
    ) {
        val intentAbrirApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            aulaId.toInt(),
            intentAbrirApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canal = canalParaTipoAlerta(tipoAlerta)
        val builder = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle("Aula $numeroAula de $nomeMateria")
            .setContentText("Está na hora da sua aula agendada.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Em versões anteriores ao Android 8 não existem canais: define som/vibração diretamente.
        when (tipoAlerta) {
            TipoAlerta.SOM -> {
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                builder.setVibrate(longArrayOf(0))
            }
            TipoAlerta.VIBRACAO -> {
                builder.setVibrate(padraoVibracao)
            }
            TipoAlerta.SOM_E_VIBRACAO -> {
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                builder.setVibrate(padraoVibracao)
            }
        }

        NotificationManagerCompat.from(context).apply {
            runCatching { notify(aulaId.toInt(), builder.build()) }
        }
    }
}
