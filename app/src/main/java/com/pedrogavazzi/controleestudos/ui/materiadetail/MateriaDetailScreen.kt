package com.pedrogavazzi.controleestudos.ui.materiadetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.pedrogavazzi.controleestudos.ControleEstudosApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaDetailScreen(
    materiaId: Long,
    onVoltar: () -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as ControleEstudosApp).repository

    val viewModel: MateriaDetailViewModel = viewModel(
        key = "materia_detail_$materiaId",
        factory = viewModelFactory {
            initializer { MateriaDetailViewModel(repository, materiaId) }
        }
    )

    val materia by viewModel.materia.collectAsState()
    val aulas by viewModel.aulas.collectAsState()
    val concluidas = aulas.count { it.concluida }
    val percentual = if (aulas.isEmpty()) 0f else concluidas / aulas.size.toFloat()
    val aulasSemData = aulas.count { it.dataHoraMillis == null }
    var mostrarDialogoLote by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(materia?.nome ?: "Aulas") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (aulasSemData > 0) {
                        IconButton(onClick = { mostrarDialogoLote = true }) {
                            Icon(Icons.Filled.EventRepeat, contentDescription = "Agendamento em lote")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "$concluidas de ${aulas.size} aulas concluídas (${(percentual * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodyLarge
                )
                LinearProgressIndicator(
                    progress = { percentual },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
            items(aulas, key = { it.id }) { aula ->
                AulaItem(
                    aula = aula,
                    onAgendar = { novaData -> viewModel.agendarAula(aula, novaData) },
                    onReagendar = { novaData -> viewModel.reagendarAula(aula, novaData) },
                    onMarcarConclusao = { concluida -> viewModel.marcarConclusao(aula, concluida) },
                    onDefinirAlerta = { ativado -> viewModel.definirAlerta(aula, ativado) },
                    onDefinirTipoAlerta = { tipo -> viewModel.definirTipoAlerta(aula, tipo) },
                    onSalvarObservacao = { texto -> viewModel.salvarObservacao(aula, texto) }
                )
            }
        }
    }

    if (mostrarDialogoLote) {
        AgendamentoEmLoteDialog(
            quantidadeMaximaDisponivel = aulasSemData,
            onDismiss = { mostrarDialogoLote = false },
            onConfirmar = { dataHoraInicial, intervaloDias, quantidade ->
                viewModel.agendarEmLote(dataHoraInicial, intervaloDias, quantidade)
                mostrarDialogoLote = false
            }
        )
    }
}
