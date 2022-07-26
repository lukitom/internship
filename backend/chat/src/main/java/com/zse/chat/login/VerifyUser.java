package com.zse.chat.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.zse.chat.user.UserNickname;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
public class VerifyUser {

    private final Environment env;

    private final String secret;
    private final JWTVerifier verifier;

    public VerifyUser(Environment env) {
        this.env = env;

        this.secret = this.env.getProperty("jwt.secret");
        this.verifier = JWT.require(Algorithm.HMAC256(this.secret)).build();
    }

    @Pointcut("@annotation(withoutArgs)")
    public void point(VerifyJWT withoutArgs) {
    }

    @Around(value = "point(withoutArgs)", argNames = "pjp,withoutArgs")
    public Object userJWT(ProceedingJoinPoint pjp, VerifyJWT withoutArgs) throws Throwable {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        final String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header)) {
            log.warn("Missing JWT token. Endpoint: {} {}", request.getMethod(), request.getServletPath());
            throw new MissingJWTException();
        }
        final String token = header.replace("Bearer ", "");

        try {
            final DecodedJWT decodedJWT = verifier.verify(token);
            final Claim decodedClaims = decodedJWT.getClaims().get("nickname");
            final String nickname = decodedClaims.asString();

            if (withoutArgs.withoutArgs()) {
                return pjp.proceed(pjp.getArgs());
            }
            final UserNickname arg = (UserNickname) pjp.getArgs()[0];
            arg.setNickname(nickname);

            final Object[] args = pjp.getArgs();
            args[0] = arg;

            return pjp.proceed(args);
        } catch (JWTVerificationException e) {
            log.error("Parsing Jwt token for : {} {} failed due to: {} ", request.getMethod(), request.getServletPath(), e.getMessage());
            throw new InvalidJWTException();
        }
    }

}
