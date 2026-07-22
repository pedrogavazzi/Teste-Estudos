package com.pedrogavazzi.controleestudos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pedrogavazzi.controleestudos.MainActivity
import com.pedrogavazzi.controleestudos.R
import android.app.PendingIntent
import android.content.Intent

object NotificationHelper {

    const val CANAL_AULAS = "canal_aulas"

    fun criarCanalNotificacao(context: Context) {
        val canal = NotificationChannel(
            CANAL_AULAS,
            "Alertas de aulas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificações de horário das aulas agendadas"
            enableVibration(true)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(canal)
    }

    fun exibirNotificacaoDeAula(
        context: Context,
        aulaId: Long,
        nomeMateria: String,
        numeroAula: Int
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

        val notificacao = NotificationCompat.Builder(context, CANAL_AULAS)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle("Aula $numeroAula de $nomeMateria")
            .setContentText("Está na hora da sua aula agendada.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).apply {
            runCatching { notify(aulaId.toInt(), notificacao) }
        }
    }
}
