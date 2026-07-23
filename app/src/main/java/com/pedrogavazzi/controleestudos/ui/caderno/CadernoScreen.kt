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
import androidx.compose.material3.Checkbox
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
import com.pedrogavazzi.controleestudos.data.statusAtual
import com.pedrogavazzi.controleestudos.ui.agenda.AulaComMateria
import com.pedrogavazzi.controleestudos.ui.components.ObservacaoEditor
import com.pedrogavazzi.controleestudos.ui.components.StatusChip
import com.pedrogavazzi.controleestudos.ui.components.formatarHora

/** Aba "Caderno": aulas agendadas para hoje, com espaço para anotar sobre cada uma e salvar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadernoScreen(viewModel: CadernoViewModel) {
    val aulas by viewModel.aulasDeHoje.collectAsState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Caderno de hoje") }) }
    ) { padding ->
        if (aulas.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        "Nenhuma aula agendada para hoje.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(aulas, key = { it.aula.id }) { item ->
                    ItemCaderno(
                        item = item,
                        onMarcarConclusao = { concluida -> viewModel.marcarConclusao(item.aula, concluida) },
                        onSalvarObservacao = { texto -> viewModel.salvarObservacao(item.aula, texto) }
                    )
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
    onSalvarObservacao: (String) -> Unit
) {
    val status = item.aula.statusAtual()
    val cor = runCatching { Color(android.graphics.Color.parseColor(item.corHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(cor, CircleShape))
                Spacer(Modifier.padding(start = 8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${item.nomeMateria} — Aula ${item.aula.numero}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(formatarHora(item.aula.dataHoraMillis!!), style = MaterialTheme.typography.bodyMedium)
                }
                StatusChip(status, modifier = Modifier.padding(end = 8.dp))
                Checkbox(checked = item.aula.concluida, onCheckedChange = onMarcarConclusao)
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
            ObservacaoEditor(
                chaveDeIdentidade = item.aula.id,
                observacaoSalva = item.aula.observacao,
                onSalvar = onSalvarObservacao,
                rotulo = "Anotações da aula"
            )
        }
    }
}
