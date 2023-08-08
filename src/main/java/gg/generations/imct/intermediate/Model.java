package gg.generations.imct.intermediate;

import de.javagl.jgltf.model.GltfConstants;
import de.javagl.jgltf.model.creation.AccessorModels;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.io.Buffers;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.SVModel;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.Vec3;
import org.joml.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.DoubleStream;

public abstract class Model {

    protected List<DefaultNodeModel> skeleton;
    protected final List<SVModel.Mesh> meshes = new ArrayList<>();
    protected final Map<String, SVModel.Material> materials = new HashMap<>();

    public abstract void writeModel(Path path);

    protected ByteBuffer read(Path path) {
        try {
            return ByteBuffer.wrap(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + path.getFileName(), e);
        }
    }

    protected float readHalfFloat(short bits) {
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

    protected boolean isIdentityMatrix(Matrix4f matrix) {
        return matrix.isAffine() && matrix.m00() == 1.0f && matrix.m11() == 1.0f && matrix.m22() == 1.0f
               && matrix.m33() == 1.0f && matrix.m01() == 0.0f && matrix.m02() == 0.0f && matrix.m03() == 0.0f
               && matrix.m10() == 0.0f && matrix.m12() == 0.0f && matrix.m13() == 0.0f && matrix.m20() == 0.0f
               && matrix.m21() == 0.0f && matrix.m23() == 0.0f && matrix.m30() == 0.0f && matrix.m31() == 0.0f
               && matrix.m32() == 0.0f;
    }

    protected static ShortBuffer toUShort4(List<Vector4i> list) {
        var buffer = ShortBuffer.wrap(new short[list.size() * 4]);
        for (var element : list)
            buffer
                    .put((short) (element.x & 65535))
                    .put((short) (element.y & 65535))
                    .put((short) (element.z & 65535))
                    .put((short) (element.w & 65535));

        return buffer.rewind();
    }

    protected static FloatBuffer toBuffer4(List<Vector4f> list) {
        var buffer = FloatBuffer.wrap(new float[list.size() * 4]);
        for (var element : list)
            buffer
                    .put(element.x)
                    .put(element.y)
                    .put(element.z)
                    .put(element.w);

        return buffer.rewind();
    }

    protected static FloatBuffer toBuffer3(List<Vector3f> list) {
        var buffer = FloatBuffer.wrap(new float[list.size() * 3]);
        for (var element : list)
            buffer
                    .put(element.x)
                    .put(element.y)
                    .put(element.z);

        return buffer.rewind();
    }

    protected static FloatBuffer toBuffer2(List<Vector2f> list) {
        var buffer = FloatBuffer.wrap(new float[list.size() * 2]);
        for (var element : list)
            buffer
                    .put(element.x)
                    .put(element.y);

        return buffer.rewind();
    }

    protected Vector3f toVec3(Vec3 vec) {
        return new Vector3f(vec.x(), vec.y(), vec.z());
    }

    public record Material(
            String name,
            List<Texture> textures
    ) {

        public Texture getTexture(String type) {
            for (var texture : textures) if (texture.type.equals(type)) return texture;
            throw new RuntimeException("Texture of type " + type + " doesn't exist");
        }
    }

    public record Texture(
            String type,
            String filePath
    ) {
    }

    public record Mesh(
            String name,
            Material material,
            List<Integer> indices,
            List<Vector3f> positions,
            List<Vector3f> normals,
            List<Vector4f> tangents,
            List<Vector4f> weights,
            List<Vector4i> boneIds,
            List<Vector3f> biNormals,
            List<Vector2f> uvs
    ) {
        public MeshPrimitiveBuilder create() {
            var weights1 = weights.stream().map(vector4f -> vector4f.div(vector4f.x + vector4f.y + vector4f.z + vector4f.w)).toList();
            System.out.println("Weights: " + Arrays.toString(weights1.stream().flatMapToDouble(a -> DoubleStream.of(a.x + a.y + a.z + a.w)).toArray()));

            return MeshPrimitiveBuilder.create()
                    .setIntIndicesAsShort(IntBuffer.wrap(indices.stream().mapToInt(Integer::intValue).toArray())) // TODO: make it use int buffer if needed
                    .addNormals3D(toBuffer3(normals))
                    .addAttribute("JOINTS_0", AccessorModels.create(GltfConstants.GL_UNSIGNED_SHORT, "VEC4", false, Buffers.createByteBufferFrom(toUShort4(boneIds))))
                    .addAttribute("WEIGHTS_0", AccessorModels.create(GltfConstants.GL_FLOAT, "VEC4", false, Buffers.createByteBufferFrom(toBuffer4(weights1))))
                    .addTexCoords02D(toBuffer2(uvs))
                    .addPositions3D(toBuffer3(positions))
                    .setTriangles();
        }
    }
}
