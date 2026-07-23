package com.pedrogavazzi.controleestudos.ui.caderno

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrogavazzi.controleestudos.data.Aula
import com.pedrogavazzi.controleestudos.ui.components.TextoNomeMateria
import com.pedrogavazzi.controleestudos.ui.components.formatarDataHora
import kotlinx.coroutines.delay

/**
 * Tela dedicada do caderno de uma aula: cada anotação é um "bloco" (parágrafo) com sua
 * própria formatação — negrito, itálico, tamanho e marcador de lista — editável através
 * da barra de ferramentas. Salva automaticamente (com um pequeno atraso) a cada alteração.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadernoEditorScreen(
    aulaId: Long,
    viewModel: CadernoEditorViewModel,
    onVoltar: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val aula = estado.aula

    var blocos by remember { mutableStateOf(listOf<BlocoCaderno>()) }
    var blocosOriginais by remember { mutableStateOf(listOf<BlocoCaderno>()) }
    var inicializado by remember(aulaId) { mutableStateOf(false) }
    var blocoFocadoId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(aula?.id, estado.carregando) {
        if (!inicializado && !estado.carregando && aula != null) {
            val carregados = CadernoSerializer.desserializar(aula.anotacoesCaderno)
            blocos = carregados
            blocosOriginais = carregados
            inicializado = true
        }
    }

    // Salva automaticamente pouco depois de parar de digitar (evita gravar a cada tecla).
    LaunchedEffect(blocos) {
        if (inicializado && aula != null) {
            delay(700)
            viewModel.salvarAnotacoes(aula, CadernoSerializer.serializar(blocos))
        }
    }

    fun atualizarBloco(id: String, transformar: (BlocoCaderno) -> BlocoCaderno) {
        blocos = blocos.map { if (it.id == id) transformar(it) else it }
    }

    fun aplicarNoBlocoFocado(transformar: (BlocoCaderno) -> BlocoCaderno) {
        val id = blocoFocadoId ?: blocos.lastOrNull()?.id ?: return
        atualizarBloco(id, transformar)
    }

    val blocoFocado = blocos.firstOrNull { it.id == blocoFocadoId }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Column {
                            TextoNomeMateria(
                                nome = estado.materia?.nome?.let { "$it — Aula ${aula?.numero ?: ""}" } ?: "Caderno",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (aula?.dataHoraMillis != null) {
                                Text(
                                    formatarDataHora(aula.dataHoraMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onVoltar) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    actions = {
                        val alterado = blocos.map { it.copy(id = "") } != blocosOriginais.map { it.copy(id = "") }
                        if (alterado) {
                            IconButton(onClick = { blocos = blocosOriginais }) {
                                Icon(Icons.Filled.Undo, contentDescription = "Desfazer alterações")
                            }
                        }
                        if (aula != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                                Text("Concluída", style = MaterialTheme.typography.labelSmall)
                                Checkbox(
                                    checked = aula.concluida,
                                    onCheckedChange = { concluida -> viewModel.marcarConclusao(aula, concluida) }
                                )
                            }
                        }
                    }
                )
                BarraDeFormatacao(
                    bloco = blocoFocado,
                    habilitada = blocoFocado != null,
                    onNegritoClick = { aplicarNoBlocoFocado { it.copy(negrito = !it.negrito) } },
                    onItalicoClick = { aplicarNoBlocoFocado { it.copy(italico = !it.italico) } },
                    onTamanhoSelecionado = { tamanho -> aplicarNoBlocoFocado { it.copy(tamanho = tamanho) } },
                    onTopicoClick = {
                        aplicarNoBlocoFocado {
                            it.copy(marcador = if (it.marcador == MarcadorBloco.TOPICO) MarcadorBloco.NENHUM else MarcadorBloco.TOPICO)
                        }
                    },
                    onNumeradoClick = {
                        aplicarNoBlocoFocado {
                            it.copy(marcador = if (it.marcador == MarcadorBloco.NUMERADO) MarcadorBloco.NENHUM else MarcadorBloco.NUMERADO)
                        }
                    }
                )
                Divider()
            }
        }
    ) { padding ->
        if (aula == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(if (estado.carregando) "Carregando…" else "Aula não encontrada")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(blocos, key = { _, bloco -> bloco.id }) { indice, bloco ->
                    LinhaDoCaderno(
                        bloco = bloco,
                        numero = numeroDoItem(blocos, indice),
                        focado = bloco.id == blocoFocadoId,
                        podeExcluir = blocos.size > 1,
                        onTextoAlterado = { novoTexto ->
                            if (novoTexto.contains('\n')) {
                                val partes = novoTexto.split('\n', limit = 2)
                                val indiceAtual = blocos.indexOfFirst { it.id == bloco.id }
                                val novoBloco = BlocoCaderno(
                                    texto = partes.getOrElse(1) { "" },
                                    tamanho = bloco.tamanho,
                                    marcador = bloco.marcador
                                )
                                val listaMutavel = blocos.toMutableList()
                                listaMutavel[indiceAtual] = bloco.copy(texto = partes[0])
                                listaMutavel.add(indiceAtual + 1, novoBloco)
                                blocos = listaMutavel
                                blocoFocadoId = novoBloco.id
                            } else {
                                atualizarBloco(bloco.id) { it.copy(texto = novoTexto) }
                            }
                        },
                        onFoco = { blocoFocadoId = bloco.id },
                        onExcluir = {
                            blocos = blocos.filter { it.id != bloco.id }.ifEmpty { listOf(BlocoCaderno()) }
                        }
                    )
                }
                item { Spacer(Modifier.padding(40.dp)) }
            }
        }
    }
}

/** Calcula o número exibido para um item marcado como NUMERADO, reiniciando a cada
 *  sequência quebrada por um bloco de outro tipo. */
