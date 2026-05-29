package edu.dyds.movies.data.external.omdb

import edu.dyds.movies.data.external.MovieDetailExternalSource
import edu.dyds.movies.domain.entities.Movie
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
data class RemoteMovie(
    val Title: String,
    val Plot: String,
    val Released: String,
    val Year: String,
    val Poster: String,
    val Language: String,
    val Metascore: String,
    val imdbRating: String
)

fun RemoteMovie.toDomainMovie(): Movie.MovieItem = Movie.MovieItem(
    id = Title.hashCode(),
    title = Title,
    overview = Plot,
    releaseDate = if (Released.isNotEmpty() && Released != "N/A") Released else Year,
    poster = Poster,
    backdrop = Poster,
    originalTitle = Title,
    originalLanguage = Language,
    popularity = imdbRating.toDoubleOrNull() ?: 0.0,
    voteAverage = if (Metascore.isNotEmpty() && Metascore != "N/A") Metascore.toDoubleOrNull() ?: 0.0 else 0.0
)

internal open class OMDBMoviesExternalSource(private val omdbHttpClient: HttpClient) : MovieDetailExternalSource {

    open override suspend fun getMovieByTitle(title: String): Movie.MovieItem? =
        omdbHttpClient.get("/?t=$title").body<RemoteMovie>().toDomainMovie()
}