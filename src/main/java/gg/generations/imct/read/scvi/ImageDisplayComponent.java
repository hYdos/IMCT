package gg.generations.imct.read.scvi;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

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

    private static final JFrame frame;

    private static final ImageDisplayComponent component;

    private static volatile Boolean response = null;

    static {
        frame = new JFrame("Image Display Test");

        // Create a panel to hold the component and buttons
        JPanel panel = new JPanel(new BorderLayout());

        // Create the ImageDisplayComponent
        component = new ImageDisplayComponent();

//        // Load two images from Paths
//        Path leftImagePath = Path.of("C:\\Users\\water\\Downloads\\20469810.png");
//        Path rightImagePath = Path.of("C:\\Users\\water\\Downloads\\Ho-oh 11.png");
//
//        BufferedImage leftImage = loadImageFromPath(leftImagePath);
//        BufferedImage rightImage = loadImageFromPath(rightImagePath);

//        component.setImages(leftImage, rightImage);

        // Create two buttons
        JButton button1 = new JButton("Same");
        JButton button2 = new JButton("Different");

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                response = true;
                frame.setVisible(false);
            }
        });

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                response = false;
                frame.setVisible(false);
            }
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
//        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static boolean compare(BufferedImage left, BufferedImage right) {
        component.setImages(left, right);
        frame.setVisible(true);

        while (response == null) {
            Thread.onSpinWait();
        }

        return response;
    }

    public static void main(String[] args) {
        System.out.println("Rawr: " + compare(null, null));
    }

    public static BufferedImage loadImageFromPath(Path imagePath) {
        try {
            return ImageIO.read(imagePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void dispose() {
        frame.dispose();
    }
}