package sales.application.sales.admin.services;


import com.sales.SalesApplication;
import com.sales.admin.dto.UserDto;
import com.sales.admin.services.UserService;
import com.sales.claims.SalesUser;
import com.sales.dto.UserSearchFilters;
import com.sales.entities.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import sales.application.sales.testglobal.GlobalConstantTest;
import sales.application.sales.util.TestUtil;

import java.util.Map;
import java.util.UUID;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserServiceTest extends TestUtil {


    @Autowired
    private UserService userService;

    private final String email = "test.naresh@gmail.com";
    private final String password = "123456";

    @Test
    public void testFindUserUsingEmailAndPassword(){
        String email = createRandomEmail();
        String password = UUID.randomUUID().toString();
        String slug =UUID.randomUUID().toString();
        createUser(slug,email, password, GlobalConstantTest.ADMIN);
        User user = userService.findByEmailAndPassword(email,password);
        Assertions.assertEquals("naresh",user.getUsername());
        Assertions.assertEquals("SA",user.getUserType());

    }


    @Test
    public void testFindUserUsingEmailAndPasswordWithWrongDetails(){
        Assertions.assertThrows(UsernameNotFoundException.class,() -> userService.findByEmailAndPassword(email,UUID.randomUUID().toString()));
    }
    @Test
    public void testFindUserUsingEmailAndPasswordWithBlankPassword() {
        createUser(UUID.randomUUID().toString(),email,password,GlobalConstantTest.STAFF);
        Assertions.assertThrows(UsernameNotFoundException.class,() -> userService.findByEmailAndPassword(email,null));
    }
    @Test
    public void testFindUserUsingEmailAndPasswordWithBlank(){
        Assertions.assertThrows(UsernameNotFoundException.class,() -> userService.findByEmailAndPassword(null,null));
    }

    @Test
    public void testGetUserCounts() {
        // Create some test users
        createUser(UUID.randomUUID().toString(), createRandomEmail(), "pass", GlobalConstantTest.ADMIN);
        createUser(UUID.randomUUID().toString(), createRandomEmail(), "pass", GlobalConstantTest.STAFF);
        createUser(UUID.randomUUID().toString(), createRandomEmail(), "pass", GlobalConstantTest.RETAILER);
        createUser(UUID.randomUUID().toString(), createRandomEmail(), "pass", GlobalConstantTest.WHOLESALER);

        Map<String, Integer> counts = userService.getUserCounts();
        Assertions.assertNotNull(counts);
        Assertions.assertTrue(counts.size() > 0);
    }

    @Test
    public void testGetAllUser() {
        User admin = createUser(UUID.randomUUID().toString(), createRandomEmail(), "pass", GlobalConstantTest.ADMIN);
        SalesUser loggedUser = new SalesUser(admin);
        UserSearchFilters filters = new UserSearchFilters();
        filters.setUserType("A"); // Set userType to avoid NPE
        Page<UserDto> users = userService.getAllUser(filters, loggedUser);
        Assertions.assertNotNull(users);
    }

    @Test
    public void testGetUserDetail() {
        User user = createUser(UUID.randomUUID().toString(), createRandomEmail(), "pass", GlobalConstantTest.ADMIN);
        SalesUser loggedUser = new SalesUser(user);
        User detail = userService.getUserDetail(user.getSlug(), loggedUser);
        Assertions.assertNotNull(detail);
        Assertions.assertEquals(user.getSlug(), detail.getSlug());
    }

    @Test
    public void testGetUserDetailWithoutAuth() {
        User user = createUser(UUID.randomUUID().toString(), createRandomEmail(), "pass", GlobalConstantTest.ADMIN);
        User detail = userService.getUserDetail(user.getSlug());
        Assertions.assertNotNull(detail);
        Assertions.assertEquals(user.getSlug(), detail.getSlug());
    }

    @Test
    public void testFindByEmail() {
        String email = createRandomEmail();
        User user = createUser(UUID.randomUUID().toString(), email, "pass", GlobalConstantTest.ADMIN);
        User found = userService.findByEmail(email);
        Assertions.assertNotNull(found);
        Assertions.assertEquals(email, found.getEmail());
    }

    @Test
    public void testGetUserIdBySlug() {
        User user = createUser(UUID.randomUUID().toString(), createRandomEmail(), "pass", GlobalConstantTest.ADMIN);
        Integer id = userService.getUserIdBySlug(user.getSlug());
        Assertions.assertNotNull(id);
        Assertions.assertEquals(user.getId(), id);
    }

}
