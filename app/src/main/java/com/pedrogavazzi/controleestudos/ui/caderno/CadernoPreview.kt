package com.pedrogavazzi.controleestudos.ui.caderno

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Pré-visualização do caderno de uma aula: usada tanto na tela da matéria (por aula) quanto
 * na aba Caderno de hoje, para manter o mesmo layout em qualquer lugar que o caderno apareça.
 * Toque para abrir o editor completo.
 */
@Composable
fun PreviaDoCaderno(
    anotacoesCaderno: String,
    onClick: () -> Unit,
    titulo: String = "Caderno da aula"
) {
    val nota = remember(anotacoesCaderno) { CadernoSerializer.desserializar(anotacoesCaderno) }
    val primeirasLinhas = remember(nota.texto) {
        nota.texto.lineSequence().filter { it.isNotBlank() }.take(2).joinToString(" ")
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.padding(start = 8.dp))
                Text(
                    titulo,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.padding(4.dp))
            }
            Spacer(Modifier.padding(top = 6.dp))
            if (primeirasLinhas.isNotBlank()) {
                Text(
                    primeirasLinhas,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    "Nenhuma anotação ainda — toque para começar a escrever",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
