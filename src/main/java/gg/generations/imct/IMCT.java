package gg.generations.imct;

import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.model.io.GltfAssetReader;
import de.javagl.jgltf.model.io.v2.GltfReaderV2;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.SVModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IMCT {

    public static void main(String[] args) throws IOException {
        var model = new SVModel(Paths.get("pm0053_00_00"));

        model.writeModel(Paths.get("out.glb"));

        GltfAssetReader reader = new GltfAssetReader();
        var gltf = reader.read(Paths.get("out.glb").toAbsolutePath()).getGltf();

        System.out.println();
    }
}