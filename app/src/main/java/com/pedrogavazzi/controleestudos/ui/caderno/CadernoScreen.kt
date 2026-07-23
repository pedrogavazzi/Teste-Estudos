package com.pedrogavazzi.controleestudos.ui.caderno

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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.pedrogavazzi.controleestudos.data.StatusAula
import com.pedrogavazzi.controleestudos.data.nomeExibido
import com.pedrogavazzi.controleestudos.data.statusAtual
import com.pedrogavazzi.controleestudos.ui.agenda.AulaComMateria
import com.pedrogavazzi.controleestudos.ui.components.CaixaConclusao
import com.pedrogavazzi.controleestudos.ui.components.StatusChip
import com.pedrogavazzi.controleestudos.ui.components.TextoNomeMateria
import com.pedrogavazzi.controleestudos.ui.components.formatarHora

/**
 * Aba "Caderno": aulas de hoje, práticas para anotar durante a própria aula. Toque em uma
 * aula para abrir o editor completo (formatação, tópicos, etc.); ao salvar alguma anotação,
 * a aula sai de "Em andamento" e passa para "Aulas feitas" — mas continua editável.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadernoScreen(viewModel: CadernoViewModel, onAbrirAula: (Long) -> Unit) {
    val estado by viewModel.estado.collectAsState()
    val semAulas = estado.emAndamento.isEmpty() && estado.aulasFeitas.isEmpty()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Caderno de hoje") }) }
    ) { padding ->
        if (semAulas) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text("Nenhuma aula agendada para hoje.", modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (estado.emAndamento.isNotEmpty()) {
                    item(key = "cabecalho_andamento") {
                        Text(
                            "Em andamento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(estado.emAndamento, key = { "andamento_${it.aula.id}" }) { item ->
                        ItemCaderno(
                            item = item,
                            onMarcarConclusao = { concluida -> viewModel.marcarConclusao(item.aula, concluida) },
                            onAbrir = { onAbrirAula(item.aula.id) }
                        )
                    }
                }
                if (estado.aulasFeitas.isNotEmpty()) {
                    item(key = "cabecalho_feitas") {
                        Text(
                            "Aulas feitas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(estado.aulasFeitas, key = { "feita_${it.aula.id}" }) { item ->
                        ItemCaderno(
                            item = item,
                            onMarcarConclusao = { concluida -> viewModel.marcarConclusao(item.aula, concluida) },
                            onAbrir = { onAbrirAula(item.aula.id) }
                        )
                    }
                }
                item { Spacer(Modifier.padding(40.dp)) }
            }
        }
    }
}

@Composable
private fun ItemCaderno(
    item: AulaComMateria,
    onMarcarConclusao: (Boolean) -> Unit,
    onAbrir: () -> Unit
) {
    val status = item.aula.statusAtual()
    val cor = runCatching { Color(android.graphics.Color.parseColor(item.corHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = com.pedrogavazzi.controleestudos.ui.theme.FormaCard,
        colors = com.pedrogavazzi.controleestudos.ui.theme.corDeCardTonal()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(cor, CircleShape))
                Spacer(Modifier.padding(start = 8.dp))
                Column(Modifier.weight(1f)) {
                    TextoNomeMateria(
                        nome = item.nomeMateria,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        item.aula.nomeExibido(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(formatarHora(item.aula.dataHoraMillis!!), style = MaterialTheme.typography.bodyMedium)
                }
                StatusChip(status, modifier = Modifier.padding(end = 8.dp))
                CaixaConclusao(concluida = item.aula.concluida, onAlterar = onMarcarConclusao)
            }

            if (status == StatusAula.ATRASADA) {
                Text(
                    "Atrasada — não concluída nem reagendada",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.padding(top = 12.dp))
            PreviaDoCaderno(
                anotacoesCaderno = item.aula.anotacoesCaderno,
                onClick = onAbrir,
                titulo = "Anotações desta aula"
            )
        }
    }
}
