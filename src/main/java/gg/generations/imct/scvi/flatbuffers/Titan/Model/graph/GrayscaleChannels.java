package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

public record GrayscaleChannels(Mat red, Mat green, Mat blue, Mat alpha) implements AutoCloseable {
    @Override
    public void close() throws Exception {
        red.release();
        green.release();
        blue.release();
        alpha.release();
    }

    public void show() {
        HighGui.imshow("red", red);
        HighGui.imshow("green", green);
        HighGui.imshow("blue", blue);
        HighGui.imshow("alpha", alpha);
    }

    public void save() {
        Imgcodecs.imwrite("red.png", red);
        Imgcodecs.imwrite("green.png", green);
        Imgcodecs.imwrite("blue.png", blue);
        Imgcodecs.imwrite("alpha.png", alpha);
    }
}
