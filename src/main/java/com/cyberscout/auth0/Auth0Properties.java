package com.cyberscout.auth0;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * A type-safe properties object for handling the Auth0 configuration
 * properties. All properties begin with {@link #AUTH0_PREFIX auth0}.
 */
@ConfigurationProperties(Auth0Properties.AUTH0_PREFIX)
@Getter
@Setter
public class Auth0Properties {

    public static final String AUTH0_PREFIX = "auth0";
    private static final int MASK_LENGTH = 3;

    /**
     * The Auth0 domain (the full tenant URL). Usually of the form:
     * {@code https://[tenant ID].auth0.com}, but might also be a custom domain.
     * <p>
     * <strong><em>IMPORTANT:</em></strong> This property controls
     * auto-configuration. If the Auth0 beans are not being initialized, check
     * to make sure that this property is supplied.
     * </p>
     */
    private String domain;
    /**
     * The issuer of the access tokens used to invoke this API. Specifically,
     * the expected value of the {@code iss} claim in the JWT. Usually, this  is
     * very similar to the {@link #getDomain() domain}, but will depend on your
     * Auth0 configuration.
     * <p>
     * <strong><em>IMPORTANT:</em></strong> This property controls
     * auto-configuration. If the Auth0 beans are not being initialized, check
     * to make sure that this property is supplied.
     * </p>
     */
    private String issuer;
    /**
     * The API identifier (a.k.a. "audience") of this API in Auth0.
     * <p>
     * <strong><em>IMPORTANT:</em></strong> This property controls
     * auto-configuration. If the Auth0 beans are not being initialized, check
     * to make sure that this property is supplied.
     * </p>
     */
    private String audience;
    /**
     * The "leeway" that the token verifier will allow. The default value is 5
     * seconds.
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration tokenLeeway = Duration.ofSeconds(5);
    @Setter(AccessLevel.NONE)
    private ClientProperties client = new ClientProperties();


    /**
     * Properties controlling how this API accesses other APIs, including the
     * Auth0 Authentication and Management APIs. A token will be acquired using
     * the Client Credentials grant (Machine-to-Machine).
     */
    @Getter
    @Setter
    public static class ClientProperties {

        /**
         * A "magic" key for the {@link #getAudiences() audiences} property,
         * which will enable the Auth0 Management API. You should have a
         * configuration property of the form:
         * <pre>
         *   auth0.client.audiences.management=https://id.for.management.api
         * </pre>
         */
        public static final String MANAGEMENT_ID = "management";

        /**
         * Whether or not to pre-cache the client tokens. This will make the
         * initial requests to other APIs a little faster, but will always incur
         * the slight cost of requesting a token from Auth0. If the API is
         * guaranteed to be needed right away, then set this value to
         * {@code true}. If there is a possibility that the API will never need
         * to invoke the Auth0 APIs, then this value should be left
         * {@code false}. If the client functionality is disabled altogether,
         * then this value has no effect.
         */
        private boolean preCache = false;
        /**
         * The client ID for this API. This value must be present to enable
         * Auth0 client functionality. This includes using the Authentication
         * and Management APIs, and acquiring tokens for other APIs via the
         * Client Credentials flow.
         */
        private String id;
        /**
         * The client secret for this API. This value must be present and valid
         * for the provided {@link #getId() client ID} to enable Auth0 client
         * functionality. This includes using the Authentication and Management
         * APIs, and acquiring tokens for other APIs via the Client Credentials
         * flow.
         */
        private String secret;
        /**
         * The API identifiers (a.k.a. "audiences") of any other APIs that this
         * API needs to access. If no audiences are configured, the Auth0
         * Authentication API will still be available, as long as the
         * {@link #getId() client ID} and
         * {@link #getSecret() client secret} are configured.
         */
        private Map<String, String> audiences = new HashMap<>();


        /**
         * Determines whether the given an audience has been configured for the
         * given API.
         *
         * @param apiId The logical identifier of the API
         * @return {@code true} if an audience has been configured for the given
         *         API; {@code false} otherwise
         * @see #getAudience()
         */
        public boolean hasAudience(String apiId) {

            return this.audiences.containsKey(apiId);
        }


        /**
         * <p>
         * Retrieves the Auth0 audience that was configured for the given API.
         * The API identifier is the logical name of the API, i.e. how the
         * application refers to it.
         * </p>
         * <p>
         * Given a property of the form:
         * </p>
         * <pre>
         *     auth0.client.audiences.foo=https://foo.api.example.com/
         * </pre>
         * <p>
         * ... then "foo" is the API identifier, and
         * {@code https://foo.api.example.com/} is the audience
         * </p>
         *
         * @param apiId The logical identifier of the API
         * @return The audience configured for the API
         * @throws IllegalArgumentException If the API identifier has not been
         *         configured
         */
        public String getAudience(final String apiId) throws IllegalArgumentException {

            return Optional.of(this.audiences.get(apiId)).<IllegalArgumentException>orElseThrow(() -> {
                throw new IllegalArgumentException(String.format("No audience configured for %s", apiId));
            });
        }


        /**
         * Whether or not the Authentication API (and client functionality as a
         * whole) is enabled. If the {@linkplain #getId() client ID} and
         * {@linkplain #getSecret() client secret} are available, then returns
         * {@code true}.
         *
         * @return {@code true} if the Authentication API is available;
         *         {@code false} otherwise
         */
        public boolean isAuthenticationEnabled() {

            return StringUtils.hasText(this.id) && StringUtils.hasText(this.secret);
        }


        /**
         * Whether or not the Management API is enabled. If
         * {@linkplain #isAuthenticationEnabled() client functionality} as a
         * whole is enabled, and an audience was supplied for the
         * {@linkplain #MANAGEMENT_ID management API}, then returns
         * {@code true}.
         *
         * @return {@code true} if the Management API is available;
         *         {@code false} otherwise
         */
        public boolean isManagementEnabled() {

            return this.isAuthenticationEnabled() && this.audiences.containsKey(MANAGEMENT_ID);
        }
    }


    @SuppressWarnings("SameParameterValue")
    void debugDump(Logger log) {

        if (log.isDebugEnabled()) {
            log.debug("Auth0: domain = {}", this.domain);
            log.debug("Auth0: issuer = {}", this.issuer);
            log.debug("Auth0: audience = {}", this.audience);
            log.debug("Auth0: tokenLeeway = {}", this.tokenLeeway);
            if (this.client.isAuthenticationEnabled()) {
                log.debug("Auth0 Client: id = {}", this.client.id);
                log.debug("Auth0 Client: secret = {}", mask(this.client.secret));
                log.debug("Auth0 Client: audiences = {}", this.client.audiences);
                log.debug("Auth0 Client: Management API = {}",
                          this.client.isManagementEnabled() ? "enabled" : "disabled");
            }
            else {
                log.debug("Auth0 Client: disabled");
            }
        }
    }


    private String mask(String secret) {

        if (StringUtils.hasText(secret) && secret.length() > MASK_LENGTH) {
            return secret.substring(0, MASK_LENGTH) + "...";
        }
        else {
            return "";
        }
    }
}
