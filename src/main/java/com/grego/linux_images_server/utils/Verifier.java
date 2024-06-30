package com.grego.linux_images_server.utils;

import java.io.File;

public class Verifier {

    public static boolean isValidImage(File name, String image) {
        String fileNameInLowerCase = image.toLowerCase();

        return fileNameInLowerCase.endsWith(".jpg") || fileNameInLowerCase.endsWith(".png") || fileNameInLowerCase.endsWith(".jpeg");
    }
}
