package com.pedrogavazzi.controleestudos.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.data.StatusAula
import com.pedrogavazzi.controleestudos.ui.theme.VerdeSucesso
import com.pedrogavazzi.controleestudos.ui.theme.VermelhoAlerta

@Composable
fun StatusChip(status: StatusAula, modifier: Modifier = Modifier) {
    val (texto, cor) = when (status) {
        StatusAula.NAO_AGENDADA -> "Não agendada" to MaterialTheme.colorScheme.outline
        StatusAula.AGENDADA -> "Agendada" to MaterialTheme.colorScheme.primary
        StatusAula.ATRASADA -> "Atrasada" to VermelhoAlerta
        StatusAula.CONCLUIDA -> "Concluída" to VerdeSucesso
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = cor.copy(alpha = 0.15f)
    ) {
        Text(
            text = texto,
            color = cor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
