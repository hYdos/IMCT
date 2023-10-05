package gg.generations.imct.write;

import de.javagl.jgltf.model.GltfConstants;
import de.javagl.jgltf.model.creation.AccessorModels;
import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.impl.DefaultSkinModel;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.v2.GltfModelWriterV2;
import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.api.ApiTexture;
import gg.generations.imct.api.Mesh;
import gg.generations.imct.api.Model;
import gg.generations.imct.read.scvi.ImageDisplayComponent;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeTextureGenerator;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.Collectors;

public class GlbWriter {

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

    public static <T extends Model> void write(Path input, BiFunction<Path, Path, T> function, Path path) {
        write(function.apply(input, path), input, path);
    }

    public static void write(Model model, Path input, Path path) {
        try {
            Files.createDirectories(path.getParent());
            var sceneModel = new DefaultSceneModel();

            model.materials.values().stream().flatMap(a -> a.entrySet().stream()).map(Map.Entry::getValue).filter(apiMaterial -> !(apiMaterial.name().contains("eyes") || apiMaterial.name().contains("fire"))).map(a -> a.getTexture("BaseColorMap")).map(ApiTexture::filePath).map(Path::of).forEach(s -> {
                copy(s, path);
            });

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
                min = Math.min(min,mesh.positions().stream().mapToDouble(a -> a.y).min().getAsDouble());

                var meshModel = new DefaultMeshModel();
                var meshPrimitiveModel = mesh.create().build();

//                meshPrimitiveModel.setMaterialModel(sceneMaterials.get(mesh.material()));
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
                meshModel.setName(mesh.name());

                // Create a node with the mesh
                var nodeModel = new DefaultNodeModel();
                nodeModel.setName(mesh.name());
                nodeModel.addMeshModel(meshModel);
                nodeModel.setSkinModel(skin);
                root.addChild(nodeModel);
            }

            var scale = 1f/(max - min);

            CompletableFuture.completedFuture(null).thenRun(checkShiny("eyes", model, path)).thenRun(checkShiny("fire", model, path)).join();



            Files.writeString(path.resolve("config.json"), generateJson(scale, model.materials, model.meshes));

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
            Files.walk(input).filter(a -> a.toString().endsWith("tranm")).forEach(a -> copy(a, path));

            var list = Files.walk(path).toList();

            PixelmonArchiveBuilder.convertToPk(path,
                    list,
                    path.getParent().resolve(path.getFileName().toString() + ".pk"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Runnable checkShiny(String name, Model model, Path path) {
        return () -> {
            var regular = model.materials.get("regular").get(name);
            var shiny = model.materials.get("rare").get(name);

            if (regular != null && shiny != null) {
                Path regularPath = path.resolve(Path.of(regular.getTexture("BaseColorMap").filePath()).getFileName());
                Path shinyPath = path.resolve(Path.of(shiny.getTexture("BaseColorMap").filePath()).getFileName());

                ImageDisplayComponent.Proxy.compare(EyeTextureGenerator.loadImage(regularPath), EyeTextureGenerator.loadImage(shinyPath)).thenAccept(aBoolean -> {
                    if (!aBoolean) {
                        model.materials.get("rare").remove(name);

                        try {
                            Files.delete(shinyPath);
                        } catch (IOException e) {
                        }

                    }
                }).join();
            }
        };
    }

    private static String generateJson(double scale, Map<String, Map<String, ApiMaterial>> materials, List<Mesh> meshes) {
        var materialsJson = new HashMap<ApiMaterial, String>();



        materials.forEach((type, materialMap) -> materialMap.forEach((s, apiMaterial) -> {
            materialsJson.put(apiMaterial, (type.equals("rare") ? "shiny_" : "") + s);
        }));

        var meshMap = new HashMap<String, String>();
        var shinyMap = new HashMap<String, String>();
        meshes.forEach(mesh -> {
            var name = materialsJson.get(mesh.material());
            meshMap.put(mesh.name(), name);
            if(materialsJson.containsValue("shiny_" + name)) shinyMap.put(mesh.name(), "shiny_" + name);
        });

        var builder = new StringBuilder();

        builder.append("{").append("\n");
        builder.append("  \"scale\": %s,".formatted(scale)).append("\n");
        builder.append("  \"materials\": {").append("\n");

        builder.append(String.join(",\n", materialsJson.entrySet().stream().map(a -> {
            String s = "    \"%s\": {\n".formatted(a.getValue());
            s += "      \"type\": \"solid\",\n";
            s += "      \"texture\": \"%s\"\n".formatted(Path.of(a.getKey().getTexture("BaseColorMap").filePath()).getFileName().toString());
            s += "    }";
            return s;
        }).collect(Collectors.toSet()))).append("\n");

        builder.append("  },").append("\n");
        builder.append("  \"defaultVariant\": {").append("\n");
        builder.append(String.join(",\n", meshMap.entrySet().stream().map(a -> {
            String s = "    \"%s\": {\n".formatted(a.getKey());
            s += "      \"material\": \"%s\",\n".formatted(a.getValue());
            s += "      \"hide\": \"false\"\n";
            s += "    }";
            return s;
        }).collect(Collectors.toSet()))).append("\n");
        builder.append("  },").append("\n");
        builder.append("  \"variants\": {").append("\n");
        builder.append("    \"normal\": {},").append("\n");
        builder.append("    \"shiny\": {").append("\n");
        builder.append(String.join("\n      },\n", shinyMap.entrySet().stream().map(a -> {
            String s = "      \"%s\": {\n".formatted(a.getKey());
            s += "        \"material\": \"%s\"".formatted(a.getValue());
            return s;
        }).collect(Collectors.toSet()))).append("\n");
        builder.append("      }").append("\n");
        builder.append("    }").append("\n");
        builder.append("  }").append("\n");
        builder.append("}");


        return builder.toString();

    }
}
