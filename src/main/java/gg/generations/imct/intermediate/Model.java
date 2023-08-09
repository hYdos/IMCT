package gg.generations.imct.intermediate;

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
import gg.generations.imct.scvi.flatbuffers.Titan.Model.Vec3;
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

    protected List<DefaultNodeModel> skeleton;

    protected List<DefaultNodeModel> joints;

    protected final List<Mesh> meshes = new ArrayList<>();
    protected final Map<String, Material> materials = new HashMap<>();

    public void writeModel(Path path) {
        try {
            var sceneModel = new DefaultSceneModel();
            var sceneMaterials = new HashMap<Material, MaterialModelV2>();

            for (var value : materials.values()) {
                var material = MaterialBuilder.create()
                        .setBaseColorTexture("file:///" + value.getTexture("BaseColorMap").filePath().replace("\\", "/"), "image/png", 0)
                        .setDoubleSided(true)
                        .build();
                material.setName(value.name());

                sceneMaterials.put(value, material);
            }

            var skin = new DefaultSkinModel();
            joints.forEach(skin::addJoint);

            var root = skeleton.get(0);

            sceneModel.addNode(root);
            var ibmBuffer = FloatBuffer.allocate(16 * (skin.getJoints().size()));

            var arr = new float[16];

            var matrix = new Matrix4f();

            for (DefaultNodeModel jointNode : joints) {
                matrix.set(jointNode.computeGlobalTransform(null)).invert();
                matrix.get(arr);
                ibmBuffer.put(arr);
            }

            ibmBuffer.rewind();

            skin.setInverseBindMatrices(AccessorModels.create(GltfConstants.GL_FLOAT, "MAT4", false, Buffers.createByteBufferFrom(ibmBuffer)));

            for (var mesh : meshes) {
                var meshModel = new DefaultMeshModel();
                var meshPrimitiveModel = mesh.create().build();

                meshPrimitiveModel.setMaterialModel(sceneMaterials.get(mesh.material()));
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
                meshModel.setName(mesh.name());

                // Create a node with the mesh
                var nodeModel = new DefaultNodeModel();
                nodeModel.setName(mesh.name());
                nodeModel.addMeshModel(meshModel);
                nodeModel.setSkinModel(skin);
                root.addChild(nodeModel);
            }


            // Pass the scene to the model builder. It will take care
            // of the other model elements that are contained in the scene.
            // (I.e. the mesh primitive and its accessors, and the material
            // and its textures)
            var gltfModelBuilder = GltfModelBuilder.create();
            gltfModelBuilder.addSkinModel(skin);
            gltfModelBuilder.addSceneModel(sceneModel);
            var gltfModel = gltfModelBuilder.build();

            var gltfWriter = new GltfModelWriterV2();
            gltfWriter.writeBinary(gltfModel, Files.newOutputStream(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create %s".formatted(path), e);
        }
    }

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
