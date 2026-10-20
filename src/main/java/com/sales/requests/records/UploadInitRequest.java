package com.sales.requests.records;


import lombok.Builder;

@Builder(toBuilder = true)
public record UploadInitRequest(
        String fileName,
        String fileHash,
        long totalSize,
        int totalChunks) {
}
