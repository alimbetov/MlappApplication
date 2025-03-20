package kz.mlapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import kz.mlapp.model.Role;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    // Генерируем безопасный 256-битный (32-байтовый) ключ
    private static final String SECRET_KEY_BASE64 = "3zTvzr5Ns1KPhX2hWv15yA5wHjZAgp0B3zTvzr5Ns1KPhX2hWv15yA==";

    private final SecretKey secretKey;
    private final long validityInMilliseconds = 3600000; // 1 час

    public JwtUtil() {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY_BASE64));
    }

/*    public String createToken(String username, Set<Role> roles) {
        Claims claims = Jwts.claims().build(); // ✅ Создаём пустой изменяемый объект
        claims.setSubject(username); // ✅ Добавляем subject (можно изменять)
        claims.put("roles", roles.stream().map(role -> role.getName().name()).collect(Collectors.toList())); // ✅ Теперь можно изменять

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityInMilliseconds))
                .signWith(secretKey)
                .compact();
    }*/
    public String createToken(String username, Set<Role> roles) {
        return Jwts.builder()
                .subject(username) // ✅ Устанавливаем subject здесь
                .claim("roles", roles.stream().map(role -> role.getName().name()).collect(Collectors.toList())) // ✅ Добавляем роли
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + validityInMilliseconds))
                .signWith(secretKey)
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Set<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<String> rolesList = claims.get("roles", List.class);
        return rolesList.stream().collect(Collectors.toSet());
    }
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
    }
}
