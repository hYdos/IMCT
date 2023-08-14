package gg.generations.imct.read.ctr;

import gg.generations.imct.api.Model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class UsUmModel extends Model {

    public UsUmModel(List<Path> files) {
        for (var file : files) {
            try {
                var bytes = Files.readAllBytes(file);
                if (bytes[0] == 'P' && bytes[1] == 'C') {
                    readModelArchive(ByteBuffer.wrap(bytes));
                } else {
                    throw new RuntimeException("Unknown model file type" + file.getFileName());
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read UsUm model", e);
            }
        }
    }

    private void readModelArchive(ByteBuffer buf) {
        var magic = readStr(buf, 2);
//        var sect
        System.out.println("ok");
    }

    private static String readStr(ByteBuffer buf, int len) {
        var str = new StringBuilder();
        for (var i = 0; i < len; i++) str.append((char) buf.get());
        return str.toString();
    }
}
