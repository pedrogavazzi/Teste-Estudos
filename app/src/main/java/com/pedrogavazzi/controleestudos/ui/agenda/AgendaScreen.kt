package com.pedrogavazzi.controleestudos.ui.agenda

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.data.StatusAula
import com.pedrogavazzi.controleestudos.data.nomeExibido
import com.pedrogavazzi.controleestudos.data.statusAtual
import com.pedrogavazzi.controleestudos.ui.components.CaixaConclusao
import com.pedrogavazzi.controleestudos.ui.components.CampoDeBusca
import com.pedrogavazzi.controleestudos.ui.components.ConteudoComLarguraMaxima
import com.pedrogavazzi.controleestudos.ui.components.IniciaisDaMateria
import com.pedrogavazzi.controleestudos.ui.components.StatusChip
import com.pedrogavazzi.controleestudos.ui.components.TextoNomeMateria
import com.pedrogavazzi.controleestudos.ui.components.abrirLinkDaAula
import com.pedrogavazzi.controleestudos.ui.components.abrirSeletorDeDataEHora
import com.pedrogavazzi.controleestudos.ui.components.formatarDiaSemanaData
import com.pedrogavazzi.controleestudos.ui.components.formatarHora
import com.pedrogavazzi.controleestudos.ui.theme.VermelhoAlerta
import kotlinx.coroutines.launch
import java.util.Calendar

private fun ehHoje(millis: Long): Boolean {
    val hoje = Calendar.getInstance()
    val data = Calendar.getInstance().apply { timeInMillis = millis }
    return hoje.get(Calendar.YEAR) == data.get(Calendar.YEAR) &&
        hoje.get(Calendar.DAY_OF_YEAR) == data.get(Calendar.DAY_OF_YEAR)
}

/** Início do dia (00:00:00.000) em que [millis] cai — usado como chave de agrupamento, já que
 *  o texto formatado ("Sexta-feira, 24/07") não inclui o ano e poderia juntar datas de anos
 *  diferentes no mesmo grupo visual. */
