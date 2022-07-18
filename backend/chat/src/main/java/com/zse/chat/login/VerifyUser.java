package com.zse.chat.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.zse.chat.user.UserNickname;
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
import java.util.ArrayList;

@Aspect
@Component
public class VerifyUser {

    private final Environment env;

    private final String secret;
    private final JWTVerifier verifier;

    public VerifyUser(Environment env){
        this.env = env;

        this.secret = this.env.getProperty("jwt.secret");
        this.verifier = JWT.require(Algorithm.HMAC256(this.secret)).build();
    }

    @Pointcut("@annotation(VerifyJWT)")
    public void point(){}

    @Around("point()")
    public Object userJWT(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header)){
            throw new MissingJWTException();
        }
        String token = header.replace("Bearer ", "");

        try{
            DecodedJWT decodedJWT = verifier.verify(token);
            Claim decodedClaims = decodedJWT.getClaims().get("nickname");
            String nickname = decodedClaims.asString();

            UserNickname arg = (UserNickname) pjp.getArgs()[0];
            arg.setNickname(nickname);

            Object[] args = pjp.getArgs();
            args[0] = arg;

            return pjp.proceed(args);
        } catch (JWTVerificationException e){
            throw new InvalidJWTException();
        }
    }

}
