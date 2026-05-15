package edu.dyds.movies.presentation.home

import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.domain.entities.QualifiedMovie
import edu.dyds.movies.presentation.fakes.FakeGetPopularMoviesUseCase
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TestHomeViewModel {

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
    fun `getAllMovies should end with loading false after invocation`() = runTest {
        // arrange
        val useCase = FakeGetPopularMoviesUseCase()
        val viewModel = HomeViewModel(useCase)
        val states = mutableListOf<HomeViewModel.MoviesUiState>()

        testScope.launch { viewModel.moviesStateFlow.collect { states.add(it) } }

        // act
        viewModel.getAllMovies()

        // assert
        assertTrue(states.isNotEmpty())
        assertEquals(false, states.last().isLoading)
    }

    @Test
    fun `getAllMovies should emit use case movies in final state`() = runTest {
        // arrange
        val movie = createDefaultQualifiedMovie()
        val useCase = FakeGetPopularMoviesUseCase(movies = listOf(movie))
        val viewModel = HomeViewModel(useCase)
        val states = mutableListOf<HomeViewModel.MoviesUiState>()

        testScope.launch { viewModel.moviesStateFlow.collect { states.add(it) } }

        // act
        viewModel.getAllMovies()

        // assert
        assertTrue(states.isNotEmpty())
        assertEquals(listOf(movie), states.last().movies)
    }

    @Test
    fun `getAllMovies should invoke use case exactly once`() = runTest {
        // arrange
        val useCase = FakeGetPopularMoviesUseCase()
        val viewModel = HomeViewModel(useCase)

        // act
        viewModel.getAllMovies()

        // assert
        assertEquals(1, useCase.invocations)
    }

    @Test
    fun `getAllMovies should emit multiple movies in final state`() = runTest {
        // arrange
        val movies = listOf(
            createDefaultQualifiedMovie(id = 1, title = "Movie 1"),
            createDefaultQualifiedMovie(id = 2, title = "Movie 2"),
            createDefaultQualifiedMovie(id = 3, title = "Movie 3")
        )
        val useCase = FakeGetPopularMoviesUseCase(movies = movies)
        val viewModel = HomeViewModel(useCase)
        val states = mutableListOf<HomeViewModel.MoviesUiState>()

        testScope.launch { viewModel.moviesStateFlow.collect { states.add(it) } }

        // act
        viewModel.getAllMovies()

        // assert
        assertTrue(states.isNotEmpty())
        assertEquals(movies, states.last().movies)
    }

    @Test
    fun `getAllMovies should emit empty list when use case returns empty`() = runTest {
        // arrange
        val useCase = FakeGetPopularMoviesUseCase(movies = emptyList())
        val viewModel = HomeViewModel(useCase)
        val states = mutableListOf<HomeViewModel.MoviesUiState>()

        testScope.launch { viewModel.moviesStateFlow.collect { states.add(it) } }

        // act
        viewModel.getAllMovies()

        // assert
        assertTrue(states.isNotEmpty())
        assertEquals(emptyList<QualifiedMovie>(), states.last().movies)
    }

    @Test
    fun `getAllMovies should emit loading true state before emitting movies`() = runTest {
        // arrange
        val useCase = FakeGetPopularMoviesUseCase(movies = listOf(createDefaultQualifiedMovie()))
        val viewModel = HomeViewModel(useCase)
        val states = mutableListOf<HomeViewModel.MoviesUiState>()

        testScope.launch { viewModel.moviesStateFlow.collect { states.add(it) } }

        // act
        viewModel.getAllMovies()

        // assert
        assertTrue(states.size >= 2)
        val intermediateState = states[states.size - 2]
        assertEquals(true, intermediateState.isLoading)
        assertTrue(intermediateState.movies.isEmpty())
    }

    @Test
    fun `initial state has loading false and empty movies`() = runTest {
        // arrange
        val useCase = FakeGetPopularMoviesUseCase()
        val viewModel = HomeViewModel(useCase)
        val states = mutableListOf<HomeViewModel.MoviesUiState>()

        // act
        testScope.launch { viewModel.moviesStateFlow.collect { states.add(it) } }

        // assert
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