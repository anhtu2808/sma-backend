package com.sma.core.service.impl;

import com.sma.core.config.FileConfig;
import com.sma.core.config.S3Properties;
import com.sma.core.dto.response.FileUploadResponse;
import com.sma.core.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final FileConfig fileConfig;
    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "pdf", "docx", "doc", "ppt",
            "pptx");

    @Override
    public List<FileUploadResponse> uploadMultiple(List<MultipartFile> files) throws IOException {
        List<FileUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();

            if (originalFilename == null || originalFilename.lastIndexOf('.') == -1) {
                throw new IllegalArgumentException("Invalid file name: " + originalFilename);
            }

            String extension = originalFilename
                    .substring(originalFilename.lastIndexOf('.') + 1)
                    .toLowerCase();

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("Không hỗ trợ dạng file " + extension);
            }

            FileUploadResponse response = upload(
                    file.getBytes(),
                    originalFilename,
                    null // Use default from config
            );

            responses.add(response);
        }

        return responses;
    }

    @Override
    public FileUploadResponse upload(
            byte[] fileBytes,
            String originalFileName,
            String downloadBaseUrl) throws IOException {

        String extension = getExtension(originalFileName);

        String newFileName = System.currentTimeMillis() + "_" + originalFileName;
        String s3Key = fileConfig.getS3StoragePath() + "/" + newFileName;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Properties.getBucketName())
                            .key(s3Key)
                            .contentType(resolveContentType(extension))
                            .build(),
                    RequestBody.fromBytes(fileBytes));

            String encodedFileName = URLEncoder.encode(newFileName, StandardCharsets.UTF_8);

            // Use downloadBaseUrl from parameter if provided, otherwise use config
            String baseUrl = (downloadBaseUrl != null && !downloadBaseUrl.isBlank())
                    ? downloadBaseUrl
                    : fileConfig.getDownloadBaseUrl();

            String downloadUrl = baseUrl.endsWith("/")
                    ? baseUrl + encodedFileName
                    : baseUrl + "/" + encodedFileName;

            return FileUploadResponse.builder()
                    .originalFileName(originalFileName)
                    .downloadUrl(downloadUrl)
                    .build();

        } catch (S3Exception e) {
            throw new IOException(
                    "Failed to upload file to S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public byte[] downloadAsBytes(String input) throws IOException {

        String fileName = extractFileName(input);

        String s3Key = fileConfig.getS3StoragePath() + "/" + fileName;

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(s3Properties.getBucketName())
                        .key(s3Key)
                        .build())) {

            return s3Object.readAllBytes();

        } catch (Exception e) {
            throw new IOException("Failed to download file from S3: " + fileName, e);
        }
    }

    @Override
    public Resource downloadAsResource(String fileName) throws IOException {
        String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        String s3Key = fileConfig.getS3StoragePath() + "/" + decodedFileName;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(s3Key)
                .build();

        try {
            ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest);
            return new InputStreamResource(s3ObjectStream);
        } catch (S3Exception e) {
            throw new IOException("Error retrieving file from S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            throw new IOException("Error retrieving file from S3: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String fileName, int durationMinutes) {
        String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        String s3Key = fileConfig.getS3StoragePath() + "/" + decodedFileName;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(durationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        try {
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (S3Exception e) {
            throw new RuntimeException("Error generating download URL: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    private String extractFileName(String input) {
        try {
            if (input.startsWith("http://") || input.startsWith("https://")) {
                URI uri = new URI(input);
                String path = uri.getPath();
                String rawFileName = path.substring(path.lastIndexOf('/') + 1);
                return URLDecoder.decode(rawFileName, StandardCharsets.UTF_8);
            }

            return URLDecoder.decode(input, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid file input: " + input, e);
        }
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf(".");
        if (idx < 0) {
            throw new IllegalArgumentException("Invalid file name: " + filename);
        }
        return filename.substring(idx + 1).toLowerCase();
    }

    private String resolveContentType(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" ->
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" ->
                "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> "application/octet-stream";
        };
    }
}
