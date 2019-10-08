# Auth0 Spring Boot Starter for APIs

_TODO: Placeholders for CI status, current version on Maven repo, etc._

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

### Using Other Auth0-Secured APIs
