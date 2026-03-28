package com.sales.scheduler;


import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor
public class ItemRatingScheduler {


    private final EntityManager entityManager;


    @Scheduled(cron = "0 0 */3 * * *") // Run in every 3 hours.
    public void updateItemRatingCount(){
        String hql = """
                UPDATE Item i
                SET i.totalRatingCount = COALESCE(
                    (SELECT 
                        COUNT(ir.id) 
                    FROM ItemRating ir 
                    WHERE ir.itemId = i.id),0
                )
                """;
        Query query = entityManager.createQuery(hql);
        query.executeUpdate();
    }


    @Scheduled(cron = "0 0 */3 * * *") // Run in every 3 hours.
    public void updateItemRatingAvg(){
        String hql = """
                UPDATE Item i
                SET i.rating = COALESCE(
                    (SELECT ROUND((sum(ir.rating)/count(ir.rating)), 2) FROM ItemRating ir WHERE ir.itemId = i.id), 0
                )
                """;
        Query query = entityManager.createQuery(hql);
        query.executeUpdate();
    }


}
