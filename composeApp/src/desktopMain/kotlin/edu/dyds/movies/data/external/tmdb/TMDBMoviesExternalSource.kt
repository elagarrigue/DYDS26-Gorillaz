package edu.dyds.movies.data.external.tmdb

import edu.dyds.movies.data.external.PopularMoviesExternalSource
import edu.dyds.movies.domain.entities.Movie
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val POPULAR_MOVIES_PATH = "/3/discover/movie?sort_by=popularity.desc"
private const val SEARCH_MOVIE_PATH = "/3/search/movie"
private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w185"
private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780"

open class TMDBMoviesExternalSource(private val tmdbHttpClient: HttpClient) : PopularMoviesExternalSource {

    override suspend fun getPopularMovies(): List<Movie.MovieItem> =
        tmdbHttpClient.get(POPULAR_MOVIES_PATH).body<RemoteResult>().results.map { it.toMovieItem() }

    open suspend fun getMovieByTitle(title: String): Movie.MovieItem? =
        tmdbHttpClient.get(SEARCH_MOVIE_PATH) {
            parameter("query", title)
        }.body<RemoteResult>().results.first().toMovieItem()
}

@Serializable
data class RemoteResult(
    val page: Int,
    val results: List<RemoteMovie>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)

@Serializable
data class RemoteMovie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("release_date") val releaseDate: String?,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("original_title") val originalTitle: String,
    @SerialName("original_language") val originalLanguage: String,
    val popularity: Double?,
    @SerialName("vote_average") val voteAverage: Double?,
)

private fun RemoteMovie.toMovieItem(): Movie.MovieItem = Movie.MovieItem(
    id = id,
    title = title,
    overview = overview,
    releaseDate = releaseDate ?: "",
    poster = "$POSTER_BASE_URL$posterPath",
    backdrop = backdropPath?.let { "$BACKDROP_BASE_URL$it" },
    originalTitle = originalTitle,
    originalLanguage = originalLanguage,
    popularity = popularity ?: 0.0,
    voteAverage = voteAverage ?: 0.0
)