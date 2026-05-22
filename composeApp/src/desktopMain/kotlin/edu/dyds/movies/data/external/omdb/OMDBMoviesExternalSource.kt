package edu.dyds.movies.data.external.omdb

import edu.dyds.movies.data.external.MovieExternalSource
import edu.dyds.movies.domain.entities.Movie
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val omdbHttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
    install(DefaultRequest) {
        url {
            protocol = URLProtocol.HTTPS
            host = "www.omdbapi.com"
            parameters.append("apikey", "a96e7f78")
        }
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 5000
    }
}

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

internal class OMDBMoviesExternalSource(private val omdbHttpClient: HttpClient) : MovieExternalSource {

    override suspend fun getMovieByTitle(title: String): Movie.MovieItem =
        omdbHttpClient.get("/?t=$title").body<RemoteMovie>().toDomainMovie()
}