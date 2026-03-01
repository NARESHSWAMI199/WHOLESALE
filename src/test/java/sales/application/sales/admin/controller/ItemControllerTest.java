package sales.application.sales.admin.controller;


import com.sales.SalesApplication;
import com.sales.entities.Item;
import com.sales.entities.ItemCategory;
import com.sales.entities.ItemSubCategory;
import com.sales.entities.Store;
import com.sales.global.GlobalConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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


@SpringBootTest(classes=SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ItemControllerTest extends TestUtil {


    /** @Important : Here we created new functions for each Test, but all called in one function
     * If you want test to separately use @Test annotation and provide you custom slug.
     * */

    private String token;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
    }


    @Test
    public void testGetAllItems() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                    {}
                """;

        mockMvc.perform(post("/admin/item/all")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.numberOfElements", notNullValue())
        )


        ;
    }




    @Test
    public void testAddItem() throws Exception {
        Store store = createStore();
        ItemSubCategory itemSubCategory = createItemSubCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        MockMultipartFile file = getImageMultipartFileToUpload("newItemImages");
        List<MockMultipartFile> imageFiles = List.of(file);
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/admin/item/add");
        imageFiles.forEach(requestBuilder::file);
        MvcResult result = mockMvc.perform(requestBuilder
                        .param("name", "Mock test item updated")
                        .param("wholesaleSlug", store.getSlug())
                        .param("price", "100")
                        .param("discount", "10")
                        .param("rating", "0")
                        .param("description", "Mock test.")
                        .param("capacity", "1") // optional
                        .param("categoryId", String.valueOf(itemSubCategory.getCategoryId()))
                        .param("subCategoryId", String.valueOf(itemSubCategory.getId()))
                        .param("inStock", "Y")
                        .param("label", "N")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(201)
                )
                .andDo(print())
                .andReturn();

    }

    @Test
    public void testUpdateItem() throws Exception {
        ItemSubCategory itemSubCategory = createItemSubCategory();
        Store store = createStore();
        Item item = createItem(store.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        MockMultipartFile file = getImageMultipartFileToUpload("newItemImages");
        List<MockMultipartFile> imageFiles = List.of(file);
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/admin/item/update");
        imageFiles.forEach(requestBuilder::file);
        mockMvc.perform(requestBuilder
                        .param("slug", item.getSlug())
                        .param("name", "Mock test item")
                        .param("wholesaleSlug", store.getSlug())
                        .param("price", "20")
                        .param("discount", "10")
                        .param("rating", "0")
                        .param("description", "Mock test.")
                        .param("capacity", "1")
                        .param("categoryId", String.valueOf(itemSubCategory.getCategoryId()))
                        .param("subCategoryId", String.valueOf(itemSubCategory.getId()))
                        .param("price", "20")
                        .param("inStock", "Y")
                        .param("label", "N")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200)
                )
                .andDo(print());
    }


    @Test
    public void testUpdateStock() throws Exception {
        Store store = createStore();
        Item item = createItem(store.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);

        String json = """
                {
                "slug" : "{slug}",
                "stock" : "Y"
                }
                """
                .replace("{slug}",item.getSlug());
        mockMvc.perform(post("/admin/item/stock")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200)
                )
                .andDo(print());
    }



    @Test
    public void testUpdateStatus() throws Exception {
        Store store = createStore();
        Item item = createItem(store.getId());
        createStoreSubCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
//        String slug = "c626ff4b-0118-47d9-964a-a8b7f6ac8569";

        String json = """
                {
                "slug" : "{slug}",
                "status" : "A"
                }
                """
                .replace("{slug}",item.getSlug());
        mockMvc.perform(post("/admin/item/status")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200)
                )
                .andDo(print());
    }

    @Test
    public void testDeleteItemWithWrongSlug() throws Exception {
        String slug = "sdfsdfs";
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                {
                "slug" : "{slug}"
                }
                """
                .replace("{slug}",slug);
        mockMvc.perform(post("/admin/item/delete")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(400)
                )
                .andDo(print());
    }



    @Test
    public void testDeleteItem() throws Exception {
        Store store = createStore();
        Item item = createItem(store.getId());
        createStoreSubCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                {
                "slug" : "{slug}"
                }
                """
                .replace("{slug}",item.getSlug());
        mockMvc.perform(post("/admin/item/delete")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200)
                )
                .andDo(print());
    }


    @Test
    public void testGetSubcategory() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                  {
                  }
                """;
        mockMvc.perform(post("/admin/item/subcategory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(406)
        );
    }


    @Test
    public void testGetCategory() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                  {
                  }
                """;
        MvcResult result = mockMvc.perform(post("/admin/item/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                ).andExpectAll(
                        status().isOk()
                )
                .andDo(print())
                .andReturn();

        List categoryList = extractCategoryListFromResponse(result);
        if(!categoryList.isEmpty()) {
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
    public void testCategoryAdd() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                  {
                  "category" : "Mock Test Category {random}",
                  "icon": "Mock test icon"
                  }
                """
                .replace("{random}", UUID.randomUUID().toString().substring(0,6)); // random added due to duplicate category issue.
        mockMvc.perform(post("/admin/item/category/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(201)
        ).andDo(print());
    }


    @Test
    public void testCategoryUpdate() throws Exception {
        ItemCategory itemCategory = createItemCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                  {
                  "id" : {id},
                  "category" : "Mock update Test Category {random}",
                  "icon": "Mock test icon"
                  }
                """
                .replace("{id}",String.valueOf(itemCategory.getId()))
                .replace("{random}", UUID.randomUUID().toString().substring(0,6)); // randomly added due to duplicate category issue.
                mockMvc.perform(post("/admin/item/category/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                ).andExpectAll(
                        status().is(200)
                ).andDo(print())
                .andReturn();
    }


    @Test
    public void testSubCategoryAdd() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                  {
                    "categoryId" : 0,
                    "subcategory" : "Mock test subcategory {random}",
                    "unit" : "kg",
                    "icon" : "test"
                  }
                """
                .replace("{random}", UUID.randomUUID().toString().substring(0,6)); // random added due to duplicate category issue.
        mockMvc.perform(post("/admin/item/subcategory/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(201)
        ).andDo(print());
    }



    @Test
    public void testSubCategoryUpdate() throws Exception {
        ItemSubCategory itemSubCategory = createItemSubCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                  {
                    "id" : {id},
                    "categoryId" : 0,
                    "subcategory" : "Mock test subcategory {random}",
                    "unit" : "liter",
                    "icon" : "test"
                  }
                """
                .replace("{id}",String.valueOf(itemSubCategory.getId()))
                .replace("{random}", UUID.randomUUID().toString().substring(0,6)); // randomly added due to duplicate category issue.
                mockMvc.perform(post("/admin/item/subcategory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                ).andExpectAll(
                        status().is(200)
                ).andDo(print())
                .andReturn();
    }



    @Test
    public void testDeleteCategoryViaStaff() throws Exception {
        ItemCategory itemCategory = createItemCategory();
        token = loginUser(GlobalConstantTest.STAFF);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                {
                    "slug" : "{slug}"
                }
                """
                .replace("{slug}",itemCategory.getSlug());
        mockMvc.perform(post("/admin/item/category/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(403), // Only super admin can delete item's category
                jsonPath("$.message",is("Only super admin can delete item's category."))
        );
    }


    @Test
    public void testDeleteCategoryViaSuperAdmin() throws Exception {
        ItemCategory itemCategory = createItemCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                {
                    "slug" : "{slug}"
                }`
                """
                .replace("{slug}",itemCategory.getSlug()); // use @Test and use valid slug for separate test
        mockMvc.perform(post("/admin/item/category/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(200)
        );
    }

    @Test
    public void testDeleteSubcategoryViaStaff() throws Exception {
        token = loginUser(GlobalConstantTest.STAFF);
        ItemSubCategory itemSubCategory = createItemSubCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                {
                    "slug" : "{slug}"
                }`
                """
                .replace("{slug}",itemSubCategory.getSlug()); // use @Test and use valid slug for separate test
        mockMvc.perform(post("/admin/item/subcategory/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(403)
        );
    }


    @Test
    public void testDeleteSubcategoryViaSuperAdmin() throws Exception {
        ItemSubCategory itemSubCategory = createItemSubCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization",token);
        String json = """
                {
                    "slug" : "{slug}"
                }`
                """
                .replace("{slug}",itemSubCategory.getSlug()); // use @Test and use valid slug for separate test
        mockMvc.perform(post("/admin/item/subcategory/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .headers(headers)
        ).andExpectAll(
                status().is(200)
        );
    }

    @Test
    public void testGetItemDetail() throws Exception {
        Store store = createStore();
        Item item = createItem(store.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(get("/admin/item/detail/{slug}", item.getSlug())
                .headers(headers)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.message", is("success")),
                jsonPath("$.res", notNullValue())
        );
    }

    @Test
    public void testGetItemDetailNotFound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(get("/admin/item/detail/{slug}", "nonexistent")
                .headers(headers)
        ).andExpectAll(
                status().is(400),
                jsonPath("$.message", is("Item Not Found"))
        );
    }

    @Test
    public void testGetAllMeasuringUnits() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(get("/admin/item/units")
                .headers(headers)
        ).andExpectAll(
                status().isOk()
        );
    }

    @Test
    public void testGetCategoryById() throws Exception {
        ItemCategory itemCategory = createItemCategory();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(get("/admin/item/category/{categoryId}", itemCategory.getId())
                .headers(headers)
        ).andExpectAll(
                status().isOk()
        );
    }

    @Test
    public void testDownloadUpdateTemplate() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(get("/admin/item/download/update/template")
                .headers(headers)
        ).andExpectAll(
                status().is(404) // file not found
        );
    }

    @Test
    public void testGetFile() throws Exception {
        // This might need actual file setup, but for now, expect 500 or something
        mockMvc.perform(get("/admin/item/image/{slug}/{filename}", "testslug", "test.jpg")
        ).andExpectAll(
                status().is(404) // file not found
        );
    }

    // Add more tests for import/export if possible, but they might be complex

}


