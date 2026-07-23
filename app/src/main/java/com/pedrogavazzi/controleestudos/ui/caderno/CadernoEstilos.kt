package com.pedrogavazzi.controleestudos.ui.caderno

/** Tipos de tamanho de fonte — mutuamente exclusivos entre si (aplicar um remove os outros
 *  no mesmo trecho), diferente de negrito/itálico/realce que podem coexistir. */
private val TIPOS_DE_TAMANHO = setOf(TipoEstilo.TITULO, TipoEstilo.GRANDE, TipoEstilo.PEQUENO)

/** Verifica se todo o trecho [inicio, fim) já está coberto por algum estilo do [tipo]. */
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
