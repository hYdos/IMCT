package gg.generations.imct.write;

import com.google.gson.*;
import de.javagl.jgltf.model.GltfConstants;
import de.javagl.jgltf.model.creation.AccessorModels;
import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.impl.DefaultSkinModel;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.v2.GltfModelWriterV2;
import gg.generations.imct.IMCT;
import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.api.ApiTexture;
import gg.generations.imct.api.Mesh;
import gg.generations.imct.api.Model;
import gg.generations.imct.read.UvGenerate;
import gg.generations.imct.read.letsgo.LGModel;
import gg.generations.imct.read.scvi.ImageDisplayComponent;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeTextureGenerator;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.Collectors;

public class GlbWriter {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static final Path loading = Path.of("loading_additional");

    public static void copy(Path src, Path dst) {
        try {
            var path1 = dst.resolve(src.getFileName());
            if (!Files.exists(path1)) {
                Files.createDirectories(path1);
            }
            Files.copy(src, path1, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Model> void write(Path input, IMCT.ThrowingBiFunction<Path, Path, T, IOException> function, Path path) throws IOException {
        write(function.apply(input, path), input, path);
    }

    public static void write(Model model, Path input, Path path) {
        try {
            Files.createDirectories(path);
            var sceneModel = new DefaultSceneModel();

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

            var max = 0d;
            var min = 0d;

            for (var mesh : model.meshes) {
                max = Math.max(max, mesh.positions().stream().mapToDouble(a -> a.y).max().getAsDouble());
                min = Math.min(min, mesh.positions().stream().mapToDouble(a -> a.y).min().getAsDouble());

                var meshModel = new DefaultMeshModel();
                var meshPrimitiveModel = mesh.create().build();

                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
                meshModel.setName(mesh.name());

                // Create a node with the mesh
                var nodeModel = new DefaultNodeModel();
                nodeModel.setName(mesh.name());
                nodeModel.addMeshModel(meshModel);
                nodeModel.setSkinModel(skin);
                root.addChild(nodeModel);
            }

            var scale = 1f / (max - min);

//            model.materials.get("regular")
//                    .keySet().stream().map(s -> checkShiny(s, model, path)).reduce(CompletableFuture.completedFuture(null), (BiFunction<CompletableFuture<? extends Object>, Runnable, CompletableFuture<? extends Object>>) CompletableFuture::thenRun, CompletableFuture::allOf).join();
//
//            CompletableFuture.completedFuture(null).thenRun(checkShiny("eyes", model, path)).thenRun(checkShiny("fire", model, path)).join();


            Files.writeString(path.resolve("config.json"), generateJson(scale, model.materials, model.variants, model.meshes));
//
            model.materials.values().stream().flatMap(a -> a.textures().stream()).map(ApiTexture::filePath).distinct().forEach(texture -> {


                var target = Path.of(texture);

                try {
                    Files.copy(target, path.resolve(target.getFileName()));
                } catch (IOException e) {
//                    throw new RuntimeException(e);
                }
            });

            // Pass the scene to the model builder. It will take care
            // of the other model elements that are contained in the scene.
            // (I.e. the mesh primitive and its accessors, and the material
            // and its textures)
            var gltfModelBuilder = GltfModelBuilder.create();
            gltfModelBuilder.addSkinModel(skin);
            gltfModelBuilder.addSceneModel(sceneModel);
            var gltfModel = gltfModelBuilder.build();

            var gltfWriter = new GltfModelWriterV2();
            gltfWriter.writeBinary(gltfModel, Files.newOutputStream(path.resolve("model.glb")));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create %s".formatted(path), e);
        }

        try {
            Files.walk(input).filter(a -> a.toString().endsWith("tranm") || a.toString().endsWith("gfbanm")).forEach(a -> copy(a, path));

//            var list = Files.walk(path).toList();

//            PixelmonArchiveBuilder.convertToPk(path,
//                    list,
//                    path.getParent().resolve(path.getFileName().toString() + ".pk"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateJson(double scale, Map<String, ApiMaterial> materials, Map<String, Map<String, String>> variants, List<Mesh> meshes) {
        var materialsJson = materials.entrySet().stream().collect(Collectors.toMap(a -> a.getValue(), a -> a.getKey()));

        System.out.println("Meshes");

        Map<String, String> meshMap = new HashMap<>();
        for (Mesh x : meshes) {
            System.out.println(x.name());
            if (meshMap.put(x.name(), x.material()) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }

        var json = new JsonObject();

        json.addProperty("scale", scale);
        var jsonMaterials = new JsonObject();

        materialsJson.forEach((material, name) -> {
            var json1 = new JsonObject();

            json1.addProperty("shader", (String) material.properties().remove("shader"));
            var textures = new JsonObject();
            material.textures().stream().filter(Objects::nonNull).forEach(new Consumer<ApiTexture>() {
                @Override
                public void accept(ApiTexture apiTexture) {
                    textures.addProperty(apiTexture.type(), Path.of(material.getTexture(apiTexture.type()).filePath()).getFileName().toString());
                }
            });

            json1.add("images", textures);

            if(!material.properties().isEmpty()) {
                var props = new JsonObject();
                material.properties().forEach((key, value) -> {
                    if (value instanceof Float f) {
                        props.addProperty(key, f);
                    } else if (value instanceof String string) {
                        props.addProperty(key, string);
                    } else if (value instanceof Boolean bool) {
                        props.addProperty(key, bool);
                    } else System.out.println(key);
                });

                json1.add("values", props);
            }



            jsonMaterials.add(name, json1);
        });

        json.add("materials", jsonMaterials);

        var defaultVariantJson = new JsonObject();
        meshMap.forEach((k, v) -> {
            var a = new JsonObject();
            a.addProperty("material", v);
            a.addProperty("hide", false);
            defaultVariantJson.add(k, a);
        });

        json.add("defaultVariant", defaultVariantJson);

        var variantsJson = new JsonObject();

        for (String variant : variants.keySet()) {
            var data = variants.get(variant);

            var a = new JsonObject();

            for(var mesh : data.keySet()) {
                var material = data.get(mesh);

                try {

                    if (!meshMap.get(mesh).equals(material)) {
                        var variantJson = new JsonObject();
                        variantJson.addProperty("material", material);
                        a.add(mesh, variantJson);
                    }
                } catch (Exception e) {
                    System.out.println();
                }
            }

            variantsJson.add(variant, a);
        }

        json.add("variants", variantsJson);

        return gson.toJson(json);

    }
}
