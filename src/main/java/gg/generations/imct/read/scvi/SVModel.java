package gg.generations.imct.read.scvi;

import com.google.gson.GsonBuilder;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import gg.generations.imct.IMCT;
import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.api.ApiTexture;
import gg.generations.imct.api.Mesh;
import gg.generations.imct.api.Model;
import gg.generations.imct.read.scvi.flatbuffers.Titan.Model.*;
import gg.generations.imct.read.scvi.flatbuffers.Titan.Model.Animaton.*;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeGraph;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeTextureGenerator;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.FIreGraph;
import gg.generations.imct.util.TrinityUtils;
import gg.generations.imct.write.GlbReader;
import org.joml.*;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.lang.Math;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SVModel extends Model {

    record InitTrack() {}

    record AnimTrack() {}

    record MaterialTrack(Map<String, ArrayList<String>> animTracks) {}

    record Track(Map<String, MaterialTrack> animation) {

    }


    public SVModel(Path modelDir, Path targetDir) throws IOException {
        // Read Data
        var meshInfo = new ArrayList<TRMSH>();
        var meshData = new ArrayList<TRMBF>();
        var trmdl = TRMDL.getRootAsTRMDL(read(modelDir.resolve(modelDir.getFileName() + ".trmdl")));
        var trskl = TRSKL.getRootAsTRSKL(read(modelDir.resolve(modelDir.getFileName() + ".trskl")));

        for (var i = 0; i < trmdl.meshesLength(); i++) {
            var meshName = Objects.requireNonNull(trmdl.meshes(i).filename(), "Mesh name was null");
            var meshI = TRMSH.getRootAsTRMSH(read(modelDir.resolve(meshName)));
            var meshD = TRMBF.getRootAsTRMBF(read(modelDir.resolve(meshName.replace(".trmsh", ".trmbf"))));
            meshInfo.add(meshI);
            meshData.add(meshD);
        }

        record Bone(
                String name,
                Vector3f translation,
                Quaternionf rotation,
                Vector3f scale,
                int parent,
                int rigIdx,
                long type,
                List<Bone> children
        ) {
        }

        List<Bone> bones = new ArrayList<>();

        // First bone pass. Get all raw info into a normal format
        for (int i = 0; i < trskl.transformNodesLength(); i++) {
            var bone = trskl.transformNodes(i);

            var rawRotation = toVec3(bone.transform().vecRot());
            bones.add(new Bone(
                    bone.name(),
                    toVec3(bone.transform().vecTranslate()),
                    new Quaternionf().rotateLocalX(rawRotation.x).rotateLocalY(rawRotation.y).rotateLocalZ(rawRotation.z),
                    toVec3(bone.transform().vecScale()),
                    bone.parentIdx(),
                    bone.rigIdx(),
                    bone.type(),
                    new ArrayList<>()
            ));
        }

        // Second bone pass. Add children
        for (var value : bones) {
            if (value.parent() != -1) {
                var parent = bones.get(value.parent());
                if (parent != null) parent.children.add(value);
            }
        }



        this.joints = new ArrayList<>(bones.stream().mapToInt(a -> a.rigIdx).max().getAsInt());

        // Second bone pass. Convert into skeleton
        this.skeleton = new ArrayList<>();

        for(var bone : bones) {
            var node = new DefaultNodeModel();
            node.setName(bone.name);

            var t = bone.translation;
            var r = bone.rotation;
            var s = bone.scale;

            if(!(t.x == 0 && t.y == 0 && t.z == 0)) {
                node.setTranslation(new float[] { t.x, t.y, t.z});
            }

            if(!(r.x == 0 && r.y == 0 && r.z == 0 && r.w == 1)) {
                node.setRotation(new float[] { r.x, r.y, r.z, r.w });
            }

            if(!(s.x == 1 && s.y == 1 && s.z == 1)) {
                node.setScale(new float[] { s.x, s.y, s.z});
            }

            if(bone.rigIdx != -1) {
                joints.add(bone.rigIdx, node);
            }

            skeleton.add(node);
        }

        for (int i = 0; i < skeleton.size(); i++) {
            var node = skeleton.get(i);
            var bone = bones.get(i);
            if (bone.parent == -1) continue;
            var parent = skeleton.get(bone.parent);
            parent.addChild(node);
        }

        if(joints.stream().noneMatch(a -> a.getName().equals("Origin"))) {
            var current = joints.get(0).getParent();

            while (current != null) {
                joints.add((DefaultNodeModel) current);

                if (current.getName().equals("Origin")) {
                    current = null;
                } else {
                    current = current.getParent();
                }
            }
        }

        var materialRemap = new HashMap<String, String>();

        // Process material data
        var materials = TRMTR.getRootAsTRMTR(read(modelDir.resolve(Objects.requireNonNull(trmdl.materials(0), "Material name was null"))));
        processMaterials("regular", modelDir, targetDir, materials, materialRemap);

        // Process extra material variants (shiny)

        var extraMaterials = TRMMT.getRootAsTRMMT(read(modelDir.resolve(modelDir.getFileName() + ".trmmt")));

        var matts = new HashMap<String, Map<String, Track> >();

        for (int i = 0; i < extraMaterials.materialLength(); i++) {
            var mtt = extraMaterials.material(i);

            Map<String, Track> tracks = new HashMap<>();

            IntStream.range(0, mtt.materialPropertiesLength())
                    .mapToObj(mtt::materialProperties)
                    .map(a -> TRACM.getRootAsTRACM(a.tracm().bytebufferAsByteBuffer()))
                    .flatMap(a -> IntStream.range(0, a.tracksLength())
                            .mapToObj(a::tracks)).forEach(a -> {
                                var materialTracks = new HashMap<String, MaterialTrack>();

                                IntStream.range(0, a.materialAnimation().materialTrackLength()).mapToObj(j -> a.materialAnimation().materialTrack(j)).forEach(trackMaterial -> {
                                    var animTracks = IntStream.range(0, trackMaterial.animValuesLength()).mapToObj(trackMaterial::animValues).collect(Collectors.toMap(a1 -> a1.name(), a1 -> extractedColors(a1.list())));

                                    materialTracks.put(trackMaterial.name(), new MaterialTrack(animTracks));
                                });
                                tracks.put(a.trackPath(), new Track(materialTracks));

                    });

            matts.put(mtt.name(), tracks);

//            processMaterials(mtt.name(), modelDir, targetDir, TRMTR.getRootAsTRMTR(read(modelDir.resolve(Objects.requireNonNull(mtt.materialName(0), "Material name was null")))), null);
        }

        Map<String, Map<Integer, Map<String, Map<String, String>>>> MAP = new HashMap<>();



        matts.forEach((type, meshMap) -> {
            var typeMap = MAP.computeIfAbsent(type, a -> new HashMap<>());
            meshMap.values().stream().map(Track::animation).forEach(track -> {
                track.forEach((materialName, materialTrack) -> {
                    materialTrack.animTracks().forEach((uniform, values) -> {
                        for (int i = 0; i < values.size(); i++) {
                            var value = values.get(i);
                            typeMap.computeIfAbsent(i, a -> new HashMap<>()).computeIfAbsent(materialName, a -> new HashMap<>()).computeIfAbsent(uniform, a -> value);
                        }
                    });
                });
            });
        });

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(new GsonBuilder().setPrettyPrinting().create().toJson(MAP)), null);

        // Process mesh data
        for (var i = 0; i < meshInfo.size(); i++) {
//            System.out.println("Processing Mesh Info " + i);
            for (int mesh = 0; mesh < meshInfo.get(i).meshesLength(); mesh++) {
//                System.out.println("Processing Mesh " + i);
                var info = meshInfo.get(i).meshes(mesh);
                var data = meshData.get(i).buffers(mesh);
                var vertexBuffer = data.vertexBuffer(0).bufferAsByteBuffer();
                var idxBuffer = data.indexBuffer(0);

                var idxLayout = IndexLayout.get((int) info.polygonType());
                var rawAttributes = info.attributes(0);


                var attributes = new ArrayList<Attribute>();
                for (var j = 0; j < rawAttributes.attrsLength(); j++) {
                    attributes.add(new Attribute(
                            AttributeType.get(rawAttributes.attrs(j).attribute()),
                            AttributeSize.get(rawAttributes.attrs(j).type())
                    ));
                }

                var indices = new ArrayList<Integer>();
                var positions = new ArrayList<Vector3f>();
                var normals = new ArrayList<Vector3f>();
                var tangents = new ArrayList<Vector4f>();
                var colors = new ArrayList<Vector4f>();
                var weights = new ArrayList<Vector4f>();
                var boneIds = new ArrayList<Vector4i>();
                var binormals = new ArrayList<Vector3f>();
                var uvs = new ArrayList<Vector2f>();

                var realIdxBuffer = idxBuffer.bufferAsByteBuffer();
                for (var j = 0; j < idxBuffer.bufferLength() / idxLayout.size; j++) {
                    switch (idxLayout) {
                        case UINT16 -> indices.add(realIdxBuffer.getShort() & 0xFFFF);
                        case UINT32 -> indices.add(realIdxBuffer.getInt() & 0xFFFFFFFF);
                        default -> throw new RuntimeException("no");
                    }
                }

                var vertexCount = data.vertexBuffer(0).bufferLength() / info.attributes(0).size(0).size();

                for (var j = 0; j < vertexCount; j++) {
                    for (var attribute : attributes) {
                        switch (attribute.type) {
                            case POSITION -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RGB_32_FLOAT) {
                                    var x = vertexBuffer.getFloat();
                                    var y = vertexBuffer.getFloat();
                                    var z = vertexBuffer.getFloat();
                                    positions.add(new Vector3f(x, y, z));
                                } else throw new RuntimeException("Unexpected position format: " + attribute.type);
                            }

                            case COLOR -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_8_UNORM) {
                                    var x = vertexBuffer.get() & 0xFF;
                                    var y = vertexBuffer.get() & 0xFF;
                                    var z = vertexBuffer.get() & 0xFF;
                                    var w = vertexBuffer.get() & 0xFF;
                                    colors.add(new Vector4f(x, y, z, w));
                                } else if(Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_32_FLOAT){
                                    colors.add(new Vector4f(vertexBuffer.getFloat(), vertexBuffer.getFloat(), vertexBuffer.getFloat(), vertexBuffer.getFloat()));
                                } else {
                                    throw new RuntimeException("Unexpected color format: " + attribute.type + " " + attribute.size);
                                }
                            }

                            case NORMAL -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_16_FLOAT) {
                                    normals.add(TrinityUtils.readRGBA16Float3(vertexBuffer));
                                } else throw new RuntimeException("Unexpected normal format: " + attribute.type);
                            }

                            case TANGENT -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_16_FLOAT) {
                                    tangents.add(TrinityUtils.readRGBA16Float4(vertexBuffer));
                                } else throw new RuntimeException("Unexpected tangent format: " + attribute.type);
                            }

                            case TEXCOORD -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RG_32_FLOAT) {
                                    var x = vertexBuffer.getFloat();
                                    var y = 1.0f - vertexBuffer.getFloat();
                                    uvs.add(new Vector2f(x, y));
                                } else throw new RuntimeException("Unexpected uv format: " + attribute.type);
                            }

                            case BLEND_INDICES -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_8_UNSIGNED) {
                                    var w = vertexBuffer.get() & 0xFF;
                                    var x = vertexBuffer.get() & 0xFF;
                                    var y = vertexBuffer.get() & 0xFF;
                                    var z = vertexBuffer.get() & 0xFF;
                                    boneIds.add(new Vector4i(x, y, z, w));
                                } else throw new RuntimeException("Unexpected bone idx format: " + attribute.type);
                            }

                            case BLEND_WEIGHTS -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_16_UNORM) {
                                    var weight = TrinityUtils.readWeights(vertexBuffer);
                                    weights.add(weight);
                                } else throw new RuntimeException("Unexpected bone weight format: " + attribute.type);
                            }

                            default -> throw new IllegalStateException("Unexpected value: " + attribute);
                        }
                    }
                }

                // Sub meshes
                for (int j = 0; j < info.materialsLength(); j++) {
                    var subMesh = info.materials(j);
                    var subIdxBuffer = indices.subList((int) subMesh.polyOffset(), (int) (subMesh.polyOffset() + subMesh.polyCount()));
                    if (!Objects.requireNonNull(info.meshName()).contains("lod")) {
                        var name = materialRemap.getOrDefault(subMesh.materialName(), subMesh.materialName());
                        meshes.add(new Mesh(info.meshName() + "_" + subMesh.materialName(), this.materials.get("regular").get(name), subIdxBuffer, positions, normals, tangents, colors, weights, boneIds, binormals, uvs));
                    }
                }
            }

        }
    }

    private void processMaterials(String name, Path modelDir, Path targetDir, TRMTR materials, Map<String, String> materialRemap) throws IOException {
        var list = this.materials.computeIfAbsent(name, a -> new HashMap<>());

        for (int i = 0; i < materials.materialsLength(); i++) {
            var properties = new HashMap<String, Object>();
            var material = materials.materials(i);
            var textures = new ArrayList<ApiTexture>();
            var materialName = material.name();
            var shader = Objects.requireNonNull(material.shaders(0).shaderName(), "Null shader name");
            properties.put("shader", shader);

            for (int j = 0; j < material.intParameterLength(); j++) {
                var property = material.intParameter(j);
                properties.put(property.intName(), property.intValue());
            }

            for (int j = 0; j < material.floatParameterLength(); j++) {
                var property = material.floatParameter(j);
                properties.put(property.floatName(), property.floatValue());
            }

            for (int j = 0; j < material.float4ParameterLength(); j++) {
                var property = material.float4Parameter(j);
                properties.put(property.colorName(), new Vector4f(property.colorValue().r(), property.colorValue().g(), property.colorValue().b(), property.colorValue().a()));
            }

            for (int j = 0; j < material.float4LightParameterLength(); j++) {
                var property = material.float4LightParameter(j);
                properties.put(property.colorName(), new Vector4f(property.colorValue().r(), property.colorValue().g(), property.colorValue().b(), property.colorValue().a()));
            }

            for (int j = 0; j < material.texturesLength(); j++) {
                var rawTexture = material.textures(j);
                textures.add(new ApiTexture(rawTexture.textureName(), modelDir.resolve(rawTexture.textureFile().replace(".bntx", ".png")).toAbsolutePath().toString()));
            }

            ApiMaterial mat = new ApiMaterial(
                            materialName,
                            textures,
                            properties
                    );

            var name1 = name.equals("rare") ? "shiny_" : "";

            var properties1 = new HashMap<String, Object>();
            properties1.put("type", "layered");

            IntStream.of(1, 2,3, 4).boxed().flatMap(a -> Stream.of("BaseColorLayer" + a, "EmissionIntensityLayer" + a, "EmissionColorLayer" + a)).forEach(s -> {
                var obj = properties.get(s);

                properties1.put(s, obj instanceof Vector4f v ? color(v.x, v.y, v.z) : obj);
            });

            ApiMaterial finalMat = mat;
            var mat1 = new ApiMaterial(name, Stream.<String>of("BaseColorMap", "LayerMaskMap").map(finalMat::getTexture).collect(Collectors.toList()), properties1);


//            Path path;

//            switch (shader) {
//                case "Eye":
//                case "EyeClearCoat":
//                    path = targetDir.resolve(name1 + materialName +  ".png").toAbsolutePath();
//
////                    if(IMCT.messWithTexture) EyeTextureGenerator.generate(SV_EYE.update(mat, modelDir), path);
//
//                    mat = new ApiMaterial(materialName, List.of(new ApiTexture("BaseColorMap", path.toString())), Map.of("type", "solid"));
//                    list.putIfAbsent(materialName, mat);
//
//
///*
//                    if(!list.containsKey("eyes")) {
//                        path = targetDir.resolve(name1 + "eyes.png").toAbsolutePath();
//
//                        EyeTextureGenerator.generate(SV_EYE.update(mat, modelDir), path);
//
//                        list.computeIfAbsent("eyes", key -> new ApiMaterial("eyes", List.of(new ApiTexture("BaseColorMap", path.toString())), Map.of("type", "solid")));
//                    }
//
//                    if (materialRemap != null) materialRemap.put(materialName, "eyes");
//*/
//
//                    continue;
//                case "Unlit":
//                    path = targetDir.resolve(name1 + materialName +  ".png").toAbsolutePath();
//
//                    if(IMCT.messWithTexture) EyeTextureGenerator.generate(SV_BODY.update(mat, modelDir), path);
//
//                    mat = new ApiMaterial(materialName, List.of(new ApiTexture("BaseColorMap", path.toString())), Map.of("type", "unlit"));
//                    list.putIfAbsent(materialName, mat);
//
//                    continue;
//                case "Transparent":
//
//                    path = targetDir.resolve(name1 + materialName +  ".png").toAbsolutePath();
//
//                    if(IMCT.messWithTexture) EyeTextureGenerator.generate(SV_BODY.update(mat, modelDir), path);
//
//                    mat = new ApiMaterial(materialName, List.of(new ApiTexture("BaseColorMap", path.toString())), Map.of("type", "transparent"));
//                    list.putIfAbsent(materialName, mat);
//
//                    continue;
//                case "SSSEffect":
//                case "Standard":
//                case "FresnelEffect":
//                case "SSS":
//                case "NonDirectional":
//                    path = targetDir.resolve(name1 + materialName +  ".png").toAbsolutePath();
//
//                    if(IMCT.messWithTexture) EyeTextureGenerator.generate(SV_BODY.update(mat, modelDir), path);
//
//                    mat = new ApiMaterial(materialName, List.of(new ApiTexture("BaseColorMap", path.toString())), Map.of("type", "layer"));
//                        list.putIfAbsent(materialName, mat);
//
////                    if (materialRemap != null) materialRemap.put(materialName, "eyes");
//
//
//                    continue;
//                case "InsideEmissionParallax":
//                    break;
//                case "FresnelBlend":
//                    break;
//            }

            list.put(materialName, mat1);

            var path = Path.of(mat1.getTexture("BaseColorMap").filePath());
            EyeTextureGenerator.copy(path, targetDir.resolve(path.getFileName()));
            path = Path.of(mat1.getTexture("LayerMaskMap").filePath());
            EyeTextureGenerator.copy(path, targetDir.resolve(path.getFileName()));

            IMCT.TOTAL_SHADERS.add(shader);
        }
    }

    public static EyeGraph SV_EYE = new EyeGraph(256);

    public static FIreGraph SV_BODY = new FIreGraph(1080);
    private static FIreGraph SV_FIRE = new FIreGraph(256);

    protected void processEyes(String k, Map<String, ApiMaterial> materials, Path modelDir, Path targetDir) {
        var left_eye = materials.remove("l_eye");
        if(left_eye == null) left_eye = materials.remove("left_eye");
        if(left_eye == null) left_eye = materials.remove("eye_l");

        var right_eye = materials.remove("r_eye");
        if(right_eye == null) right_eye = materials.remove("right_eye");
        if(right_eye == null) right_eye = materials.remove("eye_r");

        if(left_eye == null) return;

        var name = k.equals("rare") ? "shiny_" : "";

        var eyes = new ApiMaterial("eyes", List.of(new ApiTexture("BaseColorMap", modelDir.resolve(name + "eyes.png").toAbsolutePath().toString())), new HashMap<>());
        var image = SV_EYE.update(left_eye, modelDir);
        EyeTextureGenerator.generate(image, targetDir.resolve(name + "eyes.png").toAbsolutePath());
        materials.put("eyes", eyes);

        List<Mesh> listToConvert = new ArrayList<>();

        var fire = materials.remove("fire");

        if(fire != null) {
            EyeTextureGenerator.generate(SV_FIRE.update(fire, modelDir), targetDir.resolve(name + "fire.png").toAbsolutePath());
            fire = new ApiMaterial("fire", List.of(new ApiTexture("BaseColorMap", modelDir.resolve(name + "fire.png").toAbsolutePath().toString())), new HashMap<>());
            materials.put("fire", fire);
        }

        if(k.equals("rare")) return;

        for (Mesh mesh : meshes) {
            if(mesh.material().name().endsWith("_eye") || mesh.material().name().equals("fire")) {
                listToConvert.add(mesh);
            }
        }

        for (Mesh mesh : listToConvert) {
            var newMesh = mesh.withNewMaterial(mesh.material().name().endsWith("_eye") ? eyes : fire);
            meshes.add(newMesh);
            meshes.remove(mesh);
        }
    }

    public static Map<String, String> findTexturePairs(Path directoryPath) {
        // Create a map to store pairs of textures
        Map<String, String> texturePairs = new HashMap<>();

        try {
            // Walk through the directory and its subdirectories
            Files.walk(directoryPath)
                    .filter(Files::isRegularFile) // Only consider regular files
                    .forEach(filePath -> {
                        String fileName = filePath.toAbsolutePath().toString();

                        // Check if "_rare" is present in the filename without ".png"
                        if (fileName.contains("_rare") && fileName.endsWith(".png")) {
                            // Remove "_rare" from the filename and find the corresponding texture name
                            String textureName = fileName.replace("_rare", "");

                            // Check if the counterpart texture exists
                            Path counterpartPath = filePath.resolveSibling(textureName);
                            if (Files.exists(counterpartPath)) {
                                texturePairs.put(textureName, fileName);
                            }
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return texturePairs;
    }

    protected Vector3f toVec3(Vec3 vec) {
        return new Vector3f(vec.x(), vec.y(), vec.z());
    }

    private record Attribute(
            AttributeType type,
            AttributeSize size
    ) {
    }

    public enum AttributeType {
        NONE,
        POSITION,
        NORMAL,
        TANGENT,
        BINORMAL,
        COLOR,
        TEXCOORD,
        BLEND_INDICES,
        BLEND_WEIGHTS;

        public static AttributeType get(long id) {
            for (var i = 0; i < values().length; i++) if (i == id) return values()[i];
            throw new RuntimeException("Unknown Attribute Type " + id);
        }
    }

    public enum AttributeSize {
        NONE(0, 0),
        RGBA_8_UNORM(20, Byte.BYTES * 4),
        RGBA_8_UNSIGNED(22, Byte.BYTES * 4),
        X32_UINT(36, Integer.BYTES),
        X32_INT(37, Integer.BYTES),
        RGBA_16_UNORM(39, Short.BYTES * 4),
        RGBA_16_FLOAT(43, Short.BYTES * 4),
        RG_32_FLOAT(48, Float.BYTES * 2),
        RGB_32_FLOAT(51, Float.BYTES * 3),
        RGBA_32_FLOAT(54, Float.BYTES * 4);

        public final int id;
        public final int size;

        AttributeSize(int id, int size) {
            this.id = id;
            this.size = size;
        }

        public static AttributeSize get(long id) {
            for (var value : values()) if (value.id == id) return value;
            throw new RuntimeException("Unknown Attribute Size " + id);
        }
    }

    public enum IndexLayout {
        UINT8(Byte.BYTES),
        UINT16(Short.BYTES),
        UINT32(Integer.BYTES),
        UINT64(Long.BYTES);

        public final int size;

        IndexLayout(int size) {
            this.size = size;
        }

        public static IndexLayout get(int i) {
            return values()[i];
        }
    }

    private ArrayList<String> extractedColors(TrackMaterialChannels list) {
        var reds = convert(list.red());
        var greens = convert(list.green());
        var blues = convert(list.blue());
        var alphas = convert(list.alpha());

        var colors = new ArrayList<String>();

        for (int j = 0; j < reds.size(); j++) {
            colors.add(color(reds.get(j), greens.get(j), blues.get(j)));
        }

        return colors;
    }
    private List<Float> convert(TrackMaterialValueList list) {
        return IntStream.range(0, list.valuesLength()).mapToObj(list::values).map(a -> new GlbReader.Pair<>(a.time(), a.value())).sorted(Comparator.comparing(GlbReader.Pair::left)).map(GlbReader.Pair::right).toList();
    }

    public static String color(float red, float green, float blue) {
        return Integer.toHexString(new Color((int) (rgbTosRGB(red) * 255), (int) (rgbTosRGB(green) * 255), (int) (rgbTosRGB(blue) * 255)).getRGB()).replaceFirst("ff", "#");
    }

    private static float rgbTosRGB(float value) {
        return (float) Math.pow(value, 1/2.2);
    }
}
