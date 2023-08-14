package gg.generations.imct.util;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

public class TrinityUtils {

    public static Vector4f readWeights(ByteBuffer vertexBuffer) {
        var w = ((float) (vertexBuffer.getShort() & 0xFFFF)) / 65535;
        var x = ((float) (vertexBuffer.getShort() & 0xFFFF)) / 65535;
        var y = ((float) (vertexBuffer.getShort() & 0xFFFF)) / 65535;
        var z = ((float) (vertexBuffer.getShort() & 0xFFFF)) / 65535;
        return new Vector4f(x, y, z, w).div(x + y + z + w);
    }

    public static Vector3f readRGBA16Float3(ByteBuffer buf) {
        var x = readHalfFloat(buf.getShort()); // Ignored. Maybe padding?
        var y = readHalfFloat(buf.getShort());
        var z = readHalfFloat(buf.getShort());
        var w = readHalfFloat(buf.getShort());
        return new Vector3f(x, y, z);
    }

    public static Vector4f readRGBA16Float4(ByteBuffer buf) {
        var x = readHalfFloat(buf.getShort()); // Ignored. Maybe padding?
        var y = readHalfFloat(buf.getShort());
        var z = readHalfFloat(buf.getShort());
        var w = readHalfFloat(buf.getShort());
        return new Vector4f(x, y, z, w);
    }

    public static float readHalfFloat(short bits) {
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
