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
        // arrange
        val expected = emptyList<Movie>()

        // act
        val result = localDataSource.getCachedMovies()

        // assert
        assertEquals(expected, result)
    }

    @Test
    fun `saveMovies with movies should store and return the same movies`() {
        // arrange
        val movies = listOf(
            createMovie(id = 1, title = "Movie 1"),
            createMovie(id = 2, title = "Movie 2")
        )

        // act
        localDataSource.saveMovies(movies)

        // assert
        val cachedMovies = localDataSource.getCachedMovies()
        assertEquals(movies, cachedMovies)
    }

    @Test
    fun `saveMovies with existing cache should replace it completely`() {
        // arrange
        localDataSource.saveMovies(listOf(createMovie(id = 1, title = "Old Movie")))
        val newMovies = listOf(
            createMovie(id = 2, title = "New Movie 1"),
            createMovie(id = 3, title = "New Movie 2")
        )

        // act
        localDataSource.saveMovies(newMovies)

        // assert
        val cachedMovies = localDataSource.getCachedMovies()
        assertEquals(newMovies, cachedMovies)
    }

    @Test
    fun `saveMovies with empty list should clear existing cache`() {
        // arrange
        localDataSource.saveMovies(listOf(createMovie(id = 1, title = "Movie to clear")))

        // act
        localDataSource.saveMovies(emptyList())

        // assert
        val cachedMovies = localDataSource.getCachedMovies()
        assertTrue(cachedMovies.isEmpty())
    }

    @Test
    fun `saveMovies with extreme values should preserve them unchanged`() {
        // arrange
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

        // act
        localDataSource.saveMovies(extremeMovies)

        // assert
        val cachedMovies = localDataSource.getCachedMovies()
        assertEquals(extremeMovies, cachedMovies)
    }

    @Test
    fun `getCachedMovies should return copy to prevent external cache corruption`() {
        // arrange
        val originalMovies = listOf(
            createMovie(id = 1, title = "Original 1"),
            createMovie(id = 2, title = "Original 2")
        )
        localDataSource.saveMovies(originalMovies)
        val exposedMovies = localDataSource.getCachedMovies()

        // act
        localDataSource.saveMovies(listOf(createMovie(id = 3, title = "New Movie")))

        // assert
        val cachedMovies = localDataSource.getCachedMovies()
        assertEquals(originalMovies, exposedMovies)
        assertEquals(listOf(createMovie(id = 3, title = "New Movie")), cachedMovies)
    }

    @Test
    fun `saveMovies with null input via JVM interop should throw NullPointerException`() {
        // arrange
        val saveMoviesMethod =
            MoviesLocalDataSourceImpl::class.java.getMethod("saveMovies", List::class.java)

        // act
        val exception = assertFailsWith<InvocationTargetException> {
            saveMoviesMethod.invoke(localDataSource, null)
        }

        // assert
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