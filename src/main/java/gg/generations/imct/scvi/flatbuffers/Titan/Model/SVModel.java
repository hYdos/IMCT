package gg.generations.imct.scvi.flatbuffers.Titan.Model;

import de.javagl.jgltf.model.GltfConstants;
import de.javagl.jgltf.model.creation.AccessorModels;
import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.creation.MaterialBuilder;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.impl.DefaultSkinModel;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.v2.GltfModelWriterV2;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import gg.generations.imct.intermediate.Model;
import org.joml.*;

import java.io.IOException;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SVModel implements Model {

    private final List<DefaultNodeModel> skeleton;
    private final List<Mesh> meshes = new ArrayList<>();
    private final Map<String, Material> materials = new HashMap<>();

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
                Matrix4f matrix,
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

            var rawRotation = toVec3(bone.transform().vecRot()).rotateX((float) Math.toRadians(90));

            bones.add(new Bone(
                    bone.name(),
                    new Matrix4f().translationRotateScale(
                            toVec3(bone.transform().vecTranslate()),
                            new Quaternionf().rotateXYZ(rawRotation.x, rawRotation.y, rawRotation.z),
                            toVec3(bone.transform().vecScale())
                    ),
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

        // Second bone pass. Convert into skeleton
        this.skeleton = bones.stream().map(bone -> {
            var node = new DefaultNodeModel();
            node.setName(bone.name);
            node.setMatrix(new float[]{
                    bone.matrix().m00(), bone.matrix().m01(), bone.matrix().m02(), bone.matrix().m03(),
                    bone.matrix().m10(), bone.matrix().m11(), bone.matrix().m12(), bone.matrix().m13(),
                    bone.matrix().m20(), bone.matrix().m21(), bone.matrix().m22(), bone.matrix().m23(),
                    bone.matrix().m30(), bone.matrix().m31(), bone.matrix().m32(), bone.matrix().m33()
            });

            return node;
        }).toList();

        for (int i = 0; i < skeleton.size(); i++) {
            var node = skeleton.get(i);
            var bone = bones.get(i);
            if(bone.parent == -1) continue;
            node.setParent(skeleton.get(bone.parent));
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
                    throw new RuntimeException("Unknown shader " + shader);
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
                                } else {
                                    throw new RuntimeException("Unexpected position format: " + attribute.type);
                                }
                            }

                            case COLOR -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_8_UNORM) {
                                    var x = vertexBuffer.get() & 0xFF;
                                    var y = vertexBuffer.get() & 0xFF;
                                    var z = vertexBuffer.get() & 0xFF;
                                    var w = vertexBuffer.get() & 0xFF;
                                } else {
                                    throw new RuntimeException("Unexpected color format: " + attribute.type);
                                }
                            }

                            case NORMAL -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_16_FLOAT) {
                                    normals.add(readRGBA16Float3(vertexBuffer));
                                } else {
                                    throw new RuntimeException("Unexpected normal format: " + attribute.type);
                                }
                            }

                            case TANGENT -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_16_FLOAT) {
                                    tangents.add(readRGBA16Float4(vertexBuffer));
                                } else {
                                    throw new RuntimeException("Unexpected tangent format: " + attribute.type);
                                }
                            }

                            case TEXCOORD -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RG_32_FLOAT) {
                                    var x = vertexBuffer.getFloat();
                                    var y = 1.0f - vertexBuffer.getFloat();
                                    uvs.add(new Vector2f(x, y));
                                } else {
                                    throw new RuntimeException("Unexpected uv format: " + attribute.type);
                                }
                            }

                            case BLEND_INDICES -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_8_UNSIGNED) {
                                    var w = vertexBuffer.get() & 0xFF;
                                    var x = vertexBuffer.get() & 0xFF;
                                    var y = vertexBuffer.get() & 0xFF;
                                    var z = vertexBuffer.get() & 0xFF;
                                    boneIds.add(new Vector4i(x, y, z, w));
                                } else {
                                    throw new RuntimeException("Unexpected bone idx format: " + attribute.type);
                                }
                            }

                            case BLEND_WEIGHTS -> {
                                if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_16_UNORM) {
                                    var w = ((float) (vertexBuffer.getShort() & 0xFFFF)) / 65535;
                                    var x = ((float) (vertexBuffer.getShort() & 0xFFFF)) / 65535;
                                    var y = ((float) (vertexBuffer.getShort() & 0xFFFF)) / 65535;
                                    var z = ((float) (vertexBuffer.getShort() & 0xFFFF)) / 65535;
                                    weights.add(new Vector4f(x, y, z, w));
                                } else {
                                    throw new RuntimeException("Unexpected bone weight format: " + attribute.type);
                                }
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

    private static Vector3f readRGBA16Float3(ByteBuffer buf) {
        var x = Model.halfFloatToFloat(buf.getShort()); // Ignored. Maybe padding?
        var y = Model.halfFloatToFloat(buf.getShort());
        var z = Model.halfFloatToFloat(buf.getShort());
        var w = Model.halfFloatToFloat(buf.getShort());
        return new Vector3f(x, y, z);
    }

    private static Vector4f readRGBA16Float4(ByteBuffer buf) {
        var x = Model.halfFloatToFloat(buf.getShort()); // Ignored. Maybe padding?
        var y = Model.halfFloatToFloat(buf.getShort());
        var z = Model.halfFloatToFloat(buf.getShort());
        var w = Model.halfFloatToFloat(buf.getShort());
        return new Vector4f(x, y, z, w);
    }

    @Override
    public void writeModel(Path path) {
        try {
            var sceneModel = new DefaultSceneModel();
            var sceneMaterials = new HashMap<Material, MaterialModelV2>();

            for (var value : materials.values()) {
                sceneMaterials.put(value, MaterialBuilder.create()
                        .setBaseColorTexture("file:///" + value.getTexture("BaseColorMap").filePath().replace("\\", "/"), "image/png", 0)
                        .setDoubleSided(true)
                        .build());
            }

            var skin = new DefaultSkinModel();
            skin.setSkeleton(skeleton.get(0));
            for (var jointNode : skeleton.subList(1, skeleton.size() - 1)) skin.addJoint(jointNode);

            var inverseBindMatrices = FloatBuffer.allocate(16 * skeleton.size());
            for (int i = 0; i < skeleton.size(); i++) {
                inverseBindMatrices
                        .put(1.0f).put(0.0f).put(0.0f).put(0.0f)
                        .put(0.0f).put(1.0f).put(0.0f).put(0.0f)
                        .put(0.0f).put(0.0f).put(1.0f).put(0.0f)
                        .put(0.0f).put(0.0f).put(0.0f).put(1.0f);
            }
            skin.setInverseBindMatrices(AccessorModels.create(GltfConstants.GL_FLOAT, "MAT4", false, Buffers.createByteBufferFrom(inverseBindMatrices)));


            for (var mesh : meshes) {
                var meshModel = new DefaultMeshModel();
                var meshPrimitiveModel = mesh.create().build();

                meshPrimitiveModel.setMaterialModel(sceneMaterials.get(mesh.material));
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);

                // Create a node with the mesh
                var nodeModel = new DefaultNodeModel();
                nodeModel.setName(mesh.name());
                nodeModel.addMeshModel(meshModel);
                nodeModel.setSkinModel(skin);
                sceneModel.addNode(nodeModel);
            }

            // Pass the scene to the model builder. It will take care
            // of the other model elements that are contained in the scene.
            // (I.e. the mesh primitive and its accessors, and the material
            // and its textures)
            var gltfModelBuilder = GltfModelBuilder.create();
            gltfModelBuilder.addSkinModel(skin);
            gltfModelBuilder.addSceneModel(sceneModel);
            var gltfModel = gltfModelBuilder.build();

            var gltfWriter = new GltfModelWriterV2();
            gltfWriter.writeBinary(gltfModel, Files.newOutputStream(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create %s".formatted(path), e);
        }
    }

    private record Material(
            String name,
            List<Texture> textures
    ) {

        public Texture getTexture(String type) {
            for (var texture : textures) if (texture.type.equals(type)) return texture;
            throw new RuntimeException("Texture of type " + type + " doesn't exist");
        }
    }

    private record Texture(
            String type,
            String filePath
    ) {
    }

    private record Mesh(
            String name,
            Material material,
            List<Integer> indices,
            List<Vector3f> positions,
            List<Vector3f> normals,
            List<Vector4f> tangents,
            List<Vector4f> weights,
            List<Vector4i> boneIds,
            List<Vector3f> biNormals,
            List<Vector2f> uvs
    ) {
        public MeshPrimitiveBuilder create() {
            return MeshPrimitiveBuilder.create()
                    .setIntIndicesAsShort(IntBuffer.wrap(indices.stream().mapToInt(Integer::intValue).toArray())) // TODO: make it use int buffer if needed
                    .addNormals3D(toBuffer3(normals))
                    .addTangents4D(toBuffer4(tangents))
                    .addAttribute("JOINTS_0", AccessorModels.create(GltfConstants.GL_UNSIGNED_SHORT, "VEC4", false, Buffers.createByteBufferFrom(toUShort4(boneIds))))
                    .addAttribute("WEIGHTS_0", AccessorModels.create(GltfConstants.GL_FLOAT, "VEC4", false, Buffers.createByteBufferFrom(toBuffer4(weights))))
                    .addTexCoords02D(toBuffer2(uvs))
                    .addPositions3D(toBuffer3(positions))
                    .setTriangles();
        }
    }

    private static Vector3f toVec3(Vec3 vec) {
        return new Vector3f(vec.x(), vec.y(), vec.z());
    }

    private static ShortBuffer toUShort4(List<Vector4i> list) {
        var buffer = ShortBuffer.wrap(new short[list.size() * 4]);
        for (var element : list)
            buffer
                    .put((short) (element.x & 65535))
                    .put((short) (element.y & 65535))
                    .put((short) (element.z & 65535))
                    .put((short) (element.w & 65535));

        return buffer.rewind();
    }


    private static FloatBuffer toBuffer4(List<Vector4f> list) {
        var buffer = FloatBuffer.wrap(new float[list.size() * 4]);
        for (var element : list)
            buffer
                    .put(element.x)
                    .put(element.y)
                    .put(element.z)
                    .put(element.w);

        return buffer.rewind();
    }

    private static FloatBuffer toBuffer3(List<Vector3f> list) {
        var buffer = FloatBuffer.wrap(new float[list.size() * 3]);
        for (var element : list)
            buffer
                    .put(element.x)
                    .put(element.y)
                    .put(element.z);

        return buffer.rewind();
    }

    private static FloatBuffer toBuffer2(List<Vector2f> list) {
        var buffer = FloatBuffer.wrap(new float[list.size() * 2]);
        for (var element : list)
            buffer
                    .put(element.x)
                    .put(element.y);

        return buffer.rewind();
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
