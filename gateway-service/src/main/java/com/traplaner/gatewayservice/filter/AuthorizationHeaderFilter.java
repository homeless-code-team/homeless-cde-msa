package com.traplaner.gatewayservice.filter;

import com.traplaner.gatewayservice.config.SecurityPropertiesUtil;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@ConfigurationProperties
@Component
@Slf4j
public class AuthorizationHeaderFilter
        extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final SecurityPropertiesUtil securityPropertiesUtil;
    @Value("${jwt.secretKey}")
    private String secretKey;

    public AuthorizationHeaderFilter(SecurityPropertiesUtil securityPropertiesUtil) {
        super(Config.class);
        this.securityPropertiesUtil = securityPropertiesUtil;
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            List<String> allowUrl = securityPropertiesUtil.getExcludedPaths();
            String path = exchange.getRequest().getURI().getPath();
            AntPathMatcher antPathMatcher = new AntPathMatcher();

            log.info("Request Path: {}", path);
            log.info("Allow URLs: {}", allowUrl);

            // 허용된 URL인지 확인
            boolean isAllowed = allowUrl.stream().anyMatch(url -> antPathMatcher.match(url, path));
            log.info("isAllowed: {}", isAllowed);

            if (isAllowed) {
                return chain.filter(exchange); // 허용된 URL은 바로 통과
            }

            // Authorization 헤더 확인
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            log.info("헤더 값: {}", authorizationHeader);
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return onError(exchange, "Authorization header is missing or invalid in gate 1", HttpStatus.UNAUTHORIZED);
            }

            // Bearer 떼기
            String token = authorizationHeader.replace("Bearer ", "");

            // JWT 토큰 유효성 검증
            Claims claims = validateJwt(token);
            if (claims == null) {
                return onError(exchange, "Invalid token in gate 2", HttpStatus.UNAUTHORIZED);
            }

            // 사용자 정보를 클레임에서 꺼내 헤더에 추가
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header("X-User-Email", claims.getSubject())
                    .header("X-User-Nickname", String.valueOf(claims.get("nickname")))
                    .header("X-User-Id", String.valueOf(claims.get("user_id")))
                    .build();
            log.info("통과~ 요구 url : {}", path);
            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    private Claims validateJwt(String token) {
        try {
            Claims body = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = body.getSubject();

            return body;
        } catch (ExpiredJwtException e) {
            log.error("Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Invalid signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {}", e.getMessage());
        }
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String msg, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error(msg);

        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Flux.just(buffer));
    }

    public static class Config {
        // 추가 설정값이 필요한 경우 여기에 작성
    }
}
