package com.pedrogavazzi.controleestudos.ui.caderno

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrogavazzi.controleestudos.data.nomeExibido
import com.pedrogavazzi.controleestudos.ui.components.CaixaConclusao
import com.pedrogavazzi.controleestudos.ui.components.TextoNomeMateria
import com.pedrogavazzi.controleestudos.ui.components.formatarDataHora
import kotlinx.coroutines.delay

private val CorRealce = Color(0xFFFFEB3B).copy(alpha = 0.45f)

/**
 * Tela dedicada do caderno de uma aula: um único texto contínuo (como um documento), com
 * formatação por seleção de texto (negrito, itálico, tamanho, realce) — em vez de linhas
 * separadas. Pode abrir em modo leitura (só visualização) ou edição.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadernoEditorScreen(
    aulaId: Long,
    somenteLeituraInicial: Boolean,
    viewModel: CadernoEditorViewModel,
    onVoltar: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val aula = estado.aula

    var campo by remember { mutableStateOf(TextFieldValue("")) }
    var estilos by remember { mutableStateOf(listOf<EstiloAplicado>()) }
    var textoOriginal by remember { mutableStateOf("") }
    var estilosOriginais by remember { mutableStateOf(listOf<EstiloAplicado>()) }
    var inicializado by remember(aulaId) { mutableStateOf(false) }
    var modoLeitura by remember(aulaId) { mutableStateOf(somenteLeituraInicial) }
    // Estilos "pendentes": ativados sem nenhum texto selecionado, pro modo apertar-a-formatação
    // -e-depois-digitar — o próximo texto digitado já sai formatado.
    var estilosPendentes by remember { mutableStateOf(setOf<TipoEstilo>()) }
    var tamanhoPendente by remember { mutableStateOf<TipoEstilo?>(null) }

    LaunchedEffect(aula?.id, estado.carregando) {
        if (!inicializado && !estado.carregando && aula != null) {
            val nota = CadernoSerializer.desserializar(aula.anotacoesCaderno)
            campo = TextFieldValue(nota.texto)
            estilos = nota.estilos
            textoOriginal = nota.texto
            estilosOriginais = nota.estilos
            inicializado = true
        }
    }

    fun salvarAgora() {
        aula?.let { viewModel.salvarAnotacoes(it, CadernoSerializer.serializar(NotaCaderno(campo.text, estilos))) }
    }

    // Salva automaticamente pouco depois de parar de digitar (evita gravar a cada tecla).
    LaunchedEffect(campo.text, estilos) {
        if (inicializado && aula != null && !modoLeitura) {
            delay(700)
            salvarAgora()
        }
    }

    val alterado = campo.text != textoOriginal || estilos != estilosOriginais

    fun aplicarEstilo(tipo: TipoEstilo) {
        val selecao = campo.selection
        if (!selecao.collapsed) {
            estilos = alternarEstilo(estilos, tipo, selecao.min, selecao.max)
        } else {
            // Sem seleção: liga/desliga esse estilo pro que for digitado a seguir.
            estilosPendentes = if (tipo in estilosPendentes) estilosPendentes - tipo else estilosPendentes + tipo
        }
    }

    fun aplicarTamanhoSelecao(tipo: TipoEstilo?) {
        val selecao = campo.selection
        if (!selecao.collapsed) {
            estilos = aplicarTamanho(estilos, tipo, selecao.min, selecao.max)
        } else {
            tamanhoPendente = tipo
        }
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Column {
                            TextoNomeMateria(
                                nome = estado.materia?.nome?.let { "$it — ${aula?.nomeExibido() ?: ""}" } ?: "Caderno",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2
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
                        IconButton(onClick = { salvarAgora(); onVoltar() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    actions = {
                        if (modoLeitura) {
                            IconButton(onClick = { modoLeitura = false }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                            }
                        } else {
                            IconButton(onClick = { modoLeitura = true }) {
                                Icon(Icons.Filled.Visibility, contentDescription = "Modo leitura")
                            }
                            if (alterado) {
                                IconButton(onClick = {
                                    campo = TextFieldValue(textoOriginal)
                                    estilos = estilosOriginais
                                }) {
                                    Icon(Icons.Filled.Undo, contentDescription = "Desfazer alterações")
                                }
                            }
                        }
                        if (aula != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                                Text("Concluída", style = MaterialTheme.typography.labelSmall)
                                CaixaConclusao(
                                    concluida = aula.concluida,
                                    onAlterar = { concluida -> viewModel.marcarConclusao(aula, concluida) }
                                )
                            }
                        }
                    }
                )
                Divider()
            }
        },
        bottomBar = {
            if (!modoLeitura) {
                Column {
                    val selecaoAtual = campo.selection
                    BarraDeFormatacao(
                        temSelecao = !selecaoAtual.collapsed,
                        negritoAtivo = if (!selecaoAtual.collapsed) trechoTemEstilo(estilos, TipoEstilo.NEGRITO, selecaoAtual.min, selecaoAtual.max) else TipoEstilo.NEGRITO in estilosPendentes,
                        italicoAtivo = if (!selecaoAtual.collapsed) trechoTemEstilo(estilos, TipoEstilo.ITALICO, selecaoAtual.min, selecaoAtual.max) else TipoEstilo.ITALICO in estilosPendentes,
                        realceAtivo = if (!selecaoAtual.collapsed) trechoTemEstilo(estilos, TipoEstilo.REALCE, selecaoAtual.min, selecaoAtual.max) else TipoEstilo.REALCE in estilosPendentes,
                        onNegritoClick = { aplicarEstilo(TipoEstilo.NEGRITO) },
                        onItalicoClick = { aplicarEstilo(TipoEstilo.ITALICO) },
                        onRealceClick = { aplicarEstilo(TipoEstilo.REALCE) },
                        onTamanhoSelecionado = { tamanho -> aplicarTamanhoSelecao(tamanho) }
                    )
                    Surface(shadowElevation = 8.dp) {
                        Button(
                            onClick = { salvarAgora(); onVoltar() },
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Salvar e sair")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (aula == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(if (estado.carregando) "Carregando…" else "Aula não encontrada")
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                    // Removido de propósito: um detector de toque próprio nessa área, usado
                    // antes pra fechar a seleção ao tocar fora do texto, atrapalhava o toque
                    // duplo/toque longo nativo do Android pra selecionar palavra — a seleção
                    // e o teclado continuam fechando normalmente ao sair da tela ou apertar
                    // voltar, sem precisar de um gesto customizado concorrendo com o do campo.
                ) {
                    BasicTextField(
                        value = campo,
                        onValueChange = { novoValor ->
                            if (!modoLeitura) {
                                if (novoValor.text != campo.text) {
                                    estilos = ajustarEAplicarPendentes(
                                        estilos, campo.text, novoValor.text, estilosPendentes, tamanhoPendente
                                    )
                                } else if (novoValor.selection != campo.selection) {
                                    // Só moveu o cursor/seleção (sem digitar nada): sai do modo
                                    // "apertar e digitar", já que ele era só pro próximo texto.
                                    estilosPendentes = emptySet()
                                    tamanhoPendente = null
                                }
                                campo = novoValor
                            }
                        },
                        readOnly = modoLeitura,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        visualTransformation = { texto -> TransformedText(construirAnnotatedString(texto.text, estilos), OffsetMapping.Identity) },
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { campoInterno ->
                            if (campo.text.isEmpty()) {
                                Text(
                                    if (modoLeitura) "Nenhuma anotação ainda." else "Escreva suas anotações aqui…",
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 16.sp
                                )
                            }
                            campoInterno()
                        }
                    )
                }
            }
        }
    }
}

private fun construirAnnotatedString(texto: String, estilos: List<EstiloAplicado>): AnnotatedString {
    return buildAnnotatedString {
        append(texto)
        estilos.forEach { estilo ->
            val inicio = estilo.inicio.coerceIn(0, texto.length)
            val fim = estilo.fim.coerceIn(inicio, texto.length)
            if (inicio >= fim) return@forEach
            val spanStyle = when (estilo.tipo) {
                TipoEstilo.NEGRITO -> SpanStyle(fontWeight = FontWeight.Bold)
                TipoEstilo.ITALICO -> SpanStyle(fontStyle = FontStyle.Italic)
                TipoEstilo.REALCE -> SpanStyle(background = CorRealce)
                TipoEstilo.TITULO -> SpanStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)
                TipoEstilo.GRANDE -> SpanStyle(fontSize = 22.sp)
                TipoEstilo.PEQUENO -> SpanStyle(fontSize = 12.sp)
            }
            addStyle(spanStyle, inicio, fim)
        }
    }
}

@Composable
private fun BarraDeFormatacao(
    temSelecao: Boolean,
    negritoAtivo: Boolean,
    italicoAtivo: Boolean,
    realceAtivo: Boolean,
    onNegritoClick: () -> Unit,
    onItalicoClick: () -> Unit,
    onRealceClick: () -> Unit,
    onTamanhoSelecionado: (TipoEstilo?) -> Unit
) {
    var menuTamanhoAberto by remember { mutableStateOf(false) }
    val corAtiva = MaterialTheme.colorScheme.primaryContainer

    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNegritoClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (negritoAtivo) corAtiva else Color.Transparent
                    )
                ) {
                    Icon(Icons.Filled.FormatBold, contentDescription = "Negrito")
                }
                IconButton(
                    onClick = onItalicoClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (italicoAtivo) corAtiva else Color.Transparent
                    )
                ) {
                    Icon(Icons.Filled.FormatItalic, contentDescription = "Itálico")
                }
                IconButton(
                    onClick = onRealceClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (realceAtivo) corAtiva else Color.Transparent
                    )
                ) {
                    Icon(
                        Icons.Filled.FormatColorFill,
                        contentDescription = "Realçar",
                        tint = Color(0xFFC9A227)
                    )
                }
                Box {
                    IconButton(onClick = { menuTamanhoAberto = true }) {
                        Icon(Icons.Filled.FormatSize, contentDescription = "Tamanho do texto")
                    }
                    DropdownMenu(expanded = menuTamanhoAberto, onDismissRequest = { menuTamanhoAberto = false }) {
                        DropdownMenuItem(text = { Text("Normal") }, onClick = { onTamanhoSelecionado(null); menuTamanhoAberto = false })
                        listOf(TipoEstilo.PEQUENO, TipoEstilo.GRANDE, TipoEstilo.TITULO).forEach { opcao ->
                            DropdownMenuItem(
                                text = { Text(opcao.rotulo) },
                                onClick = { onTamanhoSelecionado(opcao); menuTamanhoAberto = false }
                            )
                        }
                    }
                }
            }
            Text(
                if (temSelecao) "Formata o trecho selecionado"
                else "Sem seleção: toque num botão pra formatar o que for digitado a seguir",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
            )
        }
    }
}
