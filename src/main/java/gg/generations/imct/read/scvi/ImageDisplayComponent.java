package gg.generations.imct.read.scvi;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ImageDisplayComponent extends Component {
    private final int IMAGE_WIDTH = 256;
    private final int IMAGE_HEIGHT = 256;

    BufferedImage leftImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

    // Create a blank image for the right side
    BufferedImage rightImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

    public ImageDisplayComponent() {
        this.setSize(256 * 2, 256);
    }

    public void setImages(BufferedImage left, BufferedImage right) {
        this.leftImage = left;
        this.rightImage = right;
    }

    @Override
    public void paint(Graphics g) {
        // Draw the left image on the left side
        if (leftImage != null) g.drawImage(leftImage, 0, 0, IMAGE_WIDTH, IMAGE_WIDTH, null);

        // Draw the right image on the right side
        if (rightImage != null) g.drawImage(rightImage, IMAGE_WIDTH, 0, IMAGE_WIDTH, IMAGE_WIDTH, null);
    }

    public static class Proxy extends Frame {
        private final ImageDisplayComponent component;
        private final JFrame frame;

        private volatile Boolean response = null;

        public Proxy(String name, BufferedImage left, BufferedImage right, CompletableFuture<Boolean> resultFuture) {
            frame = new JFrame(name);

            // Create a panel to hold the component and buttons
            JPanel panel = new JPanel(new BorderLayout());

            // Create the ImageDisplayComponent
            component = new ImageDisplayComponent();
            component.setImages(left, right);

            // Create two buttons
            JButton button1 = new JButton("Same");
            JButton button2 = new JButton("Different");

            button2.addActionListener(e -> {
                resultFuture.complete(true);
                frame.dispose();
            });

            button1.addActionListener(e -> {
                resultFuture.complete(false);
                frame.dispose();
            });

            // Add the component and buttons to the panel
            panel.add(component, BorderLayout.CENTER);

            // Create another panel for the buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(button1);
            buttonPanel.add(button2);

            // Add the button panel to the panel at the SOUTH position
            panel.add(buttonPanel, BorderLayout.SOUTH);

            frame.add(panel);
            frame.setSize(512, 320); // Adjust the frame size to accommodate buttons
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        public static CompletableFuture<Boolean> compare(String name, BufferedImage left, BufferedImage right) {

            if(right == null) {
                return CompletableFuture.completedFuture(true);
            }
            CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();

            new Proxy(name, left, right, resultFuture);

            return resultFuture;
        }

        public static void main(String[] args) {
            System.out.println("Rawr: " + compare("rawre", null, null));
        }

        public static BufferedImage loadImageFromPath(Path imagePath) {
            try {
                return ImageIO.read(imagePath.toFile());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}