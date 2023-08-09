package gg.generations.imct.scvi;

import de.javagl.jgltf.model.impl.DefaultNodeModel;
import gg.generations.imct.intermediate.Model;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.*;
import gg.generations.imct.util.TrinityUtils;
import org.joml.*;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        joints = new ArrayList<>(bones.stream().mapToInt(a -> a.rigIdx).max().getAsInt());


        // Second bone pass. Convert into skeleton

        this.skeleton = new ArrayList<>();

        var arr = new float[16];

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
        var material = TRMTR.getRootAsTRMTR(read(modelDir.resolve(Objects.requireNonNull(trmdl.materials(0), "Material name was null"))));
        for (int i = 0; i < material.materialsLength(); i++) {
            var rawMaterial = material.materials(i);
            var textures = new ArrayList<Texture>();
            var materialName = rawMaterial.name();
            var shader = Objects.requireNonNull(rawMaterial.shaders(0).shaderName(), "Null shader name");

            if (!shader.equals("SSS")) {
                if (shader.equals("EyeClearCoat")) {
                    System.out.println("Material Properties");
                    for (int j = 0; j < rawMaterial.float4ParameterLength(); j++) {
                        var colorParam = rawMaterial.float4Parameter(j);
                        System.out.println("Name: " + colorParam.colorName());
                        System.out.println("R: " + colorParam.colorValue().r() * 255);
                        System.out.println("G: " + colorParam.colorValue().g() * 255);
                        System.out.println("B: " + colorParam.colorValue().b() * 255);
                        System.out.println("A: " + colorParam.colorValue().a() * 255);
                    }

                    System.out.println();
                } else {
                    System.err.println("Unknown shader " + shader);
                }
            }

            for (int j = 0; j < rawMaterial.texturesLength(); j++) {
                var rawTexture = rawMaterial.textures(j);
                textures.add(new Texture(rawTexture.textureName(), modelDir.resolve(rawTexture.textureFile().replace(".bntx", ".png")).toAbsolutePath().toString()));
            }

            materials.put(materialName, new Material(
                    materialName,
                    textures
            ));
        }

        // Process mesh data
        for (var i = 0; i < meshInfo.size(); i++) {
            System.out.println("Processing Mesh Info " + i);
            for (int mesh = 0; mesh < meshInfo.get(i).meshesLength(); mesh++) {
                System.out.println("Processing Mesh " + i);
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
                for (int j = 0; j < info.materialsLength(); j++) {
                    var subMesh = info.materials(j);
                    var subIdxBuffer = indices.subList((int) subMesh.polyOffset(), (int) (subMesh.polyOffset() + subMesh.polyCount()));
                    if (!Objects.requireNonNull(info.meshName()).contains("lod"))
                        meshes.add(new Mesh(info.meshName() + "_" + subMesh.materialName(), materials.get(subMesh.materialName()), subIdxBuffer, positions, normals, tangents, weights, boneIds, binormals, uvs));
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
}
