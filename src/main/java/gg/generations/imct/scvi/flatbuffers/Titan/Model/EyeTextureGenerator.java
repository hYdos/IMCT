package gg.generations.imct.scvi.flatbuffers.Titan.Model;

import org.joml.Vector4i;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class EyeTextureGenerator {
    public static BufferedImage generate(SVModel.Material material) {
        BufferedImage base = createBase(material, "BaseColorLayer");
        displayImage(additionModeComposition(createBase(material, "EmissionColorLayer"), base), "Emission");
        return base;
    }

    public static BufferedImage additionModeComposition(BufferedImage image1, BufferedImage image2) {
        int width = Math.min(image1.getWidth(), image2.getWidth());
        int height = Math.min(image1.getHeight(), image2.getHeight());

        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resultImage.createGraphics();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the pixel color of both images
                int pixel1 = image1.getRGB(x, y);
                int pixel2 = image2.getRGB(x, y);

                // Extract the color components (R, G, B, A) from each pixel
                int red1 = (pixel1 >> 16) & 0xFF;
                int green1 = (pixel1 >> 8) & 0xFF;
                int blue1 = pixel1 & 0xFF;
                int alpha1 = (pixel1 >> 24) & 0xFF;

                int red2 = (pixel2 >> 16) & 0xFF;
                int green2 = (pixel2 >> 8) & 0xFF;
                int blue2 = pixel2 & 0xFF;
                int alpha2 = (pixel2 >> 24) & 0xFF;

                // Perform the ADDITION operation by adding the color components and clamping to the valid range (0 to 255)
                int resultRed = Math.min(red1 + red2, 255);
                int resultGreen = Math.min(green1 + green2, 255);
                int resultBlue = Math.min(blue1 + blue2, 255);
                int resultAlpha = Math.min(alpha1 + alpha2, 255);

                // Combine the result color components into a single pixel
                int resultPixel = (resultAlpha << 24) | (resultRed << 16) | (resultGreen << 8) | resultBlue;

                // Set the combined pixel in the result image
                resultImage.setRGB(x, y, resultPixel);
            }
        }

        g.dispose();

        return resultImage;
    }

    private static BufferedImage createBase(SVModel.Material material, String baseColorLayer) {
        var layerMask = splitImageChannels(material.getTexture("LayerMaskMap").getBufferedImage());
        var baseColor = resizeImage(material.getTexture("BaseColorMap").getBufferedImage(), layerMask.alpha.getWidth(), layerMask.alpha.getHeight());

        var color1 = material.colors().get(baseColorLayer + "1");
        var color2 = material.colors().get(baseColorLayer + "2");
        var color3 = material.colors().get(baseColorLayer + "3");
        var color4 = material.colors().get(baseColorLayer + "4");

        overlayWithTint(baseColor, layerMask.red, color1);
        overlayWithTint(baseColor, layerMask.green, color2);
        overlayWithTint(baseColor, layerMask.blue, color3);
        overlayWithTint(baseColor, layerMask.alpha, color4);

        return baseColor;
    }

    public static void overlayWithTint(BufferedImage baseImage, BufferedImage maskImage, Vector4i tint) {
        int width = baseImage.getWidth();
        int height = baseImage.getHeight();

        Graphics2D g = baseImage.createGraphics();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the grayscale intensity (0-255) from the mask image
                int maskIntensity = new Color(maskImage.getRGB(x, y)).getRed();

                // Calculate the alpha value (normalized between 0.0 and 1.0)
                float alpha = maskIntensity / 255.0f;

                // Get the original pixel color from the base image
                int basePixel = baseImage.getRGB(x, y);
                Color baseColor = new Color(basePixel, true); // Use true to preserve the alpha channel

                // Blend the base color with the tint color based on the alpha
                int blendedRed = (int) (alpha * tint.x + (1.0f - alpha) * baseColor.getRed());
                int blendedGreen = (int) (alpha * tint.y + (1.0f - alpha) * baseColor.getGreen());
                int blendedBlue = (int) (alpha * tint.z + (1.0f - alpha) * baseColor.getBlue());

                // Create the blended pixel ARGB value
                int blendedPixel = (baseColor.getAlpha() << 24) | (blendedRed << 16) | (blendedGreen << 8) | blendedBlue;

                // Set the blended pixel in the new image
                baseImage.setRGB(x, y, blendedPixel);
            }
        }

        g.dispose();

