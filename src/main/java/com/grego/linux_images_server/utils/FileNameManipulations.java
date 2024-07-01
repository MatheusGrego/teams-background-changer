package com.grego.linux_images_server.utils;

public class FileNameManipulations {
    public static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');
        return lastIndexOfDot == -1 ? "" : fileName.substring(lastIndexOfDot + 1).toLowerCase();
    }
}
