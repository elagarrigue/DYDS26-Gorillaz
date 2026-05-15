package edu.dyds.movies.presentation.detail

import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.presentation.fakes.FakeGetMovieDetailsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has loading false and null movie`() = runTest {
        // arrange
        val useCase = FakeGetMovieDetailsUseCase()
        val viewModel = DetailViewModel(useCase)
        val states = mutableListOf<DetailViewModel.MovieDetailUiState>()

        // act
        testScope.launch { viewModel.movieDetailStateFlow.collect { states.add(it) } }

        // assert
        assertEquals(1, states.size)
        assertEquals(false, states[0].isLoading)
        assertNull(states[0].movie)
    }

    @Test
    fun `getMovieDetail should end with loading false and movie`() = runTest {
        // arrange
        val expected = createDefaultMovie(id = 42, title = "The Answer")
        val useCase = FakeGetMovieDetailsUseCase(movie = expected)
        val viewModel = DetailViewModel(useCase)
        val states = mutableListOf<DetailViewModel.MovieDetailUiState>()

        testScope.launch { viewModel.movieDetailStateFlow.collect { states.add(it) } }

        // act
        viewModel.getMovieDetail(42)

        // assert
        assertTrue(states.isNotEmpty())
        assertEquals(false, states.last().isLoading)
        assertNotNull(states.last().movie)
        assertEquals(expected.title, states.last().movie?.title)
    }

    @Test
    fun `getMovieDetail should invoke use case exactly once and pass correct id`() = runTest {
        // arrange
        val expected = createDefaultMovie()
        val useCase = FakeGetMovieDetailsUseCase(movie = expected)
        val viewModel = DetailViewModel(useCase)

        // act
        viewModel.getMovieDetail(42)

        // assert
        assertEquals(1, useCase.invocations)
        assertEquals(42, useCase.lastRequestedId)
    }

    @Test
    fun `getMovieDetail should end with null movie when use case returns null`() = runTest {
        // arrange
        val useCase = FakeGetMovieDetailsUseCase(movie = null)
        val viewModel = DetailViewModel(useCase)
        val states = mutableListOf<DetailViewModel.MovieDetailUiState>()

        testScope.launch { viewModel.movieDetailStateFlow.collect { states.add(it) } }

        // act
        viewModel.getMovieDetail(7)

        // assert
        assertTrue(states.isNotEmpty())
        assertEquals(false, states.last().isLoading)
        assertNull(states.last().movie)
    }

    @Test
    fun `getMovieDetail should emit loading true state before emitting movie`() = runTest {
        // arrange
        val useCase = FakeGetMovieDetailsUseCase(movie = createDefaultMovie())
        val viewModel = DetailViewModel(useCase)
        val states = mutableListOf<DetailViewModel.MovieDetailUiState>()

        testScope.launch { viewModel.movieDetailStateFlow.collect { states.add(it) } }

        // act
        viewModel.getMovieDetail(1)

        // assert
        assertTrue(states.size >= 2)
        val intermediateState = states[states.size - 2]
        assertEquals(true, intermediateState.isLoading)
        assertNull(intermediateState.movie)
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