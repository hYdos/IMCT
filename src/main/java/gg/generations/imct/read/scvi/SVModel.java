package gg.generations.imct.read.scvi;

import com.google.gson.*;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import gg.generations.imct.IMCT;
import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.api.ApiTexture;
import gg.generations.imct.api.Mesh;
import gg.generations.imct.api.Model;
import gg.generations.imct.read.scvi.flatbuffers.Titan.Model.*;
import gg.generations.imct.read.scvi.flatbuffers.Titan.Model.Animaton.*;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeTextureGenerator;
import gg.generations.imct.util.TrinityUtils;
import gg.generations.imct.write.GlbReader;
import org.joml.*;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.Math;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SVModel extends Model {

    record InitTrack() {}

    record AnimTrack() {}

    record MaterialTrack(Map<String, List<String>> animTracks) {}

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

        // Process material data;

        var defaultTRMMT = TRMTR.getRootAsTRMTR(read(modelDir.resolve(Objects.requireNonNull(trmdl.materials(0), "Material name was null"))));

        createMaterials(Stream.of(defaultTRMMT)).get(0).forEach((k, v) -> {
            materials.put(k, v.toApiMaterial(modelDir, k));
        });



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
                        materialRemap.computeIfAbsent(subMesh.materialName(), a -> new ArrayList<>()).add(info.meshName() + "_" + subMesh.materialName());
                        meshes.add(new Mesh(info.meshName() + "_" + subMesh.materialName(), subMesh.materialName(), subIdxBuffer, positions, normals, tangents, colors, weights, boneIds, binormals, uvs));
                    }
                }
            }


            // Process extra material variants (shiny)

            var extraMaterials = TRMMT.getRootAsTRMMT(read(modelDir.resolve(modelDir.getFileName() + ".trmmt")));

            var map = new HashMap<>(IntStream.range(0, extraMaterials.materialLength()).mapToObj(extraMaterials::material).collect(Collectors.toMap(MMT::name, mtt -> {
                var trmtrs = IntStream.range(0, mtt.materialNameLength()).mapToObj(mtt::materialName).map(a -> TRMTR.getRootAsTRMTR(read(modelDir.resolve(Objects.requireNonNull(a, "Material name was null"))))).toList();


                var materials = createMaterials(trmtrs.isEmpty() ? Stream.of(defaultTRMMT) : trmtrs.stream());

                var materialProperties = IntStream.range(0, mtt.materialPropertiesLength()).mapToObj(mtt::materialProperties).collect(Collectors.toMap(MaterialProperties::name, materialProperties1 -> {
                    var tracm = TRACM.getRootAsTRACM(materialProperties1.tracm().bytebufferAsByteBuffer());

                    var tracks = IntStream.range(0, tracm.tracksLength()).mapToObj(tracm::tracks).collect(Collectors.toMap(gg.generations.imct.read.scvi.flatbuffers.Titan.Model.Animaton.Track::trackPath, a -> IntStream.range(0, a.materialAnimation().materialTrackLength()).mapToObj(j -> a.materialAnimation().materialTrack(j)).collect(Collectors.toMap(track -> track.name(), trackMaterial -> new MaterialTrack(IntStream.range(0, trackMaterial.animValuesLength()).mapToObj(trackMaterial::animValues).collect(Collectors.toMap(TrackMaterialAnim::name, a1 -> extractedColors(a1.list()))))))));

                    var mappers = new HashMap<String, Map<String, List<String>>>();
                    IntStream.range(0, materialProperties1.mappersLength()).mapToObj(materialProperties1::mappers).forEach(materialMapper -> {
                        var map1 = mappers.computeIfAbsent(materialMapper.meshName(), a -> new HashMap<>());
                        map1.computeIfAbsent(materialMapper.materialName(), a -> new ArrayList<>()).add(materialMapper.layerName());
                    });

                    var config = new TrackConfig(tracm.config().framerate(), tracm.config().duration());

                    return new MaterialProperty(config, tracks, mappers);
                }));

                return new Generic(materialProperties, materials);
            })));

            var regularPair = new GlbReader.Pair<>("normal", new Generic(new HashMap<>(), createMaterials(Stream.of(defaultTRMMT))));


            if(map.size() == 1 && map.containsKey("rare")) {
                map.put(regularPair.left(), regularPair.right());
            }

            var variantMap = new HashMap<String, Map<String, String>>();



            map.forEach((variantBase, data) -> {
                var correctedVariantBase = variantBase.equals("rare") ? "shiny" : variantBase;

                var material = data.materials().get(0);

                if(data.materialProperties() != null && !data.materialProperties().isEmpty()) {
                    var amount = data.materialProperties().entrySet().stream().flatMap(a -> a.getValue().tracks().values().stream()).map(a -> a.values()).flatMap(a -> a.stream()).flatMap(a -> a.animTracks().values().stream()).mapToInt(a -> a.size()).max().getAsInt();

//                    var properties = data.materialProperties().get("color").tracks().entrySet().stream().collect(Collectors.toMap(a -> a.getKey(), a -> a.getValue().entrySet().stream().collect(Collectors.toMap(b -> b.getKey(), b -> b.getValue().animTracks))));

                    var map1 = data.materialProperties().get("color").tracks().values().stream().flatMap(a -> a.entrySet().stream()).map(a -> new GlbReader.Pair<String, Map<String, List<String>>>(a.getKey(), a.getValue().animTracks())).collect(Collectors.toMap(GlbReader.Pair::left, a -> a.right(), (a, b) -> a));

                    var properties = transformMap(map1, amount);


                    for (int j = 0; j < amount; j++) {
                        var materialProxies = new HashMap<String, String>();

                        for (Map.Entry<String, Material> entry : material.entrySet()) {
                            String materialName = entry.getKey();
                            Material v = entry.getValue();
                            var meshName = materialRemap.get(materialName);

                            if (meshName == null) throw new RuntimeException("Material needs mesh!");


                            var m = properties.get(j).get(materialName);

                            var mat = v.toApiMaterial(modelDir, materialName, m);

                            var existing = materials.entrySet().stream().filter(a -> a.getValue().equals(mat)).findAny();


                            var materialNameFinal = correctedVariantBase + "_" + j + "_" + materialName;

                            if (existing.isEmpty()) {
                                materials.put(materialNameFinal, mat);
                            } else {
                                materialNameFinal = existing.get().getKey();
                            }


                            for (String name : meshName) {
                                materialProxies.put(name, materialNameFinal);
                            }

                        }
                        variants.put(correctedVariantBase + "_" + j, materialProxies);

                    }

                } else {

                    var materialProxies = new HashMap<String, String>();

                    material.forEach((materialName, v) -> {
                        var mat = v.toApiMaterial(modelDir, materialName);

                        var meshName = materialRemap.get(materialName);

                        if(meshName == null) throw new RuntimeException("Material needs mesh!");

                        var existing = materials.entrySet().stream().filter(a -> a.getValue().equals(mat)).findAny();

                        var materialNameFinal = correctedVariantBase + "_" + materialName;

                        if(existing.isEmpty()) {
                            materials.put(materialNameFinal, mat);
                        } else {
                            materialNameFinal = existing.get().getKey();
                        }


                        for (String name : meshName) {
                            materialProxies.put(name, materialNameFinal);
                        }
                    });

                    variants.put(correctedVariantBase, materialProxies);
                }

            });



//        System.out.println();


            var gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Vector4f.class, (JsonSerializer<Vector4f>) (src, typeOfSrc, context) -> {
                var color = vectorToString(src);

                return new JsonPrimitive(color);
            }).create();

//
//            try {
//                var reader = createFileWriter(targetDir.resolve("data.json"));
//                reader.append(gson.toJson(variants));
//                reader.flush();
//            } catch (Exception e) {
//                throw new RuntimeException("Fuck", e);
//            }
        }
    }

    private static String vectorToString(Vector4f color) {
        // Ensure the RGB components are in the range of 0.0 to 1.0
        float r = Math.max(0.0f, Math.min(1.0f, color.x));
        float g = Math.max(0.0f, Math.min(1.0f, color.y));
        float b = Math.max(0.0f, Math.min(1.0f, color.z));

        // Convert normalized values to 8-bit integer values
        int red = (int) (r * 255);
        int green = (int) (g * 255);
        int blue = (int) (b * 255);

        // Create the hexadecimal color string

        return String.format("#%02X%02X%02X", red, green, blue);
    }

    private static float rgbTosRGB(float value) {
        return (float) Math.pow(value, 1/2.2);
    }


    private List<Map<String, Material>> createMaterials(Stream<TRMTR> trmtrStream) {
        return trmtrStream.map(a -> IntStream.range(0, a.materialsLength()).mapToObj(a::materials).collect(Collectors.toMap(gg.generations.imct.read.scvi.flatbuffers.Titan.Model.Material::name, material -> {

            var floatParmeters = IntStream.range(0, material.floatParameterLength()).mapToObj(material::floatParameter).collect(Collectors.<FloatParameter, String, Float>toMap(FloatParameter::floatName, FloatParameter::floatValue));

            var shaderParameters = new HashMap<String, Map<String, String>>();
            IntStream.range(0, material.shadersLength()).mapToObj(material::shaders).forEach(shader -> shaderParameters.put(shader.shaderName(), IntStream.range(0, shader.shaderValuesLength()).mapToObj(shader::shaderValues).collect(Collectors.toMap(StringParameter::stringName, StringParameter::stringValue))));

            var textures = IntStream.range(0, material.texturesLength()).mapToObj(material::textures).collect(Collectors.toMap(Texture::textureName, texture -> texture.textureFile().replace(".bntx", ".png")));

            var vectors = IntStream.range(0, material.float4ParameterLength()).mapToObj(material::float4Parameter).collect(Collectors.toMap(Float4Parameter::colorName, param -> new Vector4f(param.colorValue().r(), param.colorValue().g(), param.colorValue().b(), param.colorValue().a())));


            return new Material(floatParmeters, shaderParameters, textures, vectors);
        }))).toList();
    }

    public static BufferedWriter createFileWriter(Path filePath) {
        try {
            // Delete the file if it exists
            if (Files.exists(filePath) && !Files.deleteIfExists(filePath)) {
                throw new RuntimeException("Unable to delete the existing file at path: " + filePath);
            }

            // Create a new file
            Files.createFile(filePath);
            System.out.println("File created at path: " + filePath);

            // Create a BufferedWriter for the new file
            return Files.newBufferedWriter(filePath, StandardOpenOption.WRITE);

        } catch (IOException e) {
            throw new RuntimeException("Error while processing file at path: " + filePath, e);
        }
    }

    public record Material(Map<String, Float> floatParmeters, Map<String, Map<String, String>> shaderParameters, Map<String, String> textures, Map<String, Vector4f> vectors) {

        public ApiMaterial toApiMaterial(Path source, String key) {
            return toApiMaterial(source, key, null);
        }

        public ApiMaterial toApiMaterial(Path source, String key, Map<String, String> propertyRemap) {
            var values = new HashMap<String, Object>();
            values.put("shader", processShaderType(shaderParameters));

            if (this.vectors.containsKey("BaseColorLayer1")) values.put("baseColor1", color("BaseColorLayer1", this.vectors, propertyRemap));
            if (this.vectors.containsKey("BaseColorLayer2")) values.put("baseColor2", color("BaseColorLayer2", this.vectors, propertyRemap));
            if (this.vectors.containsKey("BaseColorLayer3")) values.put("baseColor3", color("BaseColorLayer3", this.vectors, propertyRemap));
            if (this.vectors.containsKey("BaseColorLayer4")) values.put("baseColor4", color("BaseColorLayer4", this.vectors, propertyRemap));
            if (this.vectors.containsKey("BaseColorLayer5")) values.put("baseColor5", color("BaseColorLayer5", this.vectors, propertyRemap));
            if (this.floatParmeters().containsKey("EmissionIntensityLayer1")) {
                var intensity = this.floatParmeters().get("EmissionIntensityLayer1");
                if(intensity != 0.0) {
                    values.put("emiIntensity1", intensity);
                    if (this.vectors.containsKey("EmissionColorLayer1")) values.put("emiColor1", color("EmissionColorLayer1", this.vectors, propertyRemap));
                }
            }
            if (this.floatParmeters().containsKey("EmissionIntensityLayer2")) {
                var intensity = this.floatParmeters().get("EmissionIntensityLayer2");
                if(intensity != 0.0) {
                    values.put("emiIntensity2", intensity);
                    if (this.vectors.containsKey("EmissionColorLayer2")) values.put("emiColor2", color("EmissionColorLayer2", this.vectors, propertyRemap));
                }
            }
            if (this.floatParmeters().containsKey("EmissionIntensityLayer3")) {
                var intensity = this.floatParmeters().get("EmissionIntensityLayer3");
                if(intensity != 0.0) {
                    values.put("emiIntensity3", intensity);
                    if (this.vectors.containsKey("EmissionColorLayer3")) values.put("emiColor3", color("EmissionColorLayer3", this.vectors, propertyRemap));
                }
            }
            if (this.floatParmeters().containsKey("EmissionIntensityLayer4")) {
                var intensity = this.floatParmeters().get("EmissionIntensityLayer4");
                if(intensity != 0.0) {
                    values.put("emiIntensity4", intensity);
                    if (this.vectors.containsKey("EmissionColorLayer4")) values.put("emiColor4", color("EmissionColorLayer4", this.vectors, propertyRemap));
                }
            }
            if (this.floatParmeters().containsKey("EmissionIntensityLayer5")) {
                var intensity = this.floatParmeters().get("EmissionIntensityLayer5");
                if(intensity != 0.0) {
                    values.put("emiIntensity5", intensity);
                    if (this.vectors.containsKey("EmissionColorLayer5")) values.put("emiColor5", color("EmissionColorLayer5", this.vectors, propertyRemap));
                }
            }

            var textures = new ArrayList<ApiTexture>();

            if (this.textures().containsKey("BaseColorMap")) textures.add(new ApiTexture("diffuse", source.resolve(this.textures.get("BaseColorMap")).toString()));
            if (this.textures().containsKey("LayerMaskMap")) textures.add(new ApiTexture("layer", source.resolve(this.textures.get("LayerMaskMap")).toString()));
            if (this.textures().containsKey("HighlightMaskMap")) textures.add(new ApiTexture("mask", source.resolve(this.textures.get("HighlightMaskMap")).toString()));

            return new ApiMaterial(key, textures, values);
        }

        private String processShaderType(Map<String, Map<String, String>> shaderParameters) {
            return "layered";
        }
    }

    public record TrackConfig(long duration, long fps) {}

    private void processMaterials(String name, Path modelDir, Path targetDir, TRMTR materials, Map<String, String> materialRemap) throws IOException {
//        var list = this.materials.computeIfAbsent(name, a -> new HashMap<>());

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

            IntStream.of(1, 2,3, 4, 5).boxed().flatMap(a -> Stream.of("BaseColorLayer" + a, "EmissionIntensityLayer" + a, "EmissionColorLayer" + a)).forEach(s -> {
                var obj = properties.get(s);

                properties1.put(s, obj instanceof Vector4f v ? color(v.x, v.y, v.z) : obj);
            });

            ApiMaterial finalMat = mat;
            var mat1 = new ApiMaterial(name, Stream.of("BaseColorMap", "LayerMaskMap", "HighlightMaskMap").map(finalMat::getTexture).collect(Collectors.toList()), properties1);


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

//            list.put(materialName, mat1);

            var path = Path.of(mat1.getTexture("BaseColorMap").filePath());
            EyeTextureGenerator.copy(path, targetDir.resolve(path.getFileName()));
            if(mat1.getTexture("LayerMaskMap") != null) {
                path = Path.of(mat1.getTexture("LayerMaskMap").filePath());
                EyeTextureGenerator.copy(path, targetDir.resolve(path.getFileName()));
            } else {
                System.out.println();
            }
            if(mat1.getTexture("HighlightMaskMap") != null) {
                path = Path.of(mat1.getTexture("HighlightMaskMap").filePath());
                EyeTextureGenerator.copy(path, targetDir.resolve(path.getFileName()));
            } else {
                System.out.println();
            }

            IMCT.TOTAL_SHADERS.add(shader);
        }
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
            var r = reds.get(j);
            var g = greens.size() < (j+1) ? 1.0f : greens.get(j);
            var b = blues.size() < (j+1) ? 1.0f : blues.get(j);

            colors.add(color(r, g, b));
        }

        return colors;
    }
    private List<Float> convert(TrackMaterialValueList list) {
        return IntStream.range(0, list.valuesLength()).mapToObj(list::values).map(a -> new GlbReader.Pair<>(a.time(), a.value())).sorted(Comparator.comparing(GlbReader.Pair::left)).map(GlbReader.Pair::right).toList();
    }

    public static String color(String key, Map<String, Vector4f> rgb, Map<String, String> remap) {
        var color = color(rgb.get(key));

        if (remap != null) {
            return remap.getOrDefault(key, color);
        } else {
            return color;
        }
    }

    public static String color(Vector4f rgb) {
        Vector3i srgb = new Vector3i();
        for (int i = 0; i < 3; i++) {
            float v = rgb.get(i);

            var value = v <= 0.0031308 ? v * 12.92 : (1.055 * Math.pow(v, 1 / 2.4)) - 0.055;

            srgb.setComponent(i, (int) (value * 255));
        }
        return Integer.toHexString(new Color(srgb.x, srgb.y, srgb.z).getRGB()).replaceFirst("ff", "#");
    }

    private static Color createColorFromLinearRGB(Vector4f color) {
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

//    public static String color(Vector4f vec) {
//        return Integer.toHexString(new Color((int) (rgbTosRGB(vec.x) * 255), (int) (rgbTosRGB(vec.y) * 255), (int) (rgbTosRGB(vec.z) * 255)).getRGB()).replaceFirst("ff", "#");
//    }

    public static String color(float red, float green, float blue) {
        return Integer.toHexString(new Color((int) (rgbTosRGB(red) * 255), (int) (rgbTosRGB(green) * 255), (int) (rgbTosRGB(blue) * 255)).getRGB()).replaceFirst("ff", "#");
    }

    public static List<Map<String, Map<String, String>>> transformMap(Map<String, Map<String, List<String>>> inputMap, int amount) {
        List<Map<String, Map<String, String>>> list = new ArrayList<>();

        IntStream.range(0, amount).forEach(i -> {
            var map = new HashMap<String, Map<String, String>>();
            inputMap.forEach((material, v) -> {
                var map1 = map.computeIfAbsent(material, a -> new HashMap<>());
                v.forEach((property, values) -> {
                    if (values.size() >= amount) {
                        map1.put(property, values.get(i));
                    }
                });
            });

            list.add(map);
        });

        return list;
    }
}
