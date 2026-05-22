package edu.dyds.movies.data.external.omdb

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class OMDBMoviesExternalSourceTest {

    private fun buildClient(jsonBody: String): HttpClient =
        HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = jsonBody,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

    @Test
    fun `getMovieByTitle returns correctly mapped MovieItem with normal data`() = runTest {
        val client = buildClient("""
            {
                "Title": "Inception",
                "Plot": "A thief who steals corporate secrets.",
                "Released": "16 Jul 2010",
                "Year": "2010",
                "Poster": "https://poster.jpg",
                "Language": "English",
                "Metascore": "74",
                "imdbRating": "8.8"
            }
        """.trimIndent())
        val source = OMDBMoviesExternalSource(client)

        val result = source.getMovieByTitle("Inception")

        assertEquals("Inception".hashCode(), result.id)
        assertEquals("Inception", result.title)
        assertEquals("A thief who steals corporate secrets.", result.overview)
        assertEquals("16 Jul 2010", result.releaseDate)
        assertEquals("https://poster.jpg", result.poster)
        assertEquals("https://poster.jpg", result.backdrop)
        assertEquals("Inception", result.originalTitle)
        assertEquals("English", result.originalLanguage)
        assertEquals(8.8, result.popularity)
        assertEquals(74.0, result.voteAverage)
    }

    @Test
    fun `getMovieByTitle returns 0_0 for popularity and voteAverage when imdbRating and Metascore are N_A`() = runTest {
        val client = buildClient("""
            {
                "Title": "Unknown",
                "Plot": "Some plot.",
                "Released": "01 Jan 2020",
                "Year": "2020",
                "Poster": "https://poster.jpg",
                "Language": "English",
                "Metascore": "N/A",
                "imdbRating": "N/A"
            }
        """.trimIndent())
        val source = OMDBMoviesExternalSource(client)

        val result = source.getMovieByTitle("Unknown")

        assertEquals(0.0, result.popularity)
        assertEquals(0.0, result.voteAverage)
    }

    @Test
    fun `getMovieByTitle uses year when released is empty or N_A`() = runTest {
        val clientNa = buildClient("""
            {
                "Title": "Old Movie",
                "Plot": "Some plot.",
                "Released": "N/A",
                "Year": "1990",
                "Poster": "https://poster.jpg",
                "Language": "English",
                "Metascore": "60",
                "imdbRating": "7.0"
            }
        """.trimIndent())
        val clientEmpty = buildClient("""
            {
                "Title": "Old Movie",
                "Plot": "Some plot.",
                "Released": "",
                "Year": "1990",
                "Poster": "https://poster.jpg",
                "Language": "English",
                "Metascore": "60",
                "imdbRating": "7.0"
            }
        """.trimIndent())

        val resultNa = OMDBMoviesExternalSource(clientNa).getMovieByTitle("Old Movie")
        val resultEmpty = OMDBMoviesExternalSource(clientEmpty).getMovieByTitle("Old Movie")

        assertEquals("1990", resultNa.releaseDate)
        assertEquals("1990", resultEmpty.releaseDate)
    }

    @Test
    fun `getMovieByTitle uses released when it is a valid date`() = runTest {
        val client = buildClient("""
            {
                "Title": "Recent Movie",
                "Plot": "Some plot.",
                "Released": "15 Mar 2023",
                "Year": "2023",
                "Poster": "https://poster.jpg",
                "Language": "English",
                "Metascore": "80",
                "imdbRating": "7.5"
            }
        """.trimIndent())
        val source = OMDBMoviesExternalSource(client)

        val result = source.getMovieByTitle("Recent Movie")

        assertEquals("15 Mar 2023", result.releaseDate)
    }
}