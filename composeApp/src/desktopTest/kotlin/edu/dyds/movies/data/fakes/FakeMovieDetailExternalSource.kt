package edu.dyds.movies.data.fakes

import edu.dyds.movies.data.external.MovieExternalSource
import edu.dyds.movies.domain.entities.Movie

class FakeMovieDetailExternalSource : MovieExternalSource {
    var movieReturn: Movie.MovieItem? = null
    var shouldThrow: Boolean = false
    var getMovieByTitleInvocations: Int = 0
    var lastQueriedTitle: String? = null

    override suspend fun getMovieByTitle(title: String): Movie.MovieItem? {
        getMovieByTitleInvocations++
        lastQueriedTitle = title
        if (shouldThrow) throw Exception("Network error")
        return movieReturn
    }
}