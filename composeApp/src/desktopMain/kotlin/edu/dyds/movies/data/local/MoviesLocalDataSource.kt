package edu.dyds.movies.data.local

import edu.dyds.movies.domain.entities.Movie

class MoviesLocalDataSource {
    private val cachedMovies: MutableList<Movie> = mutableListOf()

    fun getCachedMovies(): List<Movie> = cachedMovies

    fun saveMovies(movies: List<Movie>) {
        cachedMovies.clear()
        cachedMovies.addAll(movies)
    }

    fun clearCache() {
        cachedMovies.clear()
    }
}
