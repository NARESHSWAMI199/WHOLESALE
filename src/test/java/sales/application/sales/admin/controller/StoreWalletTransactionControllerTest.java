package sales.application.sales.admin.controller;

import com.sales.SalesApplication;
import com.sales.request.WalletTransactionRequest;
import com.sales.entities.WalletTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import sales.application.sales.util.TestUtil;

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StoreWalletTransactionControllerTest extends TestUtil {

    @Autowired
    private com.sales.admin.services.StoreWalletTransactionService storeWalletTransactionService;

    private String token;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser("SA");
    }

    @Test
    public void getAllTransactionsByUser() throws Exception {
        String slug = UUID.randomUUID().toString();
        var user = createUser(slug, createRandomEmail(), "pw", "W");

        // add a transaction
        WalletTransaction wt = storeWalletTransactionService.addWalletTransaction(WalletTransactionRequest.builder().amount(100f).transactionType("CR").status("S").build(), user.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);

        String json = "{}";

        mockMvc.perform(post("/admin/store/wallet/transactions/all/" + user.getSlug()).contentType(MediaType.APPLICATION_JSON).content(json).headers(headers))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.numberOfElements", notNullValue()))
                .andDo(print());
    }

    @Test
    public void getTransactionsForMissingUser() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        String json = "{}";

        mockMvc.perform(post("/admin/store/wallet/transactions/all/invalid-slug").contentType(MediaType.APPLICATION_JSON).content(json).headers(headers))
                .andExpect(status().is(404))
                .andDo(print());
    }

}
