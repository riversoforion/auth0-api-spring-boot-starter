package com.cyberscout.util.auth0;


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
         * Whether or not access to other APIs is enabled. If {@code true}, then
         * the other API-related properties will be validated, and tokens for
         * those APIs can be acquired. Furthermore, this API will be able to
         * access the Authentication API and also the Management API, if the
         * appropriate audience is configured.
         */
        private boolean enabled = false;
        /**
         * Whether or not to pre-cache the tokens and client objects. This will
         * make the initial requests to other APIs a little faster, but will
         * always incur the slight cost of requesting a token from Auth0. If the
         * API is guaranteed to be needed right away, then set this value to
         * {@code true}. If there is a possibility that the API will never need
         * to invoke the Auth0 APIs, then this value should be left
         * {@code false}. If the client functionality is disabled altogether,
         * then this value has no effect.
         */
        private boolean preCache = false;
        /**
         * The client ID for this API.
         */
        private String clientId;
        /**
         * The client secret for this API.
         */
        private String clientSecret;
        /**
         * The API identifiers (a.k.a. "audiences") of any other APIs that this
         * API needs to access. If no audiences are configured, the Auth0
         * Authentication API will still be available, as long as the
         * {@link #getClientId() client ID} and
         * {@link #getClientSecret() client secret} are configured.
         */
        private Map<String, String> audiences = new HashMap<>();


        public boolean hasAudience(String apiId) {

            return this.audiences.containsKey(apiId);
        }


        public String getAudience(String apiId) {

            return this.audiences.get(apiId);
        }


        public boolean isAuthenticationEnabled() {

            return this.isEnabled() && StringUtils.hasText(this.clientId) && StringUtils.hasText(this.clientSecret);
        }


        public boolean isManagementEnabled() {

            return this.audiences.containsKey(MANAGEMENT_ID);
        }
    }


    @SuppressWarnings("SameParameterValue")
    void debugDump(Logger log) {

        if (log.isDebugEnabled()) {
            log.debug("Auth0: domain = {}", this.domain);
            log.debug("Auth0: issuer = {}", this.issuer);
            log.debug("Auth0: audience = {}", this.audience);
            log.debug("Auth0: tokenLeeway = {}", this.tokenLeeway);
            if (this.client.enabled) {
                log.debug("Auth0 Client: clientId = {}", this.client.clientId);
                log.debug("Auth0 Client: clientSecret = {}", mask(this.client.clientSecret));
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
