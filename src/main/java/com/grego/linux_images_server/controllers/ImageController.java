package com.grego.linux_images_server.controllers;

import com.grego.linux_images_server.models.Image;
import com.grego.linux_images_server.service.ImageService;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ImageController {

    private final ResourceLoader resourceLoader;
    private final ImageService imageService;

    @Value("${upload.directory}")
    private String uploadDirectory;

    public ImageController(ResourceLoader resourceLoader, ImageService imageService) {
        this.resourceLoader = resourceLoader;
        this.imageService = imageService;
    }

    @GetMapping("/config.json")
    public ResponseEntity<?> getConfigJson() throws IOException {
        List<Image> videoBackgroundImages = this.imageService.listVideoBackgroundImages();

        if (videoBackgroundImages.isEmpty()) {
            return new ResponseEntity<>("No images found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new ConfigJson(videoBackgroundImages), HttpStatus.OK);
    }

    @GetMapping(value = "/{filename}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> getFileRoot(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDirectory).resolve(filename);
            Resource resource = resourceLoader.getResource("file:" + filePath.toString());
            if (resource.exists()) {
                return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "/evergreen-assets/backgroundimages/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDirectory).resolve(filename);
            Resource resource = resourceLoader.getResource("file:" + filePath.toString());
            if (resource.exists()) {
                String mimeType = Files.probeContentType(filePath);
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(mimeType)).body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }

        try {
            Path filePath = Paths.get(uploadDirectory).resolve(file.getOriginalFilename());
            file.transferTo(filePath);
            generateThumbnail(filePath);
            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + e.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return "";
        }
        return fileName.substring(lastIndexOfDot + 1);
    }

    private String getThumbnailName(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return fileName + "_thumb";
        }
        return fileName.substring(0, lastIndexOfDot) + "_thumb" + fileName.substring(lastIndexOfDot);
    }

    private void generateThumbnail(Path filePath) {
        try {
            String fileName = filePath.getFileName().toString();
            String thumbnailFileName = getThumbnailName(fileName);
            Path thumbnailPath = Paths.get(uploadDirectory).resolve(thumbnailFileName);
            Thumbnails.of(filePath.toFile())
                    .size(200, 200)
                    .toFile(thumbnailPath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private record ConfigJson(List<Image> videoBackgroundImages) {
    }
}
