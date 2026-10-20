package com.sales.admin.services;


import com.sales.admin.dto.UploadInitDto;
import com.sales.requests.records.UploadInitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkUploadService {

    private static final String UPLOAD_DIR = "./uploads/completed/";
    private static final String TEMP_DIR = "./uploads/temp/";

    // Memory cache for active uploads. In clustered multi-node environments, replace this with Redis.
    private final Map<String, UploadInitRequest> uploadMetaMap = new ConcurrentHashMap<>();

    public UploadInitDto initiateUpload(UploadInitRequest request) {
        Path finalPath = Paths.get(UPLOAD_DIR, request.fileHash() + "_" + request.fileName());
        if (Files.exists(finalPath)) {
            return UploadInitDto.builder()
                    .isCompleted(true)
                    .uploadId("")
                    .build();
        }

        String uploadId = UUID.randomUUID().toString();
        uploadMetaMap.put(uploadId, request);

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

    public String mergeChunks(String uploadId) throws IOException {
        log.info("Start merge chunks :{} : {} ", uploadMetaMap,  uploadId);
        UploadInitRequest request = uploadMetaMap.get(uploadId);
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
        // Async or immediate cleanup of chunk data

        // TODO :// handle this task on separate threadpool
        cleanupDirectory(sourceDir);
        uploadMetaMap.remove(uploadId);

        return targetFile.toAbsolutePath().toString();
    }

    private void cleanupDirectory(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
