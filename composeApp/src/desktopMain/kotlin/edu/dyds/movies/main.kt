package edu.dyds.movies

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import edu.dyds.movies.di.MoviesDependencyInjector
import edu.dyds.movies.presentation.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DYDSProject",
    ) {
        App(
            getHomeViewModel = { MoviesDependencyInjector.getHomeViewModel() },
            getDetailViewModel = { MoviesDependencyInjector.getDetailViewModel() }
        )
    }
}
