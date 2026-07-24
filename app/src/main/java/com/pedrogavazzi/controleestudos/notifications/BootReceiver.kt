package com.pedrogavazzi.controleestudos.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Reprograma todos os alarmes pendentes após o aparelho reiniciar. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = (context.applicationContext as ControleEstudosApp).repository
                repository.reagendarTodosOsAlarmes()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