//        return tintedImage;
    }


    public static BufferedImage fillColor(BufferedImage image, Vector4i color) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage filledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = filledImage.createGraphics();

        // Convert the Vector4i color to a Color object
        Color fillColor = new Color(color.x, color.y, color.z, color.w);

        // Fill the entire image with the specified color
        g.setColor(fillColor);
        g.fillRect(0, 0, width, height);
        g.dispose();

        return filledImage;
    }

    public static void overlayFillWithMask(BufferedImage baseImage, BufferedImage mask, Vector4i color) {
        int width = mask.getWidth();
        int height = mask.getHeight();

        var graphics = baseImage.createGraphics();


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int maskValue = mask.getRGB(x, y) & 0xFF; // Extract the grayscale value from the mask
                float alpha = maskValue / 255.0f; // Normalize the alpha value to range [0, 1]

                // Get the RGBA components of the fill color (from the Vector4i)
                var fillColor = ((color.x & 0xFF) << 16) | ((color.y & 0xFF) << 8) | (color.z & 0xFF) | ((color.w & 0xFF) << 24);

                // Get the pixel color from the baseImage
                int baseRGB = baseImage.getRGB(x, y);
                int baseRed = (baseRGB >> 16) & 0xFF;
                int baseGreen = (baseRGB >> 8) & 0xFF;
                int baseBlue = baseRGB & 0xFF;

                // Blend the fill color with the base image based on the mask value
                int blendedRed = (int) (alpha * fillColor + (1.0f - alpha) * baseRed);
                int blendedGreen = (int) (alpha * fillColor + (1.0f - alpha) * baseGreen);
                int blendedBlue = (int) (alpha * fillColor + (1.0f - alpha) * baseBlue);

                int blendedRGB = (blendedRed << 16) | (blendedGreen << 8) | blendedBlue;

                // Set the pixel in the baseImage with the blended color
                baseImage.setRGB(x, y, blendedRGB);
            }
        }
    }

    public static BufferedImage applyAlphaMask(BufferedImage baseImage, BufferedImage mask, Vector4i tint) {
        int width = baseImage.getWidth();
        int height = baseImage.getHeight();

        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the color of the mask pixel (assuming it's grayscale)
                int maskPixel = mask.getRGB(x, y) & 0xFF; // Extract the grayscale value

                // Compute the alpha value (normalized to [0, 1])
                float alpha = maskPixel / 255.0f;

                // Get the color of the base image pixel
                int basePixel = baseImage.getRGB(x, y);
                int baseRed = (basePixel >> 16) & 0xFF;
                int baseGreen = (basePixel >> 8) & 0xFF;
                int baseBlue = basePixel & 0xFF;

                // Compute the blended color or use fillValue/baseValue
                int blendedRed, blendedGreen, blendedBlue;



                blendedRed = tint.x;
                blendedGreen = tint.y;
                blendedBlue = tint.z;

//                if (alpha == 1.0f) {
//                    blendedRed = tint.x;
//                    blendedGreen = tint.y;
//                    blendedBlue = tint.z;
//                } else if (alpha == 0.0f) {
//                    blendedRed = baseRed;
//                    blendedGreen = baseGreen;
//                    blendedBlue = baseBlue;
//                } else {
//                    blendedRed = (int) (alpha * tint.x + (1.0f - alpha) * baseRed);
//                    blendedGreen = (int) (alpha * tint.y + (1.0f - alpha) * baseGreen);
//                    blendedBlue = (int) (alpha * tint.z + (1.0f - alpha) * baseBlue);
//                }

                // Create the blended pixel ARGB value
                int blendedPixel = (blendedRed << 16) | (blendedGreen << 8) | blendedBlue;

                resultImage.setRGB(x, y, blendedPixel);
            }
        }

        return resultImage;
    }
    public static record GrayscaleImages(BufferedImage red, BufferedImage green, BufferedImage blue, BufferedImage alpha) {}

    public static BufferedImage resizeImage(BufferedImage originalImage, int newWidth, int newHeight) {
//        displayImage(originalImage, "Original");

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage.getSubimage(0, 0, 1,1).getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT), 0, 0, null);
        g2d.dispose();

//        displayImage(resizedImage, "Resized");


        return resizedImage;
    }

    public static GrayscaleImages splitImageChannels(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage redImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage greenImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage blueImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage alphaImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = image.getRGB(x, y);

                int red = (rgba >> 16) & 0xFF;
                int green = (rgba >> 8) & 0xFF;
                int blue = rgba & 0xFF;
                int alpha = (rgba >> 24) & 0xFF;

                redImage.setRGB(x, y, (red << 24) | (red << 16) | (red << 8) | red);
                greenImage.setRGB(x, y, (green << 24) | (green << 16) | (green << 8) | green);
                blueImage.setRGB(x, y, (blue << 24) | (blue << 16) | (blue << 8) | blue);
                alphaImage.setRGB(x, y, (alpha << 24) | (alpha << 16) | (alpha << 8) | alpha);
            }
        }

        redImage = applyCurvesEffect(redImage);
        greenImage = applyCurvesEffect(greenImage);
        blueImage = applyCurvesEffect(blueImage);
        alphaImage = applyCurvesEffect(alphaImage);

        return new GrayscaleImages(redImage, greenImage, blueImage, alphaImage);
    }

    public static BufferedImage applyCurvesEffect(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resultImage.createGraphics();

        // Apply Brightness adjustment to each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the pixel color of the original image
                int pixel = image.getRGB(x, y);

                // Extract the color components (R, G, B, A) from the pixel
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;
                int alpha = (pixel >> 24) & 0xFF;

                // Convert RGB to HSB color space
                float[] hsb = Color.RGBtoHSB(red, green, blue, null);

                // Adjust the brightness using the specified factor
                float adjustedBrightness = Math.min(hsb[2] * 2.0f, 1.0f);

                // Convert back to RGB color space
                int adjustedRGB = Color.HSBtoRGB(hsb[0], hsb[1], adjustedBrightness);

                // Set the new pixel in the result image
                resultImage.setRGB(x, y, (alpha << 24) | (adjustedRGB & 0xFFFFFF));
            }
        }

        g.dispose();

        return resultImage;
    }
    public static void displayImage(BufferedImage image, String title) {
        try {
            ImageIO.write(image, "PNG", new File("blep.png"));
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
}
