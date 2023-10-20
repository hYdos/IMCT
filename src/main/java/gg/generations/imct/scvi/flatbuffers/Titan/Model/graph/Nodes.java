package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import org.joml.Vector4i;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;

public class Nodes {
    public static BufferedImage DEFAULT_IMAGE = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);

    public static EyeTextureGenerator.ChannelImages splitImageChannels(BufferedImage originalImage) {
        BufferedImage[] resultImages = new BufferedImage[4]; // Four grayscale images: R, G, B, A

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Loop through each channel (R, G, B, A)
        for (int channel = 0; channel < 4; channel++) {
            resultImages[channel] = new BufferedImage(width, height, TYPE_BYTE_GRAY);
            WritableRaster raster = resultImages[channel].getRaster();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = originalImage.getRGB(x, y);
                    int value = 0;

                    // Extract the desired channel's value (R, G, B, or A)
                    switch (channel) {
                        case 0: // Red channel
                            value = (pixel >> 16) & 0xFF;
                            break;
                        case 1: // Green channel
                            value = (pixel >> 8) & 0xFF;
                            break;
                        case 2: // Blue channel
                            value = pixel & 0xFF;
                            break;
                        case 3: // Alpha channel
                            value = (pixel >> 24) & 0xFF;
                            break;
                    }

                    // Set the grayscale value for the channel
                    raster.setSample(x, y, 0, value);
                }
            }
        }

        return new EyeTextureGenerator.ChannelImages(resultImages[0], resultImages[1], resultImages[2], resultImages[3]);
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

}


