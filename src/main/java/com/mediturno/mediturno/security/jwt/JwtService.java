package com.mediturno.mediturno.security.jwt;

import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;


@Service
public class JwtService {


    //Clave secreta 256 bits minimo
    private final String secretKey = "494297f466f07537fde3d03d4c52f1be9d855d4bdfacb57ea364cdd6148710fb";

    public String getSecretKeyHex() {
    return this.secretKey; 
}

    
    private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(this.secretKey); 
    return Keys.hmacShaKeyFor(keyBytes);
}
    //Tiempo de expiracion
    private final  long jwtExpiration = 7200000;

   


    //Generar el Token
    public String generateToken(String userMail){
        return generateToken(userMail, new java.util.HashMap<>());
    }

    public String generateToken(String userMail, java.util.Map<String, Object> extraClaims) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userMail)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    //Extraer el username
    public String extractUsername(String token){
       return extractAllClaims(token).getSubject();
    }


    //Validar el Token
    private Claims extractAllClaims(String token){
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
         final String username = extractUsername(token);
         return ( username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

   
}
