package com.sma.core.service;

import com.sma.core.dto.response.FileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileStorageService {

    /**
     * Upload multiple files to S3 with validation
     * 
     * @param files list of multipart files
     * @return list of FileUploadResponse
     * @throws IOException if upload fails
     */
    List<FileUploadResponse> uploadMultiple(List<MultipartFile> files) throws IOException;

    /**
     * Upload file to S3
     * 
     * @param fileBytes        file content as bytes
     * @param originalFileName original file name
     * @param downloadBaseUrl  base URL for download (optional)
     * @return FileUploadResponse containing original filename and download URL
     * @throws IOException if upload fails
     */
    FileUploadResponse upload(byte[] fileBytes, String originalFileName, String downloadBaseUrl) throws IOException;

    /**
     * Download file from S3 as bytes
     * 
     * @param input file URL or filename
     * @return file content as bytes
     * @throws IOException if download fails
     */
    byte[] downloadAsBytes(String input) throws IOException;

    /**
     * Download file from S3 as Resource (for streaming)
     * 
     * @param fileName file name
     * @return Resource containing file stream and metadata
     * @throws IOException if download fails
     */
    Resource downloadAsResource(String fileName) throws IOException;

    /**
     * Generate presigned URL for file download
     * 
     * @param fileName        file name
     * @param durationMinutes URL validity duration in minutes
     * @return presigned URL
     */
    String generatePresignedUrl(String fileName, int durationMinutes);
}
