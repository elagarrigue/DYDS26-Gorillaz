package edu.dyds.movies.domain.repository

import edu.dyds.movies.domain.entities.Movie

interface MoviesRepository {
    suspend fun getPopularMovies(): List<Movie>
    suspend fun getMovieDetails(id: Int): Movie?
}