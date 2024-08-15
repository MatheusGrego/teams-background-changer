package com.grego.linux_images_server.service.impl;

import com.grego.linux_images_server.models.Image;
import com.grego.linux_images_server.service.ImageService;
import com.grego.linux_images_server.utils.Verifier;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



@Log4j2
@Service
public class ImageServiceImpl implements ImageService {

    private static final String IMAGE_DIRECTORY = "/evergreen-assets/backgroundimages/";
    private static final String THUMBNAIL_SUFFIX = "_thumb";

    @Value("${upload.directory}")
    private String uploadDirectory;

    @Override
    public List<Image> listVideoBackgroundImages() {
        File directory = new File(uploadDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            return new ArrayList<>();
        }
        File[] files = directory.listFiles(Verifier::isValidImage);

        return Arrays.stream(Objects.requireNonNull(files))
                .map(this::toImage)
                .collect(Collectors.toList());
    }

    private Image toImage(File file) {
        String fileName = file.getName();
        if (fileName.contains(THUMBNAIL_SUFFIX)) {
            return null; // Skip thumbnails
        }

        String fileType = getFileExtension(fileName);
        String id = "Custom_" + fileName.replaceAll("\\s+", "_").replace(THUMBNAIL_SUFFIX, "");
        String src = IMAGE_DIRECTORY + fileName;
        String thumbSrc = IMAGE_DIRECTORY + getThumbnailName(fileName, THUMBNAIL_SUFFIX);

        try {
            this.generateThumbnail(file.toPath(), uploadDirectory, THUMBNAIL_SUFFIX);
        } catch (IOException e) {
            log.error(e);
        }

        return new Image(fileType, id, fileName, src, thumbSrc);
    }

    private void generateThumbnail(Path imagePath, String uploadDirectory, String suffix) throws IOException {

        String fileName = imagePath.getFileName().toString();
        String thumbnailFileName = this.getThumbnailName(fileName, suffix);
        Path thumbnailPath = Paths.get(uploadDirectory).resolve(thumbnailFileName);
        Thumbnails.of(imagePath.toFile())
                .size(200, 200)
                .toFile(thumbnailPath.toFile());

    }

    private String getThumbnailName(String originalFileName, String suffix) {
        int lastIndexOfDot = originalFileName.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return originalFileName + suffix;
        }
        return originalFileName.substring(0, lastIndexOfDot) + suffix + originalFileName.substring(lastIndexOfDot);
    }

    private String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return "";
        }
        return fileName.substring(lastIndexOfDot + 1);
    }


}
