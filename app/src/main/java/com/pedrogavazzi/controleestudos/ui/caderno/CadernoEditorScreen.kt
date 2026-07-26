package com.pedrogavazzi.controleestudos.ui.caderno

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
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
import com.pedrogavazzi.controleestudos.data.FotoAula
import com.pedrogavazzi.controleestudos.data.nomeExibido
import com.pedrogavazzi.controleestudos.ui.components.CaixaConclusao
import com.pedrogavazzi.controleestudos.ui.components.TextoNomeMateria
import com.pedrogavazzi.controleestudos.ui.components.abrirLinkDaAula
import com.pedrogavazzi.controleestudos.ui.components.formatarDataHora
import kotlinx.coroutines.delay

// Realce mais opaco no tema claro (fundo claro, texto escuro) e mais sutil no escuro — um
// mesmo amarelo "cheio" sobre fundo escuro esmaeceria o texto claro por cima, reduzindo o
// contraste em vez de só destacar o trecho.
private val CorRealceClaro = Color(0xFFFFEB3B).copy(alpha = 0.45f)
private val CorRealceEscuro = Color(0xFFFFEB3B).copy(alpha = 0.28f)

/** Estado do indicador de salvamento do caderno — pra deixar claro que o texto está sendo
 *  guardado, já que antes não havia nenhum sinal disso (o usuário só tinha que confiar). */
