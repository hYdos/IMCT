package gg.generations.imct;

import gg.generations.imct.read.la.LAModel;
import gg.generations.imct.read.letsgo.LGModel;
import gg.generations.imct.read.scvi.SVModel;
import gg.generations.imct.read.swsh.SWSHModel;
import gg.generations.imct.write.GlbWriter;
import nu.pattern.OpenCV;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class IMCT {
    public static final Set<String> TOTAL_SHADERS = new HashSet<>();
    public static boolean messWithTexture = true;
    public static boolean shouldGenerateDebug = false; //Set to true to print various stages of the image assembly.
    public static boolean shouldCheckOrigin = false; //Set to true to check alter skeleton to include origin.


    public static void main(String[] args) throws IOException {

//        GlbWriter.write(new SVModel(Paths.get("F:\\PokemonModels\\SV\\pokemon\\data\\pm0006\\pm0006_00_00")), Paths.get("output/ScarletViolet.glb"));
//        GlbWriter.write(Paths.get("C:\\Users\\water\\Downloads\\SV-Poke\\pokemon\\data\\pm0004\\pm0004_00_00"), SVModel::new, Paths.get("output/0004"));
//        GlbWriter.write(Paths.get("C:\\Users\\water\\Downloads\\SV-Poke\\pokemon\\data\\pm0005\\pm0005_00_00"), SVModel::new, Paths.get("output/0005"));

//        GlbWriter.write(Paths.get("C:\\Users\\water\\Downloads\\BrokenPokemon\\BrokenPokemon\\pm0157\\pm0157_00_41"), SVModel::new, Paths.get("output\\blep\\pm0157_00_41"));
//        GlbWriter.write(Paths.get("C:\\Users\\water\\Downloads\\BrokenPokemon\\BrokenPokemon\\pm0049\\pm0049_00_00"), SVModel::new, Paths.get("output\\bart\\pm0049_00_00"));
//        GlbWriter.write(Paths.get("C:\\Users\\water\\Downloads\\pm0025\\pm0025_01_00"), SVModel::new, Paths.get("output\\pikachu\\pm0025_11_00"));
//        GlbWriter.write(Paths.get("C:\\Users\\water\\Downloads\\pm0025\\pm0025_11_00"), SVModel::new, Paths.get("output\\pikachu\\pm0025_01_00"));

//
        var path = Paths.get("C:\\Users\\water\\Downloads\\BrokenPokemon\\BrokenPokemon");

//        if(true) return;

        var paths = Files.walk(path, 0).flatMap(x -> {
            try {
                return Files.walk(x, 1).filter(a -> !x.equals(a)).flatMap(a -> {
//                    System.out.print("a -> " + a);
                    try {
                        return Files.walk(a, 1).filter(b -> !b.equals(a));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        int size = paths.size();

        executeSequentially(paths, (p, i) -> () -> {
            System.out.println((i + 1) + "/" + (size) + " Processing " + p.toString());
            try {
                write(p);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }, 27).join();

        System.out.println("Tasks Complete.");
//
//        System.out.println(TOTAL_SHADERS);

//        write(Paths.get("C:\\Users\\water\\Downloads\\SV-Poke\\pokemon\\data\\pm0006\\pm0006_00_00"));
//        GlbWriter.write(new SWSHModel(Paths.get("F:\\PokemonModels\\SWSH\\pm0006_81_00")), Paths.get("output/SwordShield.glb"));
//        GlbWriter.write(new LGModel(Paths.get("F:\\PokemonModels\\LGPE\\pm0008_00")), Paths.get("output/LetsGoPikachuEevee.glb"));
//        GlbWriter.write(new UsUmModel(List.of(
//                Paths.get("F:\\PokemonModels\\USUM\\1 (Model)\\1162 - Poipole.bin"),
//                Paths.get("F:\\PokemonModels\\USUM\\2 (Tex)\\1162 - Poipole.bin")
//        )), Paths.get("output/UltraSunUltraMoon.glb"));
    }

    public static void write(Path path) {
        try {
            var oput = Paths.get("brokenpokes/" + path.getFileName().toString());
        GlbWriter.write(path, SVModel::new, oput);

//            deleteFolder(oput);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteFolder(Path folderPath) throws IOException {
        Files.walkFileTree(folderPath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        // Handle file visit failure (optional)
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            // Directory iteration failed
                            throw exc;
                        }
                    }
                });
    }

    private static CompletableFuture<Void> executeSequentially(List<Path> tasks, BiFunction<Path, Integer, Runnable> function, int index) {
        var futureFunction = function;

        CompletableFuture<Void> resultFuture = CompletableFuture.completedFuture(null);


        for (int i = 0; i < tasks.size(); i++) {
            resultFuture = resultFuture.thenRun(futureFunction.apply(tasks.get(i), i));
        }

        return resultFuture;
    }

    @FunctionalInterface
    public interface ThrowingBiFunction<T, U, R, E extends Exception> {
        R apply(T t, U u) throws E;
    }
}