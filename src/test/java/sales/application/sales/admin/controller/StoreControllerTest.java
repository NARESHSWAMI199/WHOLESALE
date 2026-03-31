package sales.application.sales.admin.controller;


import com.google.gson.Gson;
import com.sales.SalesApplication;
import com.sales.entities.*;
import com.sales.global.GlobalConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import sales.application.sales.testglobal.GlobalConstantTest;
import sales.application.sales.util.TestUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StoreControllerTest extends TestUtil {

    /**
     * @Note : We are not adding any new store in this test, but from userController we can create store and wholesale user
     */

    private String token;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
    }

    @Test
    public void testAddStoreWithStaffSlug() throws Exception {
        String randomEmail = UUID.randomUUID().toString().substring(0, 6) + "@mocktest.in";
        String randomPhone = getRandomMobileNumber();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        StoreSubCategory storeSubCategory = createStoreSubCategory();
        City city = createCity();
        String json = """
                    {
                        "userSlug" : "{userSlug}",
                        "storeName" : "Mock test store",
                        "storeEmail" : "{storeEmail}",
                        "description" : "test",
                        "categoryId" : "{categoryId}",
                        "subCategoryId"  : "{subCategoryId}",
                        "storePhone" : "{storePhone}",
                        "zipCode" : "302013",
                        "city" :  "{city}",
                        "state" : "{state}",
                        "street" : "1 Mock test jaipur"
                    }
                """
                .replace("{userSlug}", selfSlug)
                .replace("{storeEmail}", randomEmail)
                .replace("{storePhone}", randomPhone)
                .replace("{city}", String.valueOf(city.getId()))
                .replace("{state}", String.valueOf(city.getStateId()))
                .replace("{subCategoryId}", String.valueOf(storeSubCategory.getId()))
                .replace("{categoryId}", String.valueOf(storeSubCategory.getCategoryId()));

        Map<String, String> params = new Gson().fromJson(json, Map.class);
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/admin/store/add");
        params.forEach(requestBuilder::param);
        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().is((403)) // userSlug : userType must be wholesaler (W).
        );

    }


    @Test
    public void testUpdateStore() throws Exception {
        String randomEmail = UUID.randomUUID().toString().substring(0, 6) + "@mocktest.in";
        String randomPhone = getRandomMobileNumber();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        Store store = createStore();
        String json = """
                    {
                        "storeSlug" : "{storeSlug}",
                        "storeName" : "Mock test store",
                        "storeEmail" : "{storeEmail}",
                        "description" : "test",
                        "categoryId" : "{categoryId}",
                        "subCategoryId"  : "{subCategoryId}",
                        "storePhone" : "{storePhone}",
                        "zipCode" : "302013",
                        "city" : "{city}",
                        "state" : "{state}",
                        "street" : "1 Mock test jaipur"
                    }
                """
                .replace("{storeSlug}", store.getSlug())
                .replace("{storeEmail}", randomEmail)
                .replace("{storePhone}", randomPhone)
                .replace("{city}", String.valueOf(store.getAddress().getCity().getId()))
                .replace("{state}", String.valueOf(store.getAddress().getState().getId()))
                .replace("{categoryId}", String.valueOf(store.getStoreCategory().getId()))
                .replace("{subCategoryId}", String.valueOf(store.getStoreSubCategory().getId()));

        Map<String, String> params = new Gson().fromJson(json, Map.class);
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/admin/store/update");
        params.forEach(requestBuilder::param);
        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().is((200))
        );

    }


    @Test
    public void testGetAllStores() throws Exception {
        HttpHeaders headers = new HttpHeaders();

        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                    {
                    }
                """;

        mockMvc.perform(post("/admin/store/all")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.content", notNullValue())
        );

    }


    @Test
    public void testGetStoreDetail() throws Exception {
        Store store = createStore();
        HttpHeaders headers = new HttpHeaders();

        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(get("/admin/store/detail/" + store.getSlug())
                .headers(headers)
        ).andExpectAll(
                status().isOk()
        ).andDo(print());

    }


    @Test
    public void testGetStoreDetailWithWrongSlug() throws Exception {
        HttpHeaders headers = new HttpHeaders();

        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(get("/admin/store/detail/" + GlobalConstantTest.TEST_STORE_SLUG + "ddsf")
                .headers(headers)
        ).andExpectAll(
                status().is(404)
        ).andDo(print());

    }


    @Test
    public void testGetStoreDetailByUserWithWrongSlug() throws Exception {
        HttpHeaders headers = new HttpHeaders();

        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(get("/admin/store/detailbyuser/" + GlobalConstantTest.WHOLESALER_SLUG + "ddsf")
                .headers(headers)
        ).andExpectAll(
                status().is(400)
        ).andDo(print());

    }


    @Test
    public void testGetStoreDetailByUser() throws Exception {
        Store store = createStore();
        String email = UUID.randomUUID() + "@sales.com";
        ;
        String password = UUID.randomUUID().toString();
        String slug = UUID.randomUUID().toString();
        User user = createUser(slug, email, password, GlobalConstantTest.WHOLESALER);
        store.setUser(user);
        storeRepository.save(store);
        HttpHeaders headers = new HttpHeaders();

        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(get("/admin/store/detailbyuser/" + user.getSlug())
                .headers(headers)
        ).andExpectAll(
                status().is(200)
        ).andDo(print());

    }


    @Test
    public void testUpdateStoreStatusWithoutParams() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                }
                """;

        mockMvc.perform(post("/admin/store/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(406)
        ).andDo(print());

    }


    @Test
    public void testUpdateStoreStatusWithWrongSlug() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                    "slug" : "sdfsdfsd"
                }
                """;

        mockMvc.perform(post("/admin/store/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(400)
        ).andDo(print());

    }


    @Test
    public void testUpdateStoreStatus() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        Store store = createStore();

        String email = createRandomEmail();
        ;
        String password = UUID.randomUUID().toString();
        String slug = UUID.randomUUID().toString();
        User user = createUser(slug, email, password, GlobalConstantTest.WHOLESALER);
        store.setUser(user);
        storeRepository.save(store);

        // Make sure by default status is deactivated
        String json = """
                {
                    "slug" : "{slug}",
                    "status" : "A"
                }
                """
                .replace("{slug}", store.getSlug());

        mockMvc.perform(post("/admin/store/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(200)
        ).andDo(print());

    }


    @Test
    public void testGetStoreSubcategory() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                  {
                  }
                """;
        mockMvc.perform(post("/admin/store/subcategory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(406)
        );
    }


    @Test
    public void testGetStoreCategory() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                  {
                  }
                """;
        MvcResult result = mockMvc.perform(post("/admin/store/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                ).andExpectAll(
                        status().isOk()
                )
                .andDo(print())
                .andReturn();

        List categoryList = extractCategoryListFromResponse(result);
        if (!categoryList.isEmpty()) {
            Map<String, Object> categoryRequest = (Map<String, Object>) categoryList.get(0);
            Integer categoryId = (Integer) categoryRequest.get("id");

            // Getting subcategories also
            headers.set(GlobalConstant.AUTHORIZATION, token);
            String subCategoryJson = """
                      {
                        "categoryId" : {categoryId}
                      }
                    """
                    .replace("{categoryId}", categoryId + "");
            mockMvc.perform(post("/admin/item/subcategory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(subCategoryJson)
                    .headers(headers)
            ).andExpectAll(
                    status().is(200)
            );
        }
    }

    @Test
    public void testStoreCategoryAdd() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                  {
                  "category" : "Mock Test Store Category {random}",
                  "icon": "Mock test icon"
                  }
                """
                .replace("{random}", UUID.randomUUID().toString().substring(0, 6)); // randomly added due to duplicate category issue.
        mockMvc.perform(post("/admin/store/category/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(201)
        ).andDo(print());
    }


    @Test
    public void testStoreCategoryUpdate() throws Exception {
        StoreCategory storeCategory = createStoreCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                  {
                  "id" : {id},
                  "category" : "Mock Test Store updated Category {random}",
                  "icon": "Mock test icon"
                  }
                """.replace("{random}", UUID.randomUUID().toString().substring(0, 6))
                .replace("{id}", String.valueOf(storeCategory.getId())); // randomly added due to duplicate category issue.
        MvcResult result = mockMvc.perform(post("/admin/store/category/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                ).andExpectAll(
                        status().is(200)
                ).andDo(print())
                .andReturn();
        String slug = extractSlugFromResponseViaRes(result);

    }


    @Test
    public void testDeleteCategoryViaStaff() throws Exception {
        StoreCategory storeCategory = createStoreCategory();
        token = loginUser(GlobalConstantTest.STAFF);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                    "slug" : "{slug}"
                }
                """
                .replace("{slug}", storeCategory.getSlug());
        mockMvc.perform(post("/admin/store/category/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(403), // Only super admin can delete item's category
                jsonPath("$.message", is("Only super admin can delete a store category."))
        );
    }


    @Test
    public void testDeleteCategoryViaSuperAdmin() throws Exception {
        StoreCategory storeCategory = createStoreCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                    "slug" : "{slug}"
                }`
                """
                .replace("{slug}", storeCategory.getSlug()); // use @Test and use valid slug for separate test
        mockMvc.perform(post("/admin/store/category/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(200)
        );
    }


    @Test
    public void testDeleteSubcategoryViaStaff() throws Exception {
        StoreSubCategory storeSubCategory = createStoreSubCategory();
        token = loginUser(GlobalConstantTest.STAFF);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                    "slug" : "{slug}"
                }`
                """
                .replace("{slug}", storeSubCategory.getSlug()); // use @Test and use valid slug for separate test
        mockMvc.perform(post("/admin/store/subcategory/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(403)
        );
    }


    @Test
    public void testDeleteSubcategoryViaSuperAdmin() throws Exception {
        StoreSubCategory storeSubCategory = createStoreSubCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                    "slug" : "{slug}"
                }`
                """
                .replace("{slug}", storeSubCategory.getSlug()); // use @Test and use valid slug for separate test
        mockMvc.perform(post("/admin/store/subcategory/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(200)
        );
    }


    @Test
    public void testStoreSubSCategoryAdd() throws Exception {
        StoreCategory storeCategory = createStoreCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                  {
                    "categoryId" : {categoryId},
                    "subcategory" : "Mock test subcategory {random}",
                    "icon" : "test"
                  }
                """
                .replace("{categoryId}", String.valueOf(storeCategory.getId()))
                .replace("{random}", UUID.randomUUID().toString().substring(0, 6)); // random added due to duplicate category issue.
        mockMvc.perform(post("/admin/store/subcategory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(201)
        ).andDo(print());
    }


    @Test
    public void testStoreSubCategoryUpdate() throws Exception {
        StoreSubCategory storeSubCategory = createStoreSubCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                  {
                    "id" : {id},
                    "categoryId" : {categoryId},
                    "subcategory" : "Mock test subcategory {random}",
                    "icon" : "test"
                  }
                """
                .replace("{id}", String.valueOf(storeSubCategory.getId()))
                .replace("{categoryId}", String.valueOf(storeSubCategory.getCategoryId()))
                .replace("{random}", UUID.randomUUID().toString().substring(0, 6)); // randomly added due to duplicate category issue.
        mockMvc.perform(post("/admin/store/subcategory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                ).andExpectAll(
                        status().is(200)
                ).andDo(print())
                .andReturn();
    }


}
