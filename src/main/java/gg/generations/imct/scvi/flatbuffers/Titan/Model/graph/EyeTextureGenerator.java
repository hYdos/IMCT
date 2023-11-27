package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.api.ApiMaterial;
import org.joml.Vector4i;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class EyeTextureGenerator {
//    public static BufferedImage generate(ApiMaterial material, Path modelDir) {
//        return ARCEUS.update(material, modelDir);
//        var channels = splitImageChannels(modelDir.resolve(modelDir.getFileName().toString() + "_eye_lym.png").toString());
//
//        var base = resizeImage(EyeTextureGenerator.loadImage(modelDir.resolve(modelDir.getFileName().toString() + "_eye_alb.png").toString()), 256, 256);
//
//        var highlight = grayScaleToColor(EyeTextureGenerator.loadImage(modelDir.resolve(modelDir.getFileName().toString() + "_eye_msk.png").toString()));
//
////        channels.display();
////        displayImage(base, "Base");
//
////material.colors().get("BaseColorLayer1")
//
//        var store = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
//
//        store = layer(base, colorReplacement(channels.redPath, material.colors().get("BaseColorLayer1")), AlphaComposite.SrcOver);
//        store = layer(store, colorReplacement(channels.greenPath, material.colors().get("BaseColorLayer2")), AlphaComposite.SrcOver);
//        store = layer(store, colorReplacement(channels.bluePath, material.colors().get("BaseColorLayer3")), AlphaComposite.SrcOver);
//        displayImage(store = layer(store, colorReplacement(channels.alphaPath, material.colors().get("BaseColorLayer4")), AlphaComposite.SrcOver),
//                "BaseColorLayer4");
////
//        var store1 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
//        store1 = layer(base, colorReplacement(channels.redPath,  material.colors().get("EmissionColorLayer1")), AlphaComposite.SrcOver);
//        store1 = layer(store1, colorReplacement(channels.greenPath,  material.colors().get("EmissionColorLayer2")), AlphaComposite.SrcOver);
//        store1 = layer(store1, colorReplacement(channels.bluePath,  material.colors().get("EmissionColorLayer3")), AlphaComposite.SrcOver);
//        displayImage(store1 = layer(store1, colorReplacement(channels.alphaPath,  material.colors().get("EmissionColorLayer4")), AlphaComposite.SrcOver), "EmissionColorLayer4");
//
//        BufferedImage finishd;
//
//        displayImage(finishd = layer(store1, store, ScreenComposite.getInstance()), "Finished");
//
//        displayImage(layer(finishd, colorReplacement(highlight, material.colors().get("EmissionColorLayer5")), AlphaComposite.SrcOver), "Finished Blep");
////
////        var rar = addition(store, store1);
////
////        new ImagePlus("Lookie", rar).show();
//
////        BufferedImage base = createBase(material, "BaseColorLayer");
////        displayImage(additionModeComposition(createBase(material, "EmissionColorLayer"), base), "Emission");
//        return finishd;
//    }

    private static BufferedImage layer(BufferedImage base, BufferedImage color, Composite composite) {
        var result = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);

        var graphics = result.createGraphics();
        graphics.drawImage(base, 0, 0, null);

        graphics.setComposite(composite);
        graphics.drawImage(color, 0, 0, null);
        graphics.dispose();

        return result;
    }

    public static BufferedImage addition(BufferedImage top, BufferedImage bottom) {
        return operation(top, bottom, (a, b, b2) -> Math.min(a + b, 255));
    }

    public static BufferedImage operation(BufferedImage top, BufferedImage bottom, Operation op) {
        var newImage = new BufferedImage(top.getWidth(), top.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < newImage.getHeight(); x++) {
            for (int y = 0; y < newImage.getWidth(); y++) {
                var topARGB = top.getRGB(x, y);
                var topAlpha = 0xFF & (topARGB >> 24);
                var topRed = 0xFF & (topARGB >> 16);
                var topGreen = 0xFF & (topARGB >> 8);
                var topBlue = 0xFF & topARGB;

                var bottomARGB = bottom.getRGB(x, y);
                var bottomAlpha = 0xFF & (bottomARGB >> 24);
                var bottomRed = 0xFF & (bottomARGB >> 16);
                var bottomGreen = 0xFF & (bottomARGB >> 8);
                var bottomBlue = 0xFF & bottomARGB;

                newImage.setRGB(x, y, op.apply(topAlpha, bottomAlpha, Operation.Channel.ALPHA) << 24 | op.apply(topRed, bottomRed, Operation.Channel.RED) << 16 | op.apply(topGreen, bottomGreen, Operation.Channel.GREEN) << 8 | op.apply(topBlue, bottomBlue, Operation.Channel.BLUE));
            }
        }

        return newImage;
    }

    public static BufferedImage color(BufferedImage top, BufferedImage bottom, Vector4i color) {
        return operation(top, bottom, (topColor, bottomColor, channel) -> 0);
    }

    public static BufferedImage multiply(BufferedImage top, BufferedImage bottom) {
        return operation(top, bottom, (topColor, bottomColor, channel) -> (int) ((topColor * bottomColor) / 255f));
    }

    public static BufferedImage colorReplacement(BufferedImage mask, Vector4i color) {
        var target = new BufferedImage(mask.getWidth(), mask.getHeight(), BufferedImage.TYPE_INT_ARGB);

        var graphic = target.createGraphics();
        graphic.setColor(new Color(color.x, color.y, color.z));
        graphic.fillRect(0, 0, target.getWidth(), target.getHeight());

        graphic.setComposite(AlphaComposite.DstIn);
        graphic.drawImage(mask, 0, 0, null);
        graphic.dispose();

        return target;
    }

    public static void copy(Path from, Path to) throws IOException {
        try {
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copied: " + from + " -> " + to);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public interface Operation {
        public int apply(int top, int bottom, Channel channel);

        public enum Channel {
            ALPHA,
            RED,
            GREEN,
            BLUE
        }
    }

    public static ChannelImages splitImageChannels(String imagePath) {
        return splitImageChannels(Objects.requireNonNull(loadImage(imagePath)));
    }

    public static ChannelImages splitImageChannels(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage[] resultImages = new BufferedImage[4];

        // Create a ColorModel and WritableRaster for the result images
        ColorModel colorModel = ColorModel.getRGBdefault();
        WritableRaster[] rasters = new WritableRaster[4];
        for (int i = 0; i < 4; i++) {
            rasters[i] = colorModel.createCompatibleWritableRaster(width, height);
        }

        // Split and transfer alpha channels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int i = 0; i < 4; i++) {
                    int newPixel = (((original.getRGB(x, y) >> (i * 8)) & 0xFF) << 24) | (255 << 16) | (255 << 8) | 255; // White color
                    rasters[i].setPixel(x, y, colorModel.getComponents(newPixel, null, 0));
                }
            }
        }

        // Create result images from the rasters
        for (int i = 0; i < 4; i++) {
            resultImages[i] = new BufferedImage(colorModel, rasters[i], false, null);
        }

        return new ChannelImages(resultImages[2], resultImages[1], resultImages[0], resultImages[3]);
    }

    public static BufferedImage grayScaleToColor(BufferedImage original) {
        int width = original != null ? original.getWidth() : 256;
        int height = original != null ? original.getHeight() : 256;

        BufferedImage resultImages;

        // Create a ColorModel and WritableRaster for the result images
        ColorModel colorModel = ColorModel.getRGBdefault();
        WritableRaster rasters = colorModel.createCompatibleWritableRaster(width, height);

        // Split and transfer alpha channels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                var pixel = original != null ? original.getRGB(x, y) : 0;

                int newPixel = (pixel & 0xFF) << 24 | 255 << 16 | 255 << 8 | 255; // White color
                rasters.setPixel(x, y, colorModel.getComponents(newPixel, null, 0));
            }
        }

        // Create result images from the rasters
        return new BufferedImage(colorModel, rasters, false, null);
    }

    public static BufferedImage loadImage(String imagePath) {
        return loadImage(new File(imagePath));
    }

    public static BufferedImage loadImage(Path imagePath) {
        return loadImage(imagePath.toFile());
    }

    // Method to load an image into BufferedImage
    public static BufferedImage loadImage(File imagePath) {
        try {
            return ImageIO.read(imagePath);
        } catch (IOException e) {
//            System.out.println(":O " + imagePath);
//            e.printStackTrace();
            return null;
        }
    }
    // Record to hold the image channels
    public record ChannelImages(BufferedImage redPath, BufferedImage greenPath, BufferedImage bluePath, BufferedImage alphaPath) {
        public void display() {
            displayImage(redPath, "Red");
            displayImage(greenPath, "Green");
            displayImage(bluePath, "Blue");
            displayImage(alphaPath, "Alpha");
        }
    }

    public static void generate(BufferedImage image, Path path) {
        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            ImageIO.write(image, "png", path.toFile());
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Failed to generate: " + path);
            e.printStackTrace();
        }
    }

    public static void displayImage(BufferedImage image, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon imageIcon = new ImageIcon(image);
        JLabel label = new JLabel(imageIcon);
        frame.getContentPane().add(label);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int newWidth, int newHeight) {
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());

        double xScale = (double)newWidth / originalImage.getWidth();
        double yScale = (double)newHeight / originalImage.getHeight();

        AffineTransform transform = new AffineTransform();
        transform.scale(xScale, yScale);

        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
        resizedImage = op.filter(originalImage, resizedImage);

        return resizedImage;
    }

    public static BufferedImage applySaturateEffect(BufferedImage image, double saturationFactor) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color originalColor = new Color(image.getRGB(x, y));
                float[] hsl = rgbToHsl(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue());

                // Modify the saturation component
                hsl[1] *= saturationFactor;

                Color newColor = hslToRgb(hsl[0], hsl[1], hsl[2]);
                result.setRGB(x, y, newColor.getRGB());
            }
        }

        return result;
    }
    public static float[] rgbToHsl(int r, int g, int b) {
        float[] hsl = new float[3];

        float rNormalized = r / 255.0f;
        float gNormalized = g / 255.0f;
        float bNormalized = b / 255.0f;

        float max = Math.max(rNormalized, Math.max(gNormalized, bNormalized));
        float min = Math.min(rNormalized, Math.min(gNormalized, bNormalized));

        float h, s, l;
        h = s = l = (max + min) / 2;

        if (max == min) {
            h = s = 0; // achromatic
        } else {
            float d = max - min;
            s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
            if (max == rNormalized) {
                h = (gNormalized - bNormalized) / d + (gNormalized < bNormalized ? 6 : 0);
            } else if (max == gNormalized) {
                h = (bNormalized - rNormalized) / d + 2;
            } else if (max == bNormalized) {
                h = (rNormalized - gNormalized) / d + 4;
            }
            h /= 6;
        }

        hsl[0] = h;
        hsl[1] = s;
        hsl[2] = l;

        return hsl;
    }

    public static Color hslToRgb(float h, float s, float l) {
        float r, g, b;

        if (s == 0) {
            r = g = b = l; // achromatic
        } else {
            float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1.0f / 3);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1.0f / 3);
        }

        int red = Math.round(r * 255);
        int green = Math.round(g * 255);
        int blue = Math.round(b * 255);

        return new Color(red, green, blue);
    }

    public static float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1.0f / 6) return p + (q - p) * 6 * t;
        if (t < 1.0f / 2) return q;
        if (t < 2.0f / 3) return p + (q - p) * (2.0f / 3 - t) * 6;
        return p;
    }
}
