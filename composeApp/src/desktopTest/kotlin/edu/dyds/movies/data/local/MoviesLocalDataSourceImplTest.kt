package edu.dyds.movies.data.local

import edu.dyds.movies.domain.entities.Movie
import java.lang.reflect.InvocationTargetException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MoviesLocalDataSourceImplTest {

    private lateinit var localDataSource: MoviesLocalDataSourceImpl

    @BeforeTest
    fun setUp() {
        localDataSource = MoviesLocalDataSourceImpl()
    }

    @Test
    fun `getCachedMovies without saved movies should return empty list`() {
        val expected = emptyList<Movie>()

        val result = localDataSource.getCachedMovies()

        assertEquals(expected, result)
    }

    @Test
    fun `saveMovies with movies should store and return the same movies`() {
        val movies = listOf(
            createMovie(id = 1, title = "Movie 1"),
            createMovie(id = 2, title = "Movie 2")
        )

        localDataSource.saveMovies(movies)

        val cachedMovies = localDataSource.getCachedMovies()
        assertEquals(movies, cachedMovies)
    }

    @Test
    fun `saveMovies with existing cache should replace it completely`() {
        localDataSource.saveMovies(listOf(createMovie(id = 1, title = "Old Movie")))
        val newMovies = listOf(
            createMovie(id = 2, title = "New Movie 1"),
            createMovie(id = 3, title = "New Movie 2")
        )

        localDataSource.saveMovies(newMovies)

        val cachedMovies = localDataSource.getCachedMovies()
        assertEquals(newMovies, cachedMovies)
    }

    @Test
    fun `saveMovies with empty list should clear existing cache`() {
        localDataSource.saveMovies(listOf(createMovie(id = 1, title = "Movie to clear")))

        localDataSource.saveMovies(emptyList())

        val cachedMovies = localDataSource.getCachedMovies()
        assertTrue(cachedMovies.isEmpty())
    }

    @Test
    fun `saveMovies with extreme values should preserve them unchanged`() {
        val extremeMovies = listOf(
            createMovie(id = -1, title = "", popularity = Double.MAX_VALUE, voteAverage = 0.0),
            createMovie(
                id = Int.MIN_VALUE,
                title = "   ",
                overview = "",
                releaseDate = "",
                poster = "",
                originalTitle = "",
                originalLanguage = "",
                popularity = Double.MIN_VALUE,
                voteAverage = Double.MAX_VALUE
            )
        )

        localDataSource.saveMovies(extremeMovies)

        val cachedMovies = localDataSource.getCachedMovies()
        assertEquals(extremeMovies, cachedMovies)
    }

    @Test
    fun `getCachedMovies should return copy to prevent external cache corruption`() {
        val originalMovies = listOf(
            createMovie(id = 1, title = "Original 1"),
            createMovie(id = 2, title = "Original 2")
        )
        localDataSource.saveMovies(originalMovies)
        val exposedMovies = localDataSource.getCachedMovies() as MutableList<Movie>

        exposedMovies.clear()

        val cachedMovies = localDataSource.getCachedMovies()
        assertEquals(originalMovies, cachedMovies)
    }

    @Test
    fun `saveMovies with null input via JVM interop should throw NullPointerException`() {
        val saveMoviesMethod =
            MoviesLocalDataSourceImpl::class.java.getMethod("saveMovies", List::class.java)

        val exception = assertFailsWith<InvocationTargetException> {
            saveMoviesMethod.invoke(localDataSource, null)
        }

        assertTrue(exception.cause is NullPointerException)
    }

    private fun createMovie(
        id: Int = 1,
        title: String = "Test Movie",
        overview: String = "overview",
        releaseDate: String = "2024-01-01",
        poster: String = "poster.jpg",
        backdrop: String? = null,
        originalTitle: String = title,
        originalLanguage: String = "en",
        popularity: Double = 1.0,
        voteAverage: Double = 1.0
    ): Movie {
        return Movie(
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
    }
}