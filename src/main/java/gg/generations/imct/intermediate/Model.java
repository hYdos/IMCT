package gg.generations.imct.intermediate;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Model {

    void writeModel(Path path);

    default ByteBuffer read(Path path) {
        try {
            return ByteBuffer.wrap(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + path.getFileName(), e);
        }
    }
}
