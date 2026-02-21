package sales.application.sales.chats.controllers;

import com.sales.SalesApplication;
import com.sales.dto.ChatRoomDto;
import com.sales.entities.ChatRoom;
import com.sales.global.ConstantResponseKeys;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatRoomControllerTest extends TestUtil {

    private String token;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
    }

    @Test
    public void getAllChatRoomsShouldReturnList() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(get("/chat_room/all").headers(headers))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void addNewChatRoomWithValidDataShouldSucceed() throws Exception {
        String json = """
                {
                    "name": "Test Chat Room",
                    "description": "This is a test chat room",
                    "users": []
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        mockMvc.perform(post("/chat_room/add")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(json))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.roomId").exists())
                .andDo(print());
    }

    @Test
    public void addNewChatRoomWithNullUsersShouldFailValidation() throws Exception {
        String json = """
                {
                    "name": "Test Chat Room",
                    "description": "This is a test chat room",
                    "users": null
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        MvcResult result = mockMvc.perform(post("/chat_room/add")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print())
                .andReturn();
        
        System.out.println("Response Status: " + result.getResponse().getStatus());
        System.out.println("Response Body: " + result.getResponse().getContentAsString());
    }

    @Test
    public void addNewChatRoomWithInvalidDataShouldFail() throws Exception {
        String json = """
                {
                    "name": "",
                    "description": ""
                }
                """;

        mockMvc.perform(post("/chat_room/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    public void updateChatRoomWithoutAuthenticationShouldFail() throws Exception {
        String json = """
                {
                    "slug": "test-slug",
                    "name": "Updated Room",
                    "description": "Updated description"
                }
                """;

        mockMvc.perform(post("/chat_room/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    public void updateChatRoomWithAuthenticationShouldProcess() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                    "slug": "test-room-slug",
                    "name": "Updated Room",
                    "description": "Updated description"
                }
                """;

        mockMvc.perform(post("/chat_room/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers))
                .andDo(print());
    }

    @Test
    public void updateNonexistentChatRoomShouldReturn404() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                    "slug": "nonexistent-slug",
                    "name": "Updated Room",
                    "description": "Updated description"
                }
                """;

        mockMvc.perform(post("/chat_room/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }
}
