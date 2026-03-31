package sales.application.sales.chats.services;

import com.sales.SalesApplication;
import com.sales.chats.services.ChatService;
import com.sales.claims.SalesUser;
import com.sales.entities.User;
import com.sales.exceptions.MyException;
import com.sales.request.MessageDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import sales.application.sales.util.TestUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatServiceTest extends TestUtil {

    @Autowired
    private ChatService chatService;

    @MockBean
    private com.sales.wholesaler.repository.WholesaleUserRepository wholesaleUserRepository;

    @MockBean
    private com.sales.chats.services.ChatUserService chatUserService;

    @MockBean
    private com.sales.chats.services.BlockListService blockListService;

    @Test
    public void sendMessage_happyPath_savesAndMarksSent() throws Exception {
        User receiver = createUser(java.util.UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");
        User sender = createUser(java.util.UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");

        when(wholesaleUserRepository.findUserBySlug(receiver.getSlug())).thenReturn(receiver);
        when(blockListService.isSenderBlockedByReceiver(any(), any())).thenReturn(false);
        when(blockListService.isReceiverBlockedBySender(any(), any())).thenReturn(false);

        MessageDto dto = new MessageDto();
        dto.setMessage("hello");
        dto.setCreatedAt(System.currentTimeMillis());

        var savedChat = chatService.sendMessage(dto, new SalesUser(sender), receiver.getSlug());
        assertThat(savedChat).isNotNull();
        assertThat(savedChat.getIsSent()).isEqualTo("S");
    }

    @Test
    public void verifyBeforeSend_throwsWhenRecipientMissing() {
        User sender = createUser(java.util.UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");
        when(wholesaleUserRepository.findUserBySlug(anyString())).thenReturn(null);

        assertThrows(MyException.class, () -> chatService.verifyBeforeSend(new SalesUser(sender), null));
    }

    @Test
    public void saveAllImages_acceptsValidImage() throws Exception {
        User sender = createUser(java.util.UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");
        // Using a small png in resources (test resources should have image added) ; fallback to creating bytes
        MockMultipartFile file = new MockMultipartFile("images", "test-image.png", "image/png", "abc".getBytes());

        MessageDto dto = new MessageDto();
        dto.setReceiver("r" + java.util.UUID.randomUUID());
        dto.setImages(List.of(file));

        var saved = chatService.saveAllImages(dto, new SalesUser(sender));
        assertThat(saved).isNotNull();
        assertThat(saved).isNotEmpty();
    }

    @Test
    public void saveAllImages_invalidImage_throws() throws Exception {
        User sender = createUser(java.util.UUID.randomUUID().toString(), createRandomEmail(), "pw", "W");
        MockMultipartFile file = new MockMultipartFile("images", "test.txt", "text/plain", "abc".getBytes());
        MessageDto dto = new MessageDto();
        dto.setReceiver("r" + java.util.UUID.randomUUID());
        dto.setImages(List.of(file));

        assertThrows(MyException.class, () -> chatService.saveAllImages(dto, new SalesUser(sender)));
    }
}
