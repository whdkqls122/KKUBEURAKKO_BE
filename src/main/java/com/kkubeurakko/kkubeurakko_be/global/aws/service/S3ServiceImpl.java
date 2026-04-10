package com.kkubeurakko.kkubeurakko_be.global.aws.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.ConvertedImageDto;
import com.kkubeurakko.kkubeurakko_be.domain.menus.util.ImageConvertUtil;
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

	private final ImageConvertUtil imageConvertUtil;

	@Override
	public S3UploadResponseDto uploadImage(MultipartFile file) throws IOException {
		ConvertedImageDto convertedImage = imageConvertUtil.convertToWebp(file);
		String key = createKey(convertedImage.extension());

		putObject(convertedImage, key);
		return S3UploadResponseDto.builder()
			.imageUrl(cloudFrontDomain + "/" + key)
			.imageKey(key)
			.build();
	}

	@Override
	public String updateImage(MultipartFile file, String key) throws IOException {
		ConvertedImageDto convertedImage = imageConvertUtil.convertToWebp(file);
		putObject(convertedImage, key);
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

	private void putObject(ConvertedImageDto convertedImage, String key) {
		s3Client.putObject(
			PutObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.contentType(convertedImage.contentType())
				.build(),
			RequestBody.fromInputStream(
				new ByteArrayInputStream(convertedImage.data()),
				convertedImage.data().length
			)
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
