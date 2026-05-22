package edu.dyds.movies.data

import edu.dyds.movies.data.fakes.FakeMoviesLocalDataSource
import edu.dyds.movies.data.fakes.FakeMoviesRemoteDataSource
import edu.dyds.movies.data.external.tmdb.RemoteMovie
import edu.dyds.movies.data.external.tmdb.RemoteResult
import edu.dyds.movies.domain.entities.Movie
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MoviesRepositoryImplTest {

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
        val local = FakeMoviesLocalDataSource().apply {
            cachedMoviesReturn = listOf(buildMovie(1), buildMovie(2))
        }
        val remote = FakeMoviesRemoteDataSource()
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
        val remote = FakeMoviesRemoteDataSource().apply {
            popularMoviesReturn = RemoteResult(1, listOf(buildRemoteMovie(10)), 1, 1)
        }
        val local = FakeMoviesLocalDataSource()
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
        val remote = FakeMoviesRemoteDataSource().apply {
            popularMoviesReturn = RemoteResult(1, listOf(remoteMovie), 1, 1)
        }
        val local = FakeMoviesLocalDataSource()
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
        val remote = FakeMoviesRemoteDataSource().apply { shouldThrow = true }
        val local = FakeMoviesLocalDataSource()
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
        val remote = FakeMoviesRemoteDataSource().apply {
            movieDetailsReturn = buildRemoteMovie(42)
        }
        val local = FakeMoviesLocalDataSource()
        val repository = MoviesRepositoryImpl(remote, local)

        // act
        val result = repository.getMovieDetails("Movie 42")

        // assert
        assertNotNull(result)
        assertEquals(42, result.id)
        assertEquals("Movie 42", result.title)
        assertEquals(1, remote.getMovieByTitleInvocations)
        assertEquals("Movie 42", remote.lastQueriedTitle)
    }

    @Test
    fun `getMovieDetails - when remote succeeds - applies URL mapping to images`() = runTest {
        // arrange
        val remote = FakeMoviesRemoteDataSource().apply {
            movieDetailsReturn = buildRemoteMovie(1).copy(
                posterPath = "/detail_poster.jpg",
                backdropPath = "/detail_backdrop.jpg"
            )
        }
        val local = FakeMoviesLocalDataSource()
        val repository = MoviesRepositoryImpl(remote, local)

        // act
        val result = repository.getMovieDetails("Movie 1")

        // assert
        assertNotNull(result)
        assertEquals("https://image.tmdb.org/t/p/w185/detail_poster.jpg", result.poster)
        assertEquals("https://image.tmdb.org/t/p/w780/detail_backdrop.jpg", result.backdrop)
    }

    @Test
    fun `getMovieDetails - when remote succeeds - applies defaults for nullable fields`() = runTest {
        // arrange
        val remote = FakeMoviesRemoteDataSource().apply {
            movieDetailsReturn = buildRemoteMovie(1).copy(
                releaseDate = null,
                popularity = null,
                voteAverage = null
            )
        }
        val local = FakeMoviesLocalDataSource()
        val repository = MoviesRepositoryImpl(remote, local)

        // act
        val result = repository.getMovieDetails("Movie 1")

        // assert
        assertNotNull(result)
        assertEquals("", result.releaseDate)
        assertEquals(0.0, result.popularity)
        assertEquals(0.0, result.voteAverage)
    }

    @Test
    fun `getMovieDetails - when remote fails - returns null`() = runTest {
        // arrange
        val remote = FakeMoviesRemoteDataSource().apply { shouldThrow = true }
        val local = FakeMoviesLocalDataSource()
        val repository = MoviesRepositoryImpl(remote, local)

        // act
        val result = repository.getMovieDetails("Movie 1")

        // assert
        assertNull(result)
    }
}
