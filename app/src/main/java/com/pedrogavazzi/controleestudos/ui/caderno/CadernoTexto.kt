package com.pedrogavazzi.controleestudos.ui.caderno

/** Um tipo de formatação aplicado a um trecho do texto do caderno. */
enum class TipoEstilo(val rotulo: String) {
    NEGRITO("Negrito"),
    ITALICO("Itálico"),
    REALCE("Realce"),
    TITULO("Título"),
    GRANDE("Grande"),
    PEQUENO("Pequeno")
}

/** Um trecho [inicio, fim) do texto com um tipo de formatação aplicado. */
data class EstiloAplicado(val inicio: Int, val fim: Int, val tipo: TipoEstilo)

/** O conteúdo do caderno de uma aula: texto contínuo (como um documento) mais os estilos
 *  aplicados sobre trechos dele — não é mais dividido em blocos/linhas separadas. */
data class NotaCaderno(val texto: String = "", val estilos: List<EstiloAplicado> = emptyList())

/**
 * Serializa/desserializa o conteúdo do caderno para uma única String (a mesma coluna
 * `anotacoesCaderno` do banco, sem precisar de migração nem biblioteca de JSON). Usa
 * caracteres de controle como separadores, praticamente impossíveis de aparecer digitados.
 */
object CadernoSerializer {
    private const val SEP_CABECALHO = '\u0001'
    private const val SEP_ESTILO = '\u0002'
    private const val SEP_CAMPO = '\u0003'

    // Separadores do formato antigo (baseado em blocos/linhas), usados só para migração.
    private const val SEP_BLOCO_ANTIGO = '\u001E'
    private const val SEP_CAMPO_ANTIGO = '\u001F'

    fun serializar(nota: NotaCaderno): String {
        val cabecalho = nota.estilos.joinToString(SEP_ESTILO.toString()) {
            listOf(it.inicio, it.fim, it.tipo.name).joinToString(SEP_CAMPO.toString())
        }
        return cabecalho + SEP_CABECALHO + nota.texto
    }

    fun desserializar(salvo: String): NotaCaderno {
        if (salvo.isEmpty()) return NotaCaderno()

        val indice = salvo.indexOf(SEP_CABECALHO)
        if (indice == -1) return migrarFormatoAntigo(salvo)

        val cabecalho = salvo.substring(0, indice)
        val texto = salvo.substring(indice + 1)
        val estilos = if (cabecalho.isBlank()) {
            emptyList()
        } else {
            cabecalho.split(SEP_ESTILO).mapNotNull { linha ->
                val campos = linha.split(SEP_CAMPO)
                if (campos.size != 3) return@mapNotNull null
                val inicio = campos[0].toIntOrNull() ?: return@mapNotNull null
                val fim = campos[1].toIntOrNull() ?: return@mapNotNull null
                val tipo = runCatching { TipoEstilo.valueOf(campos[2]) }.getOrNull() ?: return@mapNotNull null
                EstiloAplicado(inicio, fim, tipo)
            }
        }
        val estilosValidos = estilos.filter { it.inicio in 0..texto.length && it.fim in it.inicio..texto.length }
        return NotaCaderno(texto, estilosValidos)
    }

    fun temConteudo(salvo: String): Boolean = desserializar(salvo).texto.isNotBlank()

    /** Melhor esforço para ler anotações salvas na versão anterior (baseada em blocos/linhas):
     *  extrai só o texto de cada linha e junta com quebras de linha, sem tentar recuperar a
     *  formatação antiga (negrito/itálico/tamanho eram por linha, não davam pra mapear 1:1). */
    private fun migrarFormatoAntigo(salvo: String): NotaCaderno {
        val partes = salvo.split(SEP_BLOCO_ANTIGO)
        val pareceFormatoAntigo = partes.any { it.split(SEP_CAMPO_ANTIGO).size >= 5 }
        if (!pareceFormatoAntigo) return NotaCaderno(texto = salvo)

        val linhas = partes.map { linha ->
            val campos = linha.split(SEP_CAMPO_ANTIGO)
            if (campos.size >= 5) campos.drop(4).joinToString(SEP_CAMPO_ANTIGO.toString()) else linha
        }
        return NotaCaderno(texto = linhas.joinToString("\n"))
    }
}
