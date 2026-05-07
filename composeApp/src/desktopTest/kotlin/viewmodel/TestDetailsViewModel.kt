package viewmodel

import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCase
import edu.dyds.movies.presentation.detail.DetailViewModel
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    private class GetMovieDetailsUseCaseFake(
        private val movie: Movie? = null
    ) : GetMovieDetailsUseCase {
        var invocations = 0

        override suspend fun invoke(id: Int): Movie? {
            invocations++
            return movie
        }
    }

    @Test
    fun `initial state has loading false and null movie`() = runTest(testDispatcher) {
        val useCase = GetMovieDetailsUseCaseFake()
        val viewModel = DetailViewModel(useCase)
        val states = mutableListOf<DetailViewModel.MovieDetailUiState>()

        val collectJob = launch {
            viewModel.movieDetailStateFlow.collect { states.add(it) }
        }

        advanceUntilIdle()

        collectJob.cancel()

        assertEquals(1, states.size)
        assertEquals(false, states[0].isLoading)
        assertNull(states[0].movie)
    }

    @Test
    fun `getMovieDetail should end with loading false and movie`() = runTest(testDispatcher) {
        val expected = createDefaultMovie(id = 42, title = "The Answer")
        val useCase = GetMovieDetailsUseCaseFake(movie = expected)
        val viewModel = DetailViewModel(useCase)
        val states = mutableListOf<DetailViewModel.MovieDetailUiState>()

        val collectJob = launch {
            viewModel.movieDetailStateFlow.collect { states.add(it) }
        }

        advanceUntilIdle()

        viewModel.getMovieDetail(42)

        advanceUntilIdle()

        collectJob.cancel()

        assertTrue(states.isNotEmpty())
        assertEquals(false, states.last().isLoading)
        assertNotNull(states.last().movie)
        assertEquals(expected.title, states.last().movie?.title)
    }

    @Test
    fun `getMovieDetail should invoke use case exactly once`() = runTest(testDispatcher) {
        val expected = createDefaultMovie()
        val useCase = GetMovieDetailsUseCaseFake(movie = expected)
        val viewModel = DetailViewModel(useCase)

        viewModel.getMovieDetail(1)

        advanceUntilIdle()

        assertEquals(1, useCase.invocations)
    }

    @Test
    fun `getMovieDetail should end with null movie when use case returns null`() = runTest(testDispatcher) {
        val useCase = GetMovieDetailsUseCaseFake(movie = null)
        val viewModel = DetailViewModel(useCase)
        val states = mutableListOf<DetailViewModel.MovieDetailUiState>()

        val collectJob = launch {
            viewModel.movieDetailStateFlow.collect { states.add(it) }
        }

        advanceUntilIdle()

        viewModel.getMovieDetail(7)

        advanceUntilIdle()

        collectJob.cancel()

        assertTrue(states.isNotEmpty())
        assertEquals(false, states.last().isLoading)
        assertNull(states.last().movie)
    }

    private fun createDefaultMovie(
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