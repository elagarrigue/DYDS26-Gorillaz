package edu.dyds.movies.data.fakes

import edu.dyds.movies.data.remote.MoviesRemoteDataSource
import edu.dyds.movies.data.remote.RemoteMovie
import edu.dyds.movies.data.remote.RemoteResult

class FakeMoviesRemoteDataSource : MoviesRemoteDataSource {
    var popularMoviesReturn: RemoteResult = RemoteResult(1, emptyList(), 0, 0)
    var movieDetailsReturn: RemoteMovie? = null
    var shouldThrow: Boolean = false
    var getPopularMoviesInvocations: Int = 0

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
        if (shouldThrow) throw Exception("Network error")
        return movieDetailsReturn ?: throw Exception("Not found")
    }
}