private fun inicioDoDia(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

/**
 * Aba Agenda: além da lista de aulas com data (agrupadas por dia, com destaque para hoje),
 * mostra também as aulas ainda sem data marcada — antes elas ficavam invisíveis aqui, só
 * apareciam entrando na matéria específica — e um filtro rápido para ver só as atrasadas.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    viewModel: AgendaViewModel,
    onAbrirCaderno: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val aulas by viewModel.aulasAgendadas.collectAsState()
    val aulasSemData by viewModel.aulasSemData.collectAsState()
    val temAulasComData by viewModel.temAulasComData.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    var termoBusca by remember { mutableStateOf("") }

    val aulasFiltradas = remember(aulas, termoBusca) {
        if (termoBusca.isBlank()) aulas
        else aulas.filter {
            it.nomeMateria.contains(termoBusca, ignoreCase = true) ||
                it.aula.nomeExibido().contains(termoBusca, ignoreCase = true)
        }
    }
    val agrupadasPorData = aulasFiltradas.groupBy { inicioDoDia(it.aula.dataHoraMillis!!) }
    val semDataFiltradas = remember(aulasSemData, termoBusca) {
        if (termoBusca.isBlank()) aulasSemData
        else aulasSemData.filter {
            it.nomeMateria.contains(termoBusca, ignoreCase = true) ||
                it.aula.nomeExibido().contains(termoBusca, ignoreCase = true)
        }
    }
    // A seção "sem data" só aparece no filtro "Pendentes" (não faz sentido misturar com
    // Atrasadas/Concluídas) — essa mesma condição decide tanto se a seção é desenhada quanto
    // se a tela está "vazia".
    val semDataVisivel = semDataFiltradas.isNotEmpty() && filtro == FiltroAgenda.PENDENTES

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val escopo = rememberCoroutineScope()
    val mostrarBotaoTopo by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("Agenda", style = MaterialTheme.typography.titleLarge) }
            )
        },
        floatingActionButton = {
            androidx.compose.animation.AnimatedVisibility(
                visible = mostrarBotaoTopo,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                androidx.compose.material3.SmallFloatingActionButton(
                    onClick = { escopo.launch { listState.animateScrollToItem(0) } }
                ) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Voltar ao topo")
                }
            }
        }
    ) { padding ->
        ConteudoComLarguraMaxima(Modifier.padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                if (aulas.size > 3 || aulasSemData.isNotEmpty()) {
                    CampoDeBusca(
                        valor = termoBusca,
                        onValorAlterado = { termoBusca = it },
                        placeholder = "Buscar matéria ou aula",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                if (temAulasComData) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filtro == FiltroAgenda.PENDENTES,
                            onClick = { viewModel.selecionarFiltro(FiltroAgenda.PENDENTES) },
                            label = { Text("Pendentes") }
                        )
                        FilterChip(
                            selected = filtro == FiltroAgenda.ATRASADAS,
                            onClick = { viewModel.selecionarFiltro(FiltroAgenda.ATRASADAS) },
                            label = { Text("Atrasadas") }
                        )
                        FilterChip(
                            selected = filtro == FiltroAgenda.CONCLUIDAS,
                            onClick = { viewModel.selecionarFiltro(FiltroAgenda.CONCLUIDAS) },
                            label = { Text("Concluídas") }
                        )
                    }
                }
                if (!temAulasComData && aulasSemData.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.padding(4.dp))
                            Text(
                                "Nenhuma aula agendada ainda.\nVá até uma matéria para marcar dia e horário das aulas.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else if (aulasFiltradas.isEmpty() && !semDataVisivel) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            when {
                                termoBusca.isNotBlank() -> "Nenhuma aula encontrada para \"$termoBusca\"."
                                filtro == FiltroAgenda.ATRASADAS -> "Nenhuma aula atrasada — tudo em dia."
                                filtro == FiltroAgenda.CONCLUIDAS -> "Nenhuma aula concluída ainda."
                                else -> "Nenhuma aula pendente — tudo agendado ou concluído."
                            },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        agrupadasPorData.forEach { (inicioDia, itensDoDia) ->
                            val diaEhHoje = ehHoje(inicioDia)
                            val dataFormatada = formatarDiaSemanaData(inicioDia)
                            item(key = "cabecalho_$inicioDia") {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                                    Text(
                                        dataFormatada,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (diaEhHoje) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (diaEhHoje) {
                                        Spacer(Modifier.padding(start = 8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                "HOJE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            items(itensDoDia, key = { it.aula.id }) { item ->
                                ItemAgenda(
                                    item = item,
                                    destaque = diaEhHoje,
                                    onAbrirCaderno = { onAbrirCaderno(item.aula.id) },
                                    onMarcarConclusao = { concluida -> viewModel.marcarConclusao(item.aula, concluida) },
                                    onAlterarData = {
                                        abrirSeletorDeDataEHora(context, item.aula.dataHoraMillis) { novaData ->
                                            if (item.aula.statusAtual() == StatusAula.ATRASADA) {
                                                viewModel.reagendarAula(item.aula, novaData)
                                            } else {
                                                viewModel.agendarAula(item.aula, novaData)
                                            }
                                        }
                                    },
                                    onSalvarLink = { link -> viewModel.salvarLink(item.aula, link) }
                                )
                            }
                        }
                        if (semDataVisivel) {
                            item(key = "cabecalho_sem_data") {
                                Text(
                                    "Sem data marcada (${semDataFiltradas.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                                )
                            }
                            items(semDataFiltradas, key = { "semdata_${it.aula.id}" }) { item ->
                                ItemSemData(
                                    item = item,
                                    onAgendar = { novaData -> viewModel.agendarAula(item.aula, novaData) }
                                )
                            }
                        }
                        item { Spacer(Modifier.padding(40.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemAgenda(
    item: AulaComMateria,
    destaque: Boolean,
    onAbrirCaderno: () -> Unit,
    onMarcarConclusao: (Boolean) -> Unit,
    onAlterarData: () -> Unit,
    onSalvarLink: (String) -> Unit
) {
    val context = LocalContext.current
    val status = item.aula.statusAtual()
    val cor = runCatching { Color(android.graphics.Color.parseColor(item.corHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)
    var mostrarDialogoLink by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrirCaderno),
        shape = com.pedrogavazzi.controleestudos.ui.theme.FormaCard,
        border = if (destaque) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        colors = if (destaque) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
        } else {
            com.pedrogavazzi.controleestudos.ui.theme.corDeCardTonal()
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IniciaisDaMateria(nomeMateria = item.nomeMateria, cor = cor)
            Spacer(Modifier.padding(start = 8.dp))
            Column(Modifier.weight(1f)) {
                TextoNomeMateria(
                    nome = item.nomeMateria,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(item.aula.nomeExibido(), style = MaterialTheme.typography.bodyLarge)
                Text(formatarHora(item.aula.dataHoraMillis!!), style = MaterialTheme.typography.bodyLarge)
                if (status == StatusAula.ATRASADA) {
                    Text("Atrasada — não concluída nem reagendada", color = VermelhoAlerta, style = MaterialTheme.typography.labelSmall)
                }
            }
            if (item.aula.link.isNotBlank()) {
                IconButton(onClick = { abrirLinkDaAula(context, item.aula.link) }) {
                    Icon(
                        Icons.Filled.Link,
                        contentDescription = "Abrir link de ${item.aula.nomeExibido()}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                // Sem link ainda: em vez de mandar pra tela da matéria só pra colar um link,
                // deixa preencher direto daqui — é bem comum descobrir o link da aula (ex.:
                // convite de videochamada) já olhando a agenda.
                IconButton(onClick = { mostrarDialogoLink = true }) {
                    Icon(
                        Icons.Filled.AddLink,
                        contentDescription = "Adicionar link de ${item.aula.nomeExibido()}",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
            StatusChip(status, modifier = Modifier.padding(end = 8.dp))
            IconButton(onClick = onAlterarData) {
                Icon(
                    if (status == StatusAula.ATRASADA) Icons.Filled.Update else Icons.Filled.Event,
                    contentDescription = if (status == StatusAula.ATRASADA) "Reagendar" else "Alterar data e horário"
                )
            }
            CaixaConclusao(concluida = item.aula.concluida, onAlterar = onMarcarConclusao)
        }
    }

    if (mostrarDialogoLink) {
        DialogoPreencherLink(
            valorInicial = item.aula.link,
            onConfirmar = { novoLink ->
                onSalvarLink(novoLink)
                mostrarDialogoLink = false
            },
            onCancelar = { mostrarDialogoLink = false }
        )
    }
}

@Composable
private fun DialogoPreencherLink(
    valorInicial: String,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf(valorInicial) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Link da aula") },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text("Videochamada, gravação, material...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { onConfirmar(texto.trim()) }) { Text("Salvar") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ItemSemData(item: AulaComMateria, onAgendar: (Long) -> Unit) {
    val context = LocalContext.current
    val cor = runCatching { Color(android.graphics.Color.parseColor(item.corHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = com.pedrogavazzi.controleestudos.ui.theme.FormaCard,
        colors = com.pedrogavazzi.controleestudos.ui.theme.corDeCardTonal()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IniciaisDaMateria(nomeMateria = item.nomeMateria, cor = cor)
            Spacer(Modifier.padding(start = 8.dp))
            Column(Modifier.weight(1f)) {
                TextoNomeMateria(
                    nome = item.nomeMateria,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(item.aula.nomeExibido(), style = MaterialTheme.typography.bodyLarge)
            }
            // Abre o calendário na hora — antes isso mandava pra tela da matéria só pra
            // agendar de lá, um passo a mais sem necessidade.
            TextButton(onClick = {
                abrirSeletorDeDataEHora(context, null) { novaData -> onAgendar(novaData) }
            }) { Text("Agendar") }
        }
    }
}
