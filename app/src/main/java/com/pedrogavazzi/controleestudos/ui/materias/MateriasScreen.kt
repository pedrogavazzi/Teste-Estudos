package com.pedrogavazzi.controleestudos.ui.materias

import kotlinx.coroutines.launch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.data.Materia
import com.pedrogavazzi.controleestudos.ui.components.CampoDeBusca

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MateriasScreen(
    viewModel: MateriasViewModel,
    onAbrirMateria: (Long) -> Unit
) {
    val materias by viewModel.materiasComProgresso.collectAsState()
    var dialogoAberto by remember { mutableStateOf(false) }
    var materiaEmEdicao by remember { mutableStateOf<Materia?>(null) }
    var materiaParaExcluir by remember { mutableStateOf<Materia?>(null) }
    var termoBusca by remember { mutableStateOf("") }
    val materiasFiltradas = remember(materias, termoBusca) {
        if (termoBusca.isBlank()) materias
        else materias.filter { it.materia.nome.contains(termoBusca, ignoreCase = true) }
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val escopo = rememberCoroutineScope()
    val mostrarBotaoTopo by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("Matérias", style = MaterialTheme.typography.titleLarge) }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = mostrarBotaoTopo,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut()
                ) {
                    androidx.compose.material3.SmallFloatingActionButton(
                        onClick = { escopo.launch { listState.animateScrollToItem(0) } },
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Voltar ao topo")
                    }
                }
                FloatingActionButton(onClick = {
                    materiaEmEdicao = null
                    dialogoAberto = true
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Adicionar matéria")
                }
            }
        }
    ) { padding ->
        com.pedrogavazzi.controleestudos.ui.components.ConteudoComLarguraMaxima(Modifier.padding(padding)) {
        Column(Modifier.fillMaxSize()) {
            if (materias.size > 1) {
                CampoDeBusca(
                    valor = termoBusca,
                    onValorAlterado = { termoBusca = it },
                    placeholder = "Buscar matéria",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (materias.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(4.dp))
                        Text("Nenhuma matéria cadastrada ainda.\nToque em + para começar.", modifier = Modifier.padding(16.dp))
                    }
                }
            } else if (materiasFiltradas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma matéria encontrada para \"$termoBusca\".", modifier = Modifier.padding(16.dp))
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(materiasFiltradas, key = { it.materia.id }) { item ->
                        MateriaCard(
                            item = item,
                            onClick = { onAbrirMateria(item.materia.id) },
                            onEditar = {
                                materiaEmEdicao = item.materia
                                dialogoAberto = true
                            },
                            onExcluir = { materiaParaExcluir = item.materia }
                        )
                    }
                    item { Spacer(Modifier.padding(40.dp)) }
                }
            }
        }
        }
    }

    if (dialogoAberto) {
        MateriaDialog(
            materiaParaEditar = materiaEmEdicao,
            onDismiss = { dialogoAberto = false },
            onConfirmar = { nome, totalAulas, cor ->
                val existente = materiaEmEdicao
                if (existente == null) {
                    viewModel.criarMateria(nome, totalAulas, cor)
                } else {
                    viewModel.atualizarMateria(existente, nome, totalAulas, cor)
                }
                dialogoAberto = false
            }
        )
    }

    materiaParaExcluir?.let { materia ->
        val progresso = materias.firstOrNull { it.materia.id == materia.id }
        val textoAviso = buildString {
            append("Isso vai apagar ${progresso?.totalAulas ?: 0} aula(s)")
            if ((progresso?.cadernosComConteudo ?: 0) > 0) {
                append(" e ${progresso?.cadernosComConteudo} caderno(s) com anotações")
            }
            append(". Essa ação não pode ser desfeita.")
        }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { materiaParaExcluir = null },
            title = { Text("Excluir \"${materia.nome}\"?") },
            text = { Text(textoAviso) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.excluirMateria(materia)
                    materiaParaExcluir = null
                }) { Text("Excluir") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { materiaParaExcluir = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun MateriaCard(
    item: MateriaComProgresso,
    onClick: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    val cor = runCatching { Color(android.graphics.Color.parseColor(item.materia.corHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = com.pedrogavazzi.controleestudos.ui.theme.FormaCard,
        colors = com.pedrogavazzi.controleestudos.ui.theme.corDeCardTonal()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                com.pedrogavazzi.controleestudos.ui.components.IniciaisDaMateria(
                    nomeMateria = item.materia.nome,
                    cor = cor,
                    tamanho = 28.dp
                )
                Spacer(Modifier.padding(start = 8.dp))
                // Nome com a largura inteira do card, quebrando em mais linhas se precisar,
                // em vez de dividir espaço com os botões de editar/excluir na mesma linha
                // (o que cortava nomes mais longos sem necessidade).
                Text(
                    item.materia.nome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f).padding(top = 4.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onExcluir) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                }
            }
            LinearProgressIndicator(
                progress = { item.percentual },
                modifier = Modifier.fillMaxWidth(),
                color = cor
            )
            Spacer(Modifier.padding(top = 4.dp))
            Text(
                "${item.aulasConcluidas} de ${item.totalAulas} aulas concluídas (${(item.percentual * 100).toInt()}%)",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
