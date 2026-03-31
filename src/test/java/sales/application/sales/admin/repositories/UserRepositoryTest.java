package sales.application.sales.admin.repositories;

import com.sales.admin.repositories.UserHbRepository;
import com.sales.admin.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public UserHbRepository userHbRepository;


}
