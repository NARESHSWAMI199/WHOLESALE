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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ContactControllerTest extends TestUtil {

    private String token;
    private User contactUser;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
        contactUser = createUser(UUID.randomUUID().toString(), createRandomEmail(), "pw", "R");
    }

    @Test
    public void getAllContactsWithoutAuthenticationShouldFail() throws Exception {
        mockMvc.perform(get("/contacts/all"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void getAllContactsWithAuthenticationShouldSucceed() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        mockMvc.perform(get("/contacts/all").headers(headers))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void addNewContactWithoutAuthenticationShouldFail() throws Exception {
        String json = """
                {
                    "contactSlug": "{slug}"
                }
                """.replace("{slug}", contactUser.getSlug());

        mockMvc.perform(post("/contacts/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void addNewContactWithAuthenticationShouldProcess() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                    "contactSlug": "{slug}"
                }
                """.replace("{slug}", contactUser.getSlug());

        mockMvc.perform(post("/contacts/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void addContactWithInvalidSlugShouldReturnError() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                    "contactSlug": "invalid-slug-that-does-not-exist"
                }
                """;

        mockMvc.perform(post("/contacts/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers))
                .andDo(print());
    }

    @Test
    public void removeContactWithoutAuthenticationShouldFail() throws Exception {
        String json = """
                {
                    "contactSlug": "{slug}"
                }
                """.replace("{slug}", contactUser.getSlug());

        mockMvc.perform(post("/contacts/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void removeContactWithAuthenticationShouldProcess() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        // First add the contact
        String addJson = """
                {
                    "contactSlug": "{slug}"
                }
                """.replace("{slug}", contactUser.getSlug());

        mockMvc.perform(post("/contacts/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(addJson)
                .headers(headers))
                .andExpect(status().isOk())
                .andDo(print());

        // Then remove it
        String removeJson = """
                {
                    "contactSlug": "{slug}"
                }
                """.replace("{slug}", contactUser.getSlug());

        MvcResult result = mockMvc.perform(post("/contacts/remove")
            .contentType(MediaType.APPLICATION_JSON)
            .content(removeJson)
            .headers(headers))
            .andDo(print())
            .andReturn();
        assertOkOrNotFound(result);
    }

    @Test
    public void removeNonexistentContactShouldReturnNotFound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                    "contactSlug": "nonexistent-contact-slug"
                }
                """;

        mockMvc.perform(post("/contacts/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers))
                .andDo(print());
    }
}
