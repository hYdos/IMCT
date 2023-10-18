package gg.generations.imct.api;

import de.javagl.jgltf.model.GltfConstants;
import de.javagl.jgltf.model.creation.AccessorModels;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.io.Buffers;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.List;

public record Mesh(
        String name,
        ApiMaterial material,
        List<Integer> indices,
        List<Vector3f> positions,
        List<Vector3f> normals,
        List<Vector4f> tangents,
        List<Vector4f> colors,
        List<Vector4f> weights,
        List<Vector4i> boneIds,
        List<Vector3f> biNormals,
        List<Vector2f> uvs
) {
    public MeshPrimitiveBuilder create() {
        if(boneIds.isEmpty()) return null;

        return MeshPrimitiveBuilder.create()
                .setIntIndicesAsShort(IntBuffer.wrap(indices.stream().mapToInt(Integer::intValue).toArray())) // TODO: make it use int buffer if needed
                .addNormals3D(toBuffer3(normals))
                .addAttribute("JOINTS_0", AccessorModels.create(GltfConstants.GL_UNSIGNED_SHORT, "VEC4", false, Buffers.createByteBufferFrom(toUShort4(boneIds))))
                .addAttribute("WEIGHTS_0", AccessorModels.create(GltfConstants.GL_FLOAT, "VEC4", false, Buffers.createByteBufferFrom(toBuffer4(weights))))
                .addTexCoords02D(toBuffer2(uvs))
                .addPositions3D(toBuffer3(positions))
                .setTriangles();
    }

    private static ShortBuffer toUShort4(List<Vector4i> list) {
        var buffer = ShortBuffer.wrap(new short[list.size() * 4]);
        for (var element : list)
            buffer
                    .put((short) (element.x & 65535))
                    .put((short) (element.y & 65535))
                    .put((short) (element.z & 65535))
                    .put((short) (element.w & 65535));

        return buffer.rewind();
    }

    private static FloatBuffer toBuffer4(List<Vector4f> list) {
        var buffer = FloatBuffer.wrap(new float[list.size() * 4]);
        for (var element : list)
            buffer
                    .put(element.x)
                    .put(element.y)
                    .put(element.z)
                    .put(element.w);

        return buffer.rewind();
    }

    private static FloatBuffer toBuffer3(List<Vector3f> list) {
        var buffer = FloatBuffer.wrap(new float[list.size() * 3]);
        for (var element : list)
            buffer
                    .put(element.x)
                    .put(element.y)
                    .put(element.z);

        return buffer.rewind();
    }

    private static FloatBuffer toBuffer2(List<Vector2f> list) {
        var buffer = FloatBuffer.wrap(new float[list.size() * 2]);
        for (var element : list)
            buffer
                    .put(element.x)
                    .put(element.y);

        return buffer.rewind();
    }

    public Mesh withNewMaterial(ApiMaterial material) {
        return new Mesh(
            name,
            material,
            indices,
            positions,
            normals,
            tangents,
            colors,
            weights,
            boneIds,
            biNormals,
            uvs
        );
    }
}
