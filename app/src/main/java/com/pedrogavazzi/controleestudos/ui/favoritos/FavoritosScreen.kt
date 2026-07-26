package com.pedrogavazzi.controleestudos.ui.favoritos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pedrogavazzi.controleestudos.data.nomeExibido
import com.pedrogavazzi.controleestudos.ui.agenda.AulaComMateria
import com.pedrogavazzi.controleestudos.ui.components.IniciaisDaMateria
import com.pedrogavazzi.controleestudos.ui.components.ConteudoComLarguraMaxima
import com.pedrogavazzi.controleestudos.ui.components.TextoNomeMateria
import com.pedrogavazzi.controleestudos.ui.components.abrirLinkDaAula

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    viewModel: FavoritosViewModel,
    onAbrirCaderno: (Long) -> Unit = {}
) {
    val favoritas by viewModel.favoritas.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Favoritos", style = MaterialTheme.typography.titleLarge) })
        }
    ) { padding ->
        ConteudoComLarguraMaxima(Modifier.padding(padding)) {
            if (favoritas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(4.dp))
                        Text(
                            "Nenhuma aula favoritada ainda.\nToque na estrela de uma aula pra guardar aqui pra revisar depois.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favoritas, key = { it.aula.id }) { item ->
                        ItemFavorita(
                            item = item,
                            onAbrirCaderno = { onAbrirCaderno(item.aula.id) },
                            onDesmarcar = { viewModel.desmarcarFavorita(item.aula) }
                        )
                    }
                    item { Spacer(Modifier.padding(40.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ItemFavorita(
    item: AulaComMateria,
    onAbrirCaderno: () -> Unit,
    onDesmarcar: () -> Unit
) {
    val context = LocalContext.current
    val cor = runCatching { Color(android.graphics.Color.parseColor(item.corHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrirCaderno),
        shape = com.pedrogavazzi.controleestudos.ui.theme.FormaCard,
        colors = com.pedrogavazzi.controleestudos.ui.theme.corDeCardTonal()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IniciaisDaMateria(nomeMateria = item.nomeMateria, cor = cor)
            Spacer(Modifier.padding(start = 8.dp))
            Column(Modifier.weight(1f)) {
                TextoNomeMateria(
                    nome = item.nomeMateria,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(item.aula.nomeExibido(), style = MaterialTheme.typography.bodyLarge)
            }
            if (item.aula.link.isNotBlank()) {
                IconButton(onClick = { abrirLinkDaAula(context, item.aula.link) }) {
                    Icon(Icons.Filled.Link, contentDescription = "Abrir link da aula", tint = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onDesmarcar) {
                Icon(Icons.Filled.Star, contentDescription = "Remover dos favoritos", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
