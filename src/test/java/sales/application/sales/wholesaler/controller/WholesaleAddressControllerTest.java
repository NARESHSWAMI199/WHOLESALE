package sales.application.sales.wholesaler.controller;


import com.sales.SalesApplication;
import com.sales.admin.controllers.AddressController;
import com.sales.admin.services.StoreService;
import com.sales.entities.City;
import com.sales.entities.State;
import com.sales.global.GlobalConstant;
import com.sales.utils.WriteExcelUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sales.application.sales.testglobal.GlobalConstantTest;
import sales.application.sales.util.TestUtil;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WholesaleAddressControllerTest extends TestUtil {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AddressController addressController;

    @Autowired
    private StoreService storeService;

    @Autowired
    private WriteExcelUtil writeExcelUtil;

    @Value(value = "${excel.export.absolute}")
    private String excelPath;

    private String token;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.WHOLESALER);
    }

    @Test
    public void getStates() throws Exception {
        State state = State.builder()
                .stateName("Rajasthan")
                .status("A")
                .build();
        stateRepository.save(state);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.get("/wholesale/address/state")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().is(200)
        ).andDo(print());
    }


    @Test
    public void getCities() throws Exception {
        State state = State.builder()
                .stateName("Rajasthan")
                .status("A")
                .build();
        state = stateRepository.save(state);

        City city = City.builder()
                .cityName("Jaipur")
                .stateId(state.getId())
                .status("A")
                .build();
        cityRepository.save(city);

        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.get("/wholesale/address/city/" + state.getId())
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().is(200)
        ).andDo(print());
    }

}
