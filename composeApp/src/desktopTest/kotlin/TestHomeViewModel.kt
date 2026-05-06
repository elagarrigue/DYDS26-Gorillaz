import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.domain.entities.QualifiedMovie
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCase
import edu.dyds.movies.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TestHomeViewModel {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    private class GetPopularMoviesUseCaseFake(
        private val movies: List<QualifiedMovie> = emptyList()
    ) : GetPopularMoviesUseCase {
        var invocations = 0

        override suspend fun invoke(): List<QualifiedMovie> {
            invocations++
            return movies
        }
    }

    @Test
    fun `getAllMovies should end with loading false after invocation`() = runTest(testDispatcher) {
        val useCase = GetPopularMoviesUseCaseFake()
        val viewModel = HomeViewModel(useCase)
        val states = mutableListOf<HomeViewModel.MoviesUiState>()

        val collectJob = launch {
            viewModel.moviesStateFlow.collect { states.add(it) }
        }

        advanceUntilIdle()

        viewModel.getAllMovies()

        advanceUntilIdle()

        collectJob.cancel()

        assertTrue(states.isNotEmpty())
        assertEquals(false, states.last().isLoading)
    }

    @Test
    fun `getAllMovies should emit use case movies in final state`() = runTest(testDispatcher) {
        val movie = createDefaultQualifiedMovie()
        val useCase = GetPopularMoviesUseCaseFake(movies = listOf(movie))
        val viewModel = HomeViewModel(useCase)
        val states = mutableListOf<HomeViewModel.MoviesUiState>()

        val collectJob = launch {
            viewModel.moviesStateFlow.collect { states.add(it) }
        }

        advanceUntilIdle()

        viewModel.getAllMovies()

        advanceUntilIdle()

        collectJob.cancel()

        assertTrue(states.isNotEmpty())
        assertEquals(listOf(movie), states.last().movies)
    }

    @Test
    fun `getAllMovies should invoke use case exactly once`() = runTest(testDispatcher) {
        val useCase = GetPopularMoviesUseCaseFake()
        val viewModel = HomeViewModel(useCase)

        viewModel.getAllMovies()

        advanceUntilIdle()

        assertEquals(1, useCase.invocations)
    }

    @Test
    fun `getAllMovies should emit multiple movies in final state`() = runTest(testDispatcher) {
        val movies = listOf(
            createDefaultQualifiedMovie(id = 1, title = "Movie 1"),
            createDefaultQualifiedMovie(id = 2, title = "Movie 2"),
            createDefaultQualifiedMovie(id = 3, title = "Movie 3")
        )
        val useCase = GetPopularMoviesUseCaseFake(movies = movies)
        val viewModel = HomeViewModel(useCase)
        val states = mutableListOf<HomeViewModel.MoviesUiState>()

        val collectJob = launch {
            viewModel.moviesStateFlow.collect { states.add(it) }
        }

        advanceUntilIdle()

        viewModel.getAllMovies()

        advanceUntilIdle()

        collectJob.cancel()

        assertTrue(states.isNotEmpty())
        assertEquals(movies, states.last().movies)
    }

    @Test
    fun `getAllMovies should emit empty list when use case returns empty`() = runTest(testDispatcher) {
        val useCase = GetPopularMoviesUseCaseFake(movies = emptyList())
        val viewModel = HomeViewModel(useCase)
        val states = mutableListOf<HomeViewModel.MoviesUiState>()

        val collectJob = launch {
            viewModel.moviesStateFlow.collect { states.add(it) }
        }

        advanceUntilIdle()

        viewModel.getAllMovies()

        advanceUntilIdle()

        collectJob.cancel()

        assertTrue(states.isNotEmpty())
        assertEquals(emptyList<QualifiedMovie>(), states.last().movies)
    }

    @Test
    fun `initial state has loading false and empty movies`() = runTest(testDispatcher) {
        val useCase = GetPopularMoviesUseCaseFake()
        val viewModel = HomeViewModel(useCase)
        val states = mutableListOf<HomeViewModel.MoviesUiState>()

        val collectJob = launch {
            viewModel.moviesStateFlow.collect { states.add(it) }
        }

        advanceUntilIdle()

        collectJob.cancel()

        assertEquals(1, states.size)
        assertEquals(false, states[0].isLoading)
        assertEquals(emptyList<QualifiedMovie>(), states[0].movies)
    }

    private fun createDefaultQualifiedMovie(
        id: Int = 1,
        title: String = "Test Movie",
        isGood: Boolean = true
    ): QualifiedMovie {
        val movie = Movie(
            id = id,
            title = title,
            overview = "Test overview",
            releaseDate = "2024-01-01",
            poster = "poster.jpg",
            backdrop = null,
            originalTitle = title,
            originalLanguage = "en",
            popularity = 8.5,
            voteAverage = 7.5
        )
        return QualifiedMovie(movie, isGood)
    }
}

