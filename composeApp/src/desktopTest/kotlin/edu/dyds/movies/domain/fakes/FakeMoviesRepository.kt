package edu.dyds.movies.domain.fakes

import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class FakeMoviesRepository(
    private val moviesToReturn: List<Movie> = emptyList(),
    private val movieToReturn: Movie? = null
) : MoviesRepository {
    var getPopularMoviesInvocations = 0
    var getMovieDetailsInvocations = 0
    var lastRequestedId: Int? = null

    override suspend fun getPopularMovies(): List<Movie> {
        getPopularMoviesInvocations++
        return moviesToReturn
    }

    override suspend fun getMovieDetails(id: Int): Movie? {
        getMovieDetailsInvocations++
        lastRequestedId = id
        return movieToReturn
    }
}
