package gg.generations.imct.scvi.flatbuffers.Titan.Model;

import gg.generations.imct.intermediate.Model;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SVModel implements Model {

    private final List<Mesh> meshes = new ArrayList<>();
    private final List<TRMTR> materials = new ArrayList<>();

    @SuppressWarnings("PointlessBitwiseExpression")
    public SVModel(Path modelDir) {
        // Read Data
        var meshInfo = new ArrayList<TRMSH>();
        var meshData = new ArrayList<TRMBF>();
        var materials = new ArrayList<TRMTR>();
        var trmdl = TRMDL.getRootAsTRMDL(read(modelDir.resolve(modelDir.getFileName() + ".trmdl")));
        var trskl = TRSKL.getRootAsTRSKL(read(modelDir.resolve(modelDir.getFileName() + ".trskl")));

        for (var i = 0; i < trmdl.meshesLength(); i++) {
            var meshName = Objects.requireNonNull(trmdl.meshes(i).filename(), "Mesh name was null");
            var meshI = TRMSH.getRootAsTRMSH(read(modelDir.resolve(meshName)));
            var meshD = TRMBF.getRootAsTRMBF(read(modelDir.resolve(meshName.replace(".trmsh", ".trmbf"))));
            meshInfo.add(meshI);
            meshData.add(meshD);
        }

        for (var i = 0; i < trmdl.materialsLength(); i++) {
            var material = TRMTR.getRootAsTRMTR(read(modelDir.resolve(Objects.requireNonNull(trmdl.materials(i), "Material name was null"))));
            materials.add(material);
        }

        // Process data
        for (var i = 0; i < meshInfo.size(); i++) {
            System.out.println("Processing Mesh " + i);
            var rawAttributes = meshInfo.get(i).meshes(0).attributes(0);
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

            var positions = new ArrayList<Vector3f>();
            var normals = new ArrayList<Vector3f>();
            var tangents = new ArrayList<Vector3f>();
            var binormals = new ArrayList<Vector3f>();
            var uvs = new ArrayList<Vector2f>();

            for (var attribute : attributes) {
                switch (attribute.type) {
                    case POSITION -> {
                        if (Objects.requireNonNull(attribute.size) == AttributeSize.X32_Y32_Z32_FLOAT) {
                            var x = vertexBuffer.getFloat();
                            var y = vertexBuffer.getFloat();
                            var z = vertexBuffer.getFloat();
                            positions.add(new Vector3f(x, y, z));
                        } else {
                            throw new RuntimeException("Unexpected position format: " + attribute.type);
                        }
                    }

                    case NORMAL -> {
                        if (Objects.requireNonNull(attribute.size) == AttributeSize.W16_X16_Y16_Z16_FLOAT) {
                            normals.add(readW16X16Y16Z16Float(vertexBuffer));
                        } else {
                            throw new RuntimeException("Unexpected normal format: " + attribute.type);
                        }
                    }

                    case TANGENT -> {
                        if (Objects.requireNonNull(attribute.size) == AttributeSize.W16_X16_Y16_Z16_FLOAT) {
                            tangents.add(readW16X16Y16Z16Float(vertexBuffer));
                        } else {
                            throw new RuntimeException("Unexpected tangent format: " + attribute.type);
                        }
                    }

                    case TEXCOORD -> {
                        if (Objects.requireNonNull(attribute.size) == AttributeSize.X32_Y32_FLOAT) {
                            var x = vertexBuffer.getFloat();
                            var y = vertexBuffer.getFloat();
                            uvs.add(new Vector2f(x, y));
                        } else {
                            throw new RuntimeException("Unexpected uv format: " + attribute.type);
                        }
                    }

                    case BLEND_INDICES -> {
                        if (Objects.requireNonNull(attribute.size) == AttributeSize.W8_X8_Y8_Z8_UNSIGNED) {
                            var w = vertexBuffer.getInt() & 0xFFFFFFFF;
                            var x = vertexBuffer.getInt() & 0xFFFFFFFF;
                            var y = vertexBuffer.getInt() & 0xFFFFFFFF;
                            var z = vertexBuffer.getInt() & 0xFFFFFFFF;
                            // TODO: add these
                        } else {
                            throw new RuntimeException("Unexpected bone idx format: " + attribute.type);
                        }
                    }

                    case BLEND_WEIGHTS -> {
                        if (Objects.requireNonNull(attribute.size) == AttributeSize.W16_X16_Y16_Z16_SIGNED_NORMALIZED) {
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

            meshes.add(new Mesh(positions, normals, tangents, binormals, uvs));
        }
    }

    private static Vector3f readW16X16Y16Z16Float(ByteBuffer buf) {
        var w = Model.halfFloatToFloat(buf.getShort()); // Ignored. Maybe padding?
        var x = Model.halfFloatToFloat(buf.getShort());
        var y = Model.halfFloatToFloat(buf.getShort());
        var z = Model.halfFloatToFloat(buf.getShort());
        return new Vector3f(x, y, z);
    }

    @Override
    public void writeModel(Path path) {
        throw new RuntimeException("Unimplemented");
    }

    private record Mesh(
            List<Vector3f> positions,
            List<Vector3f> normals,
            List<Vector3f> tangents,
            List<Vector3f> biNormals,
            List<Vector2f> uvs
    ) {
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
            for (int i = 0; i < values().length; i++) if (i == id) return values()[i];
            throw new RuntimeException("Unknown Attribute Type " + id);
        }
    }

    private enum AttributeSize {
        NONE(0),
        R8_G8_B8_A8_UNSIGNED_NORMALIZED(20),
        W8_X8_Y8_Z8_UNSIGNED(22),
        X32_UINT(36),
        X32_INT(37),
        W16_X16_Y16_Z16_SIGNED_NORMALIZED(39),
        W16_X16_Y16_Z16_FLOAT(43),
        X32_Y32_FLOAT(48),
        X32_Y32_Z32_FLOAT(51),
        W32_X32_Y32_Z32_FLOAT(54);

        private final int id;

        AttributeSize(int id) {
            this.id = id;
        }

        public static AttributeSize get(long id) {
            for (var value : values()) if (value.id == id) return value;
            throw new RuntimeException("Unknown Attribute Size " + id);
        }
    }
}
