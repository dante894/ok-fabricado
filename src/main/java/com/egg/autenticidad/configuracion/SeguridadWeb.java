package com.egg.autenticidad.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Quien puede entrar a que. Las reglas se leen de arriba hacia abajo: gana la primera que coincide. */
@Configuration
@EnableWebSecurity
public class SeguridadWeb {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Publico: la portada, el login, los estilos y, sobre todo, la verificacion del cliente
                .requestMatchers("/", "/login", "/error", "/css/**", "/img/**", "/v/**").permitAll()
                // "/**" cubre todos los niveles (en biblioteca "/admin/*" cubria solo uno)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/fabrica/**", "/qr/**", "/alertas/**").hasAnyRole("FABRICA", "ADMIN")
                .requestMatchers("/caja/**").hasRole("TIENDA")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/logincheck")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/panel", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
