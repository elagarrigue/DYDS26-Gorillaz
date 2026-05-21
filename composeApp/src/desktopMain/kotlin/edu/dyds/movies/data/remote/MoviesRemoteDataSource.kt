package edu.dyds.movies.data.remote

import edu.dyds.movies.domain.entities.Movie
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val POPULAR_MOVIES_PATH = "/3/discover/movie?sort_by=popularity.desc"
private const val MOVIE_DETAILS_PATH = "/3/movie"
private const val MOVIE_SEARCH_PATH = "/3/search/movie"
private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w185"
private const val BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780"

interface MoviesRemoteDataSource {
    suspend fun getPopularMovies(): RemoteResult

    suspend fun getMovieDetails(id: Int): RemoteMovie

    suspend fun getMovieByTitle(title: String): RemoteMovie
}

class MoviesRemoteDataSourceImpl(private val tmdbHttpClient: HttpClient) : MoviesRemoteDataSource {

    override suspend fun getPopularMovies(): RemoteResult =
        getTMDBMovies()

    override suspend fun getMovieDetails(id: Int): RemoteMovie =
        tmdbHttpClient.get("$MOVIE_DETAILS_PATH/$id").body()

    override suspend fun getMovieByTitle(title: String): RemoteMovie =
        getTMDBMovieDetails(title).results.first()

    private suspend fun getTMDBMovies(): RemoteResult =
        tmdbHttpClient.get(POPULAR_MOVIES_PATH).body()

    private suspend fun getTMDBMovieDetails(title: String): RemoteResult =
        tmdbHttpClient.get("$MOVIE_SEARCH_PATH?query=$title").body()
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

fun RemoteMovie.toDomainMovie(): Movie {
    return Movie(
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
}
