package gg.generations.imct.read.letsgo;

import de.javagl.jgltf.model.impl.DefaultNodeModel;
import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.api.ApiTexture;
import gg.generations.imct.api.Mesh;
import gg.generations.imct.api.Model;
import gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.TextureMap;
import gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.Vector3;
import gg.generations.imct.util.TrinityUtils;
import org.joml.*;

import java.nio.file.Path;
import java.util.*;

public class LGModel extends Model {

    private final Map<Integer, String> materialIds = new HashMap<>();

    public LGModel(Path modelDir) {
        var gfbmdl = gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.Model.getRootAsModel(read(modelDir.resolve(modelDir.getFileName() + ".gfbmdl")));
        if (gfbmdl.groupsLength() != gfbmdl.meshesLength())
            System.err.println("There may be an error Groups format != Mesh format");

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
        for (int i = 0; i < gfbmdl.bonesLength(); i++) {
            var bone = gfbmdl.bones(i);

            var rawRotation = toVec3(bone.rotation());
            bones.add(new Bone(
                    bone.name(),
                    toVec3(bone.translation()),
                    new Quaternionf().rotateLocalX(rawRotation.x).rotateLocalY(rawRotation.y).rotateLocalZ(rawRotation.z),
                    toVec3(bone.scale()),
                    bone.parent(),
                    i,
                    bone.boneType(),
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

            if (!(t.x == 0 && t.y == 0 && t.z == 0)) node.setTranslation(new float[]{t.x, t.y, t.z});
            if (!(r.x == 0 && r.y == 0 && r.z == 0 && r.w == 1)) node.setRotation(new float[]{r.x, r.y, r.z, r.w});
            if (!(s.x == 1 && s.y == 1 && s.z == 1)) node.setScale(new float[]{s.x, s.y, s.z});

            if (bone.rigIdx != -1) joints.add(bone.rigIdx, node);
            skeleton.add(node);
        }

        for (int i = 0; i < skeleton.size(); i++) {
            var node = skeleton.get(i);
            var bone = bones.get(i);
            if (bone.parent == -1) continue;
            var parent = skeleton.get(bone.parent);
            parent.addChild(node);
        }

        for (int i = 0; i < gfbmdl.materialsLength(); i++) {
            var rawMaterial = gfbmdl.materials(i);
            var properties = new HashMap<String, Object>();
            var textures = new ArrayList<ApiTexture>();
            var materialName = rawMaterial.name();
            var shader = Objects.requireNonNull(rawMaterial.shaderGroup(), "Null shader name");
            properties.put("shader", shader);

            for (int j = 0; j < rawMaterial.common().valuesLength(); j++) {
                var property = rawMaterial.common().values(j);
                properties.put(property.name(), property.value());
            }

            for (int j = 0; j < rawMaterial.valuesLength(); j++) {
                var property = rawMaterial.values(j);
                properties.put(property.name(), property.value());
            }

            for (int j = 0; j < rawMaterial.common().colorsLength(); j++) {
                var property = rawMaterial.common().colors(j);
                properties.put(property.name(), new Vector3f(property.color().r(), property.color().g(), property.color().b()));
            }

            for (int j = 0; j < rawMaterial.colorsLength(); j++) {
                var property = rawMaterial.colors(j);
                properties.put(property.name(), new Vector3f(property.color().r(), property.color().g(), property.color().b()));
            }

            for (int j = 0; j < rawMaterial.textureMapsLength(); j++) {
                var rawTexture = rawMaterial.textureMaps(j);
                var texName = gfbmdl.textureNames(rawTexture.index());
                textures.add(new ApiTexture(processTextureName(rawTexture), modelDir.resolve(texName + ".png").toAbsolutePath().toString()));
            }

            materialIds.put(i, materialName);
            materials.put(materialName, new ApiMaterial(
                    materialName,
                    textures,
                    properties
            ));
        }

        for (int i = 0; i < gfbmdl.groupsLength(); i++) {
            System.out.println("Processing Mesh " + i);
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
                                uvs.add(processSmartLetsGoUvs(x, y));
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
                                var x = vertexBuffer.get() & 0xFF;
                                var y = vertexBuffer.get() & 0xFF;
                                var z = vertexBuffer.get() & 0xFF;
                                var w = vertexBuffer.get() & 0xFF;
                                colors.add(new Vector4f(x, y, z, w));
                            } else throw new RuntimeException("Unexpected color format: " + attribute.format);
                        }
                        case BLEND_INDICES -> {
                            if (Objects.requireNonNull(attribute.format) == AttributeFormat.BYTE) {
                                var w = vertexBuffer.get() & 0xFF;
                                var x = vertexBuffer.get() & 0xFF;
                                var y = vertexBuffer.get() & 0xFF;
                                var z = vertexBuffer.get() & 0xFF;
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
                System.out.println("Processing Sub-mesh " + j);
                var mesh = meshGroup.polygons(j);
                var indices = new ArrayList<Integer>();
                for (var idx = 0; idx < mesh.facesLength(); idx++) indices.add(mesh.faces(idx));
                meshes.add(new Mesh(name + "_" + mesh.materialIndex(), materials.get(idToName(mesh.materialIndex())), indices, positions, normals, tangents, colors, weights, boneIds, biNormals, uvs));
            }
        }
    }

    private Vector2f processSmartLetsGoUvs(float x, float y) {
        x *= 2;
        if(x > 1) x *= -1;

        return new Vector2f(x, y);
    }

    private String idToName(long idx) {
        return materialIds.get((int) idx);
    }

    private String processTextureName(TextureMap rawTexture) {
        return rawTexture.sampler().replace("Col0Tex", "BaseColorMap");
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
