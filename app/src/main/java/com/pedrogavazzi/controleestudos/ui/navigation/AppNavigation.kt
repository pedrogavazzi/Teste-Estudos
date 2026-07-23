package com.pedrogavazzi.controleestudos.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.pedrogavazzi.controleestudos.ui.agenda.AgendaScreen
import com.pedrogavazzi.controleestudos.ui.agenda.AgendaViewModel
import com.pedrogavazzi.controleestudos.ui.caderno.CadernoScreen
import com.pedrogavazzi.controleestudos.ui.caderno.CadernoViewModel
import com.pedrogavazzi.controleestudos.ui.desempenho.DesempenhoScreen
import com.pedrogavazzi.controleestudos.ui.desempenho.DesempenhoViewModel
import com.pedrogavazzi.controleestudos.ui.materiadetail.MateriaDetailScreen
import com.pedrogavazzi.controleestudos.ui.materias.MateriasScreen
import com.pedrogavazzi.controleestudos.ui.materias.MateriasViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BarraNavegacaoInferior(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destino.Materias.rota,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destino.Materias.rota) {
                val viewModel: MateriasViewModel = viewModel()
                MateriasScreen(
                    viewModel = viewModel,
                    onAbrirMateria = { id -> navController.navigate(Destino.MateriaDetail.rotaComId(id)) }
                )
            }
            composable(Destino.Agenda.rota) {
                val viewModel: AgendaViewModel = viewModel()
                AgendaScreen(viewModel = viewModel)
            }
            composable(Destino.Caderno.rota) {
                val viewModel: CadernoViewModel = viewModel()
                CadernoScreen(viewModel = viewModel)
            }
            composable(Destino.Desempenho.rota) {
                val viewModel: DesempenhoViewModel = viewModel()
                DesempenhoScreen(viewModel = viewModel)
            }
            composable(
                route = Destino.MateriaDetail.rota,
                arguments = listOf(navArgument("materiaId") { type = NavType.LongType })
            ) { backStackEntry ->
                val materiaId = backStackEntry.arguments?.getLong("materiaId") ?: return@composable
                MateriaDetailScreen(
                    materiaId = materiaId,
                    onVoltar = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun BarraNavegacaoInferior(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination

    val destinosVisiveisComBarra = setOf(Destino.Materias.rota, Destino.Agenda.rota, Destino.Caderno.rota, Destino.Desempenho.rota)
    val mostrarBarra = rotaAtual?.hierarchy?.any { it.route in destinosVisiveisComBarra } == true

    if (mostrarBarra) {
        NavigationBar {
            itensNavegacaoInferior.forEach { item ->
                val selecionado = rotaAtual?.hierarchy?.any { it.route == item.destino.rota } == true
                NavigationBarItem(
                    selected = selecionado,
                    onClick = {
                        navController.navigate(item.destino.rota) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { androidx.compose.material3.Icon(item.icone, contentDescription = item.rotulo) },
                    label = { Text(item.rotulo) }
                )
            }
        }
    }
}
