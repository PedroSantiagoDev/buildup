package com.maistech.buildup.shared.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
        SecurityFilter securityFilter,
        CorsConfigurationSource corsConfigurationSource
    ) {
        this.securityFilter = securityFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorize ->
                authorize
                    .dispatcherTypeMatchers(DispatcherType.ERROR)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/register")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/refresh")
                    .permitAll()
                    .requestMatchers("/api/password/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/dev/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/companies/**")
                    .hasAnyRole("SUPER_ADMIN", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/companies")
                    .hasRole("SUPER_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/companies/**")
                    .hasAnyRole("SUPER_ADMIN", "ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/companies/**")
                    .hasRole("SUPER_ADMIN")
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/docs/**")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            )
            .addFilterBefore(
                securityFilter,
                UsernamePasswordAuthenticationFilter.class
            )
            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
