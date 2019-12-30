# Auth0 Spring Boot Starter for APIs

![](https://github.com/CyberScout/auth0-api-spring-boot-starter/workflows/CI%20Build/badge.svg)
![](https://github.com/CyberScout/auth0-api-spring-boot-starter/workflows/CI%20Publish%20Release/badge.svg)
[ ![Download](https://api.bintray.com/packages/cyberscout/cyberscout-oss-maven/auth0-api-spring-boot-starter/images/download.svg) ](https://bintray.com/cyberscout/cyberscout-oss-maven/auth0-api-spring-boot-starter/_latestVersion)

This Spring Boot starter simplifies using Auth0 to secure an API built with
Spring Security and Spring MVC. The project only needs to include this starter
as a dependency, and the requisite Spring Security and Auth0 libraries will be
brought in transitively. Also, necessary configuration will be performed, and
useful beans will be created.

## Installation

Installation is as simple as including the dependency in your project:

###### Gradle

```groovy
dependencies {
    // ...
    implementation('com.cyberscout.auth0:auth0-api-spring-boot-starter:1.0.0-SNAPSHOT')
    // ...
}
```

###### Maven

```xml
    <dependencies>
        <dependency>
            <groupId>com.cyberscout.auth0</groupId>
            <artifactId>auth0-api-spring-boot-starter</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
```

## Usage

The starter supports several usage scenarios related to Auth0. Primarily, it
allows an API to be secured with Auth0-issued JWTs. It also allows the API to
invoke Auth0's Authentication and Management APIs, as well as external APIs that
are secured via Auth0.

### Acting as an Auth0-Secured API

This is the most basic usage of the starter. After installing according to the
instructions above, then define the following properties in your application
configuration:

- `auth0.domain`
- `auth0.issuer`
- `auth0.audience`

... and optionally:

- `auth0.tokenLeeway`

See [Auth0Properties](src/main/java/com/cyberscout/auth0/Auth0Properties.java)
for details on each property.

#### Injecting authorization context

_TODO: Proof of concept and document_

### Using the Authentication API

In order to use the Authentication API, some additional properties need to be
configured:

- `auth0.client.id`
- `auth0.client.secret`

See [Auth0Properties](src/main/java/com/cyberscout/auth0/Auth0Properties.java)
for details on each property.

With the correct configuration in place, the `AuthAPI` object can be injected as
follows:

```java
@Service
public class MyService {
    
    private AuthAPI authApi;
    
    @Autowired
    public MyService(AuthAPI authApi) {
        this.authApi = authApi;
    }
}
```

### Using the Management API

To use the Auth0 Management API, the Authentication API must be configured,
along with the following additional properties:

- `auth0.client.audiences.management`

This property must be set with the audience identifier of your tenant's
Management API.

See [Auth0Properties](src/main/java/com/cyberscout/auth0/Auth0Properties.java)
for more details.

With the correct configuration in place, an `TenantManagementContext` bean can
be injected and used to acquire a wrapper around the Management API:

```java
@Service
public class MyService {
    
    private TenantManagementContext tenant;
    
    @Autowired
    public MyService(TenantManagementContext tenant) {
        this.tenant = tenant;
    }
    
    public void someServiceMethod() {
        ManagementAPI mgmtApi = this.tenant.manage();
        // Use the Management API wrapper object
    }
}
```

See
[Auth0ManagementContext](src/main/java/com/cyberscout/auth0/TenantManagementContext.java)
for more details on usage.

### Using Other Auth0-Secured APIs

If your API needs to invoke other APIs that are also secured with Auth0, then
"context" beans can be constructed that will acquire the needed token
transparently.

Since these arbitrary APIs cannot be known by this starter, your consumer code
must construct the beans. The starter will assist with providing the necessary
dependency beans and configuration.

```java
// MySpringConfiguration.java
public class MySpringConfiguration {
    private AuthAPI authApi;
    private Auth0Properties props;
    
    @Autowired
    public MySpringConfiguration(AuthAPI authApi, Auth0Properties props) {
        this.authApi = authApi;
        this.props = props;
    }
    
    @Bean
    public ClientTokenContext someApiTokenContext() {
        return ClientTokenContext
                .buildFor(
                        props.getAudience("someApi"),
                        this.props.getClient(),
                        this.authApi
                );
    }
}

// SomeApi.java
public class SomeApi {
    private ClientTokenContext tokenContext;
    
    @Autowired
    public SomeApi(ClientTokenContext tokenContext) {
        this.tokenContext = tokenContext;
    }
    
    public void someApiOperation() {
        String token = this.tokenContext.accessToken().getToken();
        // Use token to construct Authorization header
        // Use header in HTTP request
    }
}
```

See
[ClientTokenContext](src/main/java/com/cyberscout/auth0/ClientTokenContext.java)
for more details on usage.
