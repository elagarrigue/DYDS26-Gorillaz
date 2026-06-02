package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entities.Movie
import edu.dyds.movies.domain.repository.MoviesRepository

class GetMovieDetailsUseCaseImpl(
    private val repository: MoviesRepository
) : GetMovieDetailsUseCase {
    override suspend operator fun invoke(title: String): Movie? {
        return repository.getMovieDetails(title)
    }
}
