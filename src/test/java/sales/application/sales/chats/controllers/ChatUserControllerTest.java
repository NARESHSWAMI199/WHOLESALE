package sales.application.sales.chats.controllers;

import com.sales.SalesApplication;
import com.sales.entities.User;
import com.sales.global.GlobalConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import sales.application.sales.testglobal.GlobalConstantTest;
import sales.application.sales.util.TestUtil;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatUserControllerTest extends TestUtil {

    private String token;
    private User chatPartner;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
        chatPartner = createUser(UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");
    }

    @Test
    public void getAllChatUsersWithoutAuthenticationShouldFail() throws Exception {
        mockMvc.perform(get("/chat-users/all"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void getAllChatUsersWithAuthenticationShouldSucceed() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        mockMvc.perform(get("/chat-users/all").headers(headers))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void checkChatRequestAcceptanceWithoutAuthenticationShouldFail() throws Exception {
        mockMvc.perform(get("/chat-users/is-accepted/" + chatPartner.getSlug()))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void checkChatRequestAcceptanceWithAuthenticationShouldSucceed() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        MvcResult result = mockMvc.perform(get("/chat-users/is-accepted/" + chatPartner.getSlug()).headers(headers))
                .andDo(print())
                .andReturn();
        assertOkOrNotFound(result);
    }

    @Test
    public void addNewChatUserWithoutAuthenticationShouldFail() throws Exception {
        String json = """
                {
                    "contactSlug": "{slug}"
                }
                """.replace("{slug}", chatPartner.getSlug());

        mockMvc.perform(post("/chat-users/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void addNewChatUserWithAuthenticationShouldProcess() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                    "contactSlug": "{slug}"
                }
                """.replace("{slug}", chatPartner.getSlug());

        mockMvc.perform(post("/chat-users/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void removeChatUserWithoutAuthenticationShouldFail() throws Exception {
        String json = """
                {
                    "contactSlug": "{slug}",
                    "deleteChats": false
                }
                """.replace("{slug}", chatPartner.getSlug());

        mockMvc.perform(post("/chat-users/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void removeChatUserWithAuthenticationShouldProcess() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                    "contactSlug": "{slug}",
                    "deleteChats": false
                }
                """.replace("{slug}", chatPartner.getSlug());

        MvcResult result = mockMvc.perform(post("/chat-users/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers))
                .andDo(print())
                .andReturn();
        assertOkOrNotFound(result);
    }

    @Test
    public void updateChatAcceptStatusWithoutAuthenticationShouldFail() throws Exception {
        String json = """
                {
                    "slug": "test-slug",
                    "accepted": true
                }
                """;

        mockMvc.perform(post("/chat-users/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void updateChatAcceptStatusWithAuthenticationShouldProcess() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                    "slug": "test-slug",
                    "accepted": true
                }
                """;

        mockMvc.perform(post("/chat-users/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers))
                .andDo(print());
    }
}
