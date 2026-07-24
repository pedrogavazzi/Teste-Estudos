package com.pedrogavazzi.controleestudos.ui.configuracoes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pedrogavazzi.controleestudos.data.OPCOES_ANTECEDENCIA_MINUTOS
import com.pedrogavazzi.controleestudos.data.TemaApp
import com.pedrogavazzi.controleestudos.ui.theme.FormaCard
import com.pedrogavazzi.controleestudos.ui.theme.corDeCardTonal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(viewModel: ConfiguracoesViewModel) {
    val tema by viewModel.tema.collectAsState()
    val usarCorDinamica by viewModel.usarCorDinamica.collectAsState()
    val notificacoesAtivadas by viewModel.notificacoesAtivadas.collectAsState()
    val somAtivado by viewModel.somAtivado.collectAsState()
    val vibracaoAtivada by viewModel.vibracaoAtivada.collectAsState()
    val minutosAntecedencia by viewModel.minutosAntecedencia.collectAsState()
    val permissaoConcedida = permissaoNotificacaoConcedida()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Configurações", style = MaterialTheme.typography.titleLarge) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SecaoConfiguracao(titulo = "Tema", icone = Icons.Filled.Palette) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = tema == TemaApp.SISTEMA,
                            onClick = { viewModel.definirTema(TemaApp.SISTEMA) },
                            label = { Text("Automático") }
                        )
                        FilterChip(
                            selected = tema == TemaApp.CLARO,
                            onClick = { viewModel.definirTema(TemaApp.CLARO) },
                            label = { Text("Claro") }
                        )
                        FilterChip(
                            selected = tema == TemaApp.ESCURO,
                            onClick = { viewModel.definirTema(TemaApp.ESCURO) },
                            label = { Text("Escuro") }
                        )
                    }
                    Column(Modifier.padding(top = 12.dp)) {
                        LinhaOpcao(
                            titulo = "Usar cor do papel de parede (Material You)",
                            checked = usarCorDinamica,
                            onCheckedChange = { viewModel.definirUsarCorDinamica(it) }
                        )
                        Text(
                            "Desligado, o app usa sempre as cores roxas originais, combinando "
                                + "melhor com as cores escolhidas para cada matéria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SecaoConfiguracao(titulo = "Notificações", icone = Icons.Filled.Notifications) {
                    LinhaOpcao(
                        titulo = "Ativar notificações de aula",
                        checked = notificacoesAtivadas,
                        onCheckedChange = { viewModel.definirNotificacoesAtivadas(it) }
                    )
                }
            }

            if (notificacoesAtivadas && !permissaoConcedida) {
                item { AvisoPermissaoNegada() }
            }

            item {
                SecaoConfiguracao(titulo = "Som", icone = Icons.Filled.VolumeUp, habilitada = notificacoesAtivadas) {
                    LinhaOpcao(
                        titulo = "Tocar som na notificação",
                        checked = somAtivado,
                        habilitada = notificacoesAtivadas,
                        onCheckedChange = { viewModel.definirSomAtivado(it) }
                    )
                }
            }

            item {
                SecaoConfiguracao(titulo = "Vibração", icone = Icons.Filled.Vibration, habilitada = notificacoesAtivadas) {
                    LinhaOpcao(
                        titulo = "Vibrar na notificação",
                        checked = vibracaoAtivada,
                        habilitada = notificacoesAtivadas,
                        onCheckedChange = { viewModel.definirVibracaoAtivada(it) }
                    )
                }
            }

            item {
                SecaoConfiguracao(titulo = "Antecedência do alerta", icone = Icons.Filled.Timer, habilitada = notificacoesAtivadas) {
                    Text(
                        "Quando o alerta deve tocar em relação ao horário agendado da aula",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OPCOES_ANTECEDENCIA_MINUTOS.chunked(4).forEach { linha ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            linha.forEach { minutos ->
                                FilterChip(
                                    selected = minutosAntecedencia == minutos,
                                    onClick = { viewModel.definirMinutosAntecedencia(minutos) },
                                    enabled = notificacoesAtivadas,
                                    label = { Text(if (minutos == 0) "Na hora" else "$minutos min") }
                                )
                            }
                        }
                    }
                }
            }

            item { SecaoExportar(viewModel) }

            item {
                Text(
                    "Controle de Estudos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SecaoExportar(viewModel: ConfiguracoesViewModel) {
    val context = LocalContext.current
    SecaoConfiguracao(titulo = "Dados", icone = Icons.Filled.Download) {
        Text(
            "Gera um resumo em texto de todas as matérias e aulas cadastradas, para guardar "
                + "ou compartilhar como backup manual.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(
            onClick = {
                viewModel.gerarTextoExportacao { texto ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, texto)
                        putExtra(Intent.EXTRA_SUBJECT, "Controle de Estudos — exportação de dados")
                    }
                    context.startActivity(Intent.createChooser(intent, "Exportar dados"))
                }
            },
            modifier = Modifier.padding(top = 4.dp)
        ) { Text("Exportar dados") }
    }
}

@Composable
private fun SecaoConfiguracao(
    titulo: String,
    icone: ImageVector,
    habilitada: Boolean = true,
    conteudo: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaCard,
        colors = corDeCardTonal()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icone,
                    contentDescription = null,
                    tint = if (habilitada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                    color = if (habilitada) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                )
            }
            Column(Modifier.padding(top = 8.dp)) { conteudo() }
        }
    }
}

@Composable
private fun LinhaOpcao(
    titulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    habilitada: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(titulo, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = habilitada)
    }
}

/**
 * Verifica se a permissão de notificação do sistema (Android 13+) está concedida, e reconfere
 * automaticamente quando a tela volta ao primeiro plano — importante porque o usuário pode ter
 * ido conceder a permissão nas configurações do sistema e voltado para o app.
 */
@Composable
private fun permissaoNotificacaoConcedida(): Boolean {
    val context = LocalContext.current

    fun checar(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    var concedida by remember { mutableStateOf(checar()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) concedida = checar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return concedida
}

@Composable
private fun AvisoPermissaoNegada() {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FormaCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.NotificationsOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Permissão de notificação desligada no sistema",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(start = 8.dp).weight(1f)
                )
            }
            Text(
                "As notificações estão ativadas aqui no app, mas o Android está bloqueando "
                    + "notificações do Controle de Estudos — nenhum alerta vai tocar até isso ser liberado.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(top = 4.dp)
            )
            TextButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(top = 4.dp)
            ) { Text("Abrir configurações de notificação") }
        }
    }
}
