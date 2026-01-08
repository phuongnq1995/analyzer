package org.phuongnq.analyzer.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import java.text.ParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private final long expirationMs = 86400000;

    private JWSSigner signer;
    private JWSVerifier verifier;

    @PostConstruct
    public void init() throws JOSEException {
        signer = new MACSigner(secret);
        verifier = new MACVerifier(secret);
    }

    public String generateToken(Map<String, Object> claims, String subject) {
        Builder claimSetBuilder = new Builder()
            .subject(subject)
            .expirationTime(new Date(System.currentTimeMillis() + expirationMs));

        claims.forEach(claimSetBuilder::claim);

        SignedJWT signedJWT = new SignedJWT(
            new JWSHeader(JWSAlgorithm.HS256),
            claimSetBuilder.build()
        );

        try {
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            log.error("Error: {}", e);
            throw new RuntimeException(e);
        }

        return signedJWT.serialize();
    }

    public String generateToken(UserDetails user) {
        List<String> roles = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        return generateToken(Map.of("roles", roles), user.getUsername());
    }

    public boolean validateToken(String token, UserDetails user) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (jwt.verify(verifier)) {
                return true;
            }
        } catch (JOSEException | ParseException e) {
            log.error("Error: {}", e);
        }
        return false;
    }

    public String extractUsername(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (jwt.verify(verifier)) {
                JWTClaimsSet claims = jwt.getJWTClaimsSet();
                return claims.getSubject();
            }
        } catch (ParseException | JOSEException e) {
            log.error("Error: {}", e);
        }
        throw new RuntimeException("Cannot extract username from token");
    }
}

