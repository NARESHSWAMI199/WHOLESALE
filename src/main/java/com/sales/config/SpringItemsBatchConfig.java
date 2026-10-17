package com.sales.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sales.admin.dto.ItemDto;
import com.sales.admin.repositories.StoreRepository;
import com.sales.admin.services.ExcelSheetProgressService;
import com.sales.admin.services.ItemService;
import com.sales.claims.AuthUser;
import com.sales.entities.User;
import com.sales.springbatch.listener.ExcelChunkProgressListener;
import com.sales.springbatch.listener.ExcelJobCompleteListener;
import com.sales.springbatch.readers.ServiceItemReader;
import com.sales.request.ItemFilterRequest;
import com.sales.springbatch.writers.ItemExcelWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SpringItemsBatchConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Bean
    public JobLauncher asyncJobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        // Yeh line batch job ko background thread par shift kar degi
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }


    @Bean
    @JobScope
    public ExcelChunkProgressListener progressListener(ExcelSheetProgressService registry) {
        return new ExcelChunkProgressListener(registry);
    }

    @Bean
    @JobScope
    public ExcelJobCompleteListener excelJobCompleteListener(
            @Value("#{jobParameters['wholesaleSlug']}") String wholesaleSlug,
            @Value("#{jobParameters['loggedUserJson']}") String loggedUserJson,
            ExcelSheetProgressService registry,
            StoreRepository storeRepository,
            ItemService itemService
    ) throws JsonProcessingException {
        AuthUser loggedUser = objectMapper.readValue(loggedUserJson, User.class);
        return new ExcelJobCompleteListener(registry,storeRepository,itemService,wholesaleSlug,loggedUser);
    }


    /**
     * Stop to call close method my spring framework because it will call close method auto during GC.
     *  but we close method called by batch, so I don't want spring framework call it before spring batch. */
    @Bean(destroyMethod = "")
    @StepScope
    public ItemExcelWriter itemExcelWriter(
            @Value("#{jobParameters['wholesaleSlug']}") String wholesaleSlug,
            @Value("#{jobParameters['loggedUserJson']}") String loggedUserJson) {
        try {
            AuthUser loggedUser = objectMapper.readValue(loggedUserJson, User.class);
            return new ItemExcelWriter(wholesaleSlug, loggedUser);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Batch Writer with dynamic parameters", e);
        }
    }

    @Bean
    @StepScope  // It will create or destroy itemReader in each batch.
    public ServiceItemReader itemReader(
            @Value("#{jobParameters['wholesaleSlug']}") String wholesaleSlug,
            @Value("#{jobParameters['searchFiltersJson']}") String searchFiltersJson,
            @Value("#{jobParameters['loggedUserJson']}") String loggedUserJson,
            @Value("#{stepExecution}") StepExecution stepExecution,
            ItemService itemService
            ) {
            // take only string or long don't we object so.
        try {
            ItemFilterRequest filters = objectMapper.readValue(searchFiltersJson, ItemFilterRequest.class);
            AuthUser loggedUser = objectMapper.readValue(loggedUserJson, User.class);

            return new ServiceItemReader(itemService, filters, wholesaleSlug, loggedUser,stepExecution);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Batch Reader parameters", e);
        }
    }
    @Bean
    public Step exportExcelStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                ItemReader<ItemDto> reader,
                                ItemExcelWriter itemExcelWriter,
                                ExcelChunkProgressListener progressListener
                                ) {
        return new StepBuilder("exportExcelStep",jobRepository)
                .<ItemDto,ItemDto>chunk(1,transactionManager)
                .reader(reader)
                .writer(itemExcelWriter)
                .listener(progressListener)
                .build();
    }

    @Bean
    public Job exportExcelJob(JobRepository jobRepository,
                             Step exportExcelStep,
                              ExcelJobCompleteListener jobCompleteListener) {
        return new JobBuilder("exportExcelJob",jobRepository)
                .start(exportExcelStep)
                .listener(jobCompleteListener)
                .build();

    }




}