private enum class EstadoSalvamento { OCIOSO, PENDENTE, SALVO }

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
    val context = androidx.compose.ui.platform.LocalContext.current
    val fotos by viewModel.fotos.collectAsState()
    val seletorDeFotos = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia(20)
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.adicionarFotos(aulaId, uris)
    }

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
    var estadoSalvamento by remember { mutableStateOf(EstadoSalvamento.OCIOSO) }

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
        estadoSalvamento = EstadoSalvamento.SALVO
    }

    // Salva automaticamente pouco depois de parar de digitar (evita gravar a cada tecla).
    // "Pendente" aparece assim que algo muda, e vira "Salvo" quando o salvamento é disparado —
    // antes não havia nenhuma indicação disso, o que deixava o usuário sem saber se o que
    // escreveu tinha sido guardado ou não.
    LaunchedEffect(campo.text, estilos) {
        if (inicializado && aula != null && !modoLeitura) {
            estadoSalvamento = EstadoSalvamento.PENDENTE
            delay(700)
            salvarAgora()
        }
    }

    val alterado = campo.text != textoOriginal || estilos != estilosOriginais
    val aparenciaCaderno = aparenciaCadernoDoTema()
    val contagemPalavras = remember(campo.text) {
        campo.text.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    }

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
                        TextoNomeMateria(
                            nome = estado.materia?.nome?.let { "$it — ${aula?.nomeExibido() ?: ""}" } ?: "Caderno",
                            style = MaterialTheme.typography.titleMedium
                        )
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
                    }
                )
                // Linha de status separada da barra de título — data, indicador de
                // salvamento e o marcador de conclusão, cada um com espaço próprio em vez de
                // disputar lugar dentro do título (o que deixava tudo espremido e cortado).
                if (aula != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            if (aula.dataHoraMillis != null) {
                                Text(
                                    formatarDataHora(aula.dataHoraMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!modoLeitura && estadoSalvamento != EstadoSalvamento.OCIOSO) {
                                Text(
                                    if (estadoSalvamento == EstadoSalvamento.PENDENTE) "Salvando…" else "Salvo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (estadoSalvamento == EstadoSalvamento.PENDENTE) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        }
                        Text(
                            "Concluída",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        CaixaConclusao(
                            concluida = aula.concluida,
                            onAlterar = { concluida -> viewModel.marcarConclusao(aula, concluida) }
                        )
                    }
                }
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
                        contagemPalavras = contagemPalavras,
                        onNegritoClick = { aplicarEstilo(TipoEstilo.NEGRITO) },
                        onItalicoClick = { aplicarEstilo(TipoEstilo.ITALICO) },
                        onRealceClick = { aplicarEstilo(TipoEstilo.REALCE) },
                        onLimparFormatacao = {
                            if (!selecaoAtual.collapsed) {
                                estilos = limparFormatacao(estilos, selecaoAtual.min, selecaoAtual.max)
                            } else {
                                estilosPendentes = emptySet()
                                tamanhoPendente = null
                            }
                        },
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
                Column(
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
                    // Link da aula fica no topo do próprio documento (como metadado da nota),
                    // acessível e editável direto do caderno — antes só dava pra ver/editar
                    // pela tela da matéria.
                    if (aula.link.isNotBlank() || !modoLeitura) {
                        LinkDoCaderno(
                            link = aula.link,
                            somenteLeitura = modoLeitura,
                            onAbrir = { abrirLinkDaAula(context, aula.link) },
                            onSalvar = { novoLink -> viewModel.salvarLink(aula, novoLink) }
                        )
                        Spacer(Modifier.padding(top = 12.dp))
                    }
                    if (fotos.isNotEmpty() || !modoLeitura) {
                        SecaoFotos(
                            fotos = fotos,
                            somenteLeitura = modoLeitura,
                            arquivoDaFoto = { foto -> viewModel.arquivoDaFoto(foto) },
                            onAdicionar = {
                                seletorDeFotos.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onRemover = { foto -> viewModel.excluirFoto(foto) }
                        )
                        Spacer(Modifier.padding(top = 12.dp))
                    }
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
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        visualTransformation = { texto ->
                            TransformedText(
                                construirAnnotatedString(texto.text, estilos, aparenciaCaderno),
                                OffsetMapping.Identity
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { campoInterno ->
                            if (campo.text.isEmpty()) {
                                Text(
                                    if (modoLeitura) "Nenhuma anotação ainda." else "Escreva suas anotações aqui…",
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
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

/** Tamanhos de fonte do caderno, derivados da escala tipográfica do tema (em vez de valores
 *  soltos) — assim ficam consistentes com o resto do app e continuam claramente diferentes
 *  entre si (a diferença entre eles foi alargada numa rodada anterior, que os deixou parecidos
 *  demais). */
private data class AparenciaCaderno(
    val tamanhoPequeno: androidx.compose.ui.unit.TextUnit,
    val tamanhoGrande: androidx.compose.ui.unit.TextUnit,
    val tamanhoTitulo: androidx.compose.ui.unit.TextUnit,
    val corRealce: Color
)

@Composable
private fun aparenciaCadernoDoTema(): AparenciaCaderno {
    val fundoEscuro = MaterialTheme.colorScheme.surfaceContainerLowest.luminance() < 0.5f
    return AparenciaCaderno(
        tamanhoPequeno = MaterialTheme.typography.bodySmall.fontSize,
        tamanhoGrande = MaterialTheme.typography.titleLarge.fontSize,
        tamanhoTitulo = MaterialTheme.typography.headlineMedium.fontSize,
        corRealce = if (fundoEscuro) CorRealceEscuro else CorRealceClaro
    )
}

private fun construirAnnotatedString(
    texto: String,
    estilos: List<EstiloAplicado>,
    aparencia: AparenciaCaderno
): AnnotatedString {
    return buildAnnotatedString {
        append(texto)
        estilos.forEach { estilo ->
            val inicio = estilo.inicio.coerceIn(0, texto.length)
            val fim = estilo.fim.coerceIn(inicio, texto.length)
            if (inicio >= fim) return@forEach
            val spanStyle = when (estilo.tipo) {
                TipoEstilo.NEGRITO -> SpanStyle(fontWeight = FontWeight.Bold)
                TipoEstilo.ITALICO -> SpanStyle(fontStyle = FontStyle.Italic)
                TipoEstilo.REALCE -> SpanStyle(background = aparencia.corRealce)
                TipoEstilo.TITULO -> SpanStyle(fontSize = aparencia.tamanhoTitulo, fontWeight = FontWeight.Bold)
                TipoEstilo.GRANDE -> SpanStyle(fontSize = aparencia.tamanhoGrande)
                TipoEstilo.PEQUENO -> SpanStyle(fontSize = aparencia.tamanhoPequeno)
            }
            addStyle(spanStyle, inicio, fim)
        }
    }
}

/**
 * Link da aula, exibido/editável no topo do próprio documento do caderno. Em modo leitura,
 * só aparece (e é clicável) quando já existe um link; em edição, reaproveita o
 * [AnotacaoEditor] já usado para a observação — mesmo comportamento de salvar ao perder o
 * foco, sem precisar escrever essa lógica de novo.
 */
@Composable
private fun LinkDoCaderno(
    link: String,
    somenteLeitura: Boolean,
    onAbrir: () -> Unit,
    onSalvar: (String) -> Unit
) {
    if (somenteLeitura) {
        if (link.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrir),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    link,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
        return
    }

    Column {
        com.pedrogavazzi.controleestudos.ui.components.AnotacaoEditor(
            chaveDeIdentidade = "link",
            valorSalvo = link,
            onSalvar = onSalvar,
            rotulo = "Link da aula (opcional)",
            minLinhas = 1
        )
        if (link.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).clickable(onClick = onAbrir),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Abrir link",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

/**
 * Fotos/prints anexados à aula — miniaturas numa fileira rolável, sem se misturar com o
 * texto (o campo de edição não suporta imagem fluindo junto do texto). Tocar numa miniatura
 * abre ela maior; segurar não faz nada de propósito — a exclusão usa um "x" visível em vez
 * de gesto escondido, mais fácil de descobrir.
 */
@Composable
private fun SecaoFotos(
    fotos: List<FotoAula>,
    somenteLeitura: Boolean,
    arquivoDaFoto: (FotoAula) -> java.io.File,
    onAdicionar: () -> Unit,
    onRemover: (FotoAula) -> Unit
) {
    var fotoEmVisualizacao by remember { mutableStateOf<FotoAula?>(null) }

    Column {
        Text(
            "Fotos",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 6.dp)
        ) {
            items(fotos, key = { it.id }) { foto ->
                Box(modifier = Modifier.size(88.dp)) {
                    coil.compose.AsyncImage(
                        model = arquivoDaFoto(foto),
                        contentDescription = "Foto anexada à aula",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .clickable { fotoEmVisualizacao = foto }
                    )
                    if (!somenteLeitura) {
                        androidx.compose.material3.Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .size(22.dp)
                                .clickable { onRemover(foto) }
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Remover esta foto",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(3.dp)
                            )
                        }
                    }
                }
            }
            if (!somenteLeitura) {
                item {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onAdicionar),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AddAPhoto,
                            contentDescription = "Adicionar fotos",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    val fotoAtual = fotoEmVisualizacao
    if (fotoAtual != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { fotoEmVisualizacao = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { fotoEmVisualizacao = null },
                contentAlignment = Alignment.Center
            ) {
                coil.compose.AsyncImage(
                    model = arquivoDaFoto(fotoAtual),
                    contentDescription = "Foto anexada à aula, ampliada",
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
                IconButton(
                    onClick = { fotoEmVisualizacao = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun BarraDeFormatacao(
    temSelecao: Boolean,
    negritoAtivo: Boolean,
    italicoAtivo: Boolean,
    realceAtivo: Boolean,
    contagemPalavras: Int,
    onNegritoClick: () -> Unit,
    onItalicoClick: () -> Unit,
    onRealceClick: () -> Unit,
    onLimparFormatacao: () -> Unit,
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
                IconButton(onClick = onLimparFormatacao) {
                    Icon(Icons.Filled.FormatClear, contentDescription = "Limpar formatação")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (temSelecao) "Formata o trecho selecionado"
                    else "Sem seleção: toque num botão pra formatar o que for digitado a seguir",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (contagemPalavras == 1) "1 palavra" else "$contagemPalavras palavras",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
