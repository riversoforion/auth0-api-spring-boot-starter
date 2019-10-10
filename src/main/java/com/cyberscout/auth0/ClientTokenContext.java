package com.cyberscout.auth0;


import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cyberscout.auth0.Auth0Properties.ClientProperties;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;


/**
 * <p>
 * An object that encapsulates the context of invoking external, Auth0-secured
 * APIs. Primarily, it manages the access token, which can then plugged into
 * HTTP requests to the external API.
 * </p>
 * <p>
 * The context object will efficiently handle access tokens by caching them
 * until they expire, and then transparently acquiring a new one. It respects
 * the system's {@linkplain ClientProperties#isPreCache() pre-cache} value by
 * acquiring a token at system startup, if pre-cache is set to {@code true}.
 * Otherwise, it will acquire and cache the tokens the first time they are
 * accessed.
 * </p>
 */
@ToString(onlyExplicitlyIncluded = true)
@Slf4j
// TODO Unit test me!
// TODO Integration test me!
public class ClientTokenContext {

    private final ClientProperties props;
    private final AuthAPI authApi;
    @Getter
    @ToString.Include
    private final String apiId;
    private DecodedJWT accessToken;
    private TokenHolder tokenInfo;
    private Instant tokenExpiration;
    @Getter
    @ToString.Include(rank = 1)
    private String audience;


    /**
     * Creates a new context object, ensuring that it is properly initialized.
     * The initial token is cached, if configured.
     *
     * @param apiId The
     *         {@linkplain ClientProperties#getAudience() API identifier} to
     *         create a context around
     * @param props The Auth0 client properties to use when constructing the
     *         context
     * @param authApi The Auth0 Authentication API wrapper object
     * @return The fully initialized client context
     * @throws Auth0Exception If there is a problem retrieving the access token
     * @throws IllegalArgumentException If the system is not properly configured
     *         as a client context, or if the given API identifier is invalid
     */
    public static ClientTokenContext buildFor(String apiId, ClientProperties props, AuthAPI authApi)
            throws Auth0Exception {

        log.debug("Building context for client '{}'", apiId);
        ClientTokenContext.preBuildCheck(apiId, props);
        return new ClientTokenContext(apiId, props, authApi).init();
    }


    /**
     * Checks the configuration to be sure it is valid for a client context. If
     * not, an exception will be thrown.
     *
     * @param apiId The
     *         {@linkplain ClientProperties#getAudience() API identifier} to
     *         create a context around
     * @param props The Auth0 client properties to use when constructing the
     *         context
     */
    protected static void preBuildCheck(String apiId, ClientProperties props) {

        if (!props.isAuthenticationEnabled()) {
            throw new IllegalArgumentException("Client functionality is disabled");
        }
        if (!props.hasAudience(apiId)) {
            throw new IllegalArgumentException(String.format("No audience found for '%s'", apiId));
        }
    }


    /**
     * Constructs a raw client context object. The context will not be
     * completely initialized by the constructor. Must only be invoked by
     * sub-classes, as part of the initialization process.
     *
     * @param apiId The
     *         {@linkplain ClientProperties#getAudience() API identifier} to
     *         create a context around
     * @param props The Auth0 client properties to use when constructing the
     *         context
     * @param authApi The Auth0 Authentication API wrapper object
     * @see #init()
     */
    protected ClientTokenContext(String apiId, ClientProperties props, AuthAPI authApi) {

        this.apiId = apiId;
        this.props = props;
        this.authApi = authApi;
    }


    /**
     * Finishes initializing the context, including caching tokens, if needed.
     *
     * @return The fully-initialized client context
     * @throws Auth0Exception If there is a problem retrieving the access token
     */
    protected ClientTokenContext init() throws Auth0Exception {

        log.debug("Initializing context for client '{}'", this.apiId);
        this.audience = props.getAudience(this.apiId);
        if (props.isPreCache()) {
            this.cacheTokenInfo();
        }
        log.debug("Finished initializing context for client '{}'", this.apiId);
        return this;
    }


    /**
     * Retrieves the decoded access token. Declared as a method, rather than an
     * accessor, to indicate that it has side effects (potentially retrieving a
     * new token).
     *
     * @return The decoded access token
     * @throws Auth0Exception If there was a problem retrieving a new access
     *         token
     */
    public DecodedJWT accessToken() throws Auth0Exception {

        this.cacheNewTokenIfNeeded();
        return this.accessToken;
    }


    /**
     * Retrieves information about this context's current token. Declared as a
     * method, rather than an accessor, to indicate that it has side effects
     * (potentially retrieving a new token).
     *
     * @return An object containing information about the token
     * @throws Auth0Exception If there was a problem retrieving a new access
     *         token
     */
    public TokenHolder tokenInfo() throws Auth0Exception {

        this.cacheNewTokenIfNeeded();
        return this.tokenInfo;
    }


    /**
     * Retrieves the time that this context's current token expires. Declared as
     * a method, rather than an accessor, to indicate that it has side effects
     * (potentially retrieving a new token).
     *
     * @return The instant when the current token will expire
     * @throws Auth0Exception If there was a problem retrieving a new access
     *         token
     */
    public Instant tokenExpiration() throws Auth0Exception {

        this.cacheNewTokenIfNeeded();
        return this.tokenExpiration;
    }


    /**
     * Requests a new token from Auth0 and caches it.
     *
     * @throws Auth0Exception If there was a problem retrieving a new access
     *         token
     */
    protected void cacheTokenInfo() throws Auth0Exception {

        // TODO Make this all thread-safe
        log.debug("Caching new token for client '{}'", this.apiId);
        this.tokenInfo = this.authApi.requestToken(this.audience).execute();
        this.accessToken = JWT.decode(this.tokenInfo.getAccessToken());
        long validForSeconds = this.tokenInfo.getExpiresIn();
        this.tokenExpiration = Instant.now().plusSeconds(validForSeconds);
        log.debug("New token successfully cached for client '{}'", this.apiId);
    }


    /**
     * Determines whether this context needs a new token, and caches it if so.
     *
     * @throws Auth0Exception If there was a problem retrieving a new access
     *         token
     */
    protected void cacheNewTokenIfNeeded() throws Auth0Exception {

        if (this.needsTokenRefresh()) {
            this.cacheTokenInfo();
        }
    }


    private boolean needsTokenRefresh() {

        return this.tokenExpiration == null || this.tokenExpiration.isBefore(Instant.now());
    }
}
