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
            for (int j = 0; j < idxBuffer.bufferLength() / idxLayout.size; j++) {
                switch (idxLayout) {
                    case UINT16 -> indices.add(realIdxBuffer.getShort() & 0xFFFF);
                    default -> throw new RuntimeException("no");
                }
            }

            var vertexCount = info.attributes(0).size(0).size();

            for (int j = 0; j < vertexCount; j++) {
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

                        case NORMAL -> {
                            if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_16_FLOAT) {
                                normals.add(readW16X16Y16Z16Float(vertexBuffer));
                            } else {
                                throw new RuntimeException("Unexpected normal format: " + attribute.type);
                            }
                        }

                        case TANGENT -> {
                            if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_16_FLOAT) {
                                tangents.add(readW16X16Y16Z16Float(vertexBuffer));
                            } else {
                                throw new RuntimeException("Unexpected tangent format: " + attribute.type);
                            }
                        }

                        case TEXCOORD -> {
                            if (Objects.requireNonNull(attribute.size) == AttributeSize.RG_32_FLOAT) {
                                var x = vertexBuffer.getFloat();
                                var y = vertexBuffer.getFloat();
                                uvs.add(new Vector2f(x, y));
                            } else {
                                throw new RuntimeException("Unexpected uv format: " + attribute.type);
                            }
                        }

                        case BLEND_INDICES -> {
                            if (Objects.requireNonNull(attribute.size) == AttributeSize.RGBA_8_UNSIGNED) {
                                readW16X16Y16Z16Float(vertexBuffer);
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

            meshes.add(new Mesh(indices, positions, normals, tangents, binormals, uvs));
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
            List<Integer> indices,
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
