package gg.generations.imct.util;

import org.joml.Vector2f;
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

    public static short writeHalfFloat(float value) {
        var floatValueBits = Float.floatToIntBits(value);

        var sign = (floatValueBits >> 31) & 0x00000001;
        var exponent = (floatValueBits >> 23) & 0x000000ff;
        var mantissa = floatValueBits & 0x007fffff;

        var halfValueBits = (sign << 15) | ((exponent - 127 + 15) << 10) | (mantissa >> 13);

        return (short) halfValueBits;
    }

    public static Vector2f readUVFloat(ByteBuffer buf) {
        var x = readHalfFloat(buf.getShort());
        var y = 1 - readHalfFloat(buf.getShort());
        return new Vector2f(x, y);
    }

    public static float readHalfFloat(short value) {
        var halfValueBits = value & 0x0000ffff;

        var sign = (halfValueBits >> 15) & 0x00000001;
        var exponent = (halfValueBits >> 10) & 0x0000001f;
        var mantissa = halfValueBits & 0x000003ff;

        var floatValueBits = (sign << 31) | ((exponent - 15 + 127) << 23) | (mantissa << 13);

        return Float.intBitsToFloat(floatValueBits);
    }
}
