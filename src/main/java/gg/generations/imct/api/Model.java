package gg.generations.imct.api;

import de.javagl.jgltf.model.GltfConstants;
import de.javagl.jgltf.model.creation.AccessorModels;
import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.creation.MaterialBuilder;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.impl.DefaultSkinModel;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.v2.GltfModelWriterV2;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import org.joml.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Model {

    public List<DefaultNodeModel> skeleton;
    public List<DefaultNodeModel> joints;
    public final List<Mesh> meshes = new ArrayList<>();
    public final Map<String, Material> materials = new HashMap<>();

    protected ByteBuffer read(Path path) {
        try {
            return ByteBuffer.wrap(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + path.getFileName(), e);
        }
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

    public record Material(
            String name,
            List<Texture> textures
    ) {

        public Texture getTexture(String type) {
            for (var texture : textures) if (texture.type.endsWith(type)) return texture;
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
            List<Vector4f> colors,
            List<Vector4f> weights,
            List<Vector4i> boneIds,
            List<Vector3f> biNormals,
            List<Vector2f> uvs
    ) {
        public MeshPrimitiveBuilder create() {
            return MeshPrimitiveBuilder.create()
                    .setIntIndicesAsShort(IntBuffer.wrap(indices.stream().mapToInt(Integer::intValue).toArray())) // TODO: make it use int buffer if needed
                    .addNormals3D(toBuffer3(normals))
                    .addAttribute("JOINTS_0", AccessorModels.create(GltfConstants.GL_UNSIGNED_SHORT, "VEC4", false, Buffers.createByteBufferFrom(toUShort4(boneIds))))
                    .addAttribute("WEIGHTS_0", AccessorModels.create(GltfConstants.GL_FLOAT, "VEC4", false, Buffers.createByteBufferFrom(toBuffer4(weights))))
                    .addTexCoords02D(toBuffer2(uvs))
                    .addPositions3D(toBuffer3(positions))
                    .setTriangles();
        }
    }
}
