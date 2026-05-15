package edu.dyds.movies.data.fakes

import edu.dyds.movies.data.local.MoviesLocalDataSource
import edu.dyds.movies.domain.entities.Movie

class FakeMoviesLocalDataSource : MoviesLocalDataSource {
    var savedMovies: List<Movie> = emptyList()
    var cachedMoviesReturn: List<Movie> = emptyList()

    override fun getCachedMovies(): List<Movie> = cachedMoviesReturn
    override fun saveMovies(movies: List<Movie>) { savedMovies = movies }
}
