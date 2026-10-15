package com.sales.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sales.admin.dto.ItemDto;
import com.sales.admin.services.ItemService;
import com.sales.claims.AuthUser;
import com.sales.entities.User;
import com.sales.readers.ServiceItemReader;
import com.sales.request.ItemFilterRequest;
import com.sales.writers.ItemExcelWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SpringItemsBatchConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ItemService itemService;
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
            @Value("#{jobParameters['loggedUserJson']}") String loggedUserJson) {
            // take only string or long don't we object so.
        try {
            ItemFilterRequest filters = objectMapper.readValue(searchFiltersJson, ItemFilterRequest.class);
            AuthUser loggedUser = objectMapper.readValue(loggedUserJson, User.class);

            return new ServiceItemReader(itemService, filters, wholesaleSlug, loggedUser);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Batch Reader parameters", e);
        }
    }
    @Bean
    public Step exportExcelStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                ItemReader<ItemDto> reader,
                                ItemExcelWriter itemExcelWriter
                                ) {
        return new StepBuilder("exportExcelStep",jobRepository)
                .<ItemDto,ItemDto>chunk(100,transactionManager)
                .reader(reader)
                .writer(itemExcelWriter)
                .build();
    }

    @Bean
    public Job exportExcelJob(JobRepository jobRepository,
                             Step exportExcelStep
                              ) {
        return new JobBuilder("exportExcelJob",jobRepository)
                .start(exportExcelStep)
                .build();

    }




}
