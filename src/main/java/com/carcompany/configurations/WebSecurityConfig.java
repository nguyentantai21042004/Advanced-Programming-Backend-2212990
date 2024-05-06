package com.carcompany.configurations;

import com.carcompany.filters.JwtTokenFilter;
import com.carcompany.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebMvc
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> {
                    requests
                            .requestMatchers(
                                    String.format("%s/users/register", apiPrefix),
                                    String.format("%s/users/login", apiPrefix)
                            )
                            .permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/vehicles", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/vehicles**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/vehicles/**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/vehicles/type", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/vehicles/type**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/vehicles/type/**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/vehicles/images/**", apiPrefix)).permitAll()
                            .requestMatchers(POST,
                                    String.format("%s/vehicles", apiPrefix)).hasAnyRole("ADMIN")
                            .requestMatchers(POST,
                                    String.format("%s/vehicles/uploads/**", apiPrefix)).hasAnyRole("ADMIN")
                            .requestMatchers(PUT,
                                    String.format("%s/vehicles/**", apiPrefix)).hasAnyRole("ADMIN")
                            .requestMatchers(DELETE,
                                    String.format("%s/vehicles/**", apiPrefix)).hasAnyRole("ADMIN")

                            .requestMatchers(GET,
                                    String.format("%s/orders", apiPrefix)).hasAnyRole("DRIVER", "ADMIN")
                            .requestMatchers(GET,
                                    String.format("%s/orders/**", apiPrefix)).hasAnyRole("DRIVER", "ADMIN")
                            .requestMatchers(POST,
                                    String.format("%s/orders", apiPrefix)).hasAnyRole("ADMIN")
                            .requestMatchers(PUT,
                                    String.format("%s/orders/**", apiPrefix)).hasAnyRole("ADMIN")
                            .requestMatchers(PUT,
                                    String.format("%s/orders**", apiPrefix)).hasAnyRole("ADMIN")
                            .requestMatchers(DELETE,
                                    String.format("%s/orders/**", apiPrefix)).hasAnyRole("ADMIN")

                            .requestMatchers(GET,
                                    String.format("%s/users/byLicense/**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/users/licensesId**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/users/images", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/users/images**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/users/images/**", apiPrefix)).permitAll()
                            .requestMatchers(POST,
                                    String.format("%s/users/uploads/**", apiPrefix)).hasAnyRole("DRIVER", "ADMIN")
                            .requestMatchers(POST,
                                    String.format("%s/users/license/**", apiPrefix)).hasAnyRole("DRIVER", "ADMIN")
                            .requestMatchers(GET,
                                    String.format("%s/users/drivers", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/users/drivers**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/users/drivers/**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/users/byDriver**", apiPrefix)).hasAnyRole("ADMIN")
                            .requestMatchers(GET,
                                    String.format("%s/users/byDriver/**", apiPrefix)).hasAnyRole("ADMIN")
                            .requestMatchers(PUT,
                                    String.format("%s/users/**", apiPrefix)).hasAnyRole("DRIVER", "ADMIN")

                            .requestMatchers(GET,
                                    String.format("%s/maintenances", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/maintenances/**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/maintenances/images", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/maintenances/images**", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/maintenances/images/**", apiPrefix)).permitAll()
                            .requestMatchers(POST,
                                    String.format("%s/maintenances", apiPrefix)).hasAnyRole("ADMIN")
                            .requestMatchers(POST,
                                    String.format("%s/maintenances/uploads/**", apiPrefix)).hasAnyRole("ADMIN")
                            .requestMatchers(PUT,
                                    String.format("%s/maintenances/**", apiPrefix)).hasAnyRole("ADMIN")
                            .requestMatchers(DELETE,
                                    String.format("%s/maintenances/**", apiPrefix)).hasAnyRole("ADMIN")

                            .requestMatchers(GET,
                                    String.format("%s/roles", apiPrefix)).permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/licenses", apiPrefix)).permitAll()

                            .anyRequest().authenticated();
                })
                .csrf(AbstractHttpConfigurer::disable);

        http.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
                configuration.setExposedHeaders(List.of("x-auth-token"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                httpSecurityCorsConfigurer.configurationSource(source);
            }
        });

        return http.build();
    }
}
