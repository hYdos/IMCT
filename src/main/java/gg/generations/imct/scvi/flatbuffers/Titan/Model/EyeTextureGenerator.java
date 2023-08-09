package gg.generations.imct.scvi.flatbuffers.Titan.Model;

import ij.IJ;
import ij.ImagePlus;
import ij.io.ImageReader;
import ij.io.Opener;
import ij.plugin.ChannelSplitter;
import ij.plugin.HyperStackConverter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import org.joml.Vector4i;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class EyeTextureGenerator {
    public static BufferedImage generate(SVModel.Material material) {
        var channels = splitImageChannels(material.getTexture("LayerMaskMap").filePath());

        var base = resizeImage(EyeTextureGenerator.loadImage(material.getTexture("BaseColorMap").filePath()), 256, 256);

//        displayImage(base, "Base");

//        var store = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
//material.colors().get("BaseColorLayer1")

        var red = colorReplacement(channels.redPath, new Vector4i(0,0, 0, 255));

        displayImage(channels.redPath, "Red");
        displayImage(channels.greenPath, "GREEN");
        displayImage(channels.bluePath, "BLUE");
        displayImage(channels.alphaPath, "Alpha");

        displayImage(multiply(red, base), " Blep");

//        displayImage(channels.redPath, "alpha");
//        displayImage(colorReplacement(channels.redPath, material.colors().get("BaseColorLayer1")), "BaseColorLayer1");
//        displayImage(colorReplacement(channels.greenPath, material.colors().get("BaseColorLayer2")), "BaseColorLayer2");
//        displayImage(colorReplacement(channels.bluePath, material.colors().get("BaseColorLayer3")), "BaseColorLayer3");
//        displayImage(colorReplacement(channels.alphaPath, material.colors().get("BaseColorLayer4")), "BaseColorLayer4");
//
//        var store1 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
//        displayImage(store1 = colorReplacement(channels.redPath, base,  material.colors().get("EmissionColorLayer1")), "EmissionColorLayer1");
//        displayImage(store1 = colorReplacement(channels.greenPath, store1,  material.colors().get("EmissionColorLayer2")), "EmissionColorLayer2");
//        displayImage(store1 = colorReplacement(channels.bluePath, store1,  material.colors().get("EmissionColorLayer3")), "EmissionColorLayer3");
//        displayImage(store1 = colorReplacement(channels.alphaPath, store1,  material.colors().get("EmissionColorLayer4")), "EmissionColorLayer4");
//
//        var rar = addition(store, store1);
//
//        new ImagePlus("Lookie", rar).show();

//        BufferedImage base = createBase(material, "BaseColorLayer");
//        displayImage(additionModeComposition(createBase(material, "EmissionColorLayer"), base), "Emission");
        return null;
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

        return operation(mask, target, (maskColor, targetColor, channel) -> {
            if(channel != Operation.Channel.ALPHA) return maskColor;
            return color.get(channel.ordinal());
        });
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
        BufferedImage bufferedImage = loadImage(imagePath);

        if (bufferedImage == null) {
            return null;
        }

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // Separate BufferedImage for red, green, blue, and alpha channels
        BufferedImage redImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage greenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage blueImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage alphaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bufferedImage.getRGB(x, y);
                var alpha = 0xFF & (pixel >> 24);
                var red = 0xFF & (pixel >> 16);
                var green = 0xFF & (pixel >> 8);
                var blue = 0xFF & pixel;

                redImage.setRGB(x, y, red << 24 | red << 16 | red << 8 | red);
                greenImage.setRGB(x, y, green << 24 | green << 16 | green << 8 | green);
                blueImage.setRGB(x, y, blue << 24 | blue << 16 | blue << 8 | blue);
                alphaImage.setRGB(x, y, alpha << 24 | alpha << 16 | alpha << 8 | alpha);
            }
        }

        return new ChannelImages(applySaturateEffect(redImage, 2.0), applySaturateEffect(greenImage, 2.0), applySaturateEffect(blueImage, 2.0), applySaturateEffect(alphaImage, 2.0));
    }

    // Method to load an image into BufferedImage
    public static BufferedImage loadImage(String imagePath) {
        try {
            return ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // Record to hold the image channels
    public record ChannelImages(BufferedImage redPath, BufferedImage greenPath, BufferedImage bluePath, BufferedImage alphaPath) {
    }

    public static void displayImage(BufferedImage image, String title) {
        try {
            ImageIO.write(image, "PNG", new File(title + ".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 256, 256);
//        g2d.drawImage(originalImage.getSubimage(0, 0, 1,1).getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT), 0, 0, null);
        g2d.dispose();


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
