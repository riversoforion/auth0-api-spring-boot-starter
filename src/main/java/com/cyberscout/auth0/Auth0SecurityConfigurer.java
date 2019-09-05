package com.cyberscout.auth0;


import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.spring.security.api.JwtAuthenticationProvider;
import com.auth0.spring.security.api.JwtWebSecurityConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


/**
 * <p>
 * Central Spring configuration to secure an API with Auth0. It performs the
 * following configuration:
 * </p>
 * <ul>
 * <li>Registers the Auth0 Spring Security integration with Spring Security</li>
 * <li>Enables Spring Security's method annotations, including the
 * {@code @Secured} and {@code @Pre/PostAuthorize} annotations</li>
 * </ul>
 * <p>
 * Requires the {@link Auth0Properties#getDomain() auth0.domain},
 * {@link Auth0Properties#getIssuer() auth0.issuer}, and
 * {@link Auth0Properties#getAudience() auth0.audience} properties to be
 * present.
 * </p>
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableConfigurationProperties({ Auth0Properties.class })
@Configuration
@ConditionalOnProperty(prefix = Auth0Properties.AUTH0_PREFIX, name = { "domain", "issuer", "audience" })
@ComponentScan(basePackageClasses = Auth0SecurityConfigurer.class)
@Slf4j
public class Auth0SecurityConfigurer extends WebSecurityConfigurerAdapter {

    private final Auth0Properties props;


    @Autowired
    Auth0SecurityConfigurer(Auth0Properties props) {

        this.props = props;
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        this.props.debugDump(log);
        final JwkProvider jwkProvider = new JwkProviderBuilder(props.getIssuer()).build();
        //@formatter:off
        JwtAuthenticationProvider jwtProvider = new JwtAuthenticationProvider(jwkProvider,
                                                                              this.props.getIssuer(),
                                                                              this.props.getAudience())
                .withJwtVerifierLeeway(this.props.getTokenLeeway().getSeconds());
        JwtWebSecurityConfigurer
                .forRS256(this.props.getAudience(), this.props.getIssuer(), jwtProvider)
                .configure(http)
                .authorizeRequests().anyRequest().permitAll();
        //@formatter:on
        log.debug("Web security configuration complete");
    }
}
