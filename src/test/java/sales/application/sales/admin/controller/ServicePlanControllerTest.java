package sales.application.sales.admin.controller;


import com.sales.SalesApplication;
import com.sales.entities.ServicePlan;
import com.sales.global.GlobalConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import sales.application.sales.testglobal.GlobalConstantTest;
import sales.application.sales.util.TestUtil;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {SalesApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ServicePlanControllerTest extends TestUtil {


    private String token;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
    }

    @Test
    public void createServicePlansViaStaffAccount() throws Exception {
        token = loginUser(GlobalConstantTest.STAFF);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                  {
                    "planName": "Mock test",
                    "months": 1,
                    "price": 18,
                    "discount": 10,
                    "description": "this is dummy plan for mock test"
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/plans/add")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(403) // because only super admin can a service plan
                )
                .andDo(print())
        ;


    }

    @Test
    public void createServicePlansViaSuperAdminAccount() throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                  {
                    "planName": "Mock test {random}",
                    "months": 1,
                    "price": {price},
                    "discount": 0,
                    "description": "this is dummy plan for mock test"
                }
                """
                .replace("{random}", UUID.randomUUID().toString().substring(0, 6))
                .replace("{price}", new Random().nextInt(99) + "");
        MvcResult result = mockMvc.perform(post("/admin/plans/add")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(201)
                )
                .andDo(print())
                .andReturn();


        String slug = extractSlugFromResponseViaRes(result);

        // update status via staff account
        testServicePlansStatusWithStaffAccount(slug);

        // update status via super admin account
        testServicePlansStatusWithSuperAdminAccount(slug);

        // delete via staff account
        testServicePlansDeleteWithStaffAccount(slug);

        // delete via super admin account
        testServicePlansDeleteWithSuperAdminAccount(slug);
    }

    @Test
    public void testGetAllServicePlans() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                    {}
                """;

        mockMvc.perform(post("/admin/plans/service-plans")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.numberOfElements", notNullValue())
        );
    }

    @Test
    public void testGetAllUserPlans() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                    {}
                """;

        mockMvc.perform(post("/admin/plans/user-plans")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.numberOfElements", notNullValue())
        ).andDo(print());
    }


    @Test
    public void testGetAllUserPlansWithNoPlansWholesaler() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        ServicePlan servicePlan = createServicePlan(new Date());
        System.err.println("serviceId : " + servicePlan.getId());
        System.err.println("services : " + servicePlanRepository.findAll());
        String json = """
                    {}
                """;

        mockMvc.perform(post("/admin/plans/user-plans/" + GlobalConstantTest.WHOLESALER_SLUG) // make sure wholesale user doesn't have any plan
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().is(406)
//                jsonPath("$.numberOfElements", is(0 ))
        ).andDo(print());
    }


    //    @Test
    public void testServicePlansStatusWithStaffAccount(String slug) throws Exception {
        token = loginUser(GlobalConstantTest.STAFF);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        //By default status id deactivate
        String json = """
                    {
                    "slug" : "{slug}"
                    }
                """
                .replace("{slug}", slug);

        mockMvc.perform(post("/admin/plans/status") // make sure wholesale user doesn't have any plan
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().is(403)
        ).andDo(print());
    }

    //@Test
    public void testServicePlansStatusWithSuperAdminAccount(String slug) throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                    {
                    "slug" : "{slug}",
                    "status" : "A"
                    }
                """
                .replace("{slug}", slug);

        mockMvc.perform(post("/admin/plans/status") // make sure wholesale user doesn't have any plan
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().is(200)
        ).andDo(print());
    }


    //    @Test
    public void testServicePlansDeleteWithStaffAccount(String slug) throws Exception {
        token = loginUser(GlobalConstantTest.STAFF);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        //By default status id deactivate
        String json = """
                    {
                    "slug" : "{slug}"
                    }
                """
                .replace("{slug}", slug);

        mockMvc.perform(post("/admin/plans/delete") // make sure wholesale user doesn't have any plan
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().is(403)
        ).andDo(print());
    }


    //    @Test
    public void testServicePlansDeleteWithSuperAdminAccount(String slug) throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        //By default status id deactivate
        String json = """
                    {
                    "slug" : "{slug}"
                    }
                """
                .replace("{slug}", slug);

        mockMvc.perform(post("/admin/plans/delete") // make sure wholesale user doesn't have any plan
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().is(200)
        ).andDo(print());
    }


}
