package com.zse.chat.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.zse.chat.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;

@Tag(name = "Login")
@RequestMapping("/login")
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final Environment env;

    @PostMapping
    public String login(@RequestBody HashMap<String, String> body){
        //TODO: check if nickname is null and if true throw Exception
        String nickname = body.get("nickname");

        userService.getUserByNick(nickname);

        String secret = env.getProperty("jwt.secret");

        return JWT.create()
                .withClaim("nickname", nickname)
                .withExpiresAt(Date.valueOf(LocalDate.now().plusDays(7)))
                .sign(Algorithm.HMAC256(secret));
    }

}
