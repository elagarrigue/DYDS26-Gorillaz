package edu.dyds.movies.data.external

import edu.dyds.movies.data.external.omdb.OMDBMoviesExternalSource
import edu.dyds.movies.data.external.tmdb.TMDBMoviesExternalSource
import edu.dyds.movies.domain.entities.Movie
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MovieDetailBrokerTest {

    @Test
    fun `getMovieByTitle when both sources succeed returns merged movie`() = runTest {
        val tmdbMovie = buildMovieItem(
            id = 10,
            title = "TMDB Title",
            overview = "TMDB overview",
            releaseDate = "2024-01-01",
            poster = "tmdb_poster.jpg",
            backdrop = "tmdb_backdrop.jpg",
            originalTitle = "TMDB Original",
            originalLanguage = "en",
            popularity = 10.0,
            voteAverage = 8.0
        )
        val omdbMovie = buildMovieItem(
            id = 99,
            title = "OMDB Title",
            overview = "OMDB overview",
            releaseDate = "1999-01-01",
            poster = "omdb_poster.jpg",
            backdrop = "omdb_backdrop.jpg",
            originalTitle = "OMDB Original",
            originalLanguage = "es",
            popularity = 6.0,
            voteAverage = 4.0
        )
        val broker = MovieDetailBroker(
            FakeTMDBMoviesExternalSource(movieReturn = tmdbMovie),
            FakeOMDBMoviesExternalSource(movieReturn = omdbMovie)
        )

        val result = broker.getMovieByTitle("Movie")

        assertNotNull(result)
        assertEquals(tmdbMovie.id, result.id)
        assertEquals(tmdbMovie.title, result.title)
        assertEquals(tmdbMovie.releaseDate, result.releaseDate)
        assertEquals(tmdbMovie.poster, result.poster)
        assertEquals(tmdbMovie.backdrop, result.backdrop)
        assertEquals(tmdbMovie.originalTitle, result.originalTitle)
        assertEquals(tmdbMovie.originalLanguage, result.originalLanguage)
        assertEquals("TMDB: ${tmdbMovie.overview}\n\nOMDB: ${omdbMovie.overview}", result.overview)
        assertEquals((tmdbMovie.popularity + omdbMovie.popularity) / 2.0, result.popularity)
        assertEquals((tmdbMovie.voteAverage + omdbMovie.voteAverage) / 2.0, result.voteAverage)
    }

    @Test
    fun `getMovieByTitle when only TMDB succeeds returns TMDB movie with prefixed overview`() = runTest {
        val tmdbMovie = buildMovieItem(overview = "TMDB overview")
        val broker = MovieDetailBroker(
            FakeTMDBMoviesExternalSource(movieReturn = tmdbMovie),
            FakeOMDBMoviesExternalSource(shouldThrow = true)
        )

        val result = broker.getMovieByTitle("Movie")

        assertEquals(tmdbMovie.copy(overview = "TMDB: ${tmdbMovie.overview}"), result)
    }

    @Test
    fun `getMovieByTitle when only OMDB succeeds returns OMDB movie with prefixed overview`() = runTest {
        val omdbMovie = buildMovieItem(overview = "OMDB overview")
        val broker = MovieDetailBroker(
            FakeTMDBMoviesExternalSource(shouldThrow = true),
            FakeOMDBMoviesExternalSource(movieReturn = omdbMovie)
        )

        val result = broker.getMovieByTitle("Movie")

        assertEquals(omdbMovie.copy(overview = "OMDB: ${omdbMovie.overview}"), result)
    }

    @Test
    fun `getMovieByTitle when both sources fail returns null`() = runTest {
        val broker = MovieDetailBroker(
            FakeTMDBMoviesExternalSource(shouldThrow = true),
            FakeOMDBMoviesExternalSource(shouldThrow = true)
        )

        val result = broker.getMovieByTitle("Movie")

        assertNull(result)
    }
}

private fun buildMovieItem(
    id: Int = 1,
    title: String = "Movie",
    overview: String = "Overview",
    releaseDate: String = "2024-01-01",
    poster: String = "poster.jpg",
    backdrop: String? = "backdrop.jpg",
    originalTitle: String = "Original",
    originalLanguage: String = "en",
    popularity: Double = 7.0,
    voteAverage: Double = 6.0
): Movie.MovieItem = Movie.MovieItem(
    id = id,
    title = title,
    overview = overview,
    releaseDate = releaseDate,
    poster = poster,
    backdrop = backdrop,
    originalTitle = originalTitle,
    originalLanguage = originalLanguage,
    popularity = popularity,
    voteAverage = voteAverage
)

private fun buildClient(): HttpClient =
    HttpClient(MockEngine) {
        engine {
            addHandler { respondError(HttpStatusCode.InternalServerError) }
        }
    }

private class FakeTMDBMoviesExternalSource(
    private val movieReturn: Movie.MovieItem? = null,
    private val shouldThrow: Boolean = false
) : TMDBMoviesExternalSource(buildClient()) {
    override suspend fun getMovieByTitle(title: String): Movie.MovieItem? {
        if (shouldThrow) throw Exception("TMDB error")
        return movieReturn
    }
}

private class FakeOMDBMoviesExternalSource(
    private val movieReturn: Movie.MovieItem? = null,
    private val shouldThrow: Boolean = false
) : OMDBMoviesExternalSource(buildClient()) {
    override suspend fun getMovieByTitle(title: String): Movie.MovieItem? {
        if (shouldThrow) throw Exception("OMDB error")
        return movieReturn
    }
}
