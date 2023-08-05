package gg.generations.imct.scvi.flatbuffers.Titan.Model;

import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.creation.MaterialBuilder;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.io.v2.GltfModelWriterV2;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import gg.generations.imct.intermediate.Model;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SVModel implements Model {

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

        // Process extra material variants (shiny)
        //var extraMaterials = TRMMT.getRootAsTRMMT(read(modelDir.resolve(modelDir.getFileName() + ".trmmt"))).material(0);

        // Process material data
        var material = TRMTR.getRootAsTRMTR(read(modelDir.resolve(Objects.requireNonNull(trmdl.materials(0), "Material name was null"))));
        for (int i = 0; i < material.materialsLength(); i++) {
            var rawMaterial = material.materials(i);
            var textures = new ArrayList<Texture>();
            var materialName = rawMaterial.name();
            var colorProperties = new HashMap<String, Vector4i>();
            var floatProperties = new HashMap<String, Float>();
            var intProperties = new HashMap<String, Integer>();
            var shader = rawMaterial.shaders(0).shaderName();

            for (int j = 0; j < rawMaterial.float4ParameterLength(); j++) {
                var colorParam = rawMaterial.float4Parameter(j);

                colorProperties.put(colorParam.colorName(), new Vector4i(
                        linearToNonLinearColor(colorParam.colorValue().r()),
                        linearToNonLinearColor(colorParam.colorValue().g()),
                        linearToNonLinearColor(colorParam.colorValue().b()),
                        linearToNonLinearColor(colorParam.colorValue().a())));
            }

            for (int j = 0; j < rawMaterial.floatParameterLength(); j++) {
                var floatParam = rawMaterial.floatParameter(j);

                floatProperties.put(floatParam.floatName(), floatParam.floatValue());
            }

            for (int j = 0; j < rawMaterial.intParameterLength(); j++) {
                var intParam = rawMaterial.intParameter(j);

                intProperties.put(intParam.intName(), intParam.intValue());
            }

            for (int j = 0; j < rawMaterial.texturesLength(); j++) {
                var rawTexture = rawMaterial.textures(j);
                textures.add(new Texture(rawTexture.textureName(), modelDir.resolve(rawTexture.textureFile().replace(".bntx", ".png")).toAbsolutePath().toString()));
            }

            materials.put(materialName, new Material(
                    materialName,
                    textures,
                    colorProperties
            ));
        }

        var display = new Vector4iDisplay(materials.get("l_eye").colors);
        display.setVisible(true);
        display.saveDisplayAsImage("display.png");

        EyeTextureGenerator.generate(materials.get("l_eye"));

        // Process mesh data
        for (var i = 0; i < meshInfo.size(); i++) {
            System.out.println("Processing Mesh " + i);
            var info = meshInfo.get(i).meshes(0);
            var idxLayout = IndexLayout.get((int) info.polygonType());
            var rawAttributes = info.attributes(0);
            var data = meshData.get(i).buffers(0);
            var vertexBuffer = data.vertexBuffer(0).bufferAsByteBuffer();
            var idxBuffer = data.indexBuffer(0);

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
            var tangents = new ArrayList<Vector3f>();
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
                                normals.add(readRGBA16Float(vertexBuffer));
                            } else {
                                throw new RuntimeException("Unexpected normal format: " + attribute.type);
                            }
                        }

                        case TANGENT -> {
                            if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_16_FLOAT) {
                                tangents.add(readRGBA16Float(vertexBuffer));
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
                                var w = vertexBuffer.get();
                                var x = vertexBuffer.get();
                                var y = vertexBuffer.get();
                                var z = vertexBuffer.get();
                                // TODO: add these
                            } else {
                                throw new RuntimeException("Unexpected bone idx format: " + attribute.type);
                            }
                        }

                        case BLEND_WEIGHTS -> {
                            if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_16_UNORM) {
                                var w = vertexBuffer.getShort();
                                var x = vertexBuffer.getShort();
                                var y = vertexBuffer.getShort();
                                var z = vertexBuffer.getShort();
                                // TODO: add these
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
                    meshes.add(new Mesh(info.meshName() + "_" + subMesh.materialName(), materials.get(subMesh.materialName()), subIdxBuffer, positions, normals, tangents, binormals, uvs));
            }
        }
    }

    private static Vector3f readRGBA16Float(ByteBuffer buf) {
        var x = Model.halfFloatToFloat(buf.getShort()); // Ignored. Maybe padding?
        var y = Model.halfFloatToFloat(buf.getShort());
        var z = Model.halfFloatToFloat(buf.getShort());
        var w = Model.halfFloatToFloat(buf.getShort());
        return new Vector3f(x, y, z);
    }

    @Override
    public void writeModel(Path path) {
        try {
            var sceneModel = new DefaultSceneModel();
            var sceneMaterials = new HashMap<Material, MaterialModelV2>();

            for (var value : materials.values()) {
                sceneMaterials.put(value, MaterialBuilder.create()
                        .setBaseColorTexture("file:///" + value.getTexture("BaseColorMap").filePath().replace("\\", "/").replace(" ", "%20"), "image/png", 0)
                        .setDoubleSided(true)
                        .build());
            }

            for (var mesh : meshes) {
                var meshModel = new DefaultMeshModel();
                var meshPrimitiveModel = mesh.create().build();

                meshPrimitiveModel.setMaterialModel(sceneMaterials.get(mesh.material));
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);

                // Create a node with the mesh
                var nodeModel = new DefaultNodeModel();
                nodeModel.setName(mesh.name());
                nodeModel.addMeshModel(meshModel);
                sceneModel.addNode(nodeModel);
            }

            // Pass the scene to the model builder. It will take care
            // of the other model elements that are contained in the scene.
            // (I.e. the mesh primitive and its accessors, and the material
            // and its textures)
            var gltfModelBuilder = GltfModelBuilder.create();
            gltfModelBuilder.addSceneModel(sceneModel);
            var gltfModel = gltfModelBuilder.build();

            var gltfWriter = new GltfModelWriterV2();
            gltfWriter.writeBinary(gltfModel, Files.newOutputStream(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create %s".formatted(path), e);
        }
    }

    public record Material(
            String name,
            List<Texture> textures,
            Map<String, Vector4i> colors
    ) {
        public Texture getTexture(String type) {
            for (var texture : textures) if (texture.type.equals(type)) return texture;
            throw new RuntimeException("Texture of type " + type + " doesn't exist");
        }
    }

    public record Texture(
            String type,
            String filePath
    ) {
        public BufferedImage getBufferedImage() {
            try {
                return ImageIO.read(new File(filePath));
            } catch (IOException e) {
                return null;
            }
        }
    }

    private record Mesh(
            String name,
            Material material,
            List<Integer> indices,
            List<Vector3f> positions,
            List<Vector3f> normals,
            List<Vector3f> tangents,
            List<Vector3f> biNormals,
            List<Vector2f> uvs
    ) {
        public MeshPrimitiveBuilder create() {
            return MeshPrimitiveBuilder.create()
                    .setIntIndicesAsShort(IntBuffer.wrap(indices.stream().mapToInt(Integer::intValue).toArray())) // TODO: make it use int buffer if needed
                    .addNormals3D(toBuffer3(normals))
                    .addTexCoords02D(toBuffer2(uvs))
                    .addPositions3D(toBuffer3(positions))
                    .setTriangles();
        }
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

    private int linearToNonLinearColor(float color) {
        return (int) (Math.ceil(Math.pow(color, 1/2.25530545664) * 255));
    }
}
