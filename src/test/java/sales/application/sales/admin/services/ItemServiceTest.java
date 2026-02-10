package sales.application.sales.admin.services;

import com.sales.SalesApplication;
import com.sales.admin.dto.ItemDto;
import com.sales.admin.services.ItemService;
import com.sales.claims.SalesUser;
import com.sales.dto.ItemSearchFields;
import com.sales.entities.Item;
import com.sales.entities.ItemCategory;
import com.sales.entities.Store;
import com.sales.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import sales.application.sales.testglobal.GlobalConstantTest;
import sales.application.sales.util.TestUtil;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ItemServiceTest extends TestUtil {

    @Autowired
    private ItemService itemService;

    @Test
    public void testGetAllItems() {
        Store store = createStore();
        ItemSearchFields searchFilters = new ItemSearchFields();
        searchFilters.setStoreId(store.getId());
        User user = createUser(UUID.randomUUID().toString(), "test@example.com", "pass", GlobalConstantTest.ADMIN);
        SalesUser loggedUser = new SalesUser(user);
        Page<ItemDto> items = itemService.getAllItems(searchFilters, loggedUser);
        assertNotNull(items);
    }

    @Test
    public void testFindItemBySLug() {
        Store store = createStore();
        Item item = createItem(store.getId());
        Item found = itemService.findItemBySLug(item.getSlug());
        assertNotNull(found);
        assertEquals(item.getSlug(), found.getSlug());
    }

    @Test
    public void testFindItemBySLugNotFound() {
        Item found = itemService.findItemBySLug("nonexistent");
        assertNull(found);
    }

    @Test
    public void testGetItemCounts() {
        Map<String, Integer> counts = itemService.getItemCounts();
        assertNotNull(counts);
    }

    @Test
    public void testGetItemCategoryById() {
        ItemCategory category = createItemCategory();
        ItemCategory found = itemService.getItemCategoryById(category.getId());
        assertNotNull(found);
        assertEquals(category.getId(), found.getId());
    }

    @Test
    public void testGetAllCategory() {
        ItemCategory category = createItemCategory();
        com.sales.dto.SearchFilters searchFilters = new com.sales.dto.SearchFilters();
        var categories = itemService.getAllCategory(searchFilters);
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
    }

    @Test
    public void testUpdateStock() {
        Store store = createStore();
        Item item = createItem(store.getId());
        int result = itemService.updateStock("N", item.getSlug());
        assertEquals(1, result);
    }

    @Test
    public void testUpdateStatusBySlug() throws Exception {
        Store store = createStore();
        Item item = createItem(store.getId());
        com.sales.dto.StatusDto statusDto = new com.sales.dto.StatusDto();
        statusDto.setSlug(item.getSlug());
        statusDto.setStatus("A");
        User user = createUser(UUID.randomUUID().toString(), "test@example.com", "pass", GlobalConstantTest.ADMIN);
        SalesUser loggedUser = new SalesUser(user);
        int result = itemService.updateStatusBySlug(statusDto, loggedUser);
        assertEquals(1, result);
    }
}