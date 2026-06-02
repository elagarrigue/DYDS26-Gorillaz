package edu.dyds.movies.data.external

import edu.dyds.movies.domain.entities.Movie

interface MovieExternalSource {
    suspend fun getMovieByTitle(title: String): Movie.MovieItem?
}