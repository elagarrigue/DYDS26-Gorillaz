package edu.dyds.movies.data.external

import edu.dyds.movies.domain.entities.Movie

interface MoviesExternalSource {
    suspend fun getPopularMovies(): List<Movie.MovieItem>
}