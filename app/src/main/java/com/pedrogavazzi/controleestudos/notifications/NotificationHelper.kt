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

    private const val CANAL_COM_SOM = "canal_aulas_com_som"
    private const val CANAL_SEM_SOM = "canal_aulas_sem_som"

    private val padraoVibracao = longArrayOf(0, 500, 250, 500)

    /**
     * Cria um canal por forma de alerta (com som ou sem som), já que a partir do Android 8
     * o som/vibração de um canal não pode mais ser alterado depois de criado — cada aula usa
     * o canal correspondente à opção escolhida pelo usuário.
     */
    fun criarCanalNotificacao(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val atributosSom = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val somPadrao = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val canalComSom = NotificationChannel(CANAL_COM_SOM, "Alertas de aulas (com som)", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notificações de horário das aulas — com som e vibração"
            enableVibration(true)
            vibrationPattern = padraoVibracao
            setSound(somPadrao, atributosSom)
        }
        val canalSemSom = NotificationChannel(CANAL_SEM_SOM, "Alertas de aulas (sem som)", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notificações de horário das aulas — apenas vibração, sem som"
            enableVibration(true)
            vibrationPattern = padraoVibracao
            setSound(null, null)
        }

        // Recria os canais antigos de versões anteriores do app, se existirem, para não deixar
        // canais obsoletos "presos" com configuração de som/vibração que não pode mais mudar.
        listOf("canal_aulas", "canal_aulas_vibracao", "canal_aulas_som_vibracao").forEach { idAntigo ->
            manager.deleteNotificationChannel(idAntigo)
        }

        manager.createNotificationChannel(canalComSom)
        manager.createNotificationChannel(canalSemSom)
    }

    private fun canalParaTipoAlerta(tipoAlerta: TipoAlerta): String = when (tipoAlerta) {
        TipoAlerta.COM_SOM -> CANAL_COM_SOM
        TipoAlerta.SEM_SOM -> CANAL_SEM_SOM
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
            // Em versões anteriores ao Android 8 não existem canais: define vibração/som direto.
            .setVibrate(padraoVibracao)

        if (tipoAlerta == TipoAlerta.COM_SOM) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        NotificationManagerCompat.from(context).apply {
            runCatching { notify(aulaId.toInt(), builder.build()) }
        }
    }
}
