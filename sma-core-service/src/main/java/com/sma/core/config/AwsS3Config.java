package com.sma.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class AwsS3Config {

    private final AwsProperties awsProps;

    @Bean
    public S3Client s3Client() {
        return createS3Client();
    }

    private S3Client createS3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                awsProps.getAccessKeyId(),
                awsProps.getSecretAccessKey());

        return S3Client.builder()
                .region(Region.of(awsProps.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                awsProps.getAccessKeyId(),
                awsProps.getSecretAccessKey());

        return S3Presigner.builder()
                .region(Region.of(awsProps.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
