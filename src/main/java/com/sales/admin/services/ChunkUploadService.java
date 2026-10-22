package com.sales.admin.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sales.admin.dto.UploadInitDto;
import com.sales.async.AsyncProcess;
import com.sales.cachemanager.services.UploadInitCacheService;
import com.sales.claims.AuthUser;
import com.sales.requests.records.UploadInitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkUploadService {


    private final UploadInitCacheService uploadInitCacheService;
    @Value("${chunk.files.upload}")
    private String UPLOAD_DIR;
    @Value("${chunk.files.temp}")
    private String TEMP_DIR;
    private final JobLauncher asyncJobLauncher;
    private final Map<String, Job> jobs;
    private final AsyncProcess asyncProcess;
    private final StoreService storeService;

    public UploadInitDto initiateUpload(UploadInitRequest request) {
        Path finalPath = Paths.get(UPLOAD_DIR, request.fileHash() + "_" + request.fileName());
        if (Files.exists(finalPath)) {
            return UploadInitDto.builder()
                    .isCompleted(true)
                    .uploadId("")
                    .build();
        }

        String uploadId = UUID.randomUUID().toString();
        uploadInitCacheService.updateUploadInitRequest(uploadId, request);

        new File(TEMP_DIR + uploadId).mkdirs();
        return UploadInitDto.builder()
                .isCompleted(false)
                .uploadId(uploadId)
                .build();
    }

    public void saveChunk(MultipartFile chunk, String uploadId, int chunkIndex, String chunkHash) throws IOException {
        Path chunkDir = Paths.get(TEMP_DIR, uploadId);
        if (!Files.exists(chunkDir)) {
            throw new IllegalArgumentException("Invalid upload ID or session expired.");
        }

        // Save chunk with its strict index padding format to preserve sorting sequence
        // it's like if file name is 1.part it will be 00001.part or min or max 5
        Path chunkPath = chunkDir.resolve(String.format("%05d", chunkIndex) + ".part");

        // Atomically write chunk files to handle concurrent requests
        Files.copy(chunk.getInputStream(), chunkPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Chunk saved to {}", chunkPath.getParent().toString());
    }

    public String mergeChunks(String uploadId, String wholesaleSlug, AuthUser loggedUser) throws IOException, JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        UploadInitRequest request = uploadInitCacheService.getCacheUploadInitRequest(uploadId);
        if (request == null) throw new IllegalArgumentException("Upload session not found.");
        Path sourceDir = Paths.get(TEMP_DIR, uploadId);
        new File(UPLOAD_DIR).mkdirs();
        Path targetFile = Paths.get(UPLOAD_DIR, request.fileHash() + "_" + request.fileName());
        // Read and sort all parts sequentially
        File[] files = sourceDir.toFile().listFiles((dir, name) -> name.endsWith(".part"));
        if (files == null || files.length != request.totalChunks()) {
            throw new IllegalStateException("Missing chunks. Found: " + (files != null ? files.length : 0) + "/" + request.totalChunks());
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        // Merge files streaming through channels
        try (FileChannel outputChannel = new FileOutputStream(targetFile.toFile(), true).getChannel()) {
            for (File file : files) {
                try (FileChannel inputChannel = new FileInputStream(file).getChannel()) {
                    inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                }
            }
        }
        asyncProcess.cleanupDirectory(sourceDir);
        uploadInitCacheService.deletingUploadInitRequest(uploadId);
        final ObjectMapper objectMapper = new ObjectMapper();
        String loggedUserJson = objectMapper.writeValueAsString(loggedUser);
        Integer storeId = storeService.getStoreIdByStoreSlug(wholesaleSlug);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("loggedUserJson", loggedUserJson)
                .addString("db", String.valueOf(storeId))
                .addString("filePath", targetFile.toAbsolutePath().toString())
                .toJobParameters();
        asyncJobLauncher.run(jobs.get("readItemsExcelStreamJob"), jobParameters);
        return targetFile.toAbsolutePath().toString();
    }

}
