package com.sales.springbatch.listener;

import com.sales.admin.repositories.StoreRepository;
import com.sales.admin.services.ExcelSheetProgressService;
import com.sales.admin.services.ItemService;
import com.sales.claims.AuthUser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@RequiredArgsConstructor
public class ExcelJobCompleteListener implements JobExecutionListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExcelSheetProgressService excelSheetProgressService;
    private final StoreRepository storeRepository;
    private final ItemService itemService;
    private final String wholesaleSlug;
    private final AuthUser loggedUser;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            logger.info("Export Excel Job has been completed successfully");
            Integer storeId = storeRepository.getStoreIdByStoreSlug(wholesaleSlug);
            itemService.sendNotification("File export is completed.", "File is ready to download.",storeId,loggedUser);
        }
        logger.info("afterJob");
        long totalItems = jobExecution.getExecutionContext().getLong("totalItems", 0L);
        excelSheetProgressService.sendProgress(jobExecution, totalItems, "COMPLETED");
        excelSheetProgressService.complete(jobExecution.getId());
    }
}
