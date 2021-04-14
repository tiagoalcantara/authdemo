package br.com.zup.edu.tiago

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.function.Executable
import javax.inject.Inject

@MicronautTest
internal class HomeControllerTest{
    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun `shoud return unauthorized on secured url without authentication`() {
        val thrown = assertThrows (HttpClientResponseException::class.java) {
            client.toBlocking().exchange<Any, Any>(HttpRequest.GET<Any>(MediaType.TEXT_PLAIN))
        }
        assertEquals(thrown.status, HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `should return jwt on successful authentication`() {
        val creds = UsernamePasswordCredentials("user", "password")
        val request: HttpRequest<Any> = HttpRequest.POST("/login", creds)
        val rsp: HttpResponse<BearerAccessRefreshToken> = client.toBlocking().exchange(request, BearerAccessRefreshToken::class.java)
        assertEquals(HttpStatus.OK, rsp.status)

        val bearerAccessRefreshToken: BearerAccessRefreshToken = rsp.body()!!

        assertEquals("user", bearerAccessRefreshToken.username)
        assertNotNull(bearerAccessRefreshToken.accessToken)
        assertTrue(JWTParser.parse(bearerAccessRefreshToken.accessToken) is SignedJWT)

        val accessToken: String = bearerAccessRefreshToken.accessToken
        val response: HttpResponse<String> = client.toBlocking().exchange(HttpRequest.GET<Any>("/")
            .accept(MediaType.TEXT_PLAIN).bearerAuth(accessToken), String::class.java)

        assertEquals(HttpStatus.OK, rsp.status)
        assertEquals("Bem-vindo user!", response.body())
    }
}