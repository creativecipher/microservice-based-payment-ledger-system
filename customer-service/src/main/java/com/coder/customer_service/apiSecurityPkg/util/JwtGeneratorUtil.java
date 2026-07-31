package com.coder.customer_service.apiSecurityPkg.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtGeneratorUtil {
    @Value("${jwt.secret}")
    private String secret;

//    private String secret ="5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    public String generateTokenFromUserName(String username){
        Map<String,Object> claims = new HashMap<>();
        return createToken(claims,username);
    }

    private String createToken(Map<String,Object> claims, String subject){
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000*60*30)) // 30 Minute Expiration
                .signWith(getSignKey())
                .compact();
    }

    private SecretKey getSignKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
