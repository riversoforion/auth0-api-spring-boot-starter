package com.cyberscout.auth0;


import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.cyberscout.auth0.Auth0Properties.ClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Spring configuration for clients of the Authentication and Management APIs.
 */
@EnableConfigurationProperties({ Auth0Properties.class })
@Configuration
@Slf4j
public class Auth0ClientConfiguration {

    private final Auth0Properties props;


    @Autowired
    public Auth0ClientConfiguration(Auth0Properties props) {

        this.props = props;
    }


    /**
     * Constructs an Auth0 Authentication API wrapper bean. The bean is
     * conditional on the presence of the
     * {@link ClientProperties#getId() auth0.client.id} and
     * {@link ClientProperties#getSecret() auth0.client.secret} properties.
     *
     * @return The Authentication API wrapper bean
     */
    @Bean
    @ConditionalOnProperty(prefix = Auth0Properties.AUTH0_PREFIX, name = { "client.id", "client.secret" })
    public AuthAPI authApi() {

        log.debug("Creating Auth0 Authentication API wrapper bean");
        return new AuthAPI(this.props.getDomain(), this.props.getClient().getId(), this.props.getClient().getSecret());
    }


    /**
     * Constructs a {@code ManagementContext} bean, which provides access to the
     * Auth0 Management API wrapper object. The bean is conditional on the
     * presence of the {@link #authApi() authApi} bean and the
     * {@link ClientProperties#getAudience() auth0.client.audiences.management}
     * property.
     *
     * @return The client context for the Management API
     * @throws Auth0Exception If there is a problem constructing the context
     */
    @Bean
    @ConditionalOnBean(AuthAPI.class)
    @ConditionalOnProperty(prefix = Auth0Properties.AUTH0_PREFIX, name = "client.audiences.management")
    public Auth0ManagementContext managementContext() throws Auth0Exception {

        log.debug("Creating Management client context bean");
        return Auth0ManagementContext.buildFor(this.props, this.authApi());
    }
}
