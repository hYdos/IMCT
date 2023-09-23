package gg.generations.imct.read.scvi;

import de.javagl.jgltf.model.impl.DefaultNodeModel;
import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.api.ApiTexture;
import gg.generations.imct.api.Mesh;
import gg.generations.imct.api.Model;
import gg.generations.imct.read.scvi.flatbuffers.Titan.Model.*;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeGraph;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeTextureGenerator;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.FIreGraph;
import gg.generations.imct.util.TrinityUtils;
import org.joml.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class SVModel extends Model {

    public SVModel(Path modelDir) {
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

        // Process extra material variants (shiny)
        //var extraMaterials = TRMMT.getRootAsTRMMT(read(modelDir.resolve(modelDir.getFileName() + ".trmmt"))).material(0);

        // Process material data
        var materials = TRMTR.getRootAsTRMTR(read(modelDir.resolve(Objects.requireNonNull(trmdl.materials(0), "Material name was null"))));
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

            this.materials.put(materialName, new ApiMaterial(
                    materialName,
                    textures,
                    properties
            ));
        }

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
                    if (!Objects.requireNonNull(info.meshName()).contains("lod"))
                        meshes.add(new Mesh(info.meshName() + "_" + subMesh.materialName(), this.materials.get(subMesh.materialName()), subIdxBuffer, positions, normals, tangents, colors, weights, boneIds, binormals, uvs));
                }
            }

        }
        processEyes(modelDir);
    }

    private static EyeGraph SV_EYE = new EyeGraph(256);

    private static FIreGraph SV_FIRE = new FIreGraph(256);

    protected void processEyes(Path modelDir) {
        var left_eye = materials.remove("l_eye");

        var right_eye = materials.remove("r_eye");

        if(left_eye == null) return;

        EyeTextureGenerator.generate(SV_EYE.update(left_eye, modelDir), "eyes");

        var eyes = new ApiMaterial("eyes", List.of(new ApiTexture("BaseColorMap", Path.of("eyes.png").toAbsolutePath().toString())), new HashMap<>());

        materials.put("eyes", eyes);

        List<Mesh> listToConvert = new ArrayList<>();

        var fire = materials.get("fire");

        if(fire != null) {
            EyeTextureGenerator.generate(SV_FIRE.update(fire, modelDir), "fire");
            fire = new ApiMaterial("fire", List.of(new ApiTexture("BaseColorMap", Path.of("fire.png").toAbsolutePath().toString())), new HashMap<>());
            materials.put("fire", fire);
        }

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

        var map = findTexturePairs(modelDir);


        materials.forEach((key, value) -> {
            var shinyMaterial = value.with(map);
            if(shinyMaterial != null) {
                shinyMaterials.put(key, shinyMaterial);
            }
        });
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
}
