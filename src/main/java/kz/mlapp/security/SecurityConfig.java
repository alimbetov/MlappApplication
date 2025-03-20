package kz.mlapp.security;


import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity { // 🔥 Используем Vaadin Security
    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtFilter jwtFilter, UserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ Разрешаем доступ к статическим файлам Vaadin
                        .requestMatchers("/VAADIN/**", "/frontend/**", "/images/**", "/styles/**", "/icons/**", "/manifest.webmanifest").permitAll()
                        // ✅ Разрешаем доступ к страницам без аутентификации
                        .requestMatchers("/", "/login", "/users/register", "/users/login").permitAll()
                        // ✅ Доступ к API только аутентифицированным пользователям
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                // ✅ Добавляем JWT-фильтр перед стандартным аутентификационным фильтром
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // 🔥 Указываем страницу входа в систему (если используем Vaadin Security)
        super.configure(http);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
