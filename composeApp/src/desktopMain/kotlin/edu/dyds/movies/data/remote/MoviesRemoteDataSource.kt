package edu.dyds.movies.data.remote

import edu.dyds.movies.data.external.tmdb.RemoteMovie
import edu.dyds.movies.data.external.tmdb.RemoteResult

interface MoviesRemoteDataSource {
    suspend fun getPopularMovies(): RemoteResult

    suspend fun getMovieDetails(id: Int): RemoteMovie

    suspend fun getMovieByTitle(title: String): RemoteMovie
}
