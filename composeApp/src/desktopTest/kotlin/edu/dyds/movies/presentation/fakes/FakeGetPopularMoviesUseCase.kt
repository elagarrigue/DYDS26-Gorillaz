package edu.dyds.movies.presentation.fakes

import edu.dyds.movies.domain.entities.QualifiedMovie
import edu.dyds.movies.domain.usecase.GetPopularMoviesUseCase

class FakeGetPopularMoviesUseCase(
    private val movies: List<QualifiedMovie> = emptyList()
) : GetPopularMoviesUseCase {
    var invocations = 0

    override suspend fun invoke(): List<QualifiedMovie> {
        invocations++
        kotlinx.coroutines.yield()
        return movies
    }
}
