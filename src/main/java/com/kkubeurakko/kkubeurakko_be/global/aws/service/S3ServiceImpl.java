package com.kkubeurakko.kkubeurakko_be.global.aws.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kkubeurakko.kkubeurakko_be.global.aws.dto.S3UploadResponseDto;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RequiredArgsConstructor
@Service
public class S3ServiceImpl implements S3Service {
	private final S3Client s3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@Value("${cloud.aws.cloudfront.domain}")
	private String cloudFrontDomain;

	@Override
	public S3UploadResponseDto uploadImage(MultipartFile file) throws IOException {
		String ext = FilenameUtils.getExtension(file.getOriginalFilename());
		String key = createKey(ext);

		putObject(file, key);
		return S3UploadResponseDto.builder()
			.imageUrl(cloudFrontDomain + "/" + key)
			.imageKey(key)
			.build();
	}

	@Override
	public String updateImage(MultipartFile file, String key) throws IOException {
		putObject(file, key);
		return cloudFrontDomain + "/" + key;
	}

	@Override
	public void deleteImage(String key) {
		s3Client.deleteObject(
			DeleteObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.build()
		);
	}

	private void putObject(MultipartFile file, String key) throws IOException {
		s3Client.putObject(
			PutObjectRequest
				.builder()
				.bucket(bucket)
				.key(key)
				.contentType(file.getContentType())
				.build(),

			RequestBody.fromInputStream(file.getInputStream(), file.getSize())
		);
	}

	private String createKey(String ext) {
		return "menu/"
			+ LocalDate.now()
			+ "/"
			+ UUID.randomUUID()
			+ "." + ext;
	}
}
