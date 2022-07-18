package com.zse.chat.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zse.chat.user.UserNotFoundException;
import com.zse.chat.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment env;

    @MockBean
    private UserService userService;

    //region POST("/login")
    @Test
    public void shouldReturnCorrectJWTToken() throws Exception {
        String nickname = "testNickname1";
        String secret = env.getProperty("jwt.secret");
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        ObjectNode node = mapper.createObjectNode();
        node.put("nickname", nickname);
        String body = node.toString();

        when(userService.getUserByNick(nickname)).thenReturn(null);

        MvcResult result = mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        DecodedJWT decodedJWT = verifier.verify(result.getResponse().getContentAsString());
        Claim decodedClaims = decodedJWT.getClaims().get("nickname");
        String decodedNickname = decodedClaims.asString();

        assertThat(decodedNickname, equalTo(nickname));
    }

    @Test
    public void shouldThrowNotExistingUserByGivenNickname() throws Exception {
        String nickname = "testNickname1";

        when(userService.getUserByNick(nickname)).thenThrow(new UserNotFoundException(nickname));

        mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"nickname\": \"" + nickname + "\"}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode", equalTo(404)))
                .andExpect(jsonPath("$.exceptionMessage", containsString(nickname)));
    }

    @Test
    public void shouldThrownMissingArgumentNickname() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode", equalTo(400)))
                .andExpect(jsonPath("$.exceptionMessage", containsString("nickname")));
    }

    //endregion

}
