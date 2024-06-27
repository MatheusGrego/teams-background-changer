package com.grego.linux_images_server.controllers;

import com.grego.linux_images_server.models.Image;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


@RestController
@CrossOrigin(origins = "*")
public class ImageController {

    private final ResourceLoader resourceLoader;

    private static final List<Image> images = new ArrayList<>();

    static {
        images.add(new Image("png", "Jala_Custom", "Jala", "/evergreen-assets/backgroundimages/sample.png", "/evergreen-assets/backgroundimages/sample.png"));
    }

    public ImageController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/config.json")
    public VideoBackgroundImagesResponse getConfig() {
        return new VideoBackgroundImagesResponse(images);
    }

    @PostMapping("/addImage")
    public String addImage(@RequestBody Image image) {
        images.add(image);
        return "Image added successfully!";
    }

    @GetMapping(value = "/sample.png", produces = MediaType.IMAGE_PNG_VALUE)
    public void getSampleImage(HttpServletResponse response) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:static/sample.png");

        try (InputStream inputStream = resource.getInputStream()) {
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            byte[] bytes = inputStream.readAllBytes();
            response.getOutputStream().write(bytes);
        }
    }

    private record VideoBackgroundImagesResponse(List<Image> videoBackgroundImages) {
    }
}

