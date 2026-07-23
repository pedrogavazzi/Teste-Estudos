package com.pedrogavazzi.controleestudos.ui.agenda

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.data.StatusAula
import com.pedrogavazzi.controleestudos.data.statusAtual
import com.pedrogavazzi.controleestudos.ui.components.StatusChip
import com.pedrogavazzi.controleestudos.ui.components.abrirSeletorDeDataEHora
import com.pedrogavazzi.controleestudos.ui.components.formatarDiaSemanaData
import com.pedrogavazzi.controleestudos.ui.components.formatarHora
import com.pedrogavazzi.controleestudos.ui.theme.VermelhoAlerta

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
                    item(key = "cabecalho_$dataFormatada") {
                        Text(
                            dataFormatada,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(itensDoDia, key = { it.aula.id }) { item ->
                        ItemAgenda(
                            item = item,
                            onMarcarConclusao = { concluida -> viewModel.marcarConclusao(item.aula, concluida) },
                            onReagendar = {
                                abrirSeletorDeDataEHora(context, item.aula.dataHoraMillis) { novaData ->
                                    viewModel.reagendarAula(item.aula, novaData)
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
    onMarcarConclusao: (Boolean) -> Unit,
    onReagendar: () -> Unit
) {
    val status = item.aula.statusAtual()
    val cor = runCatching { Color(android.graphics.Color.parseColor(item.corHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).background(cor, CircleShape))
            Spacer(Modifier.padding(start = 8.dp))
            Column(Modifier.weight(1f)) {
                Text("${item.nomeMateria} — Aula ${item.aula.numero}", fontWeight = FontWeight.Medium)
                Text(formatarHora(item.aula.dataHoraMillis!!), style = MaterialTheme.typography.bodyLarge)
                if (status == StatusAula.ATRASADA) {
                    Text("Atrasada — não concluída nem reagendada", color = VermelhoAlerta, style = MaterialTheme.typography.labelSmall)
                }
            }
            StatusChip(status, modifier = Modifier.padding(end = 8.dp))
            if (status == StatusAula.ATRASADA) {
                IconButton(onClick = onReagendar) {
                    Icon(Icons.Filled.Update, contentDescription = "Reagendar")
                }
            }
            Checkbox(checked = item.aula.concluida, onCheckedChange = onMarcarConclusao)
        }
    }
}
