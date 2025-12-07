package com.dms.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret:defaultSecretKeyForDevEnvironmentOnlyMustBeAtLeast256BitsLong}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // include role if available
        if (userDetails.getAuthorities() != null && userDetails.getAuthorities().iterator().hasNext()) {
            claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        }
        // Set subject to staffId when available so UserDetailsService (which loads by staffId) can find the user during validation
        String subject;
        if (userDetails instanceof com.dms.entity.User) {
            subject = ((com.dms.entity.User) userDetails).getStaffId();
        } else {
            subject = userDetails.getUsername();
        }
        return createToken(claims, subject);
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        // Use parser().setSigningKey(...).build().parseClaimsJws(...) for compatibility with current jjwt version
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String subject = extractUsername(token);
            if (userDetails instanceof com.dms.entity.User) {
                String staffId = ((com.dms.entity.User) userDetails).getStaffId();
                return (subject.equals(staffId) && !isTokenExpired(token));
            } else {
                return (subject.equals(userDetails.getUsername()) && !isTokenExpired(token));
            }
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public long getExpirationTime() {
        return jwtExpiration;
    }
}
