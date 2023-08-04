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

    static float halfFloatToFloat(short bits) {
        int s = (bits >> 15) & 0x0001;
        int e = (bits >> 10) & 0x001F;
        int m = bits & 0x03FF;

        if (e == 0) {
            if (m == 0) {
                // +/- 0
                return Float.intBitsToFloat(s << 31);
            } else {
                // Denormalized number
                e = 1;
            }
        } else if (e == 31) {
            if (m == 0) {
                // +/- Infinity
                return Float.intBitsToFloat((s << 31) | 0x7F800000);
            } else {
                // NaN
                return Float.intBitsToFloat((s << 31) | 0x7F800000 | (m << 13));
            }
        }

        var exponent = e - 15;
        var mantissa = m << 13;
        var floatValueBits = (s << 31) | ((exponent + 127) << 23) | mantissa;
        return Float.intBitsToFloat(floatValueBits);
    }
}
