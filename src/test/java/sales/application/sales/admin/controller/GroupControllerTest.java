package sales.application.sales.admin.controller;


import com.sales.SalesApplication;
import com.sales.entities.Group;
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

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GroupControllerTest extends TestUtil {


    private String token;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser(GlobalConstantTest.ADMIN);
    }


    @Test
    public void testCreateGroupWithoutParams() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/group/create")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().is(406)
        ).andDo(print());
    }


    @Test
    public void testCreateGroupWithStaffAccount() throws Exception {
        token = loginUser(GlobalConstantTest.STAFF);
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                    "name" : "Mock test group"
                }
                
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/group/create")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
        ).andExpectAll(
                status().is(403)
        ).andDo(print());
    }


    @Test
    public void testCreateGroupWithSuperAdminAccount() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                    "name" : "Mock test group {random}"
                }
                
                """
                .replace("{random}", UUID.randomUUID().toString().substring(0, 6));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/group/create")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(201)
                ).andDo(print())
                .andReturn();
    }


    @Test
    public void testUpdateGroupWithSuperAdminAccountWithoutSlug() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                {
                    "name" : "Mock test group {random}"
                }
                
                """
                .replace("{random}", UUID.randomUUID().toString().substring(0, 6));
        mockMvc.perform(MockMvcRequestBuilders.post("/group/update")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(406)
                ).andDo(print())
                .andReturn();
    }


    @Test
    public void testUpdateGroupWithSuperAdminAccountWithWrongSlug() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                    "slug" : "{slug}",
                    "name" : "Mock test updated group {random}"
                }
                
                """
                .replace("{slug}", "sdfsd")
                .replace("{random}", UUID.randomUUID().toString().substring(0, 6));
        mockMvc.perform(MockMvcRequestBuilders.post("/group/update")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(400)
                ).andDo(print());
    }


    @Test
    public void testUpdateGroupWithSuperAdminAccountWithSlug() throws Exception {
        Group group = createGroup();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                    "slug" : "{slug}",
                    "name" : "Mock test updated group {random}"
                }
                
                """
                .replace("{slug}", group.getSlug())
                .replace("{random}", UUID.randomUUID().toString().substring(0, 6));
        mockMvc.perform(MockMvcRequestBuilders.post("/group/update")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200)
                ).andDo(print());
    }

    @Test
    public void testGetAllGroups() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                {
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("/group/all")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200),
                        jsonPath("$.content", notNullValue())
                ).andDo(print());
    }


    @Test
    public void testFindGroupBySlug() throws Exception {
        Group group = createGroup();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        mockMvc.perform(MockMvcRequestBuilders.get("/group/detail/" + group.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                )
                .andExpectAll(
                        status().is(200),
                        jsonPath("$.res.group", notNullValue()),
                        jsonPath("$.res.permissions", notNullValue())
                ).andDo(print());
    }


    @Test
    public void testDeleteGroupByStaffAccount() throws Exception {
        token = loginUser(GlobalConstantTest.STAFF);
        Group group = createGroup();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);

        String json = """
                    {
                        "slug" : "{slug}"
                    }
                """
                .replace("{slug}", group.getSlug());

        mockMvc.perform(MockMvcRequestBuilders.post("/group/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .content(json)
                )
                .andExpectAll(
                        status().is(403)
                ).andDo(print());
    }


    @Test
    public void testDeleteGroupBySuperAdminAccount() throws Exception {
        Group group = createGroup();
        HttpHeaders headers = new HttpHeaders();
        headers.set(GlobalConstant.AUTHORIZATION, token);
        String json = """
                    {
                        "slug" : "{slug}"
                    }
                """
                .replace("{slug}", group.getSlug());

        mockMvc.perform(MockMvcRequestBuilders.post("/group/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .content(json)
                )
                .andExpectAll(
                        status().is(200)
                ).andDo(print());
    }

}
