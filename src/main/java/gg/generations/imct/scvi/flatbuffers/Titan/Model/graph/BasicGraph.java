package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_UNCHANGED;

public class BasicGraph {
    public static void main(String[] args) {
        OpenCV.loadLocally();

        var channels = splitChannels("C:\\Users\\water\\Downloads\\SV-Poke\\pokemon\\data\\pm1080\\pm1080_11_00\\pm1080_11_00_eye_lym.png");

        Scalar color1 = new Scalar(0, 255, 0, 255);

        for (int i = 0; i < channels.size(); i++) {

            Mat m = new Mat(); // performHSVManipulation(channels.get(i));

            Imgproc.cvtColor(channels.get(i), m, Imgproc.COLOR_GRAY2RGBA);
            Mat color = colorizeRGBA(m, color1);

            Imgcodecs.imwrite("channel " + i + ".png", color);
            channels.get(i).release();
//            m.release();
            color.release();

        }

//        var color = new ColorNode().setSize(128, 128).setColor(1.0f, 0, 0, 1);

//        color.display("RawreRawr");

//        var channelSplitter = new ChannelSplitterNode().setInput(new ScaleNode().setScale(128).setInput(texture));
//
//        channelSplitter.getRedChannel().getInputData().display("RedChannel");
//        channelSplitter.getGreenChannel().getInputData().display("GreenChannel");
//        channelSplitter.getBlueChannel().getInputData().display("BlueChannel");
//        channelSplitter.getAlphaChannel().getInputData().display("AlphaChannel");
//
//        channelSplitter.getBlueChannel().getInputData().display("rawr");
//
//        new MaskNode().setMask(channelSplitter.getRedChannel()).setColor(3, 3, 3).getInputData().display("Red");
//        new CompositeNode().setComposite(BlendComposite.Multiply).setTop(color).setBottom(channelSplitter.getGreenChannel()).getInputData().display("Green");
//        new CompositeNode().setComposite(BlendComposite.Difference).setTop(color).setBottom(channelSplitter.getBlueChannel()).getInputData().display("Blue");
//        new CompositeNode().setComposite(Composites.SCREEN).setTop(color).setBottom(channelSplitter.getAlphaChannel()).getInputData().display("Alpha");
    }

    public static Mat colorizeRGBA(Mat rgbaImage, Scalar color) {
        if (rgbaImage.channels() != 4 || rgbaImage.depth() != CvType.CV_8U) {
            System.out.println("Invalid input image format");
            return null;
        }

        Mat coloredImage = new Mat();
        List<Mat> rgbaChannels = new ArrayList<>();

        // Split the channels
        Core.split(rgbaImage, rgbaChannels);

        // Apply the color to each channel
        for (int i = 0; i < 4; i++) {
            Core.add(rgbaChannels.get(i), color, rgbaChannels.get(i));
        }

        // Merge the channels back into a colored image
        Core.merge(rgbaChannels, coloredImage);

        // Release channel Mats
        for (Mat channel : rgbaChannels) {
            channel.release();
        }

        return coloredImage;
    }

    public static Mat performHSVManipulation(Mat inputMat) {
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(inputMat, hsvImage, Imgproc.COLOR_GRAY2RGBA);
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_RGB2HSV);

        // Perform HSV manipulation here
        // For example, increase the saturation:
        Core.multiply(hsvImage, new Scalar(0.5, 1.0, 2), hsvImage);

        Mat manipulatedBgrImage = new Mat();
        Imgproc.cvtColor(hsvImage, manipulatedBgrImage, Imgproc.COLOR_HSV2RGB);

        hsvImage.release();

        return manipulatedBgrImage;
    }

    public static List<Mat> splitChannels(String imagePath) {
        Mat rgbaImage = Imgcodecs.imread(imagePath, IMREAD_UNCHANGED);

        if (rgbaImage.empty()) {
            System.out.println("Error loading image");
            return null;
        }

        List<Mat> channels = new ArrayList<Mat>();
        Core.split(rgbaImage, channels);

        return channels;

//        return new GrayscaleChannels(channels.get(0), channels.get(1), channels.get(2), channels.get(3));
    }
}
