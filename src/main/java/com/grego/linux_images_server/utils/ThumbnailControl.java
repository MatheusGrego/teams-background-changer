package com.grego.linux_images_server.utils;

import net.coobird.thumbnailator.Thumbnails;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ThumbnailControl {
    public static void generateThumbnail(Path imagePath, String uploadDirectory, String suffix) throws IOException {

            String fileName = imagePath.getFileName().toString();
            String thumbnailFileName = getThumbnailName(fileName, suffix);
            Path thumbnailPath = Paths.get(uploadDirectory).resolve(thumbnailFileName);
            Thumbnails.of(imagePath.toFile())
                    .size(200, 200)
                    .toFile(thumbnailPath.toFile());

    }

    public static String getThumbnailName(String originalFileName, String suffix) {
        int lastIndexOfDot = originalFileName.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return originalFileName + suffix;
        }
        return originalFileName.substring(0, lastIndexOfDot) + suffix + originalFileName.substring(lastIndexOfDot);
    }
}
