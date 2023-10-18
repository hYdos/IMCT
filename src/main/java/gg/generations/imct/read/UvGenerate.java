package gg.generations.imct.read;

import org.joml.Vector2f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class UvGenerate {
    private int width;
    private int height;

    // Define predefined mid-range pastel colors as static
    // Define a wider range of mid-range background colors as static
    private static final Color[] midRangeColors = {
            new Color(255, 220, 185).darker().darker(), // Peach
            new Color(210, 200, 233).darker().darker(), // Lavender
            new Color(181, 234, 215).darker().darker(), // Mint
            new Color(255, 196, 215).darker().darker(), // Pink
            new Color(200, 220, 255).darker().darker(), // SkyBlue
            new Color(255, 220, 190).darker().darker(), // Coral
            new Color(190, 210, 240).darker().darker(), // LightBlue
            new Color(250, 210, 246).darker().darker(), // Lilac
            new Color(215, 240, 210).darker().darker(), // PaleGreen
            new Color(255, 210, 160).darker().darker(), // Salmon
            new Color(210, 240, 210).darker().darker(), // MintGreen
            new Color(210, 220, 255).darker().darker(), // BabyBlue
            new Color(255, 220, 180).darker().darker(), // Apricot
            new Color(205, 215, 230).darker().darker(), // LightGray
            new Color(180, 200, 210).darker().darker(), // Teal
            new Color(250, 255, 220).darker().darker()  // LightYellow
    };


    public UvGenerate(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public BufferedImage generateUvMap(ArrayList<Vector2f> uvs, ArrayList<Integer> indices) {
        BufferedImage uvMapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = uvMapImage.createGraphics();

        // Set the background color to a generic UV map color (light gray)
        g2d.setColor(new Color(211, 211, 211));
        g2d.fillRect(0, 0, width, height);

        // Draw the 4x4 grid with mid-range predefined colors (background)
        drawGridWithMidRangeColors(g2d);

        boolean[][] filledPixels = new boolean[width][height];

        for (int i = 0; i < indices.size(); i += 3) {
            Vector2f uv1 = uvs.get(indices.get(i));
            Vector2f uv2 = uvs.get(indices.get(i + 1));
            Vector2f uv3 = uvs.get(indices.get(i + 2));

            // Calculate UV coordinates for the triangle
            int x1 = (int) (uv1.x * (width - 1));
            int y1 = (int) (uv1.y * (height - 1));
            int x2 = (int) (uv2.x * (width - 1));
            int y2 = (int) (uv2.y * (height - 1));
            int x3 = (int) (uv3.x * (width - 1));
            int y3 = (int) (uv3.y * (height - 1));

            // Draw the triangle in the UV map image, filling only unprocessed pixels
            drawTriangle(g2d, x1, y1, x2, y2, x3, y3, filledPixels);
        }

        g2d.dispose();
        return uvMapImage;
    }

    private void drawGridWithMidRangeColors(Graphics2D g2d) {
        int cellSizeX = width / 4;
        int cellSizeY = height / 4;

        int colorIndex = 0;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int x = j * cellSizeX;
                int y = i * cellSizeY;
                g2d.setColor(midRangeColors[colorIndex]);
                g2d.fillRect(x, y, cellSizeX, cellSizeY);
                colorIndex++;
            }
        }
    }

    private void drawTriangle(Graphics2D g2d, int x1, int y1, int x2, int y2, int x3, int y3, boolean[][] filledPixels) {
        g2d.setColor(Color.LIGHT_GRAY); // Color of the triangle lines

        int[] xPoints = {x1, x2, x3};
        int[] yPoints = {y1, y2, y3};

        for (int i = 0; i < 3; i++) {
            int xStart = xPoints[i];
            int yStart = yPoints[i];
            int xEnd = xPoints[(i + 1) % 3];
            int yEnd = yPoints[(i + 1) % 3];

            int dx = Math.abs(xEnd - xStart);
            int dy = Math.abs(yEnd - yStart);
            int sx = (xStart < xEnd) ? 1 : -1;
            int sy = (yStart < yEnd) ? 1 : -1;

            int err = dx - dy;

            while (true) {
                if (!filledPixels[xStart][yStart]) {
                    g2d.drawLine(xStart, yStart, xStart, yStart);
                    filledPixels[xStart][yStart] = true;
                }

                if (xStart == xEnd && yStart == yEnd) break;

                int e2 = 2 * err;
                if (e2 > -dy) {
                    err -= dy;
                    xStart += sx;
                }
                if (e2 < dx) {
                    err += dx;
                    yStart += sy;
                }
            }
        }
    }
}