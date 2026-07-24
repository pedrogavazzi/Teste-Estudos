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
import android.app.PendingIntent
import android.content.Intent

object NotificationHelper {

    private const val CANAL_SOM_VIBRACAO = "canal_aulas_som_vibracao"
    private const val CANAL_SOM_SEM_VIBRACAO = "canal_aulas_som"
    private const val CANAL_VIBRACAO_SEM_SOM = "canal_aulas_vibracao"
    private const val CANAL_SILENCIOSO = "canal_aulas_silencioso"

    private val padraoVibracao = longArrayOf(0, 500, 250, 500)

    /**
     * Cria um canal para cada combinação de som/vibração (a partir do Android 8, o som e a
     * vibração de um canal não podem mais ser alterados depois de criado) — a combinação
     * escolhida nas Configurações do app decide qual canal cada notificação usa.
     */
    fun criarCanaisNotificacao(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val atributosSom = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val somPadrao = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val canalSomVibracao = NotificationChannel(CANAL_SOM_VIBRACAO, "Alertas de aulas (som e vibração)", NotificationManager.IMPORTANCE_HIGH).apply {
            enableVibration(true)
            vibrationPattern = padraoVibracao
            setSound(somPadrao, atributosSom)
        }
        val canalSomSemVibracao = NotificationChannel(CANAL_SOM_SEM_VIBRACAO, "Alertas de aulas (só som)", NotificationManager.IMPORTANCE_HIGH).apply {
            enableVibration(false)
            setSound(somPadrao, atributosSom)
        }
        val canalVibracaoSemSom = NotificationChannel(CANAL_VIBRACAO_SEM_SOM, "Alertas de aulas (só vibração)", NotificationManager.IMPORTANCE_HIGH).apply {
            enableVibration(true)
            vibrationPattern = padraoVibracao
            setSound(null, null)
        }
        val canalSilencioso = NotificationChannel(CANAL_SILENCIOSO, "Alertas de aulas (silencioso)", NotificationManager.IMPORTANCE_DEFAULT).apply {
            enableVibration(false)
            setSound(null, null)
        }

        // Remove canais de versões anteriores do app, que podiam ficar com configuração de
        // som/vibração "presa" e não podem mais ser alterados.
        listOf(
            "canal_aulas", "canal_aulas_vibracao", "canal_aulas_som_vibracao_antigo",
            "canal_aulas_com_som", "canal_aulas_sem_som"
        ).forEach { idAntigo -> manager.deleteNotificationChannel(idAntigo) }

        manager.createNotificationChannel(canalSomVibracao)
        manager.createNotificationChannel(canalSomSemVibracao)
        manager.createNotificationChannel(canalVibracaoSemSom)
        manager.createNotificationChannel(canalSilencioso)
    }

    private fun canalPara(somAtivado: Boolean, vibracaoAtivada: Boolean): String = when {
        somAtivado && vibracaoAtivada -> CANAL_SOM_VIBRACAO
        somAtivado -> CANAL_SOM_SEM_VIBRACAO
        vibracaoAtivada -> CANAL_VIBRACAO_SEM_SOM
        else -> CANAL_SILENCIOSO
    }

    fun exibirNotificacaoDeAula(
        context: Context,
        aulaId: Long,
        nomeMateria: String,
        numeroAula: Int,
        somAtivado: Boolean,
        vibracaoAtivada: Boolean
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

        val canal = canalPara(somAtivado, vibracaoAtivada)
        val builder = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle("Aula $numeroAula de $nomeMateria")
            .setContentText("Está na hora da sua aula agendada.")
            .setPriority(if (somAtivado || vibracaoAtivada) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Em versões anteriores ao Android 8 não existem canais: define som/vibração direto.
        if (somAtivado) builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        if (vibracaoAtivada) builder.setVibrate(padraoVibracao) else builder.setVibrate(longArrayOf(0))

        NotificationManagerCompat.from(context).apply {
            runCatching { notify(aulaId.toInt(), builder.build()) }
        }
    }
}
