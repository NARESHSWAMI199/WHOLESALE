package sales.application.sales.wholesaler.controller;


import com.google.gson.Gson;
import com.sales.SalesApplication;
import com.sales.entities.City;
import com.sales.entities.StoreSubCategory;
import com.sales.global.GlobalConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import sales.application.sales.testglobal.GlobalConstantTest;
import sales.application.sales.util.TestUtil;

import java.util.Map;
import java.util.UUID;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WholesaleStoreControllerTest extends TestUtil {

    private String token;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.WHOLESALER);
    }


    @Test
    public void testAddStore() throws Exception {
        StoreSubCategory storeSubCategory = createStoreSubCategory();
        City city = createCity();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String randomEmail = UUID.randomUUID().toString().substring(0, 6) + "@mocktest.in";
        String randomPhone = getRandomMobileNumber();
        MockMultipartFile file = getImageMultipartFileToUpload("storePic");
        String json = """
                {
                  "storeEmail": "{email}",
                  "storeName": "Mock test wholesaler",
                  "storePhone": "{phone}",
                  "description": "Mock test store created by mock test user",
                  "street": "Murlipura",
                  "zipCode": "302013",
                  "city": "{city}",
                  "state": "{state}",
                  "categoryId" : "{categoryId}",
                  "subCategoryId" : "{subCategoryId}"
                }
                """
                .replace("{email}", randomEmail)
                .replace("{phone}", randomPhone)
                .replace("{city}", String.valueOf(city.getId()))
                .replace("{state}", String.valueOf(city.getStateId()))
                .replace("{categoryId}", String.valueOf(storeSubCategory.getCategoryId()))
                .replace("{subCategoryId}", String.valueOf(storeSubCategory.getId()));

        Map<String, String> params = new Gson().fromJson(json, Map.class);
        MockMultipartHttpServletRequestBuilder multipart = MockMvcRequestBuilders.multipart("/wholesale/store/add");
        params.forEach(multipart::param);
        mockMvc.perform(multipart
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .headers(headers)
        ).andExpectAll(
                // TODO : If user don't have store then change it to 200
                MockMvcResultMatchers.status().is(409)
        ).andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUpdateStore() throws Exception {
        StoreSubCategory storeSubCategory = createStoreSubCategory();
        City city = createCity();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String randomEmail = UUID.randomUUID().toString().substring(0, 6) + "@mocktest.in";
        String randomPhone = getRandomMobileNumber();
        MockMultipartFile file = getImageMultipartFileToUpload("storePic");
        String json = """
                {
                  "storeSlug" : "{storeSlug}",
                  "storeEmail": "{email}",
                  "storeName": "Mock test wholesaler updated",
                  "storePhone": "{phone}",
                  "description": "Mock test store created by mock test user",
                  "street": "Murlipura updated",
                  "zipCode": "302013",
                  "city": "{city}",
                  "state": "{state}",
                  "categoryId" : "{categoryId}",
                  "subCategoryId" : "{subCategoryId}"
                }
                """
                .replace("{storeSlug}", storeSlug)
                .replace("{email}", randomEmail)
                .replace("{phone}", randomPhone)
                .replace("{city}", String.valueOf(city.getId()))
                .replace("{state}", String.valueOf(city.getStateId()))
                .replace("{categoryId}", String.valueOf(storeSubCategory.getCategoryId()))
                .replace("{subCategoryId}", String.valueOf(storeSubCategory.getId()));

        Map<String, String> params = new Gson().fromJson(json, Map.class);
        MockMultipartHttpServletRequestBuilder multipart = MockMvcRequestBuilders.multipart("/wholesale/store/update");
        params.forEach(multipart::param);
        mockMvc.perform(multipart
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .headers(headers)
        ).andExpectAll(
                MockMvcResultMatchers.status().is(200)
        ).andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUpdateNotifications() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                    "seenIds" : [0,0]
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/wholesale/store/update/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                MockMvcResultMatchers.status().is(200)
        ).andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void testUpdateNotificationsWithoutParams() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/wholesale/store/update/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                MockMvcResultMatchers.status().is(406)
        ).andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testNotifications() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {}
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/wholesale/store/notifications")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                ).andExpectAll(
                        MockMvcResultMatchers.status().isOk()
                )
                .andDo(MockMvcResultHandlers.print())

        ;
    }

    @Test
    public void testGetWholesaleStoreCategory() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(MockMvcRequestBuilders.get("/wholesale/store/category")
                .headers(headers)
        ).andExpectAll(
                MockMvcResultMatchers.status().isOk()
        ).andReturn();

    }

    @Test
    public void testGetWholesaleStoreSubcategory() throws Exception {
        StoreSubCategory storeSubCategory = createStoreSubCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(MockMvcRequestBuilders.get("/wholesale/store/subcategory/" + storeSubCategory.getId())
                .headers(headers)
        ).andExpectAll(
                MockMvcResultMatchers.status().isOk()
        );
    }


}
