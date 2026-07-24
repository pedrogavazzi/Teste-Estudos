package com.pedrogavazzi.controleestudos.data

import android.content.Context
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import com.pedrogavazzi.controleestudos.ui.caderno.CadernoSerializer
import com.pedrogavazzi.controleestudos.ui.components.formatarDataHora
import java.io.File
import java.io.FileOutputStream

/**
 * Gera um PDF com o resumo de todas as matérias e aulas (nome, data, status, observação) e,
 * quando a aula tiver anotações reais no caderno, o texto delas também — usando a API nativa
 * do Android para PDF (sem precisar de biblioteca externa). Fica salvo na pasta de cache do
 * app, pronta pra ser compartilhada por um FileProvider.
 */
object ExportadorPdf {

    private const val LARGURA_PT = 595 // A4 a 72 dpi
    private const val ALTURA_PT = 842
    private const val MARGEM = 40f

    fun gerar(context: Context, dados: List<Pair<Materia, List<Aula>>>): File {
        val documento = PdfDocument()
        var numeroPagina = 1
        var pagina = documento.startPage(PdfDocument.PageInfo.Builder(LARGURA_PT, ALTURA_PT, numeroPagina).create())
        var canvas = pagina.canvas
        var y = MARGEM

        fun novaPagina() {
            documento.finishPage(pagina)
            numeroPagina++
            pagina = documento.startPage(PdfDocument.PageInfo.Builder(LARGURA_PT, ALTURA_PT, numeroPagina).create())
            canvas = pagina.canvas
            y = MARGEM
        }

        fun desenharTexto(texto: String, paint: TextPaint, espacamentoDepois: Float = 4f) {
            if (texto.isBlank()) return
            val larguraDisponivel = (LARGURA_PT - 2 * MARGEM).toInt()
            val layout = StaticLayout.Builder
                .obtain(texto, 0, texto.length, paint, larguraDisponivel)
                .build()
            if (y + layout.height > ALTURA_PT - MARGEM) novaPagina()
            canvas.save()
            canvas.translate(MARGEM, y)
            layout.draw(canvas)
            canvas.restore()
            y += layout.height + espacamentoDepois
        }

        val paintTitulo = TextPaint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
        val paintData = TextPaint().apply { color = Color.DKGRAY; textSize = 9f }
        val paintMateria = TextPaint().apply { color = Color.rgb(103, 80, 164); textSize = 14f; isFakeBoldText = true }
        val paintAula = TextPaint().apply { color = Color.BLACK; textSize = 11f; isFakeBoldText = true }
        val paintCorpo = TextPaint().apply { color = Color.rgb(58, 53, 64); textSize = 10f }
        val paintRotuloCaderno = TextPaint().apply { color = Color.rgb(103, 80, 164); textSize = 9.5f; isFakeBoldText = true }

        desenharTexto("Controle de Estudos — exportação de dados", paintTitulo, 4f)
        desenharTexto("Gerado em ${formatarDataHora(System.currentTimeMillis())}", paintData, 18f)

        if (dados.isEmpty()) {
            desenharTexto("Nenhuma matéria cadastrada ainda.", paintCorpo)
        }

        dados.forEach { (materia, aulas) ->
            val concluidas = aulas.count { it.concluida }
            desenharTexto("${materia.nome} — $concluidas de ${aulas.size} aulas concluídas", paintMateria, 8f)

            aulas.forEach { aula ->
                val dataTexto = aula.dataHoraMillis?.let { formatarDataHora(it) } ?: "sem data definida"
                val statusTexto = if (aula.concluida) "concluída" else "pendente"
                desenharTexto("${aula.nomeExibido()} — $dataTexto ($statusTexto)", paintAula, 2f)

                if (aula.observacao.isNotBlank()) {
                    desenharTexto("Observação: ${aula.observacao}", paintCorpo, 4f)
                }

                // Só entra no PDF se o caderno dessa aula tiver conteúdo de verdade — cadernos
                // vazios (nunca abertos, ou abertos e não editados) ficam de fora.
                val notaCaderno = CadernoSerializer.desserializar(aula.anotacoesCaderno)
                if (notaCaderno.texto.isNotBlank()) {
                    desenharTexto("Caderno da aula:", paintRotuloCaderno, 2f)
                    desenharTexto(notaCaderno.texto, paintCorpo, 8f)
                }
            }
            y += 10f
        }

        documento.finishPage(pagina)

        val pasta = File(context.cacheDir, "exportacoes")
        if (!pasta.exists()) pasta.mkdirs()
        val arquivo = File(pasta, "controle_de_estudos_exportacao.pdf")
        FileOutputStream(arquivo).use { saida -> documento.writeTo(saida) }
        documento.close()
        return arquivo
    }
}
