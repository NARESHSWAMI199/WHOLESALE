package sales.application.sales.wholesaler.services;

import com.sales.SalesApplication;
import com.sales.request.ItemFilterRequest;
import com.sales.wholesaler.dto.WholesaleItemDto;
import com.sales.wholesaler.dto.WholesaleItemListDto;
import com.sales.entities.Item;
import com.sales.entities.Store;
import com.sales.wholesaler.services.WholesaleItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import sales.application.sales.util.TestUtil;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SalesApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WholesaleItemServiceTest extends TestUtil {

    @Autowired
    private WholesaleItemService wholesaleItemService;

    @Test
    public void testGetAllItems() {
        Store store = createStore();
        ItemFilterRequest searchFilters = new ItemFilterRequest();
        searchFilters.setStoreId(store.getId());
        Page<WholesaleItemListDto> items = wholesaleItemService.getAllItems(searchFilters, store.getId());
        assertNotNull(items);
    }

    @Test
    public void testFindItemBySLug() {
        Store store = createStore();
        Item item = createItem(store.getId());
        WholesaleItemDto found = wholesaleItemService.findItemDtoBySlug(item.getSlug());
        assertNotNull(found);
        assertEquals(item.getSlug(), found.slug());
    }

    @Test
    public void testGetItemCounts() {
        Store store = createStore();
        Map<String, Integer> counts = wholesaleItemService.getItemCounts(store.getId());
        assertNotNull(counts);
        assertTrue(counts.size() > 0);
    }

    @Test
    public void testGetItemCountsForNewLabel() {
        Store store = createStore();
        Map<String, Integer> counts = wholesaleItemService.getItemCountsForNewLabel(store.getId());
        assertNotNull(counts);
    }

    @Test
    public void testGetItemCountsForOldLabel() {
        Store store = createStore();
        Map<String, Integer> counts = wholesaleItemService.getItemCountsForOldLabel(store.getId());
        assertNotNull(counts);
    }

    @Test
    public void testGetItemCountsForInStock() {
        Store store = createStore();
        Map<String, Integer> counts = wholesaleItemService.getItemCountsForInStock(store.getId());
        assertNotNull(counts);
    }

    @Test
    public void testGetItemCountsForOutStock() {
        Store store = createStore();
        Map<String, Integer> counts = wholesaleItemService.getItemCountsForOutStock(store.getId());
        assertNotNull(counts);
    }

    @Test
    public void testGetItemStatus() {
        Store store = createStore();
        Item item = createItem(store.getId());
        String status = wholesaleItemService.getItemStatus(item.getSlug());
        assertNotNull(status);
    }

    @Test
    public void testGetAllCategory() {
        var categories = wholesaleItemService.getAllCategory();
        assertNotNull(categories);
    }

    @Test
    public void testGetAllItemsSubCategories() {
        // Create a category first
        var category = createItemCategory();
        var subCategories = wholesaleItemService.getAllItemsSubCategories(category.getId());
        assertNotNull(subCategories);
    }
}