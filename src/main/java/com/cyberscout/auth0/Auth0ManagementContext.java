package com.cyberscout.auth0;


import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;

import static com.cyberscout.auth0.Auth0Properties.ClientProperties.MANAGEMENT_ID;


public final class Auth0ManagementContext extends Auth0ClientContext {

    private final String domain;
    private ManagementAPI managementApi;


    public static Auth0ManagementContext buildFor(Auth0Properties props, AuthAPI authApi) throws Auth0Exception {

        Auth0ClientContext.preBuildCheck(MANAGEMENT_ID, props.getClient());
        return new Auth0ManagementContext(MANAGEMENT_ID, props, authApi).init();
    }


    private Auth0ManagementContext(String apiId, Auth0Properties props, AuthAPI authApi) {

        super(apiId, props.getClient(), authApi);
        this.domain = props.getDomain();
    }


    @Override
    protected Auth0ManagementContext init() throws Auth0Exception {

        super.init();
        return this;
    }


    public ManagementAPI manage() throws Auth0Exception {

        if (this.needsTokenRefresh()) {
            this.cacheTokenInfo();
        }
        return this.managementApi;
    }


    @Override
    protected void cacheTokenInfo() throws Auth0Exception {

        super.cacheTokenInfo();
        this.managementApi = new ManagementAPI(this.domain, this.rawToken().getAccessToken());
    }
}
