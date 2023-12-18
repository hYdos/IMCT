package gg.generations.imct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class SendOver {
    public static void main(String args[]) throws IOException {
        var target = Path.of("C:\\Users\\water\\Downloads\\SV-PokeDLC2\\pokemon\\data");

        var source = Path.of("C:\\Users\\water\\Downloads\\SV-PokeAnimDLC2\\pokemon\\data");

        Files.walk(source, 1).forEach(new Consumer<Path>() {
                    @Override
                    public void accept(Path path) {
                        try {
                            var path1 = target.resolve(path.getFileName());
                            Files.walk(path, 1).filter(a -> {
                                try {
                                    return !Files.isSameFile(path, a);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }).forEach(x -> {
                                try {
                                    var path2 = path1.resolve(x.getFileName());

                                    if(!Files.exists(path2)) return;

                                    Files.walk(x, 1).filter(a -> {
                                        try {
                                            return !Files.isSameFile(path, a);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }).filter(a -> a.toString().endsWith("tranm")).forEach(new Consumer<Path>() {
                                        @Override
                                        public void accept(Path path) {
                                            try {
                                                Files.copy(path, path2.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });


//                .filter(a -> a.toString().endsWith("tranm")).forEach(x -> {
//            var anim = source.resolve(x).toAbsolutePath();



//            Files.copy()

//            System.out.println(anim);
//        });
    }
}
