package gg.generations.imct.write;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.gson.*;
import de.javagl.jgltf.model.GltfConstants;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.creation.AccessorModels;
import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.impl.*;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.io.v2.GltfModelWriterV2;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import org.joml.Matrix4f;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GlbReader {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static void main(String[] args) {
        read(Path.of("C:\\Users\\water\\Downloads\\fridge.glb"));
    }

    public static record Material(String name, String image) {}

    public static void read(Path path) {
        var gltfReader = new GltfModelReader();

        try {
            var gltf = (DefaultGltfModel) gltfReader.readWithoutReferences(Files.newInputStream(path));


            var images = gltf.getTextureModels().stream().collect(Collectors.toMap(a -> a, a -> {
                var node = a.getImageModel();
                return new Texture(node.getName(), node.getImageData());
            }));

            var materials = gltf.getMaterialModels().stream().map(a -> (MaterialModelV2) a).map(a -> new Material(a.getName(), a.getBaseColorTexture().getImageModel().getName().replace(".tga", "") + ".png")).toList();

            var defaultVariant = new HashMap<String, String>();

            for(var mesh : gltf.getMeshModels()) {
                if(mesh.getName().endsWith("Iris")) continue;

                defaultVariant.put(mesh.getName(), mesh.getMeshPrimitiveModels().get(0).getMaterialModel().getName());
            }

            var p = Path.of("output1").resolve(path.getFileName().toString().replace(".glb", ""));

            if(Files.notExists(p)) Files.createDirectories(p);

            images.values().forEach(texture -> {
                try {
                    var x = p.resolve(texture.name.replace(".tga", "") + ".png");
                    var image = ImageIO.read(new ByteBufferBackedInputStream(texture.buffer()));
                    ImageIO.write(image, "PNG", Files.newOutputStream(x));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            var gltfModel = strip(gltf);

            JsonObject config = new JsonObject();
            config.addProperty("scale", gltfModel.right());
            var jsonMaterials = new JsonObject();
            materials.forEach(material -> {
                var json = new JsonObject();
                json.addProperty("type", "solid");
                json.addProperty("texture", material.image);
                jsonMaterials.add(material.name(), json);
            });
            config.add("materials", jsonMaterials);

            config.add("defaultVariant", defaultVarint(defaultVariant));


            var variantJson = new JsonObject();
            variantJson.add("regular", new JsonObject());
            config.add("variants", variantJson);

            var string = gson.toJson(config);

            var p1 = p.resolve("config.json");
            Files.writeString(p1, string);


            var gltfWriter = new GltfModelWriterV2();
            Files.createDirectories(p);
            gltfWriter.writeBinary(gltfModel.left(), Files.newOutputStream(p.resolve(path.getFileName())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static JsonElement defaultVarint(HashMap<String, String> defaultVariant) {
        var defaultVariantJson = new JsonObject();
        defaultVariant.forEach((s, s2) -> {
            var json = new JsonObject();
            json.addProperty("material", s2);
            json.addProperty("hide", false);
            defaultVariantJson.add(s, json);
        });

        return defaultVariantJson;
    }

    private static record Texture(String name, ByteBuffer buffer) {}

    public static Pair<GltfModel, Double> strip(DefaultGltfModel gltf) {
        var sceneModel = new DefaultSceneModel();

        var pair = calculateSkin(sceneModel);

        var max = 0d;
        var min = 0d;

        List<MeshModel> meshModels = gltf.getMeshModels();
        for (MeshModel mesh : meshModels) {
            if(mesh.getName().endsWith("Iris")) {
                continue;
            }
            var primitive = (DefaultMeshPrimitiveModel) mesh.getMeshPrimitiveModels().get(0);

            var meshModel = new DefaultMeshModel();
            var meshPrimitiveModel = MeshPrimitiveBuilder.create();

            primitive.getAttributes().forEach((s, a) -> meshPrimitiveModel.addAttribute(s, AccessorModels.create(a.getComponentType(), a.getElementType().name(), a.isNormalized(), a.getAccessorData().createByteBuffer())));
            meshPrimitiveModel.setTriangles();
            var indices = primitive.getIndices();
            meshPrimitiveModel.setIndices(AccessorModels.create(indices.getComponentType(), indices.getElementType().name(), indices.isNormalized(), indices.getAccessorData().createByteBuffer()));


            var accessor = mesh.getMeshPrimitiveModels().get(0).getAttributes().get("POSITION");
            max = Math.max(max, accessor.getMin()[1].doubleValue());
            min = Math.min(min, accessor.getMax()[1].doubleValue());

            var m = meshPrimitiveModel.build();
            meshModel.addMeshPrimitiveModel(m);
            meshModel.setName(mesh.getName());
            // Create a node with the mesh

            var nodeModel = new DefaultNodeModel();
            nodeModel.setName(mesh.getName());
            nodeModel.addMeshModel(meshModel);
//            nodeModel.setSkinModel(pair.right());
            pair.left().addChild(nodeModel);
        }

        var gltfModelBuilder = GltfModelBuilder.create();
//        gltfModelBuilder.addSkinModel(pair.right());
        gltfModelBuilder.addSceneModel(sceneModel);

        return new Pair<>(gltfModelBuilder.build(), (1f/ (max - min)));
    }

    private static Pair<DefaultNodeModel, DefaultSkinModel> calculateSkin(DefaultSceneModel sceneModel) {

//        var newSkin = new DefaultSkinModel();
        var newRoot = new DefaultNodeModel();

//        var ibmBuffer = FloatBuffer.allocate(16 * (newSkin.getJoints().size()));

//        var arr = new float[16];

//        var matrix = new Matrix4f();

//        List<NodeModel> joints = newSkin.getJoints();
//        for (NodeModel jointNode : joints) {
//            matrix.set(jointNode.computeGlobalTransform(null)).invert();
//            matrix.get(arr);
//            ibmBuffer.put(arr);
//        }
//
//        ibmBuffer.rewind();
//
//        newSkin.setInverseBindMatrices(AccessorModels.create(GltfConstants.GL_FLOAT, "MAT4", false, Buffers.createByteBufferFrom(ibmBuffer)));

        sceneModel.addNode(newRoot);

        return new Pair<DefaultNodeModel, DefaultSkinModel>(newRoot, null);
    }

    private static void populate(DefaultNodeModel root, DefaultNodeModel newRoot, DefaultSkinModel newSkin, boolean isRoot) {
        newRoot.setTranslation(root.getTranslation());
        newRoot.setRotation(root.getRotation());
        newRoot.setScale(root.getScale());
        newRoot.setName(root.getName());
        if(!isRoot) newSkin.addJoint(newRoot);

        if(root.getChildren() != null) {
            for(var child : root.getChildren()) {
                if(!child.getMeshModels().isEmpty()) continue;
                var newChild = new DefaultNodeModel();
                newRoot.addChild(newChild);
                populate((DefaultNodeModel) child, newChild, newSkin, false);
            }
        }
    }

    public static record Pair<L, R>(L left, R right) {}
}