private fun numeroDoItem(blocos: List<BlocoCaderno>, indice: Int): Int? {
    if (blocos[indice].marcador != MarcadorBloco.NUMERADO) return null
    var contagem = 1
    var i = indice - 1
    while (i >= 0 && blocos[i].marcador == MarcadorBloco.NUMERADO) {
        contagem++
        i--
    }
    return contagem
}

@Composable
private fun BarraDeFormatacao(
    bloco: BlocoCaderno?,
    habilitada: Boolean,
    onNegritoClick: () -> Unit,
    onItalicoClick: () -> Unit,
    onTamanhoSelecionado: (TamanhoBloco) -> Unit,
    onTopicoClick: () -> Unit,
    onNumeradoClick: () -> Unit
) {
    var menuTamanhoAberto by remember { mutableStateOf(false) }
    val corAtiva = MaterialTheme.colorScheme.primaryContainer

    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNegritoClick,
                enabled = habilitada,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (bloco?.negrito == true) corAtiva else Color.Transparent
                )
            ) { Icon(Icons.Filled.FormatBold, contentDescription = "Negrito") }

            IconButton(
                onClick = onItalicoClick,
                enabled = habilitada,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (bloco?.italico == true) corAtiva else Color.Transparent
                )
            ) { Icon(Icons.Filled.FormatItalic, contentDescription = "Itálico") }

            Box {
                IconButton(onClick = { menuTamanhoAberto = true }, enabled = habilitada) {
                    Icon(Icons.Filled.FormatSize, contentDescription = "Tamanho do texto")
                }
                DropdownMenu(expanded = menuTamanhoAberto, onDismissRequest = { menuTamanhoAberto = false }) {
                    TamanhoBloco.entries.forEach { opcao ->
                        DropdownMenuItem(
                            text = { Text(opcao.rotulo) },
                            onClick = {
                                onTamanhoSelecionado(opcao)
                                menuTamanhoAberto = false
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = onTopicoClick,
                enabled = habilitada,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (bloco?.marcador == MarcadorBloco.TOPICO) corAtiva else Color.Transparent
                )
            ) { Icon(Icons.Filled.FormatListBulleted, contentDescription = "Lista com tópicos") }

            IconButton(
                onClick = onNumeradoClick,
                enabled = habilitada,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (bloco?.marcador == MarcadorBloco.NUMERADO) corAtiva else Color.Transparent
                )
            ) { Icon(Icons.Filled.FormatListNumbered, contentDescription = "Lista numerada") }

            if (!habilitada) {
                Text(
                    "Toque em uma linha para formatar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LinhaDoCaderno(
    bloco: BlocoCaderno,
    numero: Int?,
    focado: Boolean,
    podeExcluir: Boolean,
    onTextoAlterado: (String) -> Unit,
    onFoco: () -> Unit,
    onExcluir: () -> Unit
) {
    val estilo = TextStyle(
        fontSize = bloco.tamanho.tamanhoSp.sp,
        fontWeight = if (bloco.negrito) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (bloco.italico) FontStyle.Italic else FontStyle.Normal,
        color = MaterialTheme.colorScheme.onSurface
    )

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        val prefixo = when (bloco.marcador) {
            MarcadorBloco.TOPICO -> "•"
            MarcadorBloco.NUMERADO -> "${numero ?: 1}."
            MarcadorBloco.NENHUM -> null
        }
        if (prefixo != null) {
            Text(
                prefixo,
                style = estilo,
                modifier = Modifier.padding(top = 10.dp, end = 6.dp).width(24.dp)
            )
        }
        BasicTextField(
            value = bloco.texto,
            onValueChange = onTextoAlterado,
            textStyle = estilo,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
                .onFocusChanged { estadoFoco -> if (estadoFoco.isFocused) onFoco() },
            decorationBox = { campoInterno ->
                if (bloco.texto.isEmpty()) {
                    Text("Escreva aqui…", style = estilo.copy(color = MaterialTheme.colorScheme.outline))
                }
                campoInterno()
            }
        )
        if (focado && podeExcluir) {
            IconButton(onClick = onExcluir, modifier = Modifier.padding(top = 2.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Remover linha", tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
