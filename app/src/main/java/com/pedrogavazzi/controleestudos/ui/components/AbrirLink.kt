package com.pedrogavazzi.controleestudos.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Abre o link salvo de uma aula (videochamada, gravação, material etc.) no navegador ou app
 * correspondente. Aceita o link sem "http(s)://" na frente (o usuário pode colar só o domínio),
 * e não quebra o app se não houver nenhum app instalado capaz de abrir o link.
 */
fun abrirLinkDaAula(context: Context, link: String) {
    val texto = link.trim()
    if (texto.isBlank()) return
    val comEsquema = if (texto.contains("://")) texto else "https://$texto"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(comEsquema)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Não foi possível abrir esse link", Toast.LENGTH_SHORT).show()
    }
}
