package gg.generations.imct;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.SVModel;

import java.nio.file.Files;
import java.nio.file.Paths;

public class IMCT {

    public static void main(String[] args) {
        var folder = Files.exists(Paths.get("C:\\users\\hydos")) ? Paths.get("F:/PokemonModels/SV/pokemon/data/pm0053/pm0053_00_00") : Paths.get("pm0053_00_00");
        var model = new SVModel(folder);
        var outModel = Paths.get("out.glb");

        model.writeModel(outModel);
    }
}