package com.pedrogavazzi.controleestudos.ui.caderno

/** Tamanho do texto de um bloco do caderno. */
enum class TamanhoBloco(val rotulo: String, val tamanhoSp: Int) {
    PEQUENO("Pequeno", 14),
    NORMAL("Normal", 16),
    GRANDE("Grande", 20),
    TITULO("Título", 26)
}

/** Marcador de lista de um bloco: nenhum, tópico (•) ou item numerado (1., 2., ...). */
enum class MarcadorBloco {
    NENHUM,
    TOPICO,
    NUMERADO
}

/** Um "parágrafo" do caderno de aula, com formatação própria. O [id] só existe durante a
 *  edição (chave estável para foco/lista no Compose) e não é salvo — o serializador o ignora. */
data class BlocoCaderno(
    val id: String = java.util.UUID.randomUUID().toString(),
    val texto: String = "",
    val negrito: Boolean = false,
    val italico: Boolean = false,
    val tamanho: TamanhoBloco = TamanhoBloco.NORMAL,
    val marcador: MarcadorBloco = MarcadorBloco.NENHUM
)

/**
 * Serializa/desserializa a lista de blocos do caderno para uma única String (guardada na
 * mesma coluna `anotacoesCaderno` do banco, sem precisar de migração nem biblioteca de JSON).
 * Usa caracteres de controle como separadores, praticamente impossíveis de aparecer digitados
 * pelo usuário. Anotações antigas (texto puro, de antes dessa tela) continuam funcionando:
 * caem automaticamente em um único bloco sem formatação.
 */
object CadernoSerializer {
    private const val SEP_CAMPO = '\u001F'
    private const val SEP_BLOCO = '\u001E'
    private const val QUANTIDADE_CAMPOS = 5

    fun serializar(blocos: List<BlocoCaderno>): String =
        blocos.joinToString(SEP_BLOCO.toString()) { bloco ->
            listOf(
                bloco.tamanho.name,
                bloco.marcador.name,
                if (bloco.negrito) "1" else "0",
                if (bloco.italico) "1" else "0",
                bloco.texto.replace(SEP_CAMPO, ' ').replace(SEP_BLOCO, ' ')
            ).joinToString(SEP_CAMPO.toString())
        }

    fun desserializar(texto: String): List<BlocoCaderno> {
        if (texto.isBlank()) return listOf(BlocoCaderno())
        return texto.split(SEP_BLOCO).map { linha ->
            val campos = linha.split(SEP_CAMPO)
            if (campos.size < QUANTIDADE_CAMPOS) {
                // Compatibilidade com anotações salvas antes desta tela existir (texto puro).
                BlocoCaderno(texto = linha)
            } else {
                BlocoCaderno(
                    tamanho = runCatching { TamanhoBloco.valueOf(campos[0]) }.getOrDefault(TamanhoBloco.NORMAL),
                    marcador = runCatching { MarcadorBloco.valueOf(campos[1]) }.getOrDefault(MarcadorBloco.NENHUM),
                    negrito = campos[2] == "1",
                    italico = campos[3] == "1",
                    texto = campos.drop(4).joinToString(SEP_CAMPO.toString())
                )
            }
        }
    }
}
