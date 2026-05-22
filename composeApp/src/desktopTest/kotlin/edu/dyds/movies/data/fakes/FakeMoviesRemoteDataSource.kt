package edu.dyds.movies.data.fakes

import edu.dyds.movies.data.remote.MoviesRemoteDataSource
import edu.dyds.movies.data.external.tmdb.RemoteMovie
import edu.dyds.movies.data.external.tmdb.RemoteResult

class FakeMoviesRemoteDataSource : MoviesRemoteDataSource {
    var popularMoviesReturn: RemoteResult = RemoteResult(1, emptyList(), 0, 0)
    var movieDetailsReturn: RemoteMovie? = null
    var shouldThrow: Boolean = false
    var getPopularMoviesInvocations: Int = 0
    var getMovieByTitleInvocations: Int = 0
    var lastQueriedTitle: String? = null

    override suspend fun getPopularMovies(): RemoteResult {
        getPopularMoviesInvocations++
        if (shouldThrow) throw Exception("Network error")
        return popularMoviesReturn
    }

    override suspend fun getMovieDetails(id: Int): RemoteMovie {
        if (shouldThrow) throw Exception("Network error")
        return movieDetailsReturn ?: throw Exception("Not found")
    }

    override suspend fun getMovieByTitle(title: String): RemoteMovie {
        getMovieByTitleInvocations++
        lastQueriedTitle = title
        if (shouldThrow) throw Exception("Network error")
        return movieDetailsReturn ?: throw Exception("Not found")
    }
}
