package sales.application.sales.admin.controller;

import com.sales.SalesApplication;
import com.sales.admin.services.PaginationService;
import com.sales.entities.Pagination;
import com.sales.entities.UserPagination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import sales.application.sales.util.TestUtil;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PaginationControllerTest extends TestUtil {

    @Autowired
    private PaginationService paginationService;

    @Autowired
    private com.sales.admin.repositories.PaginationRepository paginationRepository;

    private String token;

    @BeforeEach
    public void loginUserTest() throws Exception {
        token = loginUser("SA");
    }

    @Test
    public void findAllUserPaginations() throws Exception {
        // create pagination
        Pagination pagination = new Pagination();
        pagination.setFieldFor("user list");
        pagination.setCanSee("B");
        pagination = paginationRepository.save(pagination);

        // insert user pagination for the same user used for authentication
        var loggedUserEntity = userRepository.findUserBySlug(selfSlug);
        UserPagination userPagination = paginationService.insertUserPagination(pagination, new com.sales.claims.SalesUser(loggedUserEntity), 25);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);

        mockMvc.perform(get("/admin/pagination/all").headers(headers))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.USERLIST", notNullValue()))
                .andDo(print());
    }

    @Test
    public void updatePaginationRowNumberSuccess() throws Exception {
        // prepare pagination and user pagination
        Pagination pagination = new Pagination();
        pagination.setFieldFor("my pager");
        pagination.setCanSee("B");
        pagination = paginationRepository.save(pagination);

        // create a user who has pagination.update permission and insert a userPagination for them
        String uEmail = createRandomEmail();
        String uPass = "pass123";
        String uSlug = UUID.randomUUID().toString();
        var targetUser = createUser(uSlug, uEmail, uPass, "SA");

        // create required permission and group and assign to targetUser
        var perm = createPermission("pagination.update", "Pagination update");
        var grp = createGroup();
        grp.setPermissions(Set.of(perm));
        groupRepository.save(grp);
        Set<com.sales.entities.Group> grps = new java.util.HashSet<>();
        grps.add(grp);
        targetUser.setGroups(grps);
        userRepository.save(targetUser);

        UserPagination userPagination = paginationService.insertUserPagination(pagination, new com.sales.claims.SalesUser(targetUser), 25);

        // login as that user to get a token which has pagination.update
        var loginMap = getLoginBeaverSlugAndToken(uEmail, uPass);
        String permToken = loginMap.get("token");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, permToken);

        String json = """
                {
                  "paginationId" : %d,
                  "userId" : %d,
                  "rowsNumber" : 50
                }
                """.formatted(pagination.getId(), targetUser.getId());

        var result = mockMvc.perform(post("/admin/pagination/update").contentType(MediaType.APPLICATION_JSON).content(json).headers(headers))
          .andDo(print())
          .andReturn();

        int status = result.getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertEquals(200, status, "Response body: " + result.getResponse().getContentAsString());

    }

    @Test
    public void updatePaginationNotFound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);

        String json = """
                {
                  "paginationId" : 999999,
                  "userId" : 1,
                  "rowsNumber" : 50
                }
                """;

        mockMvc.perform(post("/admin/pagination/update").contentType(MediaType.APPLICATION_JSON).content(json).headers(headers))
          .andExpect(status().is4xxClientError())
          .andDo(print());
    }

}
