package com.pedrogavazzi.controleestudos.ui.desempenho

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.InsertChartOutlined
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.ui.components.ConteudoComLarguraMaxima
import com.pedrogavazzi.controleestudos.ui.components.IniciaisDaMateria
import com.pedrogavazzi.controleestudos.ui.theme.VermelhoAlerta

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DesempenhoScreen(
    viewModel: DesempenhoViewModel,
    onIrParaMaterias: () -> Unit = {},
    onVerAtrasadas: () -> Unit = {}
) {
    val desempenho by viewModel.desempenho.collectAsState()

    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("Desempenho", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { padding ->
        ConteudoComLarguraMaxima(Modifier.padding(padding)) {
            if (desempenho.totalAulas == 0) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.InsertChartOutlined,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(4.dp))
                        Text(
                            "Ainda não há aulas cadastradas.\nCadastre matérias para acompanhar seu desempenho aqui.",
                            modifier = Modifier.padding(16.dp)
                        )
                        Spacer(Modifier.padding(top = 8.dp))
                        Button(onClick = onIrParaMaterias) { Text("Ir para Matérias") }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        CardDesempenhoGeral(
                            totalAulas = desempenho.totalAulas,
                            concluidas = desempenho.aulasConcluidas,
                            atrasadas = desempenho.aulasAtrasadas,
                            percentual = desempenho.percentual,
                            onVerAtrasadas = onVerAtrasadas
                        )
                    }
                    item {
                        Text(
                            "Desempenho por matéria",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            "Ordenado por quem precisa de mais atenção primeiro.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(desempenho.porMateria, key = { it.materia.id }) { item ->
                        CardDesempenhoMateria(item)
                    }
                    item { Spacer(Modifier.padding(40.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CardDesempenhoGeral(
    totalAulas: Int,
    concluidas: Int,
    atrasadas: Int,
    percentual: Float,
    onVerAtrasadas: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = com.pedrogavazzi.controleestudos.ui.theme.FormaCard,
        colors = com.pedrogavazzi.controleestudos.ui.theme.corDeCardTonalDestacado()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Desempenho geral", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.padding(top = 8.dp))
            Text(
                "$concluidas de $totalAulas aulas concluídas",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.padding(top = 8.dp))
            LinearProgressIndicator(
                progress = { percentual },
                modifier = Modifier.fillMaxWidth().height(10.dp)
            )
            Spacer(Modifier.padding(top = 4.dp))
            Text("${(percentual * 100).toInt()}% concluído", style = MaterialTheme.typography.bodyLarge)

            if (atrasadas > 0) {
                Spacer(Modifier.padding(top = 12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onVerAtrasadas),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$atrasadas aula(s) atrasada(s)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = VermelhoAlerta,
                        modifier = Modifier.weight(1f)
                    )
                    Text("Ver na Agenda", style = MaterialTheme.typography.labelMedium, color = VermelhoAlerta)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = VermelhoAlerta)
                }
            }
        }
    }
}

@Composable
private fun CardDesempenhoMateria(item: DesempenhoMateria) {
    val cor = runCatching { Color(android.graphics.Color.parseColor(item.materia.corHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = com.pedrogavazzi.controleestudos.ui.theme.FormaCard,
        colors = com.pedrogavazzi.controleestudos.ui.theme.corDeCardTonal()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IniciaisDaMateria(nomeMateria = item.materia.nome, cor = cor)
                Spacer(Modifier.padding(start = 8.dp))
                Text(item.materia.nome, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text("${(item.percentual * 100).toInt()}%")
            }
            Spacer(Modifier.padding(top = 6.dp))
            LinearProgressIndicator(progress = { item.percentual }, modifier = Modifier.fillMaxWidth(), color = cor)
            Spacer(Modifier.padding(top = 4.dp))
            Text(
                "${item.aulasConcluidas} de ${item.totalAulas} concluídas" +
                    if (item.aulasAtrasadas > 0) " • ${item.aulasAtrasadas} atrasada(s)" else "",
                style = MaterialTheme.typography.labelSmall,
                color = if (item.aulasAtrasadas > 0) VermelhoAlerta else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
