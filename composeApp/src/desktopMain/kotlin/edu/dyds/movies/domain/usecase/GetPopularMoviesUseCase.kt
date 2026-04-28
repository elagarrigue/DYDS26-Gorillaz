package edu.dyds.movies.domain.usecase

import edu.dyds.movies.domain.entities.QualifiedMovie

interface GetPopularMoviesUseCase {
    suspend operator fun invoke(): List<QualifiedMovie>
}
