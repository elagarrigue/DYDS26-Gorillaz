package edu.dyds.movies.presentation.fakes

import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCase

class FakeGetMovieDetailsUseCase(
    private val movie: Movie? = null
) : GetMovieDetailsUseCase {
    var invocations = 0
    var lastRequestedId: Int? = null

    override suspend fun invoke(id: Int): Movie? {
        invocations++
        lastRequestedId = id
        kotlinx.coroutines.yield()
        return movie
    }
}
