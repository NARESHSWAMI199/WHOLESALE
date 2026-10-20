package com.sales.springbatch.policy;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.beans.factory.annotation.Value;

public class GenericSkipPolicy implements SkipPolicy {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${springBatch.maxSkipCount:50}")
    private int MAX_SKIP_COUNT;

    @Override
    public boolean shouldSkip(@NonNull Throwable t, long skipCount) {
        if (t instanceof Error) {
            return false;
        }
        if (skipCount < MAX_SKIP_COUNT) {
            logger.info("Skipping due to dynamic error: {}", t.getMessage());
            return true;
        }
        return false;
    }
}
