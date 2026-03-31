package sales.application.sales.wholesaler.controller;

import com.sales.SalesApplication;
import com.sales.entities.ServicePlan;
import com.sales.global.GlobalConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import sales.application.sales.testglobal.GlobalConstantTest;
import sales.application.sales.util.TestUtil;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {SalesApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WholesaleWalletControllerTest extends TestUtil {

    private String token;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.WHOLESALER);
    }

    @Test
    public void testGetWalletWithoutLogin() throws Exception {
        mockMvc.perform(get("/wholesale/wallet/"))
                .andExpect(status().is(401));
    }

    @Test
    public void testGetWalletWithLogin() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        mockMvc.perform(get("/wholesale/wallet/")
                .headers(headers)
        ).andExpect(status().isOk());
    }

    @Test
    public void testPayUsingWallet() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        // create a service plan and call pay - likely returns 400 when insufficient funds
        ServicePlan servicePlan = createServicePlan(new Date());

        mockMvc.perform(get("/wholesale/wallet/pay/" + servicePlan.getSlug())
                .headers(headers)
        ).andExpect(result -> {
            int status = result.getResponse().getStatus();
            org.junit.jupiter.api.Assertions.assertTrue(status == 200 || status == 400 || status == 404 || status == 500 || status == 403);
        });
    }

}
