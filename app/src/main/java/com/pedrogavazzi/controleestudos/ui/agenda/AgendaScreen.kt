package com.pedrogavazzi.controleestudos.ui.agenda

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.data.StatusAula
import com.pedrogavazzi.controleestudos.data.nomeExibido
import com.pedrogavazzi.controleestudos.data.statusAtual
import com.pedrogavazzi.controleestudos.ui.components.CaixaConclusao
import com.pedrogavazzi.controleestudos.ui.components.StatusChip
import com.pedrogavazzi.controleestudos.ui.components.TextoNomeMateria
import com.pedrogavazzi.controleestudos.ui.components.abrirSeletorDeDataEHora
import com.pedrogavazzi.controleestudos.ui.components.formatarDiaSemanaData
import com.pedrogavazzi.controleestudos.ui.components.formatarHora
import com.pedrogavazzi.controleestudos.ui.theme.VermelhoAlerta
import java.util.Calendar

private fun ehHoje(millis: Long): Boolean {
    val hoje = Calendar.getInstance()
    val data = Calendar.getInstance().apply { timeInMillis = millis }
    return hoje.get(Calendar.YEAR) == data.get(Calendar.YEAR) &&
        hoje.get(Calendar.DAY_OF_YEAR) == data.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun AgendaScreen(viewModel: AgendaViewModel) {
    val context = LocalContext.current
    val aulas by viewModel.aulasAgendadas.collectAsState()
    val agrupadasPorData = aulas.groupBy { formatarDiaSemanaData(it.aula.dataHoraMillis!!) }

    Scaffold { padding ->
        if (aulas.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
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
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                agrupadasPorData.forEach { (dataFormatada, itensDoDia) ->
                    val diaEhHoje = itensDoDia.first().aula.dataHoraMillis?.let { ehHoje(it) } == true
                    item(key = "cabecalho_$dataFormatada") {
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
                            onMarcarConclusao = { concluida -> viewModel.marcarConclusao(item.aula, concluida) },
                            onAlterarData = {
                                abrirSeletorDeDataEHora(context, item.aula.dataHoraMillis) { novaData ->
                                    if (item.aula.statusAtual() == StatusAula.ATRASADA) {
                                        viewModel.reagendarAula(item.aula, novaData)
                                    } else {
                                        viewModel.agendarAula(item.aula, novaData)
                                    }
                                }
                            }
                        )
                    }
                }
                item { Spacer(Modifier.padding(40.dp)) }
            }
        }
    }
}

@Composable
private fun ItemAgenda(
    item: AulaComMateria,
    destaque: Boolean,
    onMarcarConclusao: (Boolean) -> Unit,
    onAlterarData: () -> Unit
) {
    val status = item.aula.statusAtual()
    val cor = runCatching { Color(android.graphics.Color.parseColor(item.corHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Box(Modifier.size(10.dp).background(cor, CircleShape))
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
}
