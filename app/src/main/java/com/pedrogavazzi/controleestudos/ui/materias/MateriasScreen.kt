package com.pedrogavazzi.controleestudos.ui.materias

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("Matérias", style = MaterialTheme.typography.titleLarge) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                materiaEmEdicao = null
                dialogoAberto = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar matéria")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
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
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { materiaParaExcluir = null },
            title = { Text("Excluir matéria") },
            text = { Text("Tem certeza que deseja excluir \"${materia.nome}\" e todas as suas aulas? Essa ação não pode ser desfeita.") },
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(cor, CircleShape)
                )
                Spacer(Modifier.padding(start = 8.dp))
                com.pedrogavazzi.controleestudos.ui.components.TextoNomeMateria(
                    nome = item.materia.nome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditar) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onExcluir) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                }
            }
            Spacer(Modifier.padding(top = 8.dp))
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
