package com.pedrogavazzi.controleestudos.ui.caderno

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
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
 * Barra de seleção de texto do próprio Android (cortar/copiar/colar) desativada de propósito:
 * ela ficava sobrepondo a barra de formatação do app e atrapalhava desmarcar o realce. A
 * seleção (destaque azul, alças de arrastar) continua funcionando normalmente — só a barra
 * flutuante de atalhos do sistema não aparece mais; a formatação é feita pela barra do app.
 */
private object BarraDeSelecaoDesativada : TextToolbar {
    override val status: TextToolbarStatus = TextToolbarStatus.Hidden
    override fun hide() {}
    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        // Intencionalmente vazio.
    }
}

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
        if (selecao.collapsed) return
        estilos = alternarEstilo(estilos, tipo, selecao.min, selecao.max)
    }

    fun aplicarTamanhoSelecao(tipo: TipoEstilo?) {
        val selecao = campo.selection
        if (selecao.collapsed) return
        estilos = aplicarTamanho(estilos, tipo, selecao.min, selecao.max)
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
                if (!modoLeitura) {
                    BarraDeFormatacao(
                        temSelecao = !campo.selection.collapsed,
                        onNegritoClick = { aplicarEstilo(TipoEstilo.NEGRITO) },
                        onItalicoClick = { aplicarEstilo(TipoEstilo.ITALICO) },
                        onRealceClick = { aplicarEstilo(TipoEstilo.REALCE) },
                        onTamanhoSelecionado = { tamanho -> aplicarTamanhoSelecao(tamanho) }
                    )
                }
                Divider()
            }
        },
        bottomBar = {
            if (!modoLeitura) {
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
    ) { padding ->
        if (aula == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(if (estado.carregando) "Carregando…" else "Aula não encontrada")
            }
        } else {
            val focusManager = LocalFocusManager.current
            CompositionLocalProvider(LocalTextToolbar provides BarraDeSelecaoDesativada) {
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
                            // Tocar em qualquer área vazia (fora do texto) fecha a seleção/cursor,
                            // já que o toque dentro do próprio campo é consumido por ele primeiro.
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { focusManager.clearFocus() })
                            }
                    ) {
                    BasicTextField(
                        value = campo,
                        onValueChange = { novoValor -> if (!modoLeitura) campo = novoValor },
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
                TipoEstilo.TITULO -> SpanStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold)
                TipoEstilo.GRANDE -> SpanStyle(fontSize = 20.sp)
                TipoEstilo.PEQUENO -> SpanStyle(fontSize = 13.sp)
            }
            addStyle(spanStyle, inicio, fim)
        }
    }
}

@Composable
private fun BarraDeFormatacao(
    temSelecao: Boolean,
    onNegritoClick: () -> Unit,
    onItalicoClick: () -> Unit,
    onRealceClick: () -> Unit,
    onTamanhoSelecionado: (TipoEstilo?) -> Unit
) {
    var menuTamanhoAberto by remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNegritoClick, enabled = temSelecao) {
                    Icon(Icons.Filled.FormatBold, contentDescription = "Negrito")
                }
                IconButton(onClick = onItalicoClick, enabled = temSelecao) {
                    Icon(Icons.Filled.FormatItalic, contentDescription = "Itálico")
                }
                IconButton(onClick = onRealceClick, enabled = temSelecao) {
                    Icon(
                        Icons.Filled.FormatColorFill,
                        contentDescription = "Realçar",
                        tint = if (temSelecao) Color(0xFFC9A227) else MaterialTheme.colorScheme.outline
                    )
                }
                Box {
                    IconButton(onClick = { menuTamanhoAberto = true }, enabled = temSelecao) {
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
            if (!temSelecao) {
                Text(
                    "Selecione um trecho do texto para aplicar formatação",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                )
            }
        }
    }
}
