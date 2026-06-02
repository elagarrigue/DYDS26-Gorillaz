package edu.dyds.movies.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.dyds.movies.data.MoviesRepositoryImpl
import edu.dyds.movies.data.external.MovieExternalSource
import edu.dyds.movies.data.external.MovieExternalSourceBroker
import edu.dyds.movies.data.external.MoviesExternalSource
import edu.dyds.movies.data.external.omdb.OMDBMoviesExternalSource
import edu.dyds.movies.data.external.tmdb.TMDBMoviesExternalSource
import edu.dyds.movies.data.local.MoviesLocalDataSource
import edu.dyds.movies.data.local.MoviesLocalDataSourceImpl
import edu.dyds.movies.domain.repository.MoviesRepository
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCase
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCaseImpl
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCase
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCaseImpl
import edu.dyds.movies.presentation.detail.DetailViewModel
import edu.dyds.movies.presentation.home.HomeViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private const val TMDB_API_KEY = "d18da1b5da16397619c688b0263cd281"
private const val OMDB_API_KEY = "a96e7f78"

object MoviesDependencyInjector {

    private val tmdbHttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.themoviedb.org"
                    parameters.append("api_key", TMDB_API_KEY)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
            }
        }

    private val omdbHttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "www.omdbapi.com"
                    parameters.append("apikey", OMDB_API_KEY)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
            }
        }

    private val tmdbMoviesExternalSource: TMDBMoviesExternalSource by lazy { TMDBMoviesExternalSource(tmdbHttpClient) }
    private val moviesExternalSource: MoviesExternalSource by lazy { tmdbMoviesExternalSource }
    private val movieExternalSource: MovieExternalSource by lazy {
        MovieExternalSourceBroker(tmdbMoviesExternalSource, OMDBMoviesExternalSource(omdbHttpClient))
    }
    private val localDataSource: MoviesLocalDataSource by lazy { MoviesLocalDataSourceImpl() }
    private val moviesRepository: MoviesRepository by lazy { MoviesRepositoryImpl(moviesExternalSource, movieExternalSource, localDataSource) }
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase by lazy { GetPopularMoviesUseCaseImpl(moviesRepository) }
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase by lazy { GetMovieDetailsUseCaseImpl(moviesRepository) }

    @Composable
    fun getHomeViewModel(): HomeViewModel {
        return viewModel { HomeViewModel(getPopularMoviesUseCase) }
    }

    @Composable
    fun getDetailViewModel(): DetailViewModel {
        return viewModel { DetailViewModel(getMovieDetailsUseCase) }
    }
}