package com.grego.linux_images_server.service.impl;

import com.grego.linux_images_server.models.Image;
import com.grego.linux_images_server.service.ImageService;
import com.grego.linux_images_server.utils.Verifier;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.grego.linux_images_server.utils.FileNameManipulations.getFileExtension;
import static com.grego.linux_images_server.utils.ThumbnailControl.generateThumbnail;
import static com.grego.linux_images_server.utils.ThumbnailControl.getThumbnailName;

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
            generateThumbnail(file.toPath(), uploadDirectory, THUMBNAIL_SUFFIX);
        } catch (IOException e) {
            log.error(e);
        }

        return new Image(fileType, id, fileName, src, thumbSrc);
    }


}
