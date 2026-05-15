package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.domain.entities.QualifiedMovie
import edu.dyds.movies.domain.fakes.FakeMoviesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestPopularMoviesUseCaseImpl {

    @Test
    fun `invoke should return empty list when repository returns empty`() = runTest {
        // arrange
        val repository = FakeMoviesRepository(moviesToReturn = emptyList())
        val useCase = GetPopularMoviesUseCaseImpl(repository)

        // act
        val result = useCase()

        // assert
        assertEquals(emptyList<QualifiedMovie>(), result)
    }

    @Test
    fun `invoke should handle single movie list correctly`() = runTest {
        // arrange
        val movie = createMovie(id = 1, voteAverage = 7.5)
        val repository = FakeMoviesRepository(moviesToReturn = listOf(movie))
        val useCase = GetPopularMoviesUseCaseImpl(repository)

        // act
        val result = useCase()

        // assert
        assertEquals(1, result.size)
        assertEquals(7.5, result[0].movie.voteAverage)
        assertEquals(true, result[0].isGoodMovie)
    }

    @Test
    fun `invoke should return movies sorted descending by vote average`() = runTest {
        // arrange
        val movies = listOf(
            createMovie(id = 1, title = "Movie 1", voteAverage = 5.0),
            createMovie(id = 2, title = "Movie 2", voteAverage = 8.5),
            createMovie(id = 3, title = "Movie 3", voteAverage = 7.0)
        )
        val repository = FakeMoviesRepository(moviesToReturn = movies)
        val useCase = GetPopularMoviesUseCaseImpl(repository)

        // act
        val result = useCase()

        // assert
        assertEquals(8.5, result[0].movie.voteAverage)
        assertEquals(7.0, result[1].movie.voteAverage)
        assertEquals(5.0, result[2].movie.voteAverage)
    }

    @Test
    fun `invoke should mark movie as good when vote average is greater than or equal to 6 dot 0`() = runTest {
        // arrange
        val movies = listOf(
            createMovie(id = 1, voteAverage = 6.0),
            createMovie(id = 2, voteAverage = 7.5),
            createMovie(id = 3, voteAverage = 9.9)
        )
        val repository = FakeMoviesRepository(moviesToReturn = movies)
        val useCase = GetPopularMoviesUseCaseImpl(repository)

        // act
        val result = useCase()

        // assert
        assertTrue(result.all { it.isGoodMovie })
    }

    @Test
    fun `invoke should mark movie as bad when vote average is less than 6 dot 0`() = runTest {
        // arrange
        val movies = listOf(
            createMovie(id = 1, voteAverage = 5.9),
            createMovie(id = 2, voteAverage = 3.0),
            createMovie(id = 3, voteAverage = 0.1)
        )
        val repository = FakeMoviesRepository(moviesToReturn = movies)
        val useCase = GetPopularMoviesUseCaseImpl(repository)

        // act
        val result = useCase()

        // assert
        assertTrue(result.none { it.isGoodMovie })
    }

    @Test
    fun `invoke should correctly classify movies with vote average at threshold boundary`() = runTest {
        // arrange
        val goodMovie = createMovie(id = 1, voteAverage = 6.0)
        val badMovie = createMovie(id = 2, voteAverage = 5.999)
        val repository = FakeMoviesRepository(moviesToReturn = listOf(goodMovie, badMovie))
        val useCase = GetPopularMoviesUseCaseImpl(repository)

        // act
        val result = useCase()

        // assert
        assertEquals(true, result[0].isGoodMovie)
        assertEquals(false, result[1].isGoodMovie)
    }

    @Test
    fun `invoke should transform all movies to qualified movies`() = runTest {
        // arrange
        val movies = listOf(
            createMovie(id = 1, title = "Movie 1", voteAverage = 8.0),
            createMovie(id = 2, title = "Movie 2", voteAverage = 5.0),
            createMovie(id = 3, title = "Movie 3", voteAverage = 7.0)
        )
        val repository = FakeMoviesRepository(moviesToReturn = movies)
        val useCase = GetPopularMoviesUseCaseImpl(repository)

        // act
        val result = useCase()

        // assert
        assertEquals(3, result.size)
        assertTrue(result.all { it.movie in movies })
    }

    @Test
    fun `invoke should invoke repository exactly once`() = runTest {
        // arrange
        val repository = FakeMoviesRepository(moviesToReturn = emptyList())
        val useCase = GetPopularMoviesUseCaseImpl(repository)

        // act
        useCase()

        // assert
        assertEquals(1, repository.getPopularMoviesInvocations)
    }

    @Test
    fun `invoke should maintain movie data integrity when transforming to qualified movies`() = runTest {
        // arrange
        val originalMovie = createMovie(
            id = 42,
            title = "Inception",
            overview = "A dream within a dream",
            releaseDate = "2010-07-16",
            poster = "inception.jpg",
            backdrop = "inception_backdrop.jpg",
            originalTitle = "Inception",
            originalLanguage = "en",
            popularity = 15.5,
            voteAverage = 8.8
        )
        val repository = FakeMoviesRepository(moviesToReturn = listOf(originalMovie))
        val useCase = GetPopularMoviesUseCaseImpl(repository)

        // act
        val result = useCase()

        // assert
        assertEquals(1, result.size)
        assertEquals(originalMovie, result[0].movie)
    }

    private fun createMovie(
        id: Int = 1,
        title: String = "Test Movie",
        overview: String = "Test overview",
        releaseDate: String = "2024-01-01",
        poster: String = "poster.jpg",
        backdrop: String? = null,
        originalTitle: String = "Test Movie",
        originalLanguage: String = "en",
        popularity: Double = 1.0,
        voteAverage: Double = 5.0
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

