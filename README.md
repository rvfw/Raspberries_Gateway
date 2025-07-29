
# Raspberries API Gateway

Simple API Gateway for microservices arhitecture written in **Java Spring Boot** + **Java Cloud Gateway**.

## Features

- Redirecting requests to the required servers.
- Parse the JWT token and add X-User-Id and X-User-Roles headers to request private endpoints.
- Handling authentication related errors.

## Related

Here are the services of my project:

- [Raspberries Auth Service](https://github.com/rvfw/Raspberries_AuthService)
- [Raspberries User Service](https://github.com/rvfw/Raspberries_UserService)

## API Reference


| Header          | Type     | Description                |
| :--------       | :------- | :------------------------- |
| `Authorization` | `string` | **Required**. Jwt token    |

Added headers:

| Header         | Type     | Description               |
| :--------      | :------- | :--------------------     |
| `X-User-Id`    | `string` | User Id from jwt token    |
| `X-User-Roles` | `string` | User roles from jwt token |




## Application Config

src\main\resources\\**application-config.properties** contains the following secret values:
```
jwt.secret = {your secret jwt key}
```

## Adding new services

To add new service to src\main\resources\application.properties you need to add:
```
spring.cloud.gateway.routes[n].id = service-name
spring.cloud.gateway.routes[n].uri = server-uri
spring.cloud.gateway.routes[n].predicates[0] = Path=/path/predicate/**

gateway.public-paths = /api/auth/login,/api/auth/register,{new public paths}
```

