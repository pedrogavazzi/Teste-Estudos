package com.pedrogavazzi.controleestudos.ui.desempenho

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertChartOutlined
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.ui.components.ConteudoComLarguraMaxima

/**
 * Painel: antes mostrava um resumo por matéria que duplicava o que a aba Matérias já
 * exibe (progresso, percentual, atrasadas) — foi simplificado pra mostrar só o que não
 * existe em nenhum outro lugar do app: o total de aulas cadastradas.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DesempenhoScreen(
    viewModel: DesempenhoViewModel,
    onIrParaMaterias: () -> Unit = {}
) {
    val totalAulas by viewModel.totalAulas.collectAsState()

    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("Painel", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { padding ->
        ConteudoComLarguraMaxima(Modifier.padding(padding)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (totalAulas == 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.InsertChartOutlined,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(4.dp))
                        Text(
                            "Ainda não há aulas cadastradas.\nCadastre matérias para ver o total aqui.",
                            modifier = Modifier.padding(16.dp)
                        )
                        Spacer(Modifier.padding(top = 8.dp))
                        Button(onClick = onIrParaMaterias) { Text("Ir para Matérias") }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        shape = com.pedrogavazzi.controleestudos.ui.theme.FormaCard,
                        colors = com.pedrogavazzi.controleestudos.ui.theme.corDeCardTonalDestacado()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "$totalAulas",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                if (totalAulas == 1) "aula cadastrada" else "aulas cadastradas",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
