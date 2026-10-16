package com.sales.springbatch.listener;

import com.sales.admin.services.ExcelSheetProgressService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@RequiredArgsConstructor
public class ExcelJobCompleteListener implements JobExecutionListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExcelSheetProgressService excelSheetProgressService;

    @Override
    public void afterJob(JobExecution jobExecution) {
        logger.info("afterJob");
        long totalItems = jobExecution.getExecutionContext().getLong("totalItems", 0L);
        excelSheetProgressService.sendProgress(jobExecution, totalItems, "COMPLETED");
        excelSheetProgressService.complete(jobExecution.getId());
    }
}
