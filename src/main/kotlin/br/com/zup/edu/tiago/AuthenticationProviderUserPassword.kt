package br.com.zup.edu.tiago

import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import org.reactivestreams.Publisher
import javax.inject.Singleton

// To keep it simple, AuthenticationProvider to simulate user’s authentication.
@Singleton
class AuthenticationProviderUserPassword: AuthenticationProvider {
    override fun authenticate(
        httpRequest: HttpRequest<*>?,
        authenticationRequest: AuthenticationRequest<*, *>?
    ): Publisher<AuthenticationResponse> {
        return Flowable.create({emitter: FlowableEmitter<AuthenticationResponse> ->
            if(authenticationRequest?.identity == "user" && authenticationRequest.secret == "password"){
                emitter.onNext(UserDetails(authenticationRequest.identity as String, ArrayList()))
                emitter.onComplete()
            } else {
                emitter.onError(AuthenticationException(AuthenticationFailed()))
            }
        }, BackpressureStrategy.ERROR)
    }

}