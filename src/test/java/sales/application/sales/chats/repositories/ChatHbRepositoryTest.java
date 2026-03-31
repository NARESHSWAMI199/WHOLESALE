package sales.application.sales.chats.repositories;

import com.sales.SalesApplication;
import com.sales.chats.repositories.ChatHbRepository;
import com.sales.chats.repositories.ChatRepository;
import com.sales.entities.Chat;
import com.sales.request.MessageDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sales.application.sales.util.TestUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SalesApplication.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatHbRepositoryTest extends TestUtil {

    @Autowired
    private ChatHbRepository chatHbRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Test
    public void testUpdateMessageToSent_updatesRecord() {
        Chat chat = Chat.builder()
                .sender("s" + java.util.UUID.randomUUID())
                .receiver("r" + java.util.UUID.randomUUID())
                .message("to be sent")
                .createdAt(System.currentTimeMillis())
                .isSent("P")
                .build();
        chat = chatRepository.save(chat);

        boolean updated = chatHbRepository.updateMessageToSent(chat.getId());
        assertThat(updated).isTrue();

        Chat fetched = chatRepository.findById(chat.getId()).orElseThrow();
        assertThat(fetched.getIsSent()).isEqualTo("S");
    }

    @Test
    public void testDeleteChats_marksAppropriateFlags() {
        String sender = "s" + java.util.UUID.randomUUID();
        String receiver = "r" + java.util.UUID.randomUUID();

        Chat a = Chat.builder().sender(sender).receiver(receiver).message("a").createdAt(System.currentTimeMillis()).build();
        Chat b = Chat.builder().sender(receiver).receiver(sender).message("b").createdAt(System.currentTimeMillis() + 1).build();
        chatRepository.save(a);
        chatRepository.save(b);

        chatHbRepository.deleteChats(sender, receiver);

        List<Chat> pairs = chatRepository.getChatBySenderKeyOrReceiverKey(sender, receiver);
        assertThat(pairs).isNotEmpty();
        boolean foundSenderDeleted = pairs.stream().anyMatch(c -> sender.equals(c.getSender()) && "Y".equals(c.getIsSenderDeleted()));
        boolean foundReceiverDeleted = pairs.stream().anyMatch(c -> receiver.equals(c.getSender()) && "Y".equals(c.getIsReceiverDeleted()));
        assertThat(foundSenderDeleted).isTrue();
        assertThat(foundReceiverDeleted).isTrue();
    }

    @Test
    public void testDeleteChat_withFlags_andId() {
        String sender = "s" + java.util.UUID.randomUUID();
        String receiver = "r" + java.util.UUID.randomUUID();
        Chat chat = Chat.builder().sender(sender).receiver(receiver).message("capt").createdAt(System.currentTimeMillis()).build();
        chat = chatRepository.save(chat);

        MessageDto dto = new MessageDto();
        dto.setSender(sender);
        dto.setReceiver(receiver);
        dto.setId(chat.getId());
        dto.setIsSenderDeleted("Y");

        int count = chatHbRepository.deleteChat(dto);
        assertThat(count).isGreaterThan(0);

        Chat fetched = chatRepository.findById(chat.getId()).orElseThrow();
        assertThat(fetched.getIsSenderDeleted()).isEqualTo("Y");
    }
}
