package com.spring.homeless_user.common.auth;

import com.spring.homeless_user.common.utill.JwtUtil;
import com.spring.homeless_user.common.utill.SecurityPropertiesUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
class JwtAuthFilterTest {

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private SecurityPropertiesUtil securityPropertiesUtil;

    @MockBean
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(Mockito.mock(RedisTemplate.class), jwtUtil, securityPropertiesUtil);
        mockMvc = MockMvcBuilders.standaloneSetup(jwtAuthFilter).build();
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).contains("Authorization header is missing or empty");
    }

    @Test
    void shouldReturnUnauthorizedForExpiredToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expiredToken");

        Mockito.doThrow(new ExpiredJwtException(null, null, "Token has expired"))
                .when(jwtUtil).extractAllClaims(anyString());

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).contains("Token has expired");
    }

    @Test
    void shouldReturnBadRequestForMalformedToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer malformedToken");

        Mockito.doThrow(new MalformedJwtException("Malformed token"))
                .when(jwtUtil).extractAllClaims(anyString());

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("Malformed token");
    }

    @Test
    void shouldNotFilterExcludedPaths() throws ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/public-api");

        Mockito.when(securityPropertiesUtil.getExcludedPaths()).thenReturn(List.of("/public-api"));

        boolean shouldNotFilter = jwtAuthFilter.shouldNotFilter(request);

        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    void shouldProcessRequestWhenTokenIsValid() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer validToken");

        Mockito.when(jwtUtil.extractAllClaims(anyString())).thenReturn(Mockito.mock(io.jsonwebtoken.Claims.class));
        Mockito.when(jwtUtil.getEmailFromToken(anyString())).thenReturn("test@example.com");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }
}
