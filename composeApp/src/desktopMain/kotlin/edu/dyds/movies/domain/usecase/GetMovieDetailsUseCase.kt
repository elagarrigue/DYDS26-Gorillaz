package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entities.Movie

interface GetMovieDetailsUseCase {
    suspend operator fun invoke(id: Int): Movie?
}
