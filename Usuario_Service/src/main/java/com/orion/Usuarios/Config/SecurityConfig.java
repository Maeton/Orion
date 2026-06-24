package com.orion.Usuarios.Config;


import com.orion.Usuarios.Entity.Usuario;
import com.orion.Usuarios.Repository.UsuarioRepository;
import com.orion.Usuarios.Security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
//                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS,"/**").permitAll();
                    auth.requestMatchers("/error").permitAll();
                    auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                            "/swagger-ui/index.html", "/swagger-resources/**", "/webjars/**").permitAll();
                    auth.requestMatchers(HttpMethod.POST,"/api/auth/login").permitAll();
                    auth.requestMatchers(HttpMethod.POST,"/api/usuarios/registro").permitAll();
                    auth.requestMatchers(HttpMethod.GET,"/api/usuarios/**").permitAll();
                    auth.requestMatchers(HttpMethod.PUT,"/api/usuarios/**").permitAll();
                    auth.requestMatchers(HttpMethod.DELETE,"/api/usuarios/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{

        return config.getAuthenticationManager();

    }



    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {

        return username -> {

            Usuario usuarioDB = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));


            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            usuarioDB.getRoles().
                    forEach(rol -> authorities.add(new SimpleGrantedAuthority(rol.getNombre())));

            usuarioDB.getRoles().stream()
                    .flatMap(rol -> rol.getPermisos().stream())
                    .forEach(permiso ->
                            authorities.add(new SimpleGrantedAuthority(permiso.getNombre())));


            return new User(usuarioDB.getUsername(), usuarioDB.getPassword(), authorities);

//            return User.builder()
//                    .username(usuarioDB.getUsername())
//                    .password(usuarioDB.getPassword())
//                    .roles("USER")
//                    .build();
//        };

        };

    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return  new BCryptPasswordEncoder();
    }



//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // permitir cualquier ip en la red
//        configuration.setAllowedOriginPatterns(List.of("*"));
//
//        configuration.setAllowedMethods(Arrays.asList("*"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//
//    }
}
