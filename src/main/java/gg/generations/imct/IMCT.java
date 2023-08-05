package gg.generations.imct;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.SVModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IMCT {

    public static void main(String[] args) throws IOException {
//        Files.list(Paths.get("F:/PokemonModels/SV/pokemon/data")).filter(Files::isDirectory).forEach(path -> {
//            try {
//                Files.list(path).forEach(modelType -> {
//                    System.out.println("Processing " + modelType);
//                    try {
//                        var model = new SVModel(modelType);
//                        model.writeModel(Paths.get(modelType.getFileName() + ".glb"));
//                    } catch (Throwable t) {
//                        System.err.println(modelType + " Failed :(");
//                    }
//
//                });
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });

        var folder = Paths.get("C:\\Users\\water\\Downloads\\pm0006_00_00");
        var model = new SVModel(folder);
        var outModel = Paths.get("out.glb");
        model.writeModel(outModel);
    }
}