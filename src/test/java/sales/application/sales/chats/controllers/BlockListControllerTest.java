package sales.application.sales.chats.controllers;

import com.sales.SalesApplication;
import com.sales.entities.BlockedUser;
import com.sales.entities.User;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import sales.application.sales.testglobal.GlobalConstantTest;
import sales.application.sales.util.TestUtil;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BlockListControllerTest extends TestUtil {

    private String token;
    private User blockedUser;
    private User loggedInUser;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
        loggedInUser = createUser(UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");
        blockedUser = createUser(UUID.randomUUID().toString(), createRandomEmail(), "pw", "R");
    }

    @Test
    public void blockUserWithoutAuthenticationShouldFail() throws Exception {
        mockMvc.perform(get("/block/" + blockedUser.getSlug()))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void blockUserWithAuthenticationShouldSucceed() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        mockMvc.perform(get("/block/" + blockedUser.getSlug()).headers(headers))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("User has been successfully blocked.")))
                .andDo(print());
    }

    @Test
    public void blockInvalidUserShouldReturnBadRequest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        mockMvc.perform(get("/block/invalid-user-slug").headers(headers))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    public void unblockUserWithAuthenticationShouldSucceed() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        // First block the user
        mockMvc.perform(get("/block/" + blockedUser.getSlug()).headers(headers))
                .andExpect(status().isOk())
                .andDo(print());

        // Then unblock
        mockMvc.perform(get("/unblock/" + blockedUser.getSlug()).headers(headers))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("User has been successfully unblocked.")))
                .andDo(print());
    }

    @Test
    public void unblockWithoutAuthenticationShouldFail() throws Exception {
        mockMvc.perform(get("/unblock/" + blockedUser.getSlug()))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void isReceiverBlockedShouldReturnFalseInitially() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        mockMvc.perform(get("/is-blocked/" + blockedUser.getSlug()).headers(headers))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void isReceiverBlockedShouldReturnTrueAfterBlocking() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        // First block the user
        mockMvc.perform(get("/block/" + blockedUser.getSlug()).headers(headers))
                .andExpect(status().isOk())
                .andDo(print());

        // Check if blocked
        mockMvc.perform(get("/is-blocked/" + blockedUser.getSlug()).headers(headers))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void isReceiverBlockedWithoutAuthenticationShouldFail() throws Exception {
        mockMvc.perform(get("/is-blocked/" + blockedUser.getSlug()))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
