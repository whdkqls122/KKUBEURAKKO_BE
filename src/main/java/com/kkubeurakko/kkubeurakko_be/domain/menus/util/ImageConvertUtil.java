package com.kkubeurakko.kkubeurakko_be.domain.menus.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.kkubeurakko.kkubeurakko_be.domain.menus.dto.ConvertedImageDto;

@Component
public class ImageConvertUtil {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"image/jpeg",
		"image/jpg",
		"image/png",
		"image/webp"
	);

	public ConvertedImageDto convertToWebp(MultipartFile file) throws IOException {
		validate(file);

		BufferedImage image = ImageIO.read(file.getInputStream());
		if (image == null) {
			throw new IllegalArgumentException("유효한 이미지 파일이 아닙니다.");
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		ImageIO.scanForPlugins();

		Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");

		if (!writers.hasNext()) {
			throw new IllegalStateException(
				"WebP ImageWriter를 찾을 수 없습니다. webp-imageio 라이브러리를 확인하세요."
			);
		}

		ImageWriter writer = writers.next();

		try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
			writer.setOutput(ios);
			writer.write(null, new IIOImage(image, null, null), null);
		} finally {
			writer.dispose();
		}

		return ConvertedImageDto.builder()
			.data(baos.toByteArray())
			.contentType("image/webp")
			.extension("webp")
			.build();
	}

	private void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
		}

		String contentType = file.getContentType();
		if (!ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
			throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
		}
	}
}