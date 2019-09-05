package com.cyberscout.auth0;


import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import static com.cyberscout.auth0.Auth0Properties.ClientProperties.MANAGEMENT_ID;


/**
 * An object that encapsulates the context of invoking Auth0's Management API.
 * In addition to managing the access token, also provides the Auth0 Management
 * API wrapper object
 */
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Slf4j
// TODO Unit test me!
// TODO Integration test me!
public final class Auth0ManagementContext extends Auth0ClientContext {

    @ToString.Include
    private final String domain;
    private ManagementAPI managementApi;


    /**
     * Creates a new context object, ensuring that it is properly initialized.
     * The initial token is cached, if configured.
     *
     * @param props The Auth0 properties to use when constructing the context
     * @param authApi The Auth0 Authentication API wrapper object
     * @return The fully initialized management context
     * @throws Auth0Exception If there is a problem retrieving the access token
     * @throws IllegalArgumentException If the system is not properly configured
     *         as a management context
     */
    public static Auth0ManagementContext buildFor(Auth0Properties props, AuthAPI authApi) throws Auth0Exception {

        Auth0ClientContext.preBuildCheck(MANAGEMENT_ID, props.getClient());
        return new Auth0ManagementContext(props, authApi).init();
    }


    /**
     * Constructs a raw client context object. The context will not be
     * completely initialized by the constructor. Must only be invoked by
     * sub-classes, as part of the initialization process.
     *
     * @param props The Auth0 properties to use when constructing the context
     * @param authApi The Auth0 Authentication API wrapper object
     * @see #init()
     */
    private Auth0ManagementContext(Auth0Properties props, AuthAPI authApi) {

        super(MANAGEMENT_ID, props.getClient(), authApi);
        this.domain = props.getDomain();
    }


    /**
     * Finishes initializing the context, including caching tokens, if needed.
     *
     * @return The fully-initialized client context
     * @throws Auth0Exception If there is a problem retrieving the access token
     */
    @Override
    protected Auth0ManagementContext init() throws Auth0Exception {

        super.init();
        return this;
    }


    /**
     * Retrieves the Auth0 Management API wrapper object. Declared as a method,
     * rather than an accessor, to indicate that it has side effects
     * (potentially retrieving a new token).
     *
     * @return The Auth0 Management API wrapper object
     * @throws Auth0Exception If there is a problem retrieving the access token
     */
    public ManagementAPI manage() throws Auth0Exception {

        cacheNewTokenIfNeeded();
        return this.managementApi;
    }


    @Override
    protected void cacheTokenInfo() throws Auth0Exception {

        super.cacheTokenInfo();
        this.managementApi = new ManagementAPI(this.domain, this.tokenInfo().getAccessToken());
    }
}
