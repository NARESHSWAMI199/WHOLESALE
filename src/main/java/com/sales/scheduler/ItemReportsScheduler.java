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
public class ItemReportsScheduler {


    private final EntityManager entityManager;


    @Scheduled(cron = "0 0 0,12 * * *") // Runs at midnight and noon
    public void updatingItemReportsCounts() {
        String hql = """
                UPDATE Item i
                SET i.totalReportsCount = COALESCE(
                    (SELECT COUNT(ir.id) FROM ItemReport ir WHERE ir.itemId = i.id),0
                )
                """;
        Query query = entityManager.createQuery(hql);
        query.executeUpdate();
    }


}
