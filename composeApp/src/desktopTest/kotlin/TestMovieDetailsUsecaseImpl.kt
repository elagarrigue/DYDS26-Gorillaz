import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.domain.repository.MoviesRepository
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCaseImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestMovieDetailsUsecaseImpl {

    private class MoviesRepositoryFake(
        private val movieToReturn: Movie? = null
    ) : MoviesRepository {
        var getMovieDetailsInvocations = 0
        var lastRequestedId: Int? = null

        override suspend fun getPopularMovies(): List<Movie> {
            return emptyList()
        }

        override suspend fun getMovieDetails(id: Int): Movie? {
            getMovieDetailsInvocations++
            lastRequestedId = id
            return movieToReturn
        }
    }

    @Test
    fun `invoke should return movie from repository`() = runTest {
        val expectedMovie = createMovie(id = 10, title = "Inception")
        val repository = MoviesRepositoryFake(movieToReturn = expectedMovie)
        val useCase = GetMovieDetailsUseCaseImpl(repository)

        val result = useCase(10)

        assertEquals(expectedMovie, result)
    }

    @Test
    fun `invoke should return null when repository has no movie`() = runTest {
        val repository = MoviesRepositoryFake(movieToReturn = null)
        val useCase = GetMovieDetailsUseCaseImpl(repository)

        val result = useCase(99)

        assertNull(result)
    }

    @Test
    fun `invoke should request repository with provided id and only once`() = runTest {
        val repository = MoviesRepositoryFake(movieToReturn = createMovie())
        val useCase = GetMovieDetailsUseCaseImpl(repository)

        useCase(42)

        assertEquals(1, repository.getMovieDetailsInvocations)
        assertEquals(42, repository.lastRequestedId)
    }

    private fun createMovie(
        id: Int = 1,
        title: String = "Test Movie"
    ): Movie {
        return Movie(
            id = id,
            title = title,
            overview = "overview",
            releaseDate = "2024-01-01",
            poster = "poster.jpg",
            backdrop = null,
            originalTitle = title,
            originalLanguage = "en",
            popularity = 1.0,
            voteAverage = 1.0
        )
    }
}