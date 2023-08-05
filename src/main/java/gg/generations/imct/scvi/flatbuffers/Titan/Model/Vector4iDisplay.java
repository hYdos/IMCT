package gg.generations.imct.scvi.flatbuffers.Titan.Model;

import org.joml.Vector4i;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Vector4iDisplay extends JFrame {

    private final Map<String, Vector4i> dataMap;

    public Vector4iDisplay(Map<String, Vector4i> dataMap) {
        this.dataMap = dataMap;
        setupUI();
    }

    private void setupUI() {
        setTitle("Vector4i Display");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String[] columnNames = {"Name", "Vector", "Color"};

        Object[][] data = new Object[dataMap.size()][3];

        int row = 0;
        for (Map.Entry<String, Vector4i> entry : dataMap.entrySet()) {
            String name = entry.getKey();
            Vector4i vector = entry.getValue();
            data[row][0] = name;
            data[row][1] = "[%s, %s, %s, %s]".formatted(vector.x, vector.y, vector.z, vector.w);
            data[row][2] = createColorImage(vector);
            row++;
        }

        JTable table = new JTable(data, columnNames);
        table.getColumnModel().getColumn(2).setCellRenderer(new ImageRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane);

        pack();
        setLocationRelativeTo(null);
    }

    private ImageIcon createColorImage(Vector4i vector) {
        int width = 32;
        int height = 32;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        int r = vector.x();
        int gVal = vector.y();
        int b = vector.z();
        int a = vector.w();

        Color color = new Color(r, gVal, b, a);
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();

        return new ImageIcon(image);
    }

    private static class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setIcon((ImageIcon) value);

            return label;
        }
    }

    public void saveDisplayAsImage(String filename) {
        try {
            Container content = getContentPane();
            Dimension size = content.getSize();
            BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            content.printAll(g2d);
            g2d.dispose();
            ImageIO.write(image, "png", new File(filename));
            System.out.println("Image saved as " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}