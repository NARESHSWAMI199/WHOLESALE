package com.sales.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sales.admin.dto.ItemDto;
import com.sales.admin.dto.StoreDto;
import com.sales.admin.mapper.ItemMapper;
import com.sales.admin.mapper.StoreMapper;
import com.sales.admin.mapper.UserMapper;
import com.sales.admin.repositories.StoreRepository;
import com.sales.admin.services.ExcelSheetProgressService;
import com.sales.admin.services.ItemService;
import com.sales.admin.services.StoreService;
import com.sales.admin.services.UserService;
import com.sales.claims.AuthUser;
import com.sales.entities.User;
import com.sales.request.ItemFilterRequest;
import com.sales.request.StoreFilterRequest;
import com.sales.request.UserSearchFilters;
import com.sales.springbatch.listener.ExcelChunkProgressListener;
import com.sales.springbatch.listener.ExcelJobCompleteListener;
import com.sales.springbatch.listener.ItemSkipListener;
import com.sales.springbatch.policy.GenericSkipPolicy;
import com.sales.springbatch.readers.ProductItemReader;
import com.sales.springbatch.readers.StoreItemReader;
import com.sales.springbatch.readers.UserItemReader;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@RequiredArgsConstructor
public class SpringItemsBatchConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();



    @Bean
    public TaskExecutor batchJobTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("batch-async-job-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }


    @Bean
    public JobLauncher asyncJobLauncher(JobRepository jobRepository,TaskExecutor batchJobTaskExecutor) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(batchJobTaskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }



    // IT'S USING FOR SSE WHERE WE RETURNING THE FILE COMPLETION PROGRESS
    @Bean
    @JobScope
    public ExcelChunkProgressListener chunkListener(ExcelSheetProgressService registry) {
        return new ExcelChunkProgressListener(registry);
    }


    // IT'S USING FOR JOB COMPLETION STATUS WITH SSE EVENT
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



    // WRITER WILL COMMON FOR ALL EXCEL SHEETS
    /**
     * Stop to call close method my spring framework because it will call close method auto during GC.
     *  but we close method called by batch, so I don't want spring framework call it before spring batch. */
    @Bean(destroyMethod = "")
    @StepScope
    public ItemExcelWriter excelWriter(
            @Value("#{jobParameters['wholesaleSlug']}") String wholesaleSlug,
            @Value("#{jobParameters['loggedUserJson']}") String loggedUserJson) {
        try {
            AuthUser loggedUser = objectMapper.readValue(loggedUserJson, User.class);
            return new ItemExcelWriter(wholesaleSlug, loggedUser);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Batch Writer with dynamic parameters", e);
        }
    }


    // READER FOR ITEMS
    @Bean
    @StepScope  // It will create or destroy itemReader in each batch.
    public ProductItemReader itemReader(
            @Value("#{jobParameters['wholesaleSlug']}") String wholesaleSlug,
            @Value("#{jobParameters['searchFiltersJson']}") String searchFiltersJson,
            @Value("#{jobParameters['loggedUserJson']}") String loggedUserJson,
            @Value("#{stepExecution}") StepExecution stepExecution,
            ItemService itemService,
            ItemMapper itemMapper
            ) {
            // take only string or long don't we object so.
        try {
            ItemFilterRequest filters = objectMapper.readValue(searchFiltersJson, ItemFilterRequest.class);
            AuthUser loggedUser = objectMapper.readValue(loggedUserJson, User.class);

            return new ProductItemReader(itemService, filters, wholesaleSlug, loggedUser,stepExecution,itemMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Batch Reader parameters", e);
        }
    }



    // READER FOR STORES
    @Bean
    @StepScope  // It will create or destroy itemReader in each batch.
    public StoreItemReader storeReader(
            @Value("#{jobParameters['wholesaleSlug']}") String wholesaleSlug,
            @Value("#{jobParameters['searchFiltersJson']}") String searchFiltersJson,
            @Value("#{jobParameters['loggedUserJson']}") String loggedUserJson,
            @Value("#{stepExecution}") StepExecution stepExecution,
            StoreService storeService,
            StoreMapper storeMapper
    ) {
        // take only string or long don't we object so.
        try {
            StoreFilterRequest filters = objectMapper.readValue(searchFiltersJson, StoreFilterRequest.class);
            return new StoreItemReader(storeService, filters,stepExecution,storeMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Batch Reader parameters", e);
        }
    }


    // READER FOR USERS
    @Bean
    @StepScope  // It will create or destroy itemReader in each batch.
    public UserItemReader userReader(
            @Value("#{jobParameters['wholesaleSlug']}") String wholesaleSlug,
            @Value("#{jobParameters['searchFiltersJson']}") String searchFiltersJson,
            @Value("#{jobParameters['loggedUserJson']}") String loggedUserJson,
            @Value("#{stepExecution}") StepExecution stepExecution,
            UserService userService,
            UserMapper userMapper
    ) {
        // take only string or long don't we object so.
        try {
            UserSearchFilters filters = objectMapper.readValue(searchFiltersJson, UserSearchFilters.class);
            AuthUser loggedUser = objectMapper.readValue(loggedUserJson, User.class);

            return new UserItemReader(userService, filters, loggedUser,stepExecution,userMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Batch Reader parameters", e);
        }
    }




    /// /////////////////////////////////////////-----STEPS STEPS --------//////////////////////////////////////////

    // JOB STEP FOR ITEMS
    @Primary
    @Bean("itemExcelStep")
    public Step itemExportStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               ProductItemReader reader,
                               ItemExcelWriter excelWriter,
                               ExcelChunkProgressListener chunkListener
    ) {
        return new StepBuilder("itemExcelStep",jobRepository)
                .<ItemDto,ItemDto>chunk(100,transactionManager)
                .reader(reader)
                .writer(excelWriter)
                .listener(chunkListener)
                .listener(new ItemSkipListener())
                .faultTolerant()
                .skip(Exception.class)
                .skipPolicy(new GenericSkipPolicy())
                .build();
    }


    // JOB STEP FOR STORE
    @Bean("storeExcelStep")
    public Step storeExportStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               StoreItemReader reader,
                               ItemExcelWriter excelWriter,
                               ExcelChunkProgressListener chunkListener
    ) {
        return new StepBuilder("storeExcelStep",jobRepository)
                .<StoreDto,StoreDto>chunk(100,transactionManager)
                .reader(reader)
                .writer(excelWriter)
                .listener(chunkListener)
                .listener(new ItemSkipListener())
                .faultTolerant()
                .skipPolicy(new GenericSkipPolicy())
                .build();
    }


    // JOB STEP FOR USERS
    @Bean("userExcelStep")
    public Step userExportStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                UserItemReader reader,
                                ItemExcelWriter excelWriter,
                                ExcelChunkProgressListener chunkListener
    ) {
        return new StepBuilder("userExcelStep",jobRepository)
                .chunk(100,transactionManager)
                .reader(reader)
                .writer(excelWriter)
                .listener(chunkListener)
                .listener(new ItemSkipListener())
                .faultTolerant()
                .skipPolicy(new GenericSkipPolicy())
                .build();
    }



    /// ///////////////////////////////------------ JOB STARTED FROM HERE -----------////////////////////////


    // JOB FOR ITEMS
    @Bean("exportProductExcelJob")
    public Job exportProductExcelJob(JobRepository jobRepository,
                             Step itemExportStep,
                              ExcelJobCompleteListener jobCompleteListener) {
        return new JobBuilder("exportProductExcelJob",jobRepository)
                .start(itemExportStep)
                .listener(jobCompleteListener)
                .build();

    }


    // JOB FOR STORES
    @Bean("exportStoreExcelJob")
    public Job exportStoreExcelJob(JobRepository jobRepository,
                                     Step storeExportStep,
                                     ExcelJobCompleteListener jobCompleteListener) {
        return new JobBuilder("exportStoreExcelJob",jobRepository)
                .start(storeExportStep)
                .listener(jobCompleteListener)
                .build();

    }

    // JOB FOR USERS
    @Bean("exportUserExcelJob")
    public Job exportUserExcelJob(JobRepository jobRepository,
                                     Step userExportStep,
                                     ExcelJobCompleteListener jobCompleteListener) {
        return new JobBuilder("exportUserExcelJob",jobRepository)
                .start(userExportStep)
                .listener(jobCompleteListener)
                .build();

    }





}
