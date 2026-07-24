package com.pedrogavazzi.controleestudos

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import com.pedrogavazzi.controleestudos.data.TemaApp
import com.pedrogavazzi.controleestudos.ui.navigation.AppNavigation
import com.pedrogavazzi.controleestudos.ui.theme.ControleDeEstudosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferencias = (application as ControleEstudosApp).preferencias
            val tema by preferencias.tema.collectAsState()
            val temaEscuro = when (tema) {
                TemaApp.CLARO -> false
                TemaApp.ESCURO -> true
                TemaApp.SISTEMA -> isSystemInDarkTheme()
            }
            ControleDeEstudosTheme(useDarkTheme = temaEscuro) {
                SolicitarPermissoesNecessarias()
                AppNavigation()
            }
        }
    }
}

/**
 * Solicita a permissão de notificações (Android 13+) e, se necessário, orienta o
 * usuário a liberar alarmes exatos (Android 12+) para que os alertas de aula
 * disparem no horário certo.
 */
@Composable
private fun SolicitarPermissoesNecessarias() {
    val context = LocalContext.current
    var mostrarDialogoAlarme by remember { mutableStateOf(false) }

    val lancadorPermissaoNotificacao = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* resultado tratado silenciosamente: sem alerta o app ainda funciona normalmente */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedida = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!concedida) {
                lancadorPermissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                mostrarDialogoAlarme = true
            }
        }
    }

    if (mostrarDialogoAlarme) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAlarme = false },
            title = { Text("Permitir alarmes exatos") },
            text = {
                Column {
                    Text("Para que os alertas de cada aula toquem exatamente no horário agendado, permita alarmes exatos para o Controle de Estudos nas configurações do sistema.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoAlarme = false
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }) { Text("Abrir configurações") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAlarme = false }) { Text("Agora não") }
            }
        )
    }
}
