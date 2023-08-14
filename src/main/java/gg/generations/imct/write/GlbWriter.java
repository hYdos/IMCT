package gg.generations.imct.write;

import de.javagl.jgltf.model.GltfConstants;
import de.javagl.jgltf.model.creation.AccessorModels;
import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.creation.MaterialBuilder;
import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.impl.DefaultSkinModel;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.v2.GltfModelWriterV2;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.api.Model;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class GlbWriter {

    public static void write(Model model, Path path) {
        try {
            Files.createDirectories(path.getParent());
            var sceneModel = new DefaultSceneModel();
            var sceneMaterials = new HashMap<ApiMaterial, MaterialModelV2>();

            for (var value : model.materials.values()) {
                var material = MaterialBuilder.create()
                        .setBaseColorTexture("file:///" + value.getTexture("BaseColorMap").filePath().replace("\\", "/"), "image/png", 0)
                        .setDoubleSided(true)
                        .build();
                material.setName(value.name());

                sceneMaterials.put(value, material);
            }

            var skin = new DefaultSkinModel();
            model.joints.forEach(skin::addJoint);

            var root = model.skeleton.get(0);

            sceneModel.addNode(root);
            var ibmBuffer = FloatBuffer.allocate(16 * (skin.getJoints().size()));

            var arr = new float[16];

            var matrix = new Matrix4f();

            for (var jointNode : model.joints) {
                matrix.set(jointNode.computeGlobalTransform(null)).invert();
                matrix.get(arr);
                ibmBuffer.put(arr);
            }

            ibmBuffer.rewind();

            skin.setInverseBindMatrices(AccessorModels.create(GltfConstants.GL_FLOAT, "MAT4", false, Buffers.createByteBufferFrom(ibmBuffer)));

            for (var mesh : model.meshes) {
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
}
