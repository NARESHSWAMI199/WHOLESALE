package com.sales.springbatch.listener;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

public class ItemSkipListener implements SkipListener<String, String> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onSkipInRead(@NonNull Throwable throwable) {
        logger.error("Exception during skip in read : {}", throwable.getMessage(), throwable);
    }

    @Override
    public void onSkipInProcess(@NonNull String item, @NonNull Throwable t) {
        logger.error("Exception during skip in process : {}", t.getMessage(), t);
    }

    @Override
    public void onSkipInWrite(@NonNull String item, @NonNull Throwable t) {
        logger.error("Exception during skip in write : {}", t.getMessage(), t);
    }
}
