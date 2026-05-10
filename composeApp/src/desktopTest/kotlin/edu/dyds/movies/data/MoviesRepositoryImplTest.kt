package edu.dyds.movies.data

import edu.dyds.movies.data.local.MoviesLocalDataSource
import edu.dyds.movies.data.remote.MoviesRemoteDataSource
import edu.dyds.movies.data.remote.RemoteMovie
import edu.dyds.movies.data.remote.RemoteResult
import edu.dyds.movies.domain.entities.Movie
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MoviesRepositoryImplTest {

    class FakeLocalDataSource : MoviesLocalDataSource {
        var savedMovies: List<Movie> = emptyList()
        var cachedMoviesReturn: List<Movie> = emptyList()

        override fun getCachedMovies(): List<Movie> = cachedMoviesReturn
        override fun saveMovies(movies: List<Movie>) { savedMovies = movies }
    }

    class FakeRemoteDataSource : MoviesRemoteDataSource {
        var popularMoviesReturn: RemoteResult = RemoteResult(1, emptyList(), 0, 0)
        var movieDetailsReturn: RemoteMovie? = null
        var shouldThrow: Boolean = false
        var getPopularMoviesInvocations: Int = 0

        override suspend fun getPopularMovies(): RemoteResult {
            getPopularMoviesInvocations++
            if (shouldThrow) throw Exception("Network error")
            return popularMoviesReturn
        }

        override suspend fun getMovieDetails(id: Int): RemoteMovie {
            if (shouldThrow) throw Exception("Network error")
            return movieDetailsReturn ?: throw Exception("Not found")
        }
    }

    private fun buildRemoteMovie(id: Int = 1) = RemoteMovie(
        id = id,
        title = "Movie $id",
        overview = "Overview",
        releaseDate = "2024-01-01",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        originalTitle = "Original $id",
        originalLanguage = "en",
        popularity = 9.0,
        voteAverage = 8.5
    )

    private fun buildMovie(id: Int = 1) = Movie(
        id = id,
        title = "Movie $id",
        overview = "Overview",
        releaseDate = "2024-01-01",
        poster = "https://image.tmdb.org/t/p/w185/poster.jpg",
        backdrop = "https://image.tmdb.org/t/p/w780/backdrop.jpg",
        originalTitle = "Original $id",
        originalLanguage = "en",
        popularity = 9.0,
        voteAverage = 8.5
    )


    @Test
    fun `getPopularMovies - when cache is not empty - returns cached movies`() = runTest {
        // arrange
        val local = FakeLocalDataSource().apply {
            cachedMoviesReturn = listOf(buildMovie(1), buildMovie(2))
        }
        val remote = FakeRemoteDataSource()
        val repository = MoviesRepositoryImpl(remote, local)

        // act
        val result = repository.getPopularMovies()

        // assert
        assertEquals(2, result.size)
        assertEquals(local.cachedMoviesReturn, result)
        assertEquals(0, remote.getPopularMoviesInvocations)
    }

    @Test
    fun `getPopularMovies - when cache is empty and remote succeeds - returns remote movies and saves them`() = runTest {
        // arrange
        val remote = FakeRemoteDataSource().apply {
            popularMoviesReturn = RemoteResult(1, listOf(buildRemoteMovie(10)), 1, 1)
        }
        val local = FakeLocalDataSource()
        val repository = MoviesRepositoryImpl(remote, local)

        // act
        val result = repository.getPopularMovies()

        // assert
        assertEquals(1, result.size)
        assertEquals(10, result.first().id)
        assertEquals(1, local.savedMovies.size)
    }

    @Test
    fun `getPopularMovies - when cache is empty and remote succeeds - applies URL mapping to images`() = runTest {
        // arrange
        val remoteMovie = buildRemoteMovie(1).copy(
            posterPath = "/test_poster.jpg",
            backdropPath = "/test_backdrop.jpg"
        )
        val remote = FakeRemoteDataSource().apply {
            popularMoviesReturn = RemoteResult(1, listOf(remoteMovie), 1, 1)
        }
        val local = FakeLocalDataSource()
        val repository = MoviesRepositoryImpl(remote, local)

        // act
        val result = repository.getPopularMovies()

        // assert
        assertEquals(1, result.size)
        val mappedMovie = result.first()
        assertEquals("https://image.tmdb.org/t/p/w185/test_poster.jpg", mappedMovie.poster)
        assertEquals("https://image.tmdb.org/t/p/w780/test_backdrop.jpg", mappedMovie.backdrop)
    }

    @Test
    fun `getPopularMovies - when cache is empty and remote fails - returns empty list`() = runTest {
        // arrange
        val remote = FakeRemoteDataSource().apply { shouldThrow = true }
        val local = FakeLocalDataSource()
        val repository = MoviesRepositoryImpl(remote, local)

        // act
        val result = repository.getPopularMovies()

        // assert
        assertTrue(result.isEmpty())
        assertTrue(local.savedMovies.isEmpty())
    }

    @Test
    fun `getMovieDetails - when remote succeeds - returns mapped movie`() = runTest {
        // arrange
        val remote = FakeRemoteDataSource().apply {
            movieDetailsReturn = buildRemoteMovie(42)
        }
        val local = FakeLocalDataSource()
        val repository = MoviesRepositoryImpl(remote, local)

        // act
        val result = repository.getMovieDetails(42)

        // assert
        assertNotNull(result)
        assertEquals(42, result.id)
        assertEquals("Movie 42", result.title)
    }

    @Test
    fun `getMovieDetails - when remote fails - returns null`() = runTest {
        // arrange
        val remote = FakeRemoteDataSource().apply { shouldThrow = true }
        val local = FakeLocalDataSource()
        val repository = MoviesRepositoryImpl(remote, local)

        // act
        val result = repository.getMovieDetails(1)

        // assert
        assertNull(result)
    }
}
