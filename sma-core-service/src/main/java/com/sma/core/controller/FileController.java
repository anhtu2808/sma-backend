package com.sma.core.controller;

import com.sma.core.config.FileConfig;
import com.sma.core.config.S3Properties;
import com.sma.core.dto.response.FileUploadResponse;
import com.sma.core.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.ResponseInputStream;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "APIs for file upload and download")
public class FileController {

    private final FileStorageService fileStorageService;
    private final FileConfig fileConfig;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple files", description = "Upload one or more files to S3 storage")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @RequestPart("files") List<MultipartFile> files) throws IOException {

        List<FileUploadResponse> responses = fileStorageService.uploadMultiple(files);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{fileName}")
    @Operation(summary = "Download file", description = "Download file from S3 storage")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {

        String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        String s3Key = fileConfig.getS3StoragePath() + "/" + decodedFileName;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(s3Key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest)) {
            GetObjectResponse metadata = s3ObjectStream.response();

            String contentType = Optional.ofNullable(metadata.contentType())
                    .orElse("application/octet-stream");
            long contentLength = metadata.contentLength();

            Resource resource = fileStorageService.downloadAsResource(fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + decodedFileName + "\"")
                    .contentLength(contentLength)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        }
    }

    @GetMapping("/{fileName}/url")
    @Operation(summary = "Get presigned download URL", description = "Generate a temporary presigned URL for file download")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable String fileName) {

        String presignedUrl = fileStorageService.generatePresignedUrl(fileName, 15);

        return ResponseEntity.ok(Map.of(
                "downloadUrl", presignedUrl,
                "expiresIn", "900" // 15 minutes = 900 seconds
        ));
    }
}
