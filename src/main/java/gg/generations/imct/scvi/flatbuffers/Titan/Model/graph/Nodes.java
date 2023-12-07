package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class Nodes {
    public static BufferedImage DEFAULT_IMAGE = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);

    public static EyeTextureGenerator.ChannelImages splitImageChannels(BufferedImage original) {
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
                    int newPixel = (process(original.getRGB(x, y), i) << 24) | (255 << 16) | (255 << 8) | 255; // White color
                    rasters[i].setPixel(x, y, colorModel.getComponents(newPixel, null, 0));
                }
            }
        }

        // Create result images from the rasters
        for (int i = 0; i < 4; i++) {
            resultImages[i] = new BufferedImage(colorModel, rasters[i], false, null);
        }

        return new EyeTextureGenerator.ChannelImages(resultImages[2], resultImages[1], resultImages[0], resultImages[3]);
    }

    private static int process(int pixel, int channel) {
        return (pixel >> (channel * 8)) & 0xFF;
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
}


