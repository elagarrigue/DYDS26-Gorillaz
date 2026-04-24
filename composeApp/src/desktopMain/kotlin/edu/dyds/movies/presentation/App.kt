@file:Suppress("FunctionName")

package edu.dyds.movies.presentation

import androidx.compose.runtime.Composable
import edu.dyds.movies.presentation.detail.DetailViewModel
import edu.dyds.movies.presentation.home.HomeViewModel

@Composable
fun App(
    getHomeViewModel: @Composable () -> HomeViewModel,
    getDetailViewModel: @Composable () -> DetailViewModel
) {
    Navigation(
        getHomeViewModel = getHomeViewModel,
        getDetailViewModel = getDetailViewModel
    )
}
