package gg.generations.imct.read.la;

import de.javagl.jgltf.model.impl.DefaultNodeModel;
import gg.generations.imct.IMCT;
import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.api.ApiTexture;
import gg.generations.imct.api.Mesh;
import gg.generations.imct.api.Model;
import gg.generations.imct.read.la.flatbuffers.Hayabusa.Model.*;
import gg.generations.imct.read.scvi.flatbuffers.Titan.Model.TRMBF;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.BodyGraph;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeGraph;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeTextureGenerator;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.FIreGraph;
import gg.generations.imct.util.TrinityUtils;
import org.joml.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static gg.generations.imct.read.scvi.SVModel.SV_BODY;
import static gg.generations.imct.read.scvi.SVModel.SV_EYE;

public class LAModel extends Model {
    public static EyeGraph SV_EYE = new EyeGraph(128);
    public static FIreGraph SV_BODY = new FIreGraph(512);
    private static FIreGraph SV_FIRE = new FIreGraph(128);

    public LAModel(Path modelDir, Path targetDir) throws IOException {
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

            var rawRotation = toVec3(bone.transform().rotation());
            bones.add(new Bone(
                    bone.name(),
                    toVec3(bone.transform().translation()),
                    new Quaternionf().rotateLocalX(rawRotation.x).rotateLocalY(rawRotation.y).rotateLocalZ(rawRotation.z),
                    toVec3(bone.transform().scale()),
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

        joints = new ArrayList<>(bones.stream().mapToInt(a -> a.rigIdx).max().getAsInt());


        // Second bone pass. Convert into skeleton
        this.skeleton = new ArrayList<>();

        for (var bone : bones) {
            var node = new DefaultNodeModel();
            node.setName(bone.name);

            var t = bone.translation;
            var r = bone.rotation;
            var s = bone.scale;

            if (!(t.x == 0 && t.y == 0 && t.z == 0)) {
                node.setTranslation(new float[]{t.x, t.y, t.z});
            }

            if (!(r.x == 0 && r.y == 0 && r.z == 0 && r.w == 1)) {
                node.setRotation(new float[]{r.x, r.y, r.z, r.w});
            }

            if (!(s.x == 1 && s.y == 1 && s.z == 1)) {
                node.setScale(new float[]{s.x, s.y, s.z});
            }

            if (bone.rigIdx != -1) {
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

        var materialRemap = new HashMap<String, String>();

        // Process material data
        var materials = gg.generations.imct.read.scvi.flatbuffers.Titan.Model.TRMTR.getRootAsTRMTR(read(modelDir.resolve(Objects.requireNonNull(trmdl.materials(0), "Material name was null"))));
        processMaterials("regular", modelDir, targetDir, materials, materialRemap);

        // Process extra material variants (shiny)
        var extraMaterials = gg.generations.imct.read.scvi.flatbuffers.Titan.Model.TRMMT.getRootAsTRMMT(read(modelDir.resolve(modelDir.getFileName() + ".trmmt"))).material(0);
        processMaterials(extraMaterials.name(), modelDir, targetDir, gg.generations.imct.read.scvi.flatbuffers.Titan.Model.TRMTR.getRootAsTRMTR(read(modelDir.resolve(Objects.requireNonNull(extraMaterials.materialName(0), "Material name was null")))), null);

//        System.out.println(IMCT.TOTAL_SHADERS);

        // Process mesh data
        for (var i = 0; i < meshInfo.size(); i++) {
            System.out.println("Processing Mesh Info " + i);
            for (int mesh = 0; mesh < meshInfo.get(i).shapesLength(); mesh++) {
                System.out.println("Processing Mesh " + i);
                var info = meshInfo.get(i).shapes(mesh);
                var data = meshData.get(i).buffers(mesh);
                var vertexBuffer = data.vertexBuffer(0).bufferAsByteBuffer();
                var idxBuffer = data.indexBuffer(0);

                var idxLayout = IndexLayout.get((int) info.indexLayoutFormat());
                var rawAttributes = info.vertexLayout(0);


                var attributes = new ArrayList<Attribute>();
                for (var j = 0; j < rawAttributes.elementsLength(); j++) {
                    attributes.add(new Attribute(
                            AttributeType.get(rawAttributes.elements(j).semanticName()),
                            AttributeSize.get(rawAttributes.elements(j).format())
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

                var vertexCount = data.vertexBuffer(0).bufferLength() / info.vertexLayout(0).layoutSize(0).size();

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
                                } else throw new RuntimeException("Unexpected color format: " + attribute.type);
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
                for (int j = 0; j < info.subMeshesLength(); j++) {
                    var subMesh = info.subMeshes(j);
                    var subIdxBuffer = indices.subList((int) subMesh.indexOffset(), (int) (subMesh.indexOffset() + subMesh.indexCount()));
                    if (!Objects.requireNonNull(info.meshName()).contains("lod")) {
                        var name = materialRemap.getOrDefault(subMesh.material(), subMesh.material());
                        meshes.add(new Mesh(info.meshName() + "_" + subMesh.material(), this.materials.get("regular").get(name), subIdxBuffer, positions, normals, tangents, colors, weights, boneIds, binormals, uvs));
                    }
                }
            }
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

    private enum AttributeType {
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

    private enum AttributeSize {
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

        private final int id;
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

    private enum IndexLayout {
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

    private static EyeGraph ARCEUS = new EyeGraph(128);

    private static BodyGraph ARCEUS_BODY = new BodyGraph(512);


    private void processMaterials(String name, Path modelDir, Path targetDir, gg.generations.imct.read.scvi.flatbuffers.Titan.Model.TRMTR materials, Map<String, String> materialRemap) throws IOException {
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


            Path path;

            switch (shader) {
                case "Eye":
                case "EyeClearCoat":
                    path = targetDir.resolve(name1 + materialName + ".png").toAbsolutePath();

                    if(IMCT.messWithTexture) EyeTextureGenerator.generate(SV_EYE.update(mat, modelDir), path);

                    mat = new ApiMaterial(materialName, List.of(new ApiTexture("BaseColorMap", path.toString())), Map.of("type", "solid"));
                    list.putIfAbsent(materialName, mat);


/*
                    if(!list.containsKey("eyes")) {
                        path = targetDir.resolve(name1 + "eyes.png").toAbsolutePath();

                        EyeTextureGenerator.generate(SV_EYE.update(mat, modelDir), path);

                        list.computeIfAbsent("eyes", key -> new ApiMaterial("eyes", List.of(new ApiTexture("BaseColorMap", path.toString())), Map.of("type", "solid")));
                    }

                    if (materialRemap != null) materialRemap.put(materialName, "eyes");
*/

                    continue;
                case "Unlit":
                    path = targetDir.resolve(name1 + materialName + ".png").toAbsolutePath();

                    if(IMCT.messWithTexture) EyeTextureGenerator.generate(SV_BODY.update(mat, modelDir), path);

                    if(IMCT.messWithTexture) SV_BODY.display();

                    mat = new ApiMaterial(materialName, List.of(new ApiTexture("BaseColorMap", path.toString())), Map.of("type", "unlit"));
                    list.putIfAbsent(materialName, mat);

                    continue;
                case "Transparent":

                    path = targetDir.resolve(name1 + materialName + ".png").toAbsolutePath();

                    if(IMCT.messWithTexture) EyeTextureGenerator.generate(SV_BODY.update(mat, modelDir), path);

                    mat = new ApiMaterial(materialName, List.of(new ApiTexture("BaseColorMap", path.toString())), Map.of("type", "transparent"));
                    list.putIfAbsent(materialName, mat);

                    continue;
                case "SSSEffect":
                case "Standard":
                case "FresnelEffect":
                case "SSS":
                case "NonDirectional":
                    path = targetDir.resolve(name1 + materialName + ".png").toAbsolutePath();

                    if(IMCT.messWithTexture) EyeTextureGenerator.generate(SV_BODY.update(mat, modelDir), path);



                    mat = new ApiMaterial(materialName, List.of(new ApiTexture("BaseColorMap", path.toString())), Map.of("type", "solid"));
                    list.putIfAbsent(materialName, mat);

//                    if (materialRemap != null) materialRemap.put(materialName, "eyes");


                    continue;
                case "InsideEmissionParallax":
                    break;
                case "FresnelBlend":
                    break;
            }

            list.put(materialName, mat);

            IMCT.TOTAL_SHADERS.add(shader);
        }
    }
}