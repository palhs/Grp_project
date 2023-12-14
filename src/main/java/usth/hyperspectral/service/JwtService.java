package usth.hyperspectral.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Singleton
public class JwtService {

    private long getExpiryTime(int expiryMins) {
        return System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expiryMins);
    }

    public String generateJwt(String role, int expiryMins){
        Set<String> roles = new HashSet<>(
                Collections.singletonList(role)
        );
        return Jwt.issuer("panhandle")
                .subject("")
                .groups(roles)
                // Smallrye expireAt() expect the time is in second format instead of milliseconds
                .expiresAt(getExpiryTime(expiryMins)/1000)
                .sign();

    }

    public String generateAdminJwt(){
        return generateJwt("admin",15);
    }

    public String generateUserJwt(){
        return generateJwt("user",15);
    }

}

