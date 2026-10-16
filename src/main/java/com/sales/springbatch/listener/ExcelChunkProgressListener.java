package com.sales.springbatch.listener;

import com.sales.admin.services.ExcelSheetProgressService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;

@RequiredArgsConstructor
public class ExcelChunkProgressListener implements ChunkListener {
    private final Logger log = LoggerFactory.getLogger(ExcelChunkProgressListener.class);
    private final ExcelSheetProgressService excelSheetProgressService;


    @Override
    public void afterChunk(ChunkContext context) {
        StepExecution stepExecution = context.getStepContext().getStepExecution();
        excelSheetProgressService.sendProgress(stepExecution.getJobExecution(), stepExecution.getWriteCount(), "WRITING");
    }
}
