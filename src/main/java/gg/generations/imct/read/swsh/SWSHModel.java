package gg.generations.imct.read.swsh;

import de.javagl.jgltf.model.impl.DefaultNodeModel;
import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.api.ApiTexture;
import gg.generations.imct.api.Mesh;
import gg.generations.imct.api.Model;
import gg.generations.imct.read.letsgo.LGModel;
import gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.TextureMap;
import gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.Vector3;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeGraph;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeTextureGenerator;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.MirrorNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.*;
import gg.generations.imct.util.TrinityUtils;
import gg.generations.imct.write.GlbReader;
import org.joml.*;

import java.awt.*;
import java.io.IOException;
import java.lang.Math;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SWSHModel extends Model {
    public static final List<String> matStrings = Stream.of("BaseColorMap", "EmissionMaskTex", "LyBaseColorMap").toList();
    public static final Map<String, String> convertTextureMap = Map.of("BaseColorMap", "diffuse", "EmissionMaskTex", "emission");
    private final Map<Integer, String> materialIds = new HashMap<>();

    private static final TextureNode bottom = new TextureNode();
    private static final TextureNode top = new TextureNode();

    private static final TileNode tiling = new TileNode().setInput(bottom);

    private static final MirrorNode mirror = new MirrorNode().setInput(top);
    private static final InputNode layerEyes = layerEyes();

    private static final InputNode eyes = eyes();



    private static InputNode layerEyes() {
        var pupil = new MirrorNode().setMirrrLeft(true).setInput(new TileNode().setTiling(1, 4).setInput(bottom));

        var layer = new EyeGraph.LayersNode(pupil, top);

        var split = new SplitNode().setInput(layer);

        var left = new MirrorNode().setInput(split.getRight()).setMirrrLeft(true);
        var right = new MirrorNode().setInput(split.getLeft());

        return new UniteNode()
                .setLeft(left)
                .setRight(right);
    }

    private static InputNode eyes() {
        var split = new SplitNode().setInput(top);

        var left = new MirrorNode().setInput(split.getRight()).setMirrrLeft(true);
        var right = new MirrorNode().setInput(split.getLeft());

        return new UniteNode()
                .setLeft(left)
                .setRight(right);
    }

    static final class Bone {
        public String name;
        public Vector3f translation;
        public Quaternionf rotation;
        public Vector3f scale;
        public int parent;
        public boolean hasSkinning;
        public long type;
        public List<Bone> children;

        Bone(
                String name,
                Vector3f translation,
                Quaternionf rotation,
                Vector3f scale,
                int parent,
                boolean hasSkinning,
                long type,
                List<Bone> children
        ) {
            this.name = name;
            this.translation = translation;
            this.rotation = rotation;
            this.scale = scale;
            this.parent = parent;
            this.hasSkinning = hasSkinning;
            this.type = type;
            this.children = children;
        }
    }

    public SWSHModel(Path modelDir, Path targetDir) {
        var gfbmdl = gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.Model.getRootAsModel(read(modelDir.resolve(modelDir.getFileName() + ".gfbmdl")));
        if (gfbmdl.groupsLength() != gfbmdl.meshesLength())
            System.err.println("There may be an error Groups format != Mesh format");

        Map<String, String> shinyMap;

        try {
            shinyMap = Files.walk(modelDir).filter(a -> a.getFileName().toString().endsWith(".png")).filter(a -> a.toString().contains("rare")).collect(Collectors.toMap(a -> a.toString().replace("_rare", ""), Path::toString));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var uvMapsToGen = new HashSet<String>();

        var shinyMaterialMap = new HashMap<String, String>();

        genMaterials(gfbmdl, modelDir, targetDir, shinyMap, uvMapsToGen, shinyMaterialMap);

        genMeshes(gfbmdl, uvMapsToGen, targetDir, shinyMaterialMap);

        List<Bone> bones = new ArrayList<>();

        var joints = new ArrayList<Integer>();

        // First bone pass. Get all raw info into a normal format
        for (int i = 0; i < gfbmdl.bonesLength(); i++) {
            var bone = gfbmdl.bones(i);

            var rawRotation = toVec3(bone.rotation());
            var b = new Bone(
                    bone.name(),
                    toVec3(bone.translation()),
                    new Quaternionf().rotateLocalX(rawRotation.x).rotateLocalY(rawRotation.y).rotateLocalZ(rawRotation.z),
                    toVec3(bone.scale()),
                    bone.parent(),
                    bone.rigidCheck() == null,
                    bone.boneType(),
                    new ArrayList<>()
            );

            bones.add(b);

            var local = -1;

            if(b.hasSkinning) {
                joints.add(i);
            }

        }

        // Second bone pass. Add children
        for (var value : bones) {
            if (value.parent != -1) {

                var parent = bones.get(value.parent);
                if (parent != null) parent.children.add(value);
            }
        }

        // Second bone pass. Convert into skeleton
        this.skeleton = new ArrayList<>();

        for (Bone bone : bones) {
            var node = new DefaultNodeModel();
            node.setName(bone.name);

            var t = bone.translation;
            var r = bone.rotation;
            var s = bone.scale;

            if (!(t.x == 0 && t.y == 0 && t.z == 0)) node.setTranslation(new float[]{t.x, t.y, t.z});
            if (!(r.x == 0 && r.y == 0 && r.z == 0 && r.w == 1)) node.setRotation(new float[]{r.x, r.y, r.z, r.w});
            if (!(s.x == 1 && s.y == 1 && s.z == 1)) node.setScale(new float[]{s.x, s.y, s.z});

            skeleton.add(node);
        }

        for (int i = 0; i < skeleton.size(); i++) {
            var node = skeleton.get(i);
            var bone = bones.get(i);
            if (bone.parent == -1) continue;
            var parent = skeleton.get(bone.parent);
            parent.addChild(node);
        }

        this.joints = joints.stream().map(skeleton::get).toList();

//        var root = skeleton.stream().filter(a -> a.getName().equals("Origin")).findFirst();
//
//        if(root.isEmpty()) {
//            throw new RuntimeException("Origin not found!");
//        } else {
//            this.root = root.get();
//        }

//        System.out.println();
    }

//    IntStream.range(0, material.colorsLength()).mapToObj(a -> material.colors(a)).collect(Collectors.toMap(a -> a.name(), a -> new Vector3f(a.color().r(), a.color().g(), a.color().b())))

    private void genMaterials(gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.Model gfbmdl, Path modelDir, Path targetDir, Map<String, String> shinyMap, Set<String> uvMapsToGen, Map<String, String> shinyMaterials) {

        var materials = new HashMap<String, ApiMaterial>();
        var groupsToExclude = new HashSet<String>();

        for (int i = 0; i < gfbmdl.materialsLength(); i++) {
            var material = gfbmdl.materials(i);
            var properties = new HashMap<String, Object>();
            var textures = new ArrayList<ApiTexture>();
            var materialName = material.name();
            var shader = Objects.requireNonNull(material.shaderGroup(), "Null shader name");
            properties.put("shader", "solid");
//            properties.put("type", "solid");

            for (int j = 0; j < material.common().valuesLength(); j++) {
                var property = material.common().values(j);
                properties.put(property.name(), property.value());
            }

            for (int j = 0; j < material.valuesLength(); j++) {
                var property = material.values(j);
                properties.put(property.name(), property.value());
            }

            for (int j = 0; j < material.common().colorsLength(); j++) {
                var property = material.common().colors(j);
                properties.put(property.name(), createColorFromLinearRGB(new Vector3f(property.color().r(), property.color().g(), property.color().b())));
            }

            for (int j = 0; j < material.colorsLength(); j++) {
                var property = material.colors(j);
                properties.put(property.name(),  createColorFromLinearRGB(new Vector3f(property.color().r(), property.color().g(), property.color().b())));
            }

            for (int j = 0; j < material.textureMapsLength(); j++) {
                var rawTexture = material.textureMaps(j);
                var texName = gfbmdl.textureNames(rawTexture.index());
                textures.add(new ApiTexture(processTextureName(rawTexture), modelDir.resolve(texName + ".png").toAbsolutePath().toString()));
            }

            if(textures.isEmpty()) {
//                if(materialName.toLowerCase().contains("mask")) {
//                    groupsToExclude.add(materialName);
//                    continue;
//                }

                uvMapsToGen.add(materialName);;

                properties.put("type", "unlit");

//                    materials.computeIfAbsent("regular", mat -> new HashMap<>()).computeIfAbsent(materialName, key -> new ApiMaterial(
//                            key,
//                            List.of(new ApiTexture("BaseColorMap", materialName + ".png")),
//                            properties
//                    ));

                materialIds.put(i, materialName);

                continue;
            }

            materialIds.put(i, materialName);
            var normal = materials.computeIfAbsent(materialName, key -> new ApiMaterial(
                    key,
                    textures.stream().filter(a -> matStrings.contains(a.type())).toList(),
                    new HashMap<>()
            ));

            var shiny = materials.computeIfAbsent(materialName + "_shiny", key -> new ApiMaterial(
                    key,
                    textures.stream().map(a -> new ApiTexture(a.type(), shinyMap.getOrDefault(a.filePath(), a.filePath()))).toList(),
                    new HashMap<>()
            ));

            if(normal.textures().stream().anyMatch(a -> shiny.textures().contains(a))) {
                shinyMaterials.put(normal.name(), shiny.name());
            }
        }

        materials.forEach((s, map) -> {

            var pair = new GlbReader.Pair<>(map.getTexture("BaseColorMap"), map.getTexture("LyBaseColorMap"));

            Path base = null;

            try {
                base = Path.of(pair.left().filePath());
            } catch (Exception e) {
                try {
                    base = Path.of(pair.right().filePath());
                } catch (Exception e1) {
                }
            }

            if(base != null) {

                if (pair.right() != null && pair.right().filePath().contains("Iris")) {
                    var ly = Path.of(pair.right().filePath());
                    top.setImage(base);
                    bottom.setImage(ly);
                    EyeTextureGenerator.generate(layerEyes.getInputData().get(), targetDir.resolve(base.getFileName()));
                } else if (pair.left().filePath().contains("Eye")) {
                    top.setImage(base);
                    EyeTextureGenerator.generate(eyes.getInputData().get(), targetDir.resolve(base.getFileName()));
                } else {
                    top.setImage(base);
                    EyeTextureGenerator.generate((pair.left().filePath().contains("Mouth") ? top : mirror).get(), targetDir.resolve(base.getFileName()));
                }
            }

        });

        materials.forEach((s, material) -> {
            var props = new HashMap<String, Object>();
            props.put("shader", material.properties().get("shader"));
            var images = material.textures().stream().filter(a -> convertTextureMap.containsKey(a.type())).map(a -> new ApiTexture(convertTextureMap.get(a.type()), a.filePath())).toList();

            if(images.isEmpty()) {
                images = List.of(new ApiTexture("diffuse", targetDir.resolve(s + "_uv.png").toString()));
            }

            SWSHModel.this.materials.put(s, new ApiMaterial(material.name(), images, props));
        });
    }

    private static Color createColorFromLinearRGB(Vector3f color) {
        return createColorFromLinearRGB(color.x, color.y, color.z);
    }

    private static Color createColorFromLinearRGB(float linearRed, float linearGreen, float linearBlue) {
        // Ensure values are in the valid range [0.0, 1.0]
        linearRed = Math.min(1.0f, Math.max(0.0f, linearRed));
        linearGreen = Math.min(1.0f, Math.max(0.0f, linearGreen));
        linearBlue = Math.min(1.0f, Math.max(0.0f, linearBlue));

        // Convert linear RGB to gamma-corrected RGB
        float gammaRed = gammaCorrect(linearRed);
        float gammaGreen = gammaCorrect(linearGreen);
        float gammaBlue = gammaCorrect(linearBlue);

        // Create and return a Color object
        return new Color(gammaRed, gammaGreen, gammaBlue);
    }

    // Gamma correction function
    private static float gammaCorrect(float value) {
        if (value <= 0.04045f) {
            return 12.92f * value;
        } else {
            return (float) (1.055 * Math.pow(value, 1.0 / 2.4) - 0.055);
        }
    }

    private void genMeshes(gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.Model gfbmdl, Set<String> uvMapsToGen, Path targetDir, HashMap<String, String> shinyMaterialMap) {
        var meshProxies = new HashMap<String, Map<String, MeshProxy>>();

        for (int i = 0; i < gfbmdl.groupsLength(); i++) {
            var group = gfbmdl.groups(i);
            var name = gfbmdl.bones((int) group.boneIndex()).name();
            var meshGroup = gfbmdl.meshes((int) group.meshIndex());
            var vertexBuffer = meshGroup.dataAsByteBuffer();

            var attributes = new ArrayList<Attribute>();
            for (var j = 0; j < meshGroup.attributesLength(); j++)
                attributes.add(new Attribute(AttributeType.get(meshGroup.attributes(j).vertexType()), AttributeFormat.get(meshGroup.attributes(j).bufferFormat())));

            var vertexStride = getTotalBufferStride(attributes);
            var vertexCount = meshGroup.dataLength() / vertexStride;
            var positions = new ArrayList<Vector3f>();
            var normals = new ArrayList<Vector3f>();
            var tangents = new ArrayList<Vector4f>();
            var colors = new ArrayList<Vector4f>();
            var weights = new ArrayList<Vector4f>();
            var boneIds = new ArrayList<Vector4i>();
            var biNormals = new ArrayList<Vector3f>();
            var uvs = new ArrayList<Vector2f>();

            for (int j = 0; j < vertexCount; j++) {
                for (var attribute : attributes) {
                    switch (attribute.type()) {
                        case POSITION -> {
                            if (Objects.requireNonNull(attribute.format) == AttributeFormat.FLOAT) {
                                var x = vertexBuffer.getFloat();
                                var y = vertexBuffer.getFloat();
                                var z = vertexBuffer.getFloat();
                                positions.add(new Vector3f(x, y, z));
                            } else throw new RuntimeException("Unexpected position format: " + attribute.format);
                        }
                        case NORMAL -> {
                            if (Objects.requireNonNull(attribute.format) == AttributeFormat.HALF_FLOAT)
                                normals.add(TrinityUtils.readRGBA16Float3(vertexBuffer));
                            else throw new RuntimeException("Unexpected normal format: " + attribute.format);
                        }
                        case TANGENTS -> {
                            if (Objects.requireNonNull(attribute.format) == AttributeFormat.HALF_FLOAT)
                                tangents.add(TrinityUtils.readRGBA16Float4(vertexBuffer));
                            else throw new RuntimeException("Unexpected tangent format: " + attribute.format);
                        }
                        case UV1 -> {
                            if (Objects.requireNonNull(attribute.format) == AttributeFormat.FLOAT) {
                                var x = vertexBuffer.getFloat();
                                var y = 1.0f - vertexBuffer.getFloat();
                                uvs.add(new Vector2f(x, y));
                            } else throw new RuntimeException("Unexpected uv format: " + attribute.format);
                        }
                        case UV2, UV3, UV4 -> {
                            if (Objects.requireNonNull(attribute.format) == AttributeFormat.FLOAT) {
                                var x = vertexBuffer.getFloat();
                                var y = 1.0f - vertexBuffer.getFloat();
                            } else throw new RuntimeException("Unexpected uv(2, 3, or 4) format: " + attribute.format);
                        }
                        case COLOR_1, COLOR_2, COLOR_3, COLOR_4 -> {
                            if (Objects.requireNonNull(attribute.format) == AttributeFormat.BYTE) {
                                var w = vertexBuffer.get() & 0xFF;
                                var x = vertexBuffer.get() & 0xFF;
                                var y = vertexBuffer.get() & 0xFF;
                                var z = vertexBuffer.get() & 0xFF;
                                colors.add(new Vector4f(x, y, z, w));
                            } else throw new RuntimeException("Unexpected color format: " + attribute.format);
                        }
                        case BLEND_INDICES -> {
                            if (Objects.requireNonNull(attribute.format) == AttributeFormat.BYTE) {
                                var x = vertexBuffer.get() & 0xFF;
                                var y = vertexBuffer.get() & 0xFF;
                                var z = vertexBuffer.get() & 0xFF;
                                var w = vertexBuffer.get() & 0xFF;
                                boneIds.add(new Vector4i(x, y, z, w));

                            } else throw new RuntimeException("Unexpected bone idx format: " + attribute.format);
                        }
                        case BLEND_WEIGHTS -> {
                            switch (attribute.format) {
                                case HALF_FLOAT -> {
                                    var weight = TrinityUtils.readWeights(vertexBuffer);
                                    weights.add(weight);
                                }
                                case BYTES_AS_FLOAT -> {
                                    var x = (vertexBuffer.get() & 0xFF) / 255.0f;
                                    var y = (vertexBuffer.get() & 0xFF) / 255.0f;
                                    var z = (vertexBuffer.get() & 0xFF) / 255.0f;
                                    var w = (vertexBuffer.get() & 0xFF) / 255.0f;
                                    weights.add(new Vector4f(x, y, z, w));
                                }
                                default ->
                                        throw new RuntimeException("Unexpected bone weight format: " + attribute.format);
                            }
                        }
                        case BITANGENT -> {
                            throw new RuntimeException("Didnt expect bitangents " + attribute.format);
                        }
                        case UNKNOWN_2 -> {
                            throw new RuntimeException("Didnt expect unknown 2 " + attribute.format);
                        }
                    }
                }
            }



            for (int j = 0; j < meshGroup.polygonsLength(); j++) {
                var mesh = meshGroup.polygons(j);
                var indices = new ArrayList<Integer>();
                for (var idx = 0; idx < mesh.facesLength(); idx++) indices.add(mesh.faces(idx));
                var materialId = idToName(mesh.materialIndex());

                var meshId = name+ "_" + mesh.materialIndex();

                if(uvMapsToGen.contains(materialId)) {
                    EyeTextureGenerator.generate(LGModel.generator.generateUvMap(uvs, indices), targetDir.resolve(materialId + "_uv.png"));
                    uvMapsToGen.remove(materialId);
                }

                variants.computeIfAbsent("regular", it -> new HashMap<>()).computeIfAbsent(meshId, it -> materialId);
                variants.computeIfAbsent("shiny", it -> new HashMap<>()).computeIfAbsent(meshId, it -> shinyMaterialMap.getOrDefault(materialId, materialId));

                meshes.add(new Mesh(meshId, materialId, indices, positions, normals, tangents, colors, weights, boneIds, biNormals, uvs));
            }

//            System.out.println(name + " " + Arrays.toString(boneIds.stream().flatMapToInt(a -> IntStream.of(a.x, a.y, a.z, a.w)).distinct().sorted().toArray()));
        }
    }

    public record MeshProxy(String name, String materialId, List<Integer> indices, List<Vector3f> positions, List<Vector3f> normals, List<Vector4f> tangents, List<Vector4f> colors, List<Vector4f> weights, List<Vector4i> boneIds, List<Vector3f> biNormals, List<Vector2f> uvs) {};

    private String idToName(long idx) {
        return materialIds.get((int) idx);
    }

    private String processTextureName(TextureMap rawTexture) {
        return rawTexture.sampler().replace("Col0Tex", "BaseColorMap").replace("LyCol0Tex", "LyBaseColorMap");
    }

    private int getTotalBufferStride(List<Attribute> attributes) {
        var vertexStride = 0;

        for (var attribute : attributes) {
            switch (attribute.type) {
                case POSITION, NORMAL, BITANGENT -> {
                    if (attribute.format == AttributeFormat.HALF_FLOAT) vertexStride += 0x08;
                    else if (attribute.format == AttributeFormat.FLOAT) vertexStride += 0x0C;
                    else throw new RuntimeException("Unknown Combination!");
                }
                case UV1, UV2, UV3, UV4 -> {
                    if (attribute.format == AttributeFormat.HALF_FLOAT) vertexStride += 0x04;
                    else if (attribute.format == AttributeFormat.FLOAT) vertexStride += 0x08;
                    else throw new RuntimeException("Unknown Combination!");
                }
                case COLOR_1, COLOR_2 -> {
                    if (attribute.format == AttributeFormat.BYTE) vertexStride += 0x04;
                    else throw new RuntimeException("Unknown Combination!");
                }
                case BLEND_INDICES -> {
                    if (attribute.format == AttributeFormat.SHORT) vertexStride += 0x08;
                    else if (attribute.format == AttributeFormat.BYTE) vertexStride += 0x04;
                    else throw new RuntimeException("Unknown Combination!");
                }
                case BLEND_WEIGHTS -> {
                    if (attribute.format == AttributeFormat.BYTES_AS_FLOAT) vertexStride += 0x04;
                    else throw new RuntimeException("Unknown Combination!");
                }
                case TANGENTS -> {
                    if (attribute.format == AttributeFormat.HALF_FLOAT) vertexStride += 0x08;
                    else throw new RuntimeException("Unknown Combination!");
                }
                default -> throw new RuntimeException("Unknown Combination!");
            }
        }

        return vertexStride;
    }

    protected Vector3f toVec3(Vector3 vec) {
        return new Vector3f(vec.x(), vec.y(), vec.z());
    }

    private record Attribute(
            AttributeType type,
            AttributeFormat format
    ) {
    }

    private enum AttributeType {
        POSITION,
        NORMAL,
        TANGENTS,
        UV1, UV2, UV3, UV4,
        COLOR_1, COLOR_2, COLOR_3, COLOR_4,
        BLEND_INDICES,
        BLEND_WEIGHTS,
        BITANGENT,
        UNKNOWN_2;

        public static AttributeType get(long id) {
            for (var i = 0; i < values().length; i++) if (i == id) return values()[i];
            throw new RuntimeException("Unknown Attribute Type " + id);
        }
    }

    private enum AttributeFormat {
        FLOAT(0),
        HALF_FLOAT(1),
        BYTE(3),
        SHORT(5),
        BYTES_AS_FLOAT(8);

        private final int id;

        AttributeFormat(int id) {
            this.id = id;
        }

        public static AttributeFormat get(long id) {
            for (var value : values()) if (value.id == id) return value;
            throw new RuntimeException("Unknown Attribute Size " + id);
        }
    }
}