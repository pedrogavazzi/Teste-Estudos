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

    /** Extra usado para saber, quando o app é aberto a partir do toque na notificação, qual
     *  aula deve abrir direto no caderno dela. */
    const val EXTRA_AULA_ID_ABRIR_CADERNO = "extra_aula_id_abrir_caderno"

    private const val CANAL_COM_SOM = "canal_aulas_com_som"
    private const val CANAL_SEM_SOM = "canal_aulas_sem_som"

    /**
     * Cria um canal para cada opção de som (a partir do Android 8, o som de um canal não pode
     * mais ser alterado depois de criado) — a opção escolhida nas Configurações decide qual
     * canal cada notificação usa. A vibração foi removida: não funcionava de forma confiável
     * (provavelmente por causa de restrições de fabricante/modo silencioso em determinados
     * aparelhos), então em vez de manter uma configuração que não faz efeito, ela foi tirada.
     */
    fun criarCanaisNotificacao(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val atributosSom = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val somPadrao = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val canalComSom = NotificationChannel(CANAL_COM_SOM, "Alertas de aulas (com som)", NotificationManager.IMPORTANCE_HIGH).apply {
            enableVibration(false)
            setSound(somPadrao, atributosSom)
        }
        val canalSemSom = NotificationChannel(CANAL_SEM_SOM, "Alertas de aulas (sem som)", NotificationManager.IMPORTANCE_DEFAULT).apply {
            enableVibration(false)
            setSound(null, null)
        }

        // Remove canais de versões anteriores do app (inclusive os que tinham vibração), que
        // podiam ficar com configuração "presa" e não podem mais ser alterados.
        listOf(
            "canal_aulas", "canal_aulas_vibracao", "canal_aulas_som_vibracao",
            "canal_aulas_som_vibracao_antigo", "canal_aulas_silencioso"
        ).forEach { idAntigo -> manager.deleteNotificationChannel(idAntigo) }

        manager.createNotificationChannel(canalComSom)
        manager.createNotificationChannel(canalSemSom)
    }

    private fun canalPara(somAtivado: Boolean): String = if (somAtivado) CANAL_COM_SOM else CANAL_SEM_SOM

    fun exibirNotificacaoDeAula(
        context: Context,
        aulaId: Long,
        nomeMateria: String,
        numeroAula: Int,
        somAtivado: Boolean
    ) {
        val intentAbrirApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_AULA_ID_ABRIR_CADERNO, aulaId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            aulaId.toInt(),
            intentAbrirApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canal = canalPara(somAtivado)
        val builder = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle("Aula $numeroAula de $nomeMateria")
            .setContentText("Toque para abrir o caderno desta aula.")
            .setPriority(if (somAtivado) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Em versões anteriores ao Android 8 não existem canais: define o som direto.
        if (somAtivado) builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        NotificationManagerCompat.from(context).apply {
            runCatching { notify(aulaId.toInt(), builder.build()) }
        }
    }
}
