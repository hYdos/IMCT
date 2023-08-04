package gg.generations.imct;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.SVModel;

import java.nio.file.Paths;

public class IMCT {

    public static void main(String[] args) {
        var model = new SVModel(Paths.get("F:/PokemonModels/SV/pokemon/data/pm0053/pm0053_00_00"));

        model.writeModel(Paths.get("out.gltf"));
    }
}