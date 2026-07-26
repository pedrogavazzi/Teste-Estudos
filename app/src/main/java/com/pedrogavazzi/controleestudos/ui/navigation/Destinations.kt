package com.pedrogavazzi.controleestudos.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destino(val rota: String) {
    data object Materias : Destino("materias")
    data object Agenda : Destino("agenda")
    data object Caderno : Destino("caderno")
    data object Favoritos : Destino("favoritos")
    data object Configuracoes : Destino("configuracoes")
    data object MateriaDetail : Destino("materia/{materiaId}") {
        fun rotaComId(materiaId: Long) = "materia/$materiaId"
    }
    data object CadernoEditor : Destino("caderno_editor/{aulaId}?somenteLeitura={somenteLeitura}") {
        fun rotaComId(aulaId: Long, somenteLeitura: Boolean = false) = "caderno_editor/$aulaId?somenteLeitura=$somenteLeitura"
    }
}

data class ItemNavegacao(val destino: Destino, val rotulo: String, val icone: ImageVector)

val itensNavegacaoInferior = listOf(
    ItemNavegacao(Destino.Materias, "Matérias", Icons.Filled.MenuBook),
    ItemNavegacao(Destino.Agenda, "Agenda", Icons.Filled.CalendarMonth),
    ItemNavegacao(Destino.Caderno, "Caderno", Icons.Filled.EditNote),
    ItemNavegacao(Destino.Favoritos, "Favoritos", Icons.Filled.Star),
    ItemNavegacao(Destino.Configuracoes, "Ajustes", Icons.Filled.Settings)
)

/** Traduz a preferência de aba inicial (que mora no modelo de dados, sem depender de nada de
 *  navegação) para a rota correspondente — só esse arquivo, que já é o dono do conceito de
 *  rota, sabe fazer essa tradução. */
fun rotaInicialPara(abaInicial: com.pedrogavazzi.controleestudos.data.AbaInicial): String = when (abaInicial) {
    com.pedrogavazzi.controleestudos.data.AbaInicial.MATERIAS -> Destino.Materias.rota
    com.pedrogavazzi.controleestudos.data.AbaInicial.AGENDA -> Destino.Agenda.rota
    com.pedrogavazzi.controleestudos.data.AbaInicial.CADERNO -> Destino.Caderno.rota
    com.pedrogavazzi.controleestudos.data.AbaInicial.FAVORITOS -> Destino.Favoritos.rota
}
