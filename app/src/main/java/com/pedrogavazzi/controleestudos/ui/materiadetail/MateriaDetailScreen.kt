package com.pedrogavazzi.controleestudos.ui.materiadetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.pedrogavazzi.controleestudos.ControleEstudosApp
import com.pedrogavazzi.controleestudos.ui.components.TextoNomeMateria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaDetailScreen(
    materiaId: Long,
    onVoltar: () -> Unit,
    onAbrirCadernoDaAula: (Long) -> Unit
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

    // Só uma aula fica expandida por vez: abrir outra fecha a anterior automaticamente.
    var aulaExpandidaId by remember { mutableStateOf<Long?>(null) }
    var renomeandoAlgumaAula by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val quantidadeItensAntesDasAulas = 1 + if (aulasSemData > 0) 1 else 0

    LaunchedEffect(aulaExpandidaId) {
        val id = aulaExpandidaId ?: return@LaunchedEffect
        val indiceNaLista = aulas.indexOfFirst { it.id == id }
        if (indiceNaLista != -1) {
            listState.animateScrollToItem(quantidadeItensAntesDasAulas + indiceNaLista)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TextoNomeMateria(
                        nome = materia?.nome ?: "Aulas",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (aulaExpandidaId == null && !renomeandoAlgumaAula) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.adicionarAula() },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Adicionar aula") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
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
            if (aulasSemData > 0) {
                item {
                    Surface(
                        onClick = { mostrarDialogoLote = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.EventRepeat, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            androidx.compose.foundation.layout.Spacer(Modifier.padding(start = 12.dp))
                            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                                Text(
                                    "Agendar várias aulas de uma vez",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "$aulasSemData aula(s) sem data — defina um padrão de repetição",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
            items(aulas, key = { it.id }) { aula ->
                AulaItem(
                    aula = aula,
                    expandido = aula.id == aulaExpandidaId,
                    onToggleExpandir = {
                        aulaExpandidaId = if (aulaExpandidaId == aula.id) null else aula.id
                    },
                    onAgendar = { novaData -> viewModel.agendarAula(aula, novaData) },
                    onReagendar = { novaData -> viewModel.reagendarAula(aula, novaData) },
                    onMarcarConclusao = { concluida -> viewModel.marcarConclusao(aula, concluida) },
                    onSalvarObservacao = { texto -> viewModel.salvarObservacao(aula, texto) },
                    onAbrirCaderno = { onAbrirCadernoDaAula(aula.id) },
                    onRenomear = { novoNome -> viewModel.renomearAula(aula, novoNome) },
                    onExcluir = { viewModel.excluirAula(aula) },
                    onEditandoAlterado = { editando -> renomeandoAlgumaAula = editando }
                )
            }
            item { androidx.compose.foundation.layout.Spacer(Modifier.padding(32.dp)) }
        }
    }

    if (mostrarDialogoLote) {
        AgendamentoEmLoteDialog(
            quantidadeMaximaDisponivel = aulasSemData,
            onDismiss = { mostrarDialogoLote = false },
            onConfirmar = { dataHoraInicial, intervaloDias, quantidade, apenasDiasUteis ->
                viewModel.agendarEmLote(dataHoraInicial, intervaloDias, quantidade, apenasDiasUteis)
                mostrarDialogoLote = false
            }
        )
    }
}
