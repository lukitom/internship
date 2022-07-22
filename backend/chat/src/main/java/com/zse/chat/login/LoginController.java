package com.zse.chat.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.zse.chat.user.MissingPayloadFieldException;
import com.zse.chat.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.time.LocalDate;

@Tag(name = "Login")
@RequestMapping("/login")
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final Environment env;

    @PostMapping
    @Operation(summary = "Get JWT valid for 7 days")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = {
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            value = "{\"nickname\":\"string\"}",
                                            summary = "Minimal request"
                                    )
                            }
                    )
            }
    )
    public String login(@RequestBody JsonNode node) {
        if (!node.has("nickname")){
            throw new MissingPayloadFieldException("nickname");
        }
        final var nickname = node.get("nickname").asText();

        userService.getUserByNick(nickname);

        final var secret = env.getProperty("jwt.secret");

        return JWT.create()
                .withClaim("nickname", nickname)
                .withExpiresAt(Date.valueOf(LocalDate.now().plusDays(7)))
                .sign(Algorithm.HMAC256(secret));
    }

}
