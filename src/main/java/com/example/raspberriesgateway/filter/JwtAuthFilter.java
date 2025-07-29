package com.example.raspberriesGateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter{
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${gateway.public-paths}")
    private String[] publicPaths;
    private JwtParser jwtParser;
    @PostConstruct
    public void init(){
        jwtParser= Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build();
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        System.out.println(exchange.getRequest().getMethod()+" "+path);
        if(Arrays.stream(publicPaths).anyMatch(path::startsWith)){
            return chain.filter(exchange);
        }
        String token=exchange.getRequest().getHeaders().getFirst("Authorization");
        if(token==null || !token.startsWith("Bearer ")){
            return buildErrorResponse(exchange,HttpStatus.UNAUTHORIZED,"Invalid Jwt Token");
        }
        try{
            Claims claims = jwtParser.parseClaimsJws(token.substring(7)).getBody();
            String roles=claims.get("roles", List.class).toString();
            exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Roles", roles!=null?roles:"")
                    .build();
            return chain.filter(exchange);
        }
        catch (ExpiredJwtException e){
            return buildErrorResponse(exchange,HttpStatus.UNAUTHORIZED,"Token expired");
        }
        catch (Exception e){
            return buildErrorResponse(exchange,HttpStatus.UNAUTHORIZED,"Authorization error: "+e.getMessage());
        }
    }
    private Mono<Void> buildErrorResponse(ServerWebExchange exchange,HttpStatus status,String message){
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String body=String.format("""
                {
                    "status":"%s"
                    "message":"%s"
                    "timestamp":"%s"
                }""",status, message, Instant.now().toString());
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
}
