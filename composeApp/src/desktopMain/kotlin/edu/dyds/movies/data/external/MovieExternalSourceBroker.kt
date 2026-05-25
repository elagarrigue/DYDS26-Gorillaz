package edu.dyds.movies.data.external

import edu.dyds.movies.data.external.omdb.OMDBMoviesExternalSource
import edu.dyds.movies.data.external.tmdb.TMDBMoviesExternalSource
import edu.dyds.movies.domain.entities.Movie

internal class MovieExternalSourceBroker(
    private val tmdbSource: TMDBMoviesExternalSource,
    private val omdbSource: OMDBMoviesExternalSource
) : MovieExternalSource {

    override suspend fun getMovieByTitle(title: String): Movie.MovieItem {
        val tmdbResult = runCatching { tmdbSource.getMovieByTitle(title) }
        val omdbResult = runCatching { omdbSource.getMovieByTitle(title) }
        val tmdbMovie = tmdbResult.getOrNull()
        val omdbMovie = omdbResult.getOrNull()

        return when {
            tmdbMovie != null && omdbMovie != null -> Movie.MovieItem(
                id = tmdbMovie.id,
                title = tmdbMovie.title,
                overview = "TMDB: ${tmdbMovie.overview}\n\nOMDB: ${omdbMovie.overview}",
                releaseDate = tmdbMovie.releaseDate,
                poster = tmdbMovie.poster,
                backdrop = tmdbMovie.backdrop,
                originalTitle = tmdbMovie.originalTitle,
                originalLanguage = tmdbMovie.originalLanguage,
                popularity = (tmdbMovie.popularity + omdbMovie.popularity) / 2.0,
                voteAverage = (tmdbMovie.voteAverage + omdbMovie.voteAverage) / 2.0
            )
            tmdbMovie != null -> tmdbMovie.copy(overview = "TMDB: ${tmdbMovie.overview}")
            omdbMovie != null -> omdbMovie.copy(overview = "OMDB: ${omdbMovie.overview}")
            else -> throw IllegalStateException("No movie found for title: $title")
        }
    }
}
