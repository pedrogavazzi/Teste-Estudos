package com.pedrogavazzi.controleestudos.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Campo de busca simples e reutilizável — usado nas telas de Matérias e Agenda para
 *  filtrar listas longas, sem precisar rolar a tela inteira procurando algo. */
@Composable
fun CampoDeBusca(
    valor: String,
    onValorAlterado: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar"
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorAlterado,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(50),
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = { onValorAlterado("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Limpar busca")
                }
            }
        }
    )
}
