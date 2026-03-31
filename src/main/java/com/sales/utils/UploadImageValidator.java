package com.sales.utils;


import com.sales.exceptions.MyException;
import com.sales.global.ResponseMessages;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class UploadImageValidator {
    private static final Logger log = LoggerFactory.getLogger(UploadImageValidator.class);

    public static boolean isValidImage(MultipartFile imageFile, int minWidth, int minHeight, int maxWidth, int maxHeight, double[] allowedAspectRatios, String[] allowedFormats) {
        try {

            BufferedImage image = ImageIO.read(imageFile.getInputStream());
            if (image == null) {
                throw new MyException(ResponseMessages.YOUR_IMAGE_FILE_IS_CORRUPTED_OR_MODIFIED_WITH_ANOTHER_EXTENSION_WE_ACCEPT_ONLY_PNG_JPG_JPEG_IMAGE_DUE_TO_SOME_SECURITY_REASONS);
            }
            int width = image.getWidth();
            int height = image.getHeight();

            log.info("image width : {} : and height : {}", width, height);
            // Check dimensions
            if (width < minWidth || width > maxWidth || height < minHeight || height > maxHeight) {
                if (width < minWidth || height < minHeight) {
                    throw new MyException(ResponseMessages.YOUR_IMAGE_FILE_IS_TOO_SMALL);
                } else {
                    throw new MyException(String.format(ResponseMessages.YOUR_IMAGE_FILE_IS_TOO_LARGE, imageFile.getOriginalFilename()));
                }
            }

            // Check aspect ratio
            double aspectRatio = (double) width / height;
            log.info("image aspectRatio : {}", aspectRatio);
            boolean validAspectRatio = false;
            for (double allowedRatio : allowedAspectRatios) {
                if (Math.abs(aspectRatio - allowedRatio) < 0.01) {
                    validAspectRatio = true;
                    break;
                }
            }
            if (!validAspectRatio) {
                return false;
            }

            // Check file format
            String format = imageFile.getOriginalFilename().substring(imageFile.getOriginalFilename().lastIndexOf(".") + 1);
            boolean validFormat = false;
            for (String allowedFormat : allowedFormats) {
                if (format.equalsIgnoreCase(allowedFormat)) {
                    validFormat = true;
                    break;
                }
            }
            if (!validFormat) {
                return false;
            }

            return true;
        } catch (IOException e) {
            log.error("The exception is : {}", e.getMessage());
            return false;
        }
    }

    public static boolean hasWhiteBackground(File file) {
        ImagePlus imp = IJ.openImage(file.getAbsolutePath());
        ImageProcessor ip = imp.getProcessor();
        boolean isWhite = false;
        int width = ip.getWidth();
        int height = ip.getHeight();
        int[] pixels = (int[]) ip.getPixels();

        int whitePixelCount = 0;
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;

            if (r > 230 && g > 230 && b > 230) {
                whitePixelCount++;
            }
        }

        double whitePixelPercentage = (double) whitePixelCount / (width * height) * 100;
        if (whitePixelPercentage > 30) {
            isWhite = true;
        }
        if (whitePixelPercentage > 90) {
            throw new MyException(ResponseMessages.ITEM_NOT_VISIBLE_CLEARLY);
        }
        return isWhite;
    }


}


