package com.pedrogavazzi.controleestudos.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Guarda as fotos/prints das aulas só localmente, na pasta privada do próprio app
 * (ninguém mais acessa, nem sai do aparelho) — sem depender de nenhum banco de dados
 * externo ou serviço de upload, que o projeto não tem.
 *
 * As imagens são reduzidas antes de salvar (no máximo ~1600px no lado maior): uma foto de
 * câmera moderna facilmente passa de 4000px e ocupa dezenas de MB — sem reduzir, tanto o
 * espaço em disco quanto a memória usada para mostrar as miniaturas cresceriam rápido demais
 * com poucas fotos.
 */
object ArmazenamentoFotos {

    private const val PASTA = "fotos_aulas"
    private const val LADO_MAXIMO_PX = 1600
    private const val QUALIDADE_JPEG = 85

    fun pastaFotos(context: Context): File {
        val pasta = File(context.filesDir, PASTA)
        if (!pasta.exists()) pasta.mkdirs()
        return pasta
    }

    fun arquivoPara(context: Context, nomeArquivo: String): File =
        File(pastaFotos(context), nomeArquivo)

    /**
     * Decodifica a imagem apontada por [uriOrigem] (vinda do seletor de fotos do sistema),
     * reduz pro tamanho máximo definido e salva como um arquivo novo na pasta privada do
     * app. Devolve o nome do arquivo salvo, ou null se não conseguir ler a imagem (arquivo
     * corrompido, sem permissão, etc. — falha silenciosa, não derruba o app).
     */
    suspend fun copiarReduzindoParaArmazenamentoLocal(context: Context, uriOrigem: Uri): String? =
        withContext(Dispatchers.IO) {
            val bitmapReduzido = decodificarReduzido(context, uriOrigem) ?: return@withContext null
            val nomeArquivo = "foto_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
            try {
                FileOutputStream(arquivoPara(context, nomeArquivo)).use { saida ->
                    bitmapReduzido.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, saida)
                }
                nomeArquivo
            } catch (e: Exception) {
                null
            } finally {
                bitmapReduzido.recycle()
            }
        }

    fun excluirArquivo(context: Context, nomeArquivo: String) {
        runCatching { arquivoPara(context, nomeArquivo).delete() }
    }

    /** Lê só as dimensões primeiro (sem carregar a imagem inteira), calcula quanto precisa
     *  reduzir, e só então decodifica de fato — evita estourar memória com fotos muito
     *  grandes (uma decodificação direta em resolução total pode passar de 50-100MB de RAM
     *  para uma foto de câmera comum). */
    private fun decodificarReduzido(context: Context, uri: Uri): Bitmap? {
        val opcoesDimensoes = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { entrada ->
            BitmapFactory.decodeStream(entrada, null, opcoesDimensoes)
        } ?: return null

        var amostragem = 1
        var largura = opcoesDimensoes.outWidth
        var altura = opcoesDimensoes.outHeight
        if (largura <= 0 || altura <= 0) return null
        while (largura / 2 >= LADO_MAXIMO_PX || altura / 2 >= LADO_MAXIMO_PX) {
            largura /= 2
            altura /= 2
            amostragem *= 2
        }

        val opcoesReais = BitmapFactory.Options().apply { inSampleSize = amostragem }
        val bitmapAmostrado = context.contentResolver.openInputStream(uri)?.use { entrada ->
            BitmapFactory.decodeStream(entrada, null, opcoesReais)
        } ?: return null

        // A amostragem só reduz em potências de 2, então ainda pode sobrar maior que o
        // limite — um ajuste fino final garante o tamanho máximo de verdade.
        val maiorLado = maxOf(bitmapAmostrado.width, bitmapAmostrado.height)
        if (maiorLado <= LADO_MAXIMO_PX) return bitmapAmostrado

        val fator = LADO_MAXIMO_PX.toFloat() / maiorLado
        val novaLargura = (bitmapAmostrado.width * fator).toInt().coerceAtLeast(1)
        val novaAltura = (bitmapAmostrado.height * fator).toInt().coerceAtLeast(1)
        val bitmapFinal = Bitmap.createScaledBitmap(bitmapAmostrado, novaLargura, novaAltura, true)
        if (bitmapFinal !== bitmapAmostrado) bitmapAmostrado.recycle()
        return bitmapFinal
    }
}
