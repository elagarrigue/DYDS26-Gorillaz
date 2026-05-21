package edu.dyds.movies.presentation.fakes

import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.domain.usecase.GetMovieDetailsUseCase

class FakeGetMovieDetailsUseCase(
    private val movie: Movie? = null
) : GetMovieDetailsUseCase {
    var invocations = 0
    var lastRequestedTitle: String? = null

    override suspend fun invoke(title: String): Movie? {
        invocations++
        lastRequestedTitle = title
        kotlinx.coroutines.yield()
        return movie
    }
}
