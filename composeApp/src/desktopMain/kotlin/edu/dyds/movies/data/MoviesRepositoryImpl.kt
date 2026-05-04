package edu.dyds.movies.data

import edu.dyds.movies.data.local.MoviesLocalDataSource
import edu.dyds.movies.data.remote.MoviesRemoteDataSource
import edu.dyds.movies.data.remote.toDomainMovie
import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class MoviesRepositoryImpl(
    private val remoteDataSource: MoviesRemoteDataSource,
    private val localDataSource: MoviesLocalDataSource
) : MoviesRepository {

    override suspend fun getPopularMovies(): List<Movie> {
        val cachedMovies = localDataSource.getCachedMovies()
        if (cachedMovies.isNotEmpty()) {
            return cachedMovies
        }
        return try {
            val result = remoteDataSource.getPopularMovies()
            val movies = result.results.map { it.toDomainMovie() }
            localDataSource.saveMovies(movies)
            movies
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getMovieDetails(id: Int): Movie? {
        return try {
            remoteDataSource.getMovieDetails(id).toDomainMovie()
        } catch (e: Exception) {
            null
        }
    }
}
