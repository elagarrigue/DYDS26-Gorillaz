package edu.dyds.movies.data

import edu.dyds.movies.data.fakes.FakeMovieExternalSource
import edu.dyds.movies.data.fakes.FakeMoviesExternalSource
import edu.dyds.movies.data.fakes.FakeMoviesLocalDataSource
import edu.dyds.movies.domain.entities.Movie
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MoviesRepositoryImplTest {

    private fun buildMovieItem(id: Int = 1) = Movie.MovieItem(
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
        val local = FakeMoviesLocalDataSource().apply {
            cachedMoviesReturn = listOf(buildMovie(1), buildMovie(2))
        }
        val moviesSource = FakeMoviesExternalSource()
        val movieSource = FakeMovieExternalSource()
        val repository = MoviesRepositoryImpl(moviesSource, movieSource, local)

        val result = repository.getPopularMovies()

        assertEquals(2, result.size)
        assertEquals(local.cachedMoviesReturn, result)
        assertEquals(0, moviesSource.getPopularMoviesInvocations)
    }

    @Test
    fun `getPopularMovies - when cache is empty and remote succeeds - returns remote movies and saves them`() = runTest {
        val moviesSource = FakeMoviesExternalSource().apply {
            popularMoviesReturn = listOf(buildMovieItem(10))
        }
        val movieSource = FakeMovieExternalSource()
        val local = FakeMoviesLocalDataSource()
        val repository = MoviesRepositoryImpl(moviesSource, movieSource, local)

        val result = repository.getPopularMovies()

        assertEquals(1, result.size)
        assertEquals(10, result.first().id)
        assertEquals(1, local.savedMovies.size)
    }

    @Test
    fun `getPopularMovies - when cache is empty and remote succeeds - passes image data from MovieItem`() = runTest {
        val moviesSource = FakeMoviesExternalSource().apply {
            popularMoviesReturn = listOf(buildMovieItem(1).copy(
                poster = "https://image.tmdb.org/t/p/w185/test_poster.jpg",
                backdrop = "https://image.tmdb.org/t/p/w780/test_backdrop.jpg"
            ))
        }
        val movieSource = FakeMovieExternalSource()
        val local = FakeMoviesLocalDataSource()
        val repository = MoviesRepositoryImpl(moviesSource, movieSource, local)

        val result = repository.getPopularMovies()

        assertEquals(1, result.size)
        val movie = result.first()
        assertEquals("https://image.tmdb.org/t/p/w185/test_poster.jpg", movie.poster)
        assertEquals("https://image.tmdb.org/t/p/w780/test_backdrop.jpg", movie.backdrop)
    }

    @Test
    fun `getPopularMovies - when cache is empty and remote fails - returns empty list`() = runTest {
        val moviesSource = FakeMoviesExternalSource().apply { shouldThrow = true }
        val movieSource = FakeMovieExternalSource()
        val local = FakeMoviesLocalDataSource()
        val repository = MoviesRepositoryImpl(moviesSource, movieSource, local)

        val result = repository.getPopularMovies()

        assertTrue(result.isEmpty())
        assertTrue(local.savedMovies.isEmpty())
    }

    @Test
    fun `getMovieDetails - when remote succeeds - returns mapped movie`() = runTest {
        val moviesSource = FakeMoviesExternalSource()
        val movieSource = FakeMovieExternalSource().apply {
            movieReturn = buildMovieItem(42)
        }
        val local = FakeMoviesLocalDataSource()
        val repository = MoviesRepositoryImpl(moviesSource, movieSource, local)

        val result = repository.getMovieDetails("Movie 42")

        assertNotNull(result)
        assertEquals(42, result.id)
        assertEquals("Movie 42", result.title)
        assertEquals(1, movieSource.getMovieByTitleInvocations)
        assertEquals("Movie 42", movieSource.lastQueriedTitle)
    }

    @Test
    fun `getMovieDetails - when remote succeeds - passes image data from MovieItem`() = runTest {
        val moviesSource = FakeMoviesExternalSource()
        val movieSource = FakeMovieExternalSource().apply {
            movieReturn = buildMovieItem(1).copy(
                poster = "https://image.tmdb.org/t/p/w185/detail_poster.jpg",
                backdrop = "https://image.tmdb.org/t/p/w780/detail_backdrop.jpg"
            )
        }
        val local = FakeMoviesLocalDataSource()
        val repository = MoviesRepositoryImpl(moviesSource, movieSource, local)

        val result = repository.getMovieDetails("Movie 1")

        assertNotNull(result)
        assertEquals("https://image.tmdb.org/t/p/w185/detail_poster.jpg", result.poster)
        assertEquals("https://image.tmdb.org/t/p/w780/detail_backdrop.jpg", result.backdrop)
    }

    @Test
    fun `getMovieDetails - when remote succeeds - returns null backdrop when not set`() = runTest {
        val moviesSource = FakeMoviesExternalSource()
        val movieSource = FakeMovieExternalSource().apply {
            movieReturn = buildMovieItem(1).copy(backdrop = null)
        }
        val local = FakeMoviesLocalDataSource()
        val repository = MoviesRepositoryImpl(moviesSource, movieSource, local)

        val result = repository.getMovieDetails("Movie 1")

        assertNotNull(result)
        assertNull(result.backdrop)
    }

    @Test
    fun `getMovieDetails - when remote fails - returns null`() = runTest {
        val moviesSource = FakeMoviesExternalSource()
        val movieSource = FakeMovieExternalSource().apply { shouldThrow = true }
        val local = FakeMoviesLocalDataSource()
        val repository = MoviesRepositoryImpl(moviesSource, movieSource, local)

        val result = repository.getMovieDetails("Movie 1")

        assertNull(result)
    }
}