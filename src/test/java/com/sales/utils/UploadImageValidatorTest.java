package com.sales.utils;

import com.sales.exceptions.MyException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class UploadImageValidatorTest {

    @Test
    void testIsValidImage_success() throws IOException {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", baos.toByteArray());

        boolean ok = UploadImageValidator.isValidImage(file, 10, 10, 200, 200, new double[]{1.0}, new String[]{"png", "jpg", "jpeg"});
        assertTrue(ok);
    }

    @Test
    void testIsValidImage_tooSmall() throws IOException {
        BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 5, 5);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        MockMultipartFile file = new MockMultipartFile("file", "small.png", "image/png", baos.toByteArray());

        assertThrows(MyException.class, () -> UploadImageValidator.isValidImage(file, 10, 10, 200, 200, new double[]{1.0}, new String[]{"png"}));
    }

    @Test
    void testIsValidImage_invalidFormat() throws IOException {
        BufferedImage img = new BufferedImage(50, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 50, 100);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        MockMultipartFile file = new MockMultipartFile("file", "test.bmp", "image/bmp", baos.toByteArray());

        boolean ok = UploadImageValidator.isValidImage(file, 10, 10, 200, 200, new double[]{0.5}, new String[]{"png", "jpg"});
        assertFalse(ok);
    }

    @Test
    void testHasWhiteBackground_trueAndException() throws IOException {
        // Create image with >50% white pixels but <90% to return true
        int w = 10, h = 10;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (x < 6) img.setRGB(x, y, Color.WHITE.getRGB());
                else img.setRGB(x, y, Color.BLACK.getRGB());
            }
        }
        File f1 = File.createTempFile("white-mid-", ".png");
        ImageIO.write(img, "png", f1);

        boolean res = UploadImageValidator.hasWhiteBackground(f1);
        assertTrue(res);

        // Create very white image (>90%) to trigger MyException
        BufferedImage img2 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 10; y++) for (int x = 0; x < 10; x++) img2.setRGB(x, y, Color.WHITE.getRGB());
        File f2 = File.createTempFile("white-all-", ".png");
        ImageIO.write(img2, "png", f2);

        assertThrows(MyException.class, () -> UploadImageValidator.hasWhiteBackground(f2));

        // cleanup
        f1.delete();
        f2.delete();
    }

}
