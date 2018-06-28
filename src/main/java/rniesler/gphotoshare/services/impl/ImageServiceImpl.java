package rniesler.gphotoshare.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import rniesler.gphotoshare.services.ImageService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class ImageServiceImpl implements ImageService {
    private final static int ICON_SIZE = 64;

    @Override
    public byte[] resizeToIcon(byte[] fullImg) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(fullImg);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            BufferedImage image = ImageIO.read(is);
            BufferedImage resized = Scalr.resize(image, ICON_SIZE);
            ImageIO.write(resized, "jpeg", os);
            return os.toByteArray();
        } catch (IOException ex) {
            log.error("Could not resize cover photo.");
            throw new RuntimeException(ex);
        }
    }
}
