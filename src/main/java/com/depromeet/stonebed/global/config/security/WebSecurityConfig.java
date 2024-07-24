package com.depromeet.stonebed.global.config.security;

import static com.depromeet.stonebed.global.common.constants.EnvironmentConstants.*;
import static com.depromeet.stonebed.global.common.constants.SwaggerUrlConstants.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.security.config.Customizer.*;

import com.depromeet.stonebed.domain.auth.application.JwtTokenService;
import com.depromeet.stonebed.global.annotation.ConditionalOnProfile;
import com.depromeet.stonebed.global.filter.JwtAuthenticationFilter;
import com.depromeet.stonebed.global.util.CookieUtil;
import com.depromeet.stonebed.global.util.SpringEnvironmentUtil;
import com.depromeet.stonebed.infra.properties.SwaggerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenService jwtTokenService;
    private final CookieUtil cookieUtil;
    private final SpringEnvironmentUtil springEnvironmentUtil;

    private final SwaggerProperties swaggerProperties;

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        UserDetails user =
                User.withUsername(swaggerProperties.user())
                        .password(passwordEncoder().encode(swaggerProperties.password()))
                        .roles("SWAGGER")
                        .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        defaultFilterChain(http);
        http.authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers("/walwal-actuator/**")
                                        .permitAll() // Actuator
                                        .requestMatchers("/auth/**")
                                        .permitAll() // Auth endpoints
                                        .anyRequest()
                                        .authenticated())
                .exceptionHandling(
                        exception ->
                                exception.authenticationEntryPoint(
                                        (request, response, authException) ->
                                                response.setStatus(401)));

        http.addFilterBefore(
                jwtAuthenticationFilter(jwtTokenService, cookieUtil),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(1)
    @ConditionalOnProfile({DEV, LOCAL})
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        defaultFilterChain(http);

        http.securityMatcher(getSwaggerUrls()).httpBasic(withDefaults());

        http.authorizeHttpRequests(
                springEnvironmentUtil.isDevProfile()
                        ? authorize -> authorize.anyRequest().permitAll()
                        : authorize -> authorize.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void defaultFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // TODO: CORS 임시 전체 허용
        configuration.addAllowedOriginPattern("*");

        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader(SET_COOKIE);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenService jwtTokenService, CookieUtil cookieUtil) {
        return new JwtAuthenticationFilter(jwtTokenService, cookieUtil);
    }
}
