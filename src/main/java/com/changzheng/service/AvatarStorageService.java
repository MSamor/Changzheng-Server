package com.changzheng.service;

import com.changzheng.common.BizException;
import com.changzheng.config.UploadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvatarStorageService {
    private final UploadProperties properties;

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
            throw new BizException(40001, "请选择不超过5MB的头像图片");
        try (InputStream source = file.getInputStream();
             ImageInputStream input = ImageIO.createImageInputStream(source)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalidImage();
            ImageReader reader = readers.next();
            BufferedImage image;
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.equals("png") && !format.equals("jpeg") && !format.equals("jpg")) throw invalidImage();
                reader.setInput(input);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > 16000000) throw invalidImage();
                image = reader.read(0);
            } finally {
                reader.dispose();
            }
            // Re-encode to discard metadata and bound storage size; preserve the aspect ratio.
            double scale = Math.min(1.0, 512.0 / Math.max(image.getWidth(), image.getHeight()));
            BufferedImage resized = new BufferedImage(Math.max(1, (int) (image.getWidth() * scale)),
                    Math.max(1, (int) (image.getHeight() * scale)), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = resized.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.drawImage(image, 0, 0, resized.getWidth(), resized.getHeight(), null);
            } finally { graphics.dispose(); }
            Path directory = properties.avatarDirectory();
            Files.createDirectories(directory);
            String filename = UUID.randomUUID() + ".png";
            if (!ImageIO.write(resized, "png", directory.resolve(filename).toFile()))
                throw new IOException("No PNG writer available");
            return "/uploads/avatars/" + filename;
        } catch (IOException e) {
            throw new BizException(40001, "头像读取或保存失败，请重新选择图片后重试");
        }
    }

    private BizException invalidImage() {
        return new BizException(40001, "请上传有效的PNG或JPG头像图片（不超过1600万像素）");
    }
}
