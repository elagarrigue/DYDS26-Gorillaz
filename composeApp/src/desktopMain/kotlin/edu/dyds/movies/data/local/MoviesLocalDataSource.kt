package edu.dyds.movies.data.local

import edu.dyds.movies.domain.entities.Movie

interface MoviesLocalDataSource {
    fun getCachedMovies(): List<Movie>

    fun saveMovies(movies: List<Movie>)
}

class MoviesLocalDataSourceImpl : MoviesLocalDataSource {
    private val cachedMovies: MutableList<Movie> = mutableListOf()

    override fun getCachedMovies(): List<Movie> = cachedMovies.toList()

    override fun saveMovies(movies: List<Movie>) {
        cachedMovies.clear()
        cachedMovies.addAll(movies)
    }
}
