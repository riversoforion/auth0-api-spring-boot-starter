package com.cyberscout.util.auth0;


import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cyberscout.util.auth0.Auth0Properties.ClientProperties;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;


@ToString
@Slf4j
public class Auth0ClientContext {

    private final ClientProperties props;
    private final AuthAPI authApi;
    @Getter
    private final String apiId;
    private DecodedJWT accessToken;
    private TokenHolder rawToken;
    private Instant tokenExpiration;
    @Getter
    private String audience;


    public static Auth0ClientContext buildFor(String apiId, ClientProperties props, AuthAPI authApi)
            throws Auth0Exception {

        Auth0ClientContext.preBuildCheck(apiId, props);
        return new Auth0ClientContext(apiId, props, authApi).init();
    }


    protected static void preBuildCheck(String apiId, ClientProperties props) {

        if (!props.isEnabled()) {
            throw new IllegalArgumentException("Client functionality is disabled");
        }
        if (!props.hasAudience(apiId)) {
            throw new IllegalArgumentException(String.format("No audience found for '%s'", apiId));
        }
    }


    protected Auth0ClientContext(String apiId, ClientProperties props, AuthAPI authApi) {

        this.apiId = apiId;
        this.props = props;
        this.authApi = authApi;
    }


    protected Auth0ClientContext init() throws Auth0Exception {

        this.audience = props.getAudience(this.apiId);
        if (props.isPreCache()) {
            this.cacheTokenInfo();
        }
        return this;
    }


    public DecodedJWT accessToken() throws Auth0Exception {

        if (this.needsTokenRefresh()) {
            this.cacheTokenInfo();
        }
        return this.accessToken;
    }


    public TokenHolder rawToken() throws Auth0Exception {

        if (this.needsTokenRefresh()) {
            this.cacheTokenInfo();
        }
        return this.rawToken;
    }


    public Instant tokenExpiration() throws Auth0Exception {

        if (this.needsTokenRefresh()) {
            this.cacheTokenInfo();
        }
        return this.tokenExpiration;
    }


    protected void cacheTokenInfo() throws Auth0Exception {

        this.rawToken = this.authApi.requestToken(this.audience).execute();
        this.accessToken = JWT.decode(this.rawToken.getAccessToken());
        long validForSeconds = this.rawToken.getExpiresIn();
        this.tokenExpiration = Instant.now().plusSeconds(validForSeconds);
    }


    protected boolean needsTokenRefresh() {

        return this.tokenExpiration == null || this.tokenExpiration.isBefore(Instant.now());
    }
}
