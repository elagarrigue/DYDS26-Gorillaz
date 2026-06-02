package edu.dyds.movies.data

import edu.dyds.movies.data.external.MovieExternalSource
import edu.dyds.movies.data.external.MoviesExternalSource
import edu.dyds.movies.data.local.MoviesLocalDataSource
import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class MoviesRepositoryImpl(
    private val moviesExternalSource: MoviesExternalSource,
    private val movieExternalSource: MovieExternalSource,
    private val localDataSource: MoviesLocalDataSource
) : MoviesRepository {

    override suspend fun getPopularMovies(): List<Movie> {
        val cachedMovies = localDataSource.getCachedMovies()
        if (cachedMovies.isNotEmpty()) {
            return cachedMovies
        }
        return try {
            val movies = moviesExternalSource.getPopularMovies().map { it.toMovie() }
            localDataSource.saveMovies(movies)
            movies
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getMovieDetails(title: String): Movie? {
        return try {
            movieExternalSource.getMovieByTitle(title)?.toMovie()
        } catch (e: Exception) {
            null
        }
    }
}

private fun Movie.MovieItem.toMovie(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    releaseDate = releaseDate,
    poster = poster,
    backdrop = backdrop,
    originalTitle = originalTitle,
    originalLanguage = originalLanguage,
    popularity = popularity,
    voteAverage = voteAverage
)