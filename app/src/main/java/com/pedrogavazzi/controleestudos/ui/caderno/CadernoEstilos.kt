package com.pedrogavazzi.controleestudos.ui.caderno

/** Tipos de tamanho de fonte — mutuamente exclusivos entre si (aplicar um remove os outros
 *  no mesmo trecho), diferente de negrito/itálico/realce que podem coexistir. */
private val TIPOS_DE_TAMANHO = setOf(TipoEstilo.TITULO, TipoEstilo.GRANDE, TipoEstilo.PEQUENO)

/** Verifica se todo o trecho [inicio, fim) já está coberto por algum estilo do [tipo] —
 *  usado tanto para decidir se um toque no botão de formatação aplica ou remove, quanto
 *  para destacar o botão quando a seleção atual já tem aquele estilo. */
fun trechoTemEstilo(estilos: List<EstiloAplicado>, tipo: TipoEstilo, inicio: Int, fim: Int): Boolean =
    estaTotalmenteCoberto(estilos, tipo, inicio, fim)

private fun estaTotalmenteCoberto(estilos: List<EstiloAplicado>, tipo: TipoEstilo, inicio: Int, fim: Int): Boolean {
    if (inicio >= fim) return false
    var cursor = inicio
    for (span in estilos.filter { it.tipo == tipo }.sortedBy { it.inicio }) {
        if (span.fim <= cursor) continue
        if (span.inicio > cursor) return false
        cursor = maxOf(cursor, span.fim)
        if (cursor >= fim) return true
    }
    return cursor >= fim
}

/**
 * Alterna um estilo binário (negrito, itálico ou realce) sobre [inicio, fim): se o trecho já
 * está totalmente coberto, remove; senão, aplica em todo o trecho — mesclando com trechos
 * adjacentes/sobrepostos do mesmo tipo para não acumular estilos fragmentados sem necessidade.
 */
fun alternarEstilo(estilos: List<EstiloAplicado>, tipo: TipoEstilo, inicio: Int, fim: Int): List<EstiloAplicado> {
    if (inicio >= fim) return estilos
    val remover = estaTotalmenteCoberto(estilos, tipo, inicio, fim)
    val outros = estilos.filter { it.tipo != tipo }

    val intervalos = mutableListOf<Pair<Int, Int>>()
    for (span in estilos.filter { it.tipo == tipo }) {
        if (span.fim <= inicio || span.inicio >= fim) {
            intervalos.add(span.inicio to span.fim)
            continue
        }
        if (span.inicio < inicio) intervalos.add(span.inicio to inicio)
        if (span.fim > fim) intervalos.add(fim to span.fim)
    }
    if (!remover) intervalos.add(inicio to fim)

    val mesclados = mutableListOf<Pair<Int, Int>>()
    intervalos.filter { it.first < it.second }.sortedBy { it.first }.forEach { atual ->
        val ultimo = mesclados.lastOrNull()
        if (ultimo != null && atual.first <= ultimo.second) {
            mesclados[mesclados.size - 1] = ultimo.first to maxOf(ultimo.second, atual.second)
        } else {
            mesclados.add(atual)
        }
    }

    return outros + mesclados.map { EstiloAplicado(it.first, it.second, tipo) }
}

/**
 * Define o tamanho de fonte sobre [inicio, fim): [tipo] nulo volta ao tamanho normal (remove
 * qualquer marcação de tamanho no trecho); um tipo de tamanho substitui qualquer outro
 * tamanho já aplicado ali (são mutuamente exclusivos).
 */
fun aplicarTamanho(estilos: List<EstiloAplicado>, tipo: TipoEstilo?, inicio: Int, fim: Int): List<EstiloAplicado> {
    if (inicio >= fim) return estilos
    val outros = estilos.filter { it.tipo !in TIPOS_DE_TAMANHO }
    val restantes = mutableListOf<EstiloAplicado>()
    for (span in estilos.filter { it.tipo in TIPOS_DE_TAMANHO }) {
        if (span.fim <= inicio || span.inicio >= fim) {
            restantes.add(span)
            continue
        }
        if (span.inicio < inicio) restantes.add(span.copy(fim = inicio))
        if (span.fim > fim) restantes.add(span.copy(inicio = fim))
    }
    if (tipo != null) restantes.add(EstiloAplicado(inicio, fim, tipo))
    return outros + restantes
}

/**
 * CORREÇÃO DO BUG CRÍTICO: quando o texto do caderno é editado (digitar ou apagar caracteres),
 * as posições dos estilos já aplicados precisam se mover junto — senão a formatação "escorrega"
 * para um trecho diferente do texto. Compara [textoAntigo] com [textoNovo] pelo prefixo/sufixo
 * comum (a forma mais simples de achar "o que mudou" numa edição de texto) e desloca cada
 * estilo de acordo: posições antes da edição não mudam, posições depois somam/subtraem a
 * diferença de tamanho, e um estilo que caía inteiramente dentro do trecho apagado é descartado.
 */
fun ajustarEstilosParaEdicao(
    estilos: List<EstiloAplicado>,
    textoAntigo: String,
    textoNovo: String
): List<EstiloAplicado> {
    if (textoAntigo == textoNovo || estilos.isEmpty()) return estilos

    val tamanhoMinimo = minOf(textoAntigo.length, textoNovo.length)
    var prefixoComum = 0
    while (prefixoComum < tamanhoMinimo && textoAntigo[prefixoComum] == textoNovo[prefixoComum]) {
        prefixoComum++
    }
    var sufixoComum = 0
    val limiteSufixo = tamanhoMinimo - prefixoComum
    while (sufixoComum < limiteSufixo &&
        textoAntigo[textoAntigo.length - 1 - sufixoComum] == textoNovo[textoNovo.length - 1 - sufixoComum]
    ) {
        sufixoComum++
    }

    val removidoInicio = prefixoComum
    val removidoFim = textoAntigo.length - sufixoComum
    val inseridoFim = textoNovo.length - sufixoComum
    val tamanhoInserido = inseridoFim - removidoInicio

    fun remapPosicao(posicao: Int): Int = when {
        posicao < removidoInicio -> posicao
        posicao >= removidoFim -> posicao - (removidoFim - removidoInicio) + tamanhoInserido
        else -> removidoInicio // a posição caía dentro do trecho apagado/substituído
    }

    return estilos.mapNotNull { estilo ->
        val novoInicio = remapPosicao(estilo.inicio)
        val novoFim = remapPosicao(estilo.fim)
        if (novoFim <= novoInicio) null else estilo.copy(inicio = novoInicio, fim = novoFim)
    }
}
