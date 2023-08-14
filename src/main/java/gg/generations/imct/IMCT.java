package gg.generations.imct;

import gg.generations.imct.read.ctr.UsUmModel;
import gg.generations.imct.read.la.LAModel;
import gg.generations.imct.read.letsgo.LGModel;
import gg.generations.imct.read.scvi.SVModel;
import gg.generations.imct.read.swsh.SWSHModel;
import gg.generations.imct.write.GlbWriter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class IMCT {

    public static void main(String[] args) {
        GlbWriter.write(new SVModel(Paths.get("F:\\PokemonModels\\SV\\pokemon\\data\\pm0006\\pm0006_00_00")), Paths.get("output/ScarletViolet.glb"));
        GlbWriter.write(new LAModel(Paths.get("F:\\PokemonModels\\LA\\pm0486_00_00")), Paths.get("output/LegendsArceus.glb"));
        GlbWriter.write(new SWSHModel(Paths.get("F:\\PokemonModels\\SWSH\\pm0006_81_00")), Paths.get("output/SwordShield.glb"));
        GlbWriter.write(new LGModel(Paths.get("F:\\PokemonModels\\LGPE\\pm0008_00")), Paths.get("output/LetsGoPikachuEevee.glb"));
        GlbWriter.write(new UsUmModel(List.of(
                Paths.get("F:\\PokemonModels\\USUM\\1 (Model)\\1162 - Poipole.bin"),
                Paths.get("F:\\PokemonModels\\USUM\\2 (Tex)\\1162 - Poipole.bin")
        )), Paths.get("output/UltraSunUltraMoon.glb"));
    }
}