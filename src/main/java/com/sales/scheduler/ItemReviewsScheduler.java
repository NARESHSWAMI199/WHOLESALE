package com.sales.scheduler;


import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@Transactional
@RequiredArgsConstructor
public class ItemReviewsScheduler {


    // NOTE: If in future we need indexing on itemId, we can go with it from db side.


    private final EntityManager entityManager;


    @Scheduled(cron = "0 0/15 * * * *") // Auto run in every 15 minutes
    public void updatedLikesAndDisLikes() {

    }


    @Scheduled(cron = "0 0/15 * * * *") // Auto run in every 15 minutes
    public void updateTotalCountsOfComments() {

    }


    @Scheduled(cron = "0 0/15 * * * *") // Auto run in every 15 minutes
    public void updateCommentCounts() {
        String hql = """
                UPDATE Item i
                    SET i.totalReviews = COALESCE(
                        (SELECT COUNT(ir.id) FROM ItemReviews ir WHERE ir.itemId = i.id),0
                    )
                """;
        Query query = entityManager.createQuery(hql);
        query.executeUpdate();

    }


}
