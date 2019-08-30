package com.cyberscout.auth0;


import com.auth0.client.auth.AuthAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@EnableConfigurationProperties({ Auth0Properties.class })
@Configuration
@Slf4j
public class Auth0ClientConfiguration {

    private final Auth0Properties props;


    @Autowired
    public Auth0ClientConfiguration(Auth0Properties props) {

        this.props = props;
    }


    @Bean
    @ConditionalOnProperty(prefix = Auth0Properties.AUTH0_PREFIX, name = "client.authenticationEnabled")
    public AuthAPI authApi() {

        return new AuthAPI(this.props.getDomain(),
                           this.props.getClient().getClientId(),
                           this.props.getClient().getClientSecret());
    }
}
