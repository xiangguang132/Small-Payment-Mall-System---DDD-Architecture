package cn.bugstack.infrastructure.adapter.port;

import cn.bugstack.domain.auth.adapter.port.IJwtPort;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtPort implements IJwtPort {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expire-seconds}")
    private Long expireSeconds;

    @Override
    public String createToken(String userId) {
        Date now = new Date();
        Date expAt = new Date(System.currentTimeMillis() + expireSeconds * 1000L);

        return JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(now)
                .withExpiresAt(expAt)
                .withClaim("userId", userId)
                .sign(Algorithm.HMAC256(secret));
    }

    @Override
    public String parseUserId(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer(issuer)
                .build()
                .verify(token);
        return decodedJWT.getClaim("userId").asString();
    }

    @Override
    public boolean verifyToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .build()
                    .verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
