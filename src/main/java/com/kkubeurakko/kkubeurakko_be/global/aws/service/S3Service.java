package com.kkubeurakko.kkubeurakko_be.global.aws.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.kkubeurakko.kkubeurakko_be.global.aws.dto.S3UploadResponseDto;

public interface S3Service {
	//업로드
	S3UploadResponseDto uploadImage(MultipartFile file) throws IOException;

	//수정
	String updateImage(MultipartFile file, String key) throws IOException;

	//삭제
	void deleteImage(String key);
}
