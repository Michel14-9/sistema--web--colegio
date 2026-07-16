package com.universidad.sistema_academico.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/consultar/**",
                                "/api/matricula/**",
                                "/api/reniec/**",
                                "/chat/**"
                        )
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/consultar/**").permitAll()
                        .requestMatchers("/", "/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                        .requestMatchers("/login", "/error", "/error/**", "/404", "/403", "/500", "/mantenimiento", "/olvide-contrasena", "/resetear-contrasena").permitAll()
                        .requestMatchers("/api/verificar-mantenimiento").permitAll()  // 👈 NUEVO
                        .requestMatchers("/api/matricula/matricula").permitAll()
                        .requestMatchers("/api/matricula/registrar").permitAll()
                        .requestMatchers("/api/matricula/verificar-descuento").permitAll()
                        .requestMatchers("/api/reniec/**").permitAll()
                        .requestMatchers("/chat", "/chat/**").permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/docente/**").hasRole("DOCENTE")
                        .requestMatchers("/estudiante/**").hasRole("ESTUDIANTE")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect("/403");
        };
    }
}