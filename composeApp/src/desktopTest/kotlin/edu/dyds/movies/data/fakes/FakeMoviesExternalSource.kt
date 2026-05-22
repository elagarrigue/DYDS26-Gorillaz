package edu.dyds.movies.data.fakes

import edu.dyds.movies.data.external.MoviesExternalSource
import edu.dyds.movies.domain.entities.Movie

class FakeMoviesExternalSource : MoviesExternalSource {
    var popularMoviesReturn: List<Movie.MovieItem> = emptyList()
    var shouldThrow: Boolean = false
    var getPopularMoviesInvocations: Int = 0

    override suspend fun getPopularMovies(): List<Movie.MovieItem> {
        getPopularMoviesInvocations++
        if (shouldThrow) throw Exception("Network error")
        return popularMoviesReturn
    }
}