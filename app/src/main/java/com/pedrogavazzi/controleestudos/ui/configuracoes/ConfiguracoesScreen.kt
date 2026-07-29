package com.pedrogavazzi.controleestudos.ui.configuracoes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
                val abaInicial by viewModel.abaInicial.collectAsState()
                SecaoConfiguracao(titulo = "Aba inicial", icone = Icons.Filled.Home) {
                    Text(
                        "Qual aba abre primeiro quando você entra no app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = abaInicial == com.pedrogavazzi.controleestudos.data.AbaInicial.AGENDA,
                            onClick = { viewModel.definirAbaInicial(com.pedrogavazzi.controleestudos.data.AbaInicial.AGENDA) },
                            label = { Text("Agenda") }
                        )
                        FilterChip(
                            selected = abaInicial == com.pedrogavazzi.controleestudos.data.AbaInicial.MATERIAS,
                            onClick = { viewModel.definirAbaInicial(com.pedrogavazzi.controleestudos.data.AbaInicial.MATERIAS) },
                            label = { Text("Matérias") }
                        )
                        FilterChip(
                            selected = abaInicial == com.pedrogavazzi.controleestudos.data.AbaInicial.CADERNO,
                            onClick = { viewModel.definirAbaInicial(com.pedrogavazzi.controleestudos.data.AbaInicial.CADERNO) },
                            label = { Text("Caderno") }
                        )
                        FilterChip(
                            selected = abaInicial == com.pedrogavazzi.controleestudos.data.AbaInicial.FAVORITOS,
                            onClick = { viewModel.definirAbaInicial(com.pedrogavazzi.controleestudos.data.AbaInicial.FAVORITOS) },
                            label = { Text("Favoritos") }
                        )
                    }
                }
            }

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
                SecaoConfiguracao(titulo = "Antecedência do alerta", icone = Icons.Filled.Timer, habilitada = notificacoesAtivadas) {
                    Text(
                        "Quando o alerta deve tocar em relação ao horário agendado da aula",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OPCOES_ANTECEDENCIA_MINUTOS.forEach { minutos ->
                            FilterChip(
                                selected = minutosAntecedencia == minutos,
                                onClick = { viewModel.definirMinutosAntecedencia(minutos) },
                                enabled = notificacoesAtivadas,
                                label = { Text(if (minutos == 0) "Na hora" else "$minutos min") }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Arraste para o lado para ver todas as opções",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            item { SecaoAgendamentoAutomatico(viewModel) }

            item { SecaoExportar(viewModel) }

            item {
                Text(
                    "Controle de Estudos — versão ${obterVersaoDoApp(LocalContext.current)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SecaoAgendamentoAutomatico(viewModel: ConfiguracoesViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val config by viewModel.configuracaoAutomatica.collectAsState()
    val materiasComPendentes by viewModel.materiasComPendentes.collectAsState()
    var expandido by remember { mutableStateOf(false) }
    var mostrarConfirmacaoRefazer by remember { mutableStateOf(false) }
    var mensagemResultado by remember { mutableStateOf<String?>(null) }

    // Começa com todas as matérias pendentes marcadas (pra continuar funcionando "liso" pra
    // quem só quer agendar tudo de uma vez) — depois disso, respeita o que o aluno escolher
    // manualmente pra cada ciclo, sem forçar a seleção de novo a cada recomposição.
    var materiasSelecionadas by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var selecaoInicializada by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(materiasComPendentes) {
        if (!selecaoInicializada && materiasComPendentes.isNotEmpty()) {
            materiasSelecionadas = materiasComPendentes.map { it.materia.id }.toSet()
            selecaoInicializada = true
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = com.pedrogavazzi.controleestudos.ui.theme.FormaCard,
        colors = com.pedrogavazzi.controleestudos.ui.theme.corDeCardTonal()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Recolhida por padrão — depois de usada, volta a ficar fechada sozinha, pra não
            // deixar uma seção de configuração avançada ocupando espaço na tela o tempo todo.
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expandido = !expandido },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "Agendamento automático",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp).weight(1f)
                )
                Icon(
                    if (expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expandido) "Recolher" else "Expandir"
                )
            }

            androidx.compose.animation.AnimatedVisibility(visible = expandido) {
                Column(Modifier.padding(top = 12.dp)) {
                    Text(
                        "Agenda sozinho as aulas ainda sem data das matérias escolhidas abaixo (um " +
                            "\"ciclo\"), misturando-as em rodízio entre si. Pra focar num grupo de " +
                            "matérias por vez, desmarque as outras — o próximo ciclo continua " +
                            "automaticamente depois deste, sem sobrepor datas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        "Matérias deste ciclo",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                    )
                    if (materiasComPendentes.isEmpty()) {
                        Text(
                            "Nenhuma matéria com aula pendente de agendar no momento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column {
                            materiasComPendentes.forEach { item ->
                                val marcada = item.materia.id in materiasSelecionadas
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            materiasSelecionadas = if (marcada) {
                                                materiasSelecionadas - item.materia.id
                                            } else {
                                                materiasSelecionadas + item.materia.id
                                            }
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = marcada,
                                        onCheckedChange = { marcar ->
                                            materiasSelecionadas = if (marcar) {
                                                materiasSelecionadas + item.materia.id
                                            } else {
                                                materiasSelecionadas - item.materia.id
                                            }
                                        }
                                    )
                                    Text(item.materia.nome, modifier = Modifier.weight(1f))
                                    Text(
                                        "${item.aulasPendentes} pendente(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "Aulas por dia",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..5).forEach { quantidade ->
                            FilterChip(
                                selected = config.aulasPorDia == quantidade,
                                onClick = {
                                    // Ajusta a lista de horários pro novo tamanho: mantém os já
                                    // escolhidos e completa o resto com um horário padrão (19h) —
                                    // nunca deixa o tamanho da lista descasar de aulasPorDia.
                                    val novosHorarios = (0 until quantidade).map { indice ->
                                        config.horariosMinutos.getOrElse(indice) { 19 * 60 }
                                    }
                                    viewModel.definirConfiguracaoAutomatica(
                                        config.copy(aulasPorDia = quantidade, horariosMinutos = novosHorarios)
                                    )
                                },
                                label = { Text("$quantidade") }
                            )
                        }
                    }

                    Text(
                        "Horários",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        config.horariosMinutos.forEachIndexed { indice, minutos ->
                            OutlinedButton(onClick = {
                                com.pedrogavazzi.controleestudos.ui.components.abrirSeletorDeHora(context, minutos) { novoMinuto ->
                                    val novaLista = config.horariosMinutos.toMutableList().apply { set(indice, novoMinuto) }
                                    viewModel.definirConfiguracaoAutomatica(config.copy(horariosMinutos = novaLista))
                                }
                            }) {
                                Text(com.pedrogavazzi.controleestudos.ui.components.formatarMinutosComoHora(minutos))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = config.incluirSabado,
                            onCheckedChange = { viewModel.definirConfiguracaoAutomatica(config.copy(incluirSabado = it)) }
                        )
                        Text("Incluir sábado")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = config.incluirDomingo,
                            onCheckedChange = { viewModel.definirConfiguracaoAutomatica(config.copy(incluirDomingo = it)) }
                        )
                        Text("Incluir domingo")
                    }

                    Button(
                        enabled = materiasSelecionadas.isNotEmpty(),
                        onClick = {
                            viewModel.agendarAutomaticamente(materiasSelecionadas) { quantidade ->
                                mensagemResultado = if (quantidade > 0) {
                                    "$quantidade aula(s) agendada(s) neste ciclo."
                                } else {
                                    "Nenhuma aula pendente nas matérias escolhidas."
                                }
                                expandido = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    ) { Text("Agendar este ciclo") }

                    OutlinedButton(
                        enabled = materiasSelecionadas.isNotEmpty(),
                        onClick = { mostrarConfirmacaoRefazer = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) { Text("Refazer agendamento deste ciclo") }
                }
            }

            mensagemResultado?.let { mensagem ->
                Text(
                    mensagem,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    if (mostrarConfirmacaoRefazer) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { mostrarConfirmacaoRefazer = false },
            title = { Text("Refazer o agendamento deste ciclo?") },
            text = {
                Text(
                    "Isso vai apagar a data de todas as aulas não concluídas das matérias " +
                        "marcadas acima — mesmo as que já têm data — e reorganizar só elas de " +
                        "novo, a partir de hoje. As outras matérias não são afetadas. Aulas já " +
                        "concluídas também não são afetadas. Essa ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacaoRefazer = false
                    viewModel.refazerAgendamentoAutomatico(materiasSelecionadas) { quantidade ->
                        mensagemResultado = "$quantidade aula(s) reorganizada(s) neste ciclo."
                    }
                    expandido = false
                }) { Text("Refazer") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacaoRefazer = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SecaoExportar(viewModel: ConfiguracoesViewModel) {
    val context = LocalContext.current
    SecaoConfiguracao(titulo = "Dados", icone = Icons.Filled.Download) {
        Text(
            "Gera um PDF com todas as matérias e aulas cadastradas — incluindo o caderno de "
                + "cada aula que já tiver alguma anotação — para guardar ou compartilhar como "
                + "backup manual.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(
            onClick = {
                viewModel.gerarPdfExportacao { arquivo ->
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context, "${context.packageName}.fileprovider", arquivo
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Controle de Estudos — exportação de dados")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Exportar dados"))
                }
            },
            modifier = Modifier.padding(top = 4.dp)
        ) { Text("Exportar dados em PDF") }
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

/** Nome da versão instalada (ex.: "1.0") — mostrado no rodapé de Configurações; se não
 *  conseguir ler por algum motivo, mostra um texto genérico em vez de quebrar a tela. */
private fun obterVersaoDoApp(context: android.content.Context): String =
    runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—"
    }.getOrDefault("—")

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
