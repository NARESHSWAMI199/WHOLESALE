package com.sales.admin.services;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.ws.rs.sse.Sse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ExcelSheetProgressService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void registerSse(Long jobId, SseEmitter emitter) {
        logger.info("registerSse(jobId={})", jobId);
        this.emitters.put(jobId,emitter);
        emitter.onCompletion(() -> {emitters.remove(jobId);});
        emitter.onTimeout(() -> {emitters.remove(jobId);});
        emitter.onError((e) -> {emitters.remove(jobId);});
    }

    public void sendServerSideEvent(Long jobId ,Object progressData){
        logger.info("sendServerSideEvent(jobId={})", jobId);
        SseEmitter emitter = emitters.get(jobId);
        if(emitter != null){
            try{
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(progressData)
                );
            }catch (Exception e){
                logger.error(e.getMessage());
                emitters.remove(jobId);
            }
        }
    }


    public void sendProgress(JobExecution jobExecution, long currentWriteCount, String status) {
        long jobExecutionId = jobExecution.getId();
        long totalItems = jobExecution.getExecutionContext().getLong("totalItems", 0L);
        double percentage = 0.0;
        if (totalItems > 0) {
            percentage = ((double) currentWriteCount / totalItems) * 100;
        }

        if ("COMPLETED".equals(status)) {
            percentage = 100.0;
        }
        Map<String, Object> progress = Map.of(
                "written", currentWriteCount,
                "total", totalItems,
                "percentage", Math.round(percentage * 100.0) / 100.0,
                "status", status
        );

        sendServerSideEvent(jobExecutionId, progress);
    }

    public void complete(Long jobId){
        SseEmitter emitter = emitters.remove(jobId);
        if(emitter != null){
            emitter.complete();
        }
    }


}
