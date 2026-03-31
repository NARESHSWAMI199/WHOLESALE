package sales.application.sales.chats.controllers;

import com.sales.SalesApplication;
import com.sales.entities.Chat;
import com.sales.entities.User;
import com.sales.request.MessageDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import sales.application.sales.util.TestUtil;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatControllerTest extends TestUtil {

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.sales.chats.services.ChatService chatService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.sales.wholesaler.services.WholesaleUserService wholesaleUserService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Test
    public void getAllChats_returnsMap() throws Exception {
        User principalUser = createUser(java.util.UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");
        principalUser.setAuthorities(java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_WHOLESALER")));
        var auth = new org.springframework.security.authentication.TestingAuthenticationToken(new com.sales.claims.SalesUser(principalUser), null, "ROLE_WHOLESALER");

        when(chatService.getAllChatBySenderAndReceiverKey(org.mockito.ArgumentMatchers.any(com.sales.request.MessageDto.class), org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletRequest.class))).thenReturn(Map.of());

        String json = "{\"receiver\":\"abc\", \"createdAt\": 0}";

        mockMvc.perform(MockMvcRequestBuilders.post("/chats/all")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(chatService, times(1)).getAllChatBySenderAndReceiverKey(org.mockito.ArgumentMatchers.any(com.sales.request.MessageDto.class), org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    public void getParentChatMessageById_returnsChat() throws Exception {
        User principalUser = createUser(java.util.UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");
        principalUser.setAuthorities(java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_WHOLESALER")));
        var auth = new org.springframework.security.authentication.TestingAuthenticationToken(new com.sales.claims.SalesUser(principalUser), null, "ROLE_WHOLESALER");

        Chat chat = Chat.builder().id(999).message("hello").sender("s").receiver("r").build();
        when(chatService.getParentMessageById(eq(999L), org.mockito.ArgumentMatchers.any(com.sales.claims.AuthUser.class), org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletRequest.class))).thenReturn(chat);

        mockMvc.perform(MockMvcRequestBuilders.get("/chats/message/999")
                        .with(authentication(auth))
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("hello"));

        verify(chatService, times(1)).getParentMessageById(eq(999L), org.mockito.ArgumentMatchers.any(com.sales.claims.AuthUser.class), org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    public void uploadImages_success_flow() throws Exception {
        User principalUser = createUser(java.util.UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");
        principalUser.setAuthorities(java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_WHOLESALER")));
        var auth = new org.springframework.security.authentication.TestingAuthenticationToken(new com.sales.claims.SalesUser(principalUser), null, "ROLE_WHOLESALER");

        User receiver = createUser(java.util.UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");
        when(wholesaleUserService.findUserBySlug(org.mockito.ArgumentMatchers.anyString())).thenReturn(receiver);
        when(chatService.verifyBeforeSend(org.mockito.ArgumentMatchers.any(com.sales.claims.AuthUser.class), org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
        when(chatService.saveAllImages(org.mockito.ArgumentMatchers.any(com.sales.request.MessageDto.class), org.mockito.ArgumentMatchers.any(com.sales.claims.AuthUser.class))).thenReturn(List.of("img.png"));

        MessageDto returned = new MessageDto();
        returned.setId(123L);
        when(chatService.addImagesList(org.mockito.ArgumentMatchers.any(com.sales.request.MessageDto.class), org.mockito.ArgumentMatchers.any(jakarta.servlet.http.HttpServletRequest.class), org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.any(com.sales.claims.AuthUser.class), org.mockito.ArgumentMatchers.anyString())).thenReturn(returned);

        MockMultipartFile image = new MockMultipartFile("images", "img.png", "image/png", "abc".getBytes());

        MvcResult res = mockMvc.perform(MockMvcRequestBuilders.multipart("/chat/upload")
                        .file(image)
                        .with(authentication(auth))
                        .param("receiver", receiver.getSlug())
                )
                .andExpect(status().isOk())
                .andReturn();

        String body = res.getResponse().getContentAsString();
        assertThat(body).contains("All images successfully sent");

        verify(chatService, times(1)).saveAllImages(org.mockito.ArgumentMatchers.any(com.sales.request.MessageDto.class), org.mockito.ArgumentMatchers.any(com.sales.claims.AuthUser.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    public void deleteMessage_callsDelete_and_returnsStatus() throws Exception {
        User principalUser = createUser(java.util.UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");
        principalUser.setAuthorities(java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_WHOLESALER")));
        var auth = new org.springframework.security.authentication.TestingAuthenticationToken(new com.sales.claims.SalesUser(principalUser), null, "ROLE_WHOLESALER");

        when(chatService.deleteMessage(any(), any())).thenReturn(1);

        String json = "{\"id\": 1, \"isDeleted\": \"SY\", \"sender\": \"s\", \"receiver\": \"r\" }";

        mockMvc.perform(MockMvcRequestBuilders.post("/chat/delete")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Deleted successfully."));

        verify(chatService, times(1)).deleteMessage(any(), any());
    }
}
