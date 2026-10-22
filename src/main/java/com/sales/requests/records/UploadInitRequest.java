package com.sales.requests.records;


import lombok.Builder;

import java.io.Serializable;

@Builder(toBuilder = true)
public record UploadInitRequest(
        String fileName,
        String fileHash,
        long totalSize,
        int totalChunks) implements Serializable {
}
