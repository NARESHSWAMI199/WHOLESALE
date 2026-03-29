package sales.application.sales.admin.controller;


import com.sales.SalesApplication;
import com.sales.admin.services.UserService;
import com.sales.entities.Group;
import com.sales.entities.Store;
import com.sales.entities.StoreSubCategory;
import com.sales.entities.User;
import com.sales.global.ConstantResponseKeys;
import com.sales.global.GlobalConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import sales.application.sales.testglobal.GlobalConstantTest;
import sales.application.sales.util.TestUtil;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest extends TestUtil {


    private String token;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
    }



    @Test
    void testLoginWithRightPassword() throws Exception {

        String email = createRandomEmail();;
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        User user = createUser(slug,email, password, GlobalConstantTest.STAFF);

        String userJson = """
            {
                "email" : "{email}",
                "password" : "{password}"
            }
            """.replace("{email}",user.getEmail())
                .replace("{password}",user.getPassword());

        mockMvc.perform(post("/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email", is(email)))
                .andDo(print());
    }


    @Test
    void testLoginWithWrongPassword() throws Exception {
        String email = createRandomEmail();;
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        User user = createUser(slug,email, password, GlobalConstantTest.STAFF);

        String userJson = """
            {
                "email" : "{email}",
                "password" : "{password}"
            }
            """.replace("{email}",user.getEmail())
                .replace("{password}",UUID.randomUUID().toString());

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().is(401))
                .andDo(print());
    }


    @Test
    void testLoginWithWrongPasswordWithoutParams() throws Exception {
        String userJson = """
            {
                "email" : "",
                "password" : ""
            }
            """;

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.message", is("Invalid Credentials !")))
                .andDo(print());
    }


    @Test
    void testLoginWithWrongPasswordWithNullParams() throws Exception {
        String userJson = """
            { }
            """;

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().is(401))
                .andDo(print());
    }



    // =========================  ADD OR UPDATE USER ================================


    @Test
    public void addUserRetailerWithoutLogin() throws Exception {
        String randomEmail = UUID.randomUUID().toString().substring(0,6) + "@mocktest.in";
        String randomPhone = getRandomMobileNumber();
        String json = """
                {
                    "email" : "{email}",
                    "username" : "naresh swami",
                    "userType"  : "R",
                    "contact" : "{contact}"
                }
                """
                .replace("{email}",randomEmail)
                .replace("{contact}",randomPhone)
                ;
        mockMvc.perform(post("/admin/auth/add")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(401))
                .andDo(print());
    }


    /** For admin login */
    @Test
    public void addRetailerMultipartRequestTest() throws Exception{
        HttpHeaders headers = new  HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                {
                    "email" : "{email}",
                    "username" : "naresh swami",
                    "userType"  : "R",
                    "contact" : "{contact}",
                }
                """;

        mockMvc.perform(post("/admin/auth/add")
                .content(json)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .headers(headers)
                )
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.message",containsString("Content-Type 'multipart/form-data")));

    }


    @Test
    public void addRetailerTest() throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        String randomEmail = UUID.randomUUID().toString().substring(0,6) + "@mocktest.in";
        String randomPhone = getRandomMobileNumber();
        String json = """
                {
                    "email" : "{email}",
                    "username" : "Retailer swami",
                    "userType"  : "R",
                    "contact" : "{contact}"
                }
                """
                .replace("{email}",randomEmail)
                .replace("{contact}",randomPhone)
                ;

         mockMvc.perform(post("/admin/auth/add")
                    .content(json)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers)
            )
            .andExpect(status().is(201))
            .andDo(print()).andReturn();
    }



    @Test
    public void addWholesalerTest() throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        StoreSubCategory storeSubCategory = createStoreSubCategory();
        String randomEmail = UUID.randomUUID().toString().substring(0,6) + "@mocktest.in";
        String randomPhone = getRandomMobileNumber();
        createStorePermission();
        // Creating wholesaler
        String json = """
                {
                    "email" : "{email}",
                    "username" : "naresh swami",
                    "userType"  : "W",
                    "contact" : "{contact}",
                    "city" : "1",
                    "state" : "1",
                    "street" : "1 Moti dungri",
                    "storeName" : "abc",
                    "storeEmail" : "{storeEmail}",
                    "description" : "test",
                    "categoryId" : {categoryId},
                    "subCategoryId"  : {subCategoryId},
                    "zipCode" : "302013",
                    "storePhone" : "{storePhone}"
                }
                """
                .replace("{email}",randomEmail)
                .replace("{contact}",randomPhone)
                .replace("{storePhone}",randomPhone)
                .replace("{storeEmail}",randomEmail)
                .replace("{categoryId}",String.valueOf(storeSubCategory.getCategoryId()))
                .replace("{subCategoryId}",String.valueOf(storeSubCategory.getId()))
                ;

            mockMvc.perform(post("/admin/auth/add")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpect(status().is(201))
                .andDo(print())
                .andReturn();
    }


    @Test
    public void  updateWholesaler() throws Exception {
        String email = createRandomEmail();
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        User user = createUser(slug,email, password, GlobalConstantTest.WHOLESALER);
        Store store = createStore();
        token = loginUser(GlobalConstantTest.STAFF);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        // Updating wholesaler
        String updatedJson = """
                {
                    "slug" : "{slug}",
                    "email" : "{email}",
                    "username" : "naresh swami",
                    "contact" : "{contact}",
                    "storeName" : "abc",
                    "storeEmail" : "{storeEmail}",
                    "description" : "test",
                    "categoryId" : 0,
                    "subCategoryId"  : 0,
                    "storePhone" : "{storePhone}",
                    "zipCode" : "302013",
                    "city" : "1",
                    "state" : "1",
                    "street" : "1 Moti dungri"
                }
                """
                .replace("{slug}",user.getSlug())
                .replace("{email}",user.getEmail())
                .replace("{contact}",user.getContact())
                .replace("{storeEmail}",user.getEmail())
                .replace("{storePhone}", store.getPhone())
                ;

                mockMvc.perform(post("/admin/auth/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200)
                );

    }


    public String addStaff(Integer groupId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        String randomEmail = UUID.randomUUID().toString().substring(0,6) + "@mocktest.in";
        String randomPhone = getRandomMobileNumber();
        String json = """
                {
                    "email" : "{email}",
                    "username" : "Mock Test Staff",
                    "userType"  : "S",
                    "contact" : "{contact}",
                    "groupList" : [{groupId}]            
                }
                """
                .replace("{email}",randomEmail)
                .replace("{contact}",randomPhone)
                .replace("{groupId}",groupId+"")
                ;

            MvcResult result = mockMvc.perform(post("/admin/auth/add")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpect(status().is(201))
                .andDo(print())
                .andReturn();

            return extractSlugFromResponseViaRes(result);
        }


    /** add user updated  */

    @Test
    public void getRetailerWithoutLogin () throws Exception {
        mockMvc.perform(get("/admin/auth/detail/"+GlobalConstantTest.RETAILER_SLUG))
                .andExpect(status().is(401))
                .andDo(print());
    }


    @Test
    public void getRetailer () throws Exception {
        String email = createRandomEmail();
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        User user = createUser(slug,email, password, GlobalConstantTest.RETAILER);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        mockMvc.perform(get("/admin/auth/detail/"+user.getSlug())
                        .headers(headers)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getDeletedWholesaler () throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        String deletedWholesalerSlug = "bad58cac-61f9-4b97-805a-fbfad48ebf5f";
        mockMvc.perform(get("/admin/auth/detail/"+deletedWholesalerSlug)
                    .headers(headers)
                )
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.message",is("User not found.")))
                .andDo(print())
        ;
    }

    @Test
    public void getWholesaler () throws Exception {
        String email = createRandomEmail();;
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        User user = createUser(slug,email, password, GlobalConstantTest.WHOLESALER);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        mockMvc.perform(get("/admin/auth/detail/"+user.getSlug())
                        .headers(headers)
                )
                .andExpect(status().isOk())
                .andDo(print())
        ;
    }


    @Test
    public void getStaff () throws Exception {
        String email = createRandomEmail();;
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        User user = createUser(slug,email, password, GlobalConstantTest.STAFF);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        mockMvc.perform(get("/admin/auth/detail/"+user.getSlug())
                        .headers(headers)
                )
                .andExpect(status().isOk())
                .andDo(print())
        ;
    }


    @Test
    public void getStaffGroups () throws Exception {
        String email = createRandomEmail();;
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        User user = createUser(slug,email, password, GlobalConstantTest.STAFF);
        Group group = createGroup();
        user.setGroups(Set.of(group));
        userRepository.save(user);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        mockMvc.perform(get("/admin/auth/groups/"+user.getSlug())
                        .headers(headers)
                )
                .andExpect(status().isOk())
                .andDo(print())
        ;
    }


    /** ======================= UPDATE USERS */
    @Test
    public void updateStaffViaStaffUserAccount() throws Exception {
        token = loginUser(GlobalConstantTest.STAFF);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);

        String email = createRandomEmail();;
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        User user = createUser(slug,email, password, GlobalConstantTest.STAFF);

        String randomEmail = UUID.randomUUID().toString().substring(0,6) + "@mocktest.in";
        String randomPhone = getRandomMobileNumber();

        String json = """
                   {
                    "slug" : "{slug}",
                    "email" : "{email}",
                    "username" : "Mock Test Staff",
                    "userType"  : "S",
                    "contact" : "{contact}",
                    "groupList" : [0,1]            
                }
                """.replace("{slug}",user.getSlug())
                .replace("{email}", randomEmail)
                .replace("{contact}", randomPhone)
                ;

        mockMvc.perform(post("/admin/auth/update")
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().is(403)) // because only a super admin can update a staff
                .andDo(print());
    }


    @Test
    public void updateStaffViaSuperAdmin() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        Group group = createGroup();
        /** user before update */
        String userSlug = addStaff(group.getId());

        String randomEmail = UUID.randomUUID().toString().substring(0,6) + "@mocktest.in";
        String randomPhone = getRandomMobileNumber();

        String json = """
                   {
                    "slug" : "{slug}",
                    "email" : "{email}",
                    "username" : "Mock Test Staff",
                    "userType"  : "S",
                    "contact" : "{contact}",
                    "groupList" : [{groupId}]            
                }
                """.replace("{slug}",userSlug)
                .replace("{email}", randomEmail)
                .replace("{contact}", randomPhone)
                .replace("{groupId}",""+group.getId());
                ;
        mockMvc.perform(post("/admin/auth/update")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().is(200))
            .andDo(print());

    }



    /** ===================== Get all users ===================== */

    @Test
    public void getAllWholesalerWithoutLogin() throws Exception {
        String json = """
                {}
                """;
        mockMvc.perform(post("/admin/auth/W/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andExpect(status().is(401))
                .andDo(print());
    }


    @Test
    public void getAllWholesaler() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        String json = """
                {}
                """;
        mockMvc.perform(post("/admin/auth/W/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content",notNullValue()),
                        jsonPath("$.numberOfElements",notNullValue())
                )
                .andDo(print());
    }



    @Test
    public void getAllRetailer() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        String json = """
                {}
                """;
        mockMvc.perform(post("/admin/auth/R/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content",notNullValue()),
                        jsonPath("$.numberOfElements",notNullValue())
                )
                .andDo(print());
    }


    @Test
    public void getAllStaff() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        String json = """
                {}
                """;
        mockMvc.perform(post("/admin/auth/O/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content",notNullValue()),
                        jsonPath("$.numberOfElements",notNullValue())
                )
                .andDo(print());
    }



/** =====================  Update Status ===================== */


@Test
public void updateUserWrongStatus() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION,token);
    String json = """
                {
                "slug" : "bd7072f2-ed92-4ee9-8c91-7c8366d0abd2",
                "status" : "F"
                }
                """;
    mockMvc.perform(post("/admin/auth/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .headers(headers)
            )
            .andExpectAll(
                    status().is(406),
                    jsonPath("$.message",is("Status must be A or D."))
            )
            .andDo(print());
}


    @Test
    public void updateUserStatus() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);

        String email = createRandomEmail();;
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        User user = createUser(slug,email, password, GlobalConstantTest.STAFF);

        String json = """
                {
                "slug" : "{slug}",
                "status" : "A"
                }
                """
                .replace("{slug}",user.getSlug())
                ;
        mockMvc.perform(post("/admin/auth/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200)
                )
                .andDo(print());
    }


    @Test
    public void updateWrongUserStatus() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        String json = """
                {
                "slug" : "wrong_slug",
                "status" : "A"
                }
                """;
        mockMvc.perform(post("/admin/auth/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(400)
                )
                .andDo(print());
    }



    @Test
    public void updateUserStatusWithoutParams() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,token);
        String json = """
                {
                }
                """;
        mockMvc.perform(post("/admin/auth/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                )
                .andExpectAll(
                    status().is(406)
                )
                .andDo(print());
    }


    @Test
    public void testCanUserUpdateSelfStatus() throws Exception {
        token = loginUser(GlobalConstantTest.STAFF);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                {
                "slug" : "{self_slug}", 
                "status" : "A"
                }
                """
                .replace("{self_slug}",selfSlug)
                ;
        mockMvc.perform(post("/admin/auth/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(403)
                )
                .andDo(print());
    }



    @Test
    public void testSendOtpWrongEmail() throws Exception {
        String json = """
                {
                  "email" : "swaminaressdfsd3@gmail.com" 
                }
                """;

        mockMvc.perform(post("/admin/auth/sendOtp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpectAll(
            status().is(401)
        );
    }

    @Test
    public void testSendOtpWithWithoutSetSupportId() throws Exception {
        createSupportEmail();
        String email = createRandomEmail();
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        createUser(slug,email, password, GlobalConstantTest.STAFF);

        String json = """
                {
                  "email" : "{email}" 
                }
                """.replace("{email}",email)
                ;

        mockMvc.perform(post("/admin/auth/sendOtp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpectAll(
                jsonPath("$.message",containsString("We are facing an issue sending OTP to this email")),
                status().is(400)
        );
    }



    @Test
    public void testLoginWithWrongOtp() throws Exception {
        // for one time otp
        String email = createRandomEmail();;
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        User user = createUser(slug, email, password, GlobalConstantTest.STAFF);

        String json = """
                {
                    "email" : "{email}",
                    "password" : "{password}"
                }
                """.replace("{email}",user.getEmail())
                .replace("{password}",user.getPassword());
        mockMvc.perform(post("/admin/auth/login/otp")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().is(401),
                jsonPath("$.message",is("Wrong OTP password."))
        )
                .andDo(print())
        ;
    }





  /*  @Test
    public void testLoginViaOtpButBlocked() throws Exception {
        // Make sure user must be blocked otherwise this test will fail
        String json = """
                {
                    "email" : "swaminaresh993@gmail.com",
                    "password" : "156384"
                }
                """;
        mockMvc.perform(post("/admin/auth/login/otp")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
            ).andExpectAll(
                status().is(401),
                jsonPath("$.message",is("You are blocked by admin."))
        );
    }*/


    @Autowired
    UserService userService;

 /*   @Test
    public void testOtpLoginWithRightCredential() throws Exception {
        String email = createRandomEmail();;
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        User user = createUser(slug,email, password, GlobalConstantTest.STAFF);
        // Make sure otp is right
        String json = """
                {
                    "email" : "{email}",
                    "password" : "{otp}"
                }
                """
                .replace("{email}",user.getEmail())
                .replace("{otp}",user.getOtp())
                ;
        mockMvc.perform(post("/admin/auth/login/otp")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().is(200),
                jsonPath("$.message",is("Successfully logged in."))
        );
    }

*/

    public void testDeleteStaffUserViaStaffAccount(String slug) throws Exception {
        Map<String,String> loggedUserResponse = getLoginBeaverSlugAndToken(GlobalConstantTest.STAFF_TEST_EMAIL, GlobalConstantTest.STAFF_TEST_PASSWORD);
        String  token = loggedUserResponse.get(ConstantResponseKeys.TOKEN);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                {
                    "slug" : "{slug}"
                }
                """
                .replace("{slug}",slug);
        mockMvc.perform(post("/admin/auth/delete")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(403)
                )
                .andDo(print());
    }



    public void testDeleteStaffUserViaSuperAdmin(String slug) throws Exception {
        Map<String,String> loggedUserResponse = getLoginBeaverSlugAndToken(GlobalConstantTest.SUPER_ADMIN_TEST_EMAIL, GlobalConstantTest.SUPER_ADMIN_TEST_PASSWORD);
        String  token = loggedUserResponse.get(ConstantResponseKeys.TOKEN);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                {
                    "slug" : "{slug}"
                }
                """
                .replace("{slug}",slug);
        mockMvc.perform(post("/admin/auth/delete")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200)
                )
                .andDo(print());
    }



    public void testDeleteWholesalerUserViaStaffAccount(String slug) throws Exception {
        Map<String,String> loggedUserResponse = getLoginBeaverSlugAndToken(GlobalConstantTest.STAFF_TEST_EMAIL, GlobalConstantTest.STAFF_TEST_PASSWORD);
        String  token = loggedUserResponse.get(ConstantResponseKeys.TOKEN);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                {
                    "slug" : "{slug}"
                }
                """
                .replace("{slug}",slug);
        mockMvc.perform(post("/admin/auth/delete")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200)
                )
                .andDo(print());
    }

    public void testDeleteRetailerUserViaStaffAccount(String slug) throws Exception {
        Map<String,String> loggedUserResponse = getLoginBeaverSlugAndToken(GlobalConstantTest.STAFF_TEST_EMAIL, GlobalConstantTest.STAFF_TEST_PASSWORD);
        String  token = loggedUserResponse.get(ConstantResponseKeys.TOKEN);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION,token);
        String json = """
                {
                    "slug" : "{slug}"
                }
                """
                .replace("{slug}",slug);
        mockMvc.perform(post("/admin/auth/delete")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200)
                )
                .andDo(print());
    }






}
