package com.sales.admin.controllers;


import com.sales.admin.dto.UploadInitDto;
import com.sales.admin.services.ChunkUploadService;
import com.sales.requests.records.UploadInitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class ExcelImportController {
    private final ChunkUploadService chunkUploadService;

    @PostMapping("/init")
    public ResponseEntity<UploadInitDto> initUpload(@RequestBody UploadInitRequest request) {
        return ResponseEntity.ok(chunkUploadService.initiateUpload(request));
    }

    @PostMapping("/chunk")
    public ResponseEntity<Void> uploadChunk(
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("chunkHash") String chunkHash) throws Exception {

        chunkUploadService.saveChunk(chunk, uploadId, chunkIndex, chunkHash);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<String> mergeChunks(@RequestParam("uploadId") String uploadId) throws Exception {
        String finalFilePath = chunkUploadService.mergeChunks(uploadId);
        return ResponseEntity.ok(finalFilePath);
    }
}
