package com.sales.admin.controllers;


import com.sales.admin.dto.UploadInitDto;
import com.sales.admin.services.ChunkUploadService;
import com.sales.claims.AuthUser;
import com.sales.claims.SalesUser;
import com.sales.requests.records.UploadInitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> uploadChunk(
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("chunkHash") String chunkHash) throws Exception {
        chunkUploadService.saveChunk(chunk, uploadId, chunkIndex, chunkHash);
        return new ResponseEntity<>(Map.of("message", "your accepted successfully", "status", 202), HttpStatus.ACCEPTED);
    }

    @PostMapping("/merge/{wholesaleSlug}")
    public ResponseEntity<String> mergeChunks(Authentication authentication, @RequestParam("uploadId") String uploadId, @PathVariable String wholesaleSlug) throws Exception {
        AuthUser loggedUser = (SalesUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String finalFilePath = chunkUploadService.mergeChunks(uploadId, wholesaleSlug, loggedUser);
        return ResponseEntity.ok(finalFilePath);
    }
}
