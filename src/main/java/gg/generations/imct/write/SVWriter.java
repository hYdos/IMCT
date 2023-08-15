package gg.generations.imct.write;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.flatbuffers.FlatBufferBuilder;
import gg.generations.imct.api.AttributeType;
import gg.generations.imct.api.Mesh;
import gg.generations.imct.api.Model;
import gg.generations.imct.read.scvi.SVModel;
import gg.generations.imct.read.scvi.flatbuffers.Titan.Model.*;
import gg.generations.imct.util.TrinityUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SVWriter {
    private static final int HALF_FLOAT_BYTES = Short.BYTES;
    // TODO: TEMPORARY. ONLY FOR CHECKING TYPES
    private static final TRMBF tmp = null;

    @SuppressWarnings("resource")
    public static void write(Model model, Path path, SVExportSettings settings) {
        try {
            Files.createDirectories(path);
            var commonName = path.getFileName().toString();
            var meshData = generateMeshData(model, settings);
            Files.write(path.resolve(commonName + ".trmdl"), new ByteBufferBackedInputStream(createTRMDL(commonName, settings)).readAllBytes());
            Files.write(path.resolve(commonName + ".trmsh"), new ByteBufferBackedInputStream(createTRMSH(commonName, meshData, model, settings)).readAllBytes());
            Files.write(path.resolve(commonName + ".trmbf"), new ByteBufferBackedInputStream(createTRMBF(meshData)).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create SV model", e);
        }
    }

    private static MeshData generateMeshData(Model model, SVExportSettings settings) {
        var vertexStride = calculateStride(settings);
        var totalVertexCount = model.meshes.stream()
                .mapToInt(mesh -> mesh.positions().size())
                .sum();
        var vertexBuffer = ByteBuffer.allocate(totalVertexCount * vertexStride).order(ByteOrder.LITTLE_ENDIAN);
        var idxBuffer = new ArrayList<Integer>();
        var meshOffsetMap = new HashMap<Mesh, Integer>();

        for (var mesh : model.meshes) {
            meshOffsetMap.put(mesh, idxBuffer.size());
            idxBuffer.addAll(mesh.indices());

            for (var vertexId = 0; vertexId < mesh.positions().size(); vertexId++) {
                for (var attrib : settings.layout()) {
                    switch (attrib) {
                        case POSITION -> vertexBuffer.putFloat(mesh.positions().get(vertexId).x())
                                .putFloat(mesh.positions().get(vertexId).y())
                                .putFloat(mesh.positions().get(vertexId).z());
                        case NORMAL ->
                                vertexBuffer.putShort(TrinityUtils.writeHalfFloat(mesh.normals().get(vertexId).x()))
                                        .putShort(TrinityUtils.writeHalfFloat(mesh.positions().get(vertexId).y()))
                                        .putShort(TrinityUtils.writeHalfFloat(mesh.positions().get(vertexId).z()));
                        case TEXCOORD -> vertexBuffer.putFloat(mesh.uvs().get(vertexId).x())
                                .putFloat(mesh.uvs().get(vertexId).y());
                        case BLEND_INDICES -> vertexBuffer.put((byte) (mesh.boneIds().get(vertexId).x() & 0xFF))
                                .put((byte) (mesh.boneIds().get(vertexId).y() & 0xFF))
                                .put((byte) (mesh.boneIds().get(vertexId).z() & 0xFF))
                                .put((byte) (mesh.boneIds().get(vertexId).w() & 0xFF));
                        case BLEND_WEIGHTS ->
                                vertexBuffer.putShort((short) Math.round(mesh.weights().get(vertexId).x() * 65535))
                                        .putShort((short) Math.round(mesh.weights().get(vertexId).y() * 65535))
                                        .putShort((short) Math.round(mesh.weights().get(vertexId).z() * 65535))
                                        .putShort((short) Math.round(mesh.weights().get(vertexId).w() * 65535));
                        default -> throw new RuntimeException("Couldn't write Attribute: " + attrib);
                    }
                }
            }
        }

        return new MeshData(
                vertexBuffer.flip(),
                idxBuffer,
                vertexStride,
                meshOffsetMap
        );
    }

    private static ByteBuffer createTRMBF(MeshData data) {
        var builder = new FlatBufferBuilder(1);

        // Index Buffer
        var rawIndexBuffer = ByteBuffer.allocate(Short.BYTES * data.indexBuffer.size()); // hope its below Unsigned Short max. TODO: unhardcode
        for (var i : data.indexBuffer) rawIndexBuffer.putShort((short) (i & 0xFFFF));

        var morphsOffset = Buffer.createMorphsVector(builder, new int[] {Morphs.createMorphs(builder, MorphBuffer.createBufferVector(builder, new byte[0]))});
        var idxBufferOffset = Buffer.createIndexBufferVector(builder, new int[] {Indexes.createIndexes(builder, builder.createByteVector(rawIndexBuffer))});
        var vertexBufferOffset = Buffer.createVertexBufferVector(builder, new int[]{Vertices.createVertices(builder, builder.createByteVector(data.vertexBuffer()))});
        var buffersOffset = TRMBF.createBuffersVector(builder, new int[]{Buffer.createBuffer(builder, idxBufferOffset, vertexBufferOffset, morphsOffset)});

        var trmbf = TRMBF.createTRMBF(builder, 0, buffersOffset);
        TRMBF.finishTRMBFBuffer(builder, trmbf);
        return builder.dataBuffer().duplicate();
    }

    private static ByteBuffer createTRMSH(String commonName, MeshData data, Model model, SVExportSettings settings) {
        var builder = new FlatBufferBuilder(1);
        var meshBufferFileNameOffset = builder.createString(commonName + ".trmbf");

        var meshes = new ArrayList<Integer>();

        for (var mesh : model.meshes) {
            var materialsOffset = MeshShape.createMaterialsVector(builder, new int[]{MaterialInfo.createMaterialInfo(builder, mesh.positions().size(), data.meshOffsetMap.get(mesh), 0, builder.createString(mesh.material().name()), -1)});
            var attributesOffset = MeshShape.createAttributesVector(builder, new int[]{createSVAttributes(builder, data.vertexStride, settings.layout())});
            var influenceOffset = MeshShape.createInfluenceVector(builder, new int[0]);
            var visShapesOffset = MeshShape.createVisShapesVector(builder, new int[0]);
            var morphShapeOffset = MeshShape.createMorphShapeVector(builder, new int[0]);

            BoundingBox.startBoundingBox(builder);
            BoundingBox.addMin(builder, Vec3.createVec3(builder, -1, -1, -1));
            BoundingBox.addMax(builder, Vec3.createVec3(builder, 1, 1, 1));
            var bbOffset = BoundingBox.endBoundingBox(builder);

            var meshNameOffset = builder.createString(mesh.name());
            var meshShapeNameOffset = builder.createString(mesh.name() + "_shape");

            MeshShape.startMeshShape(builder);
            MeshShape.addMeshName(builder, meshNameOffset);
            MeshShape.addMeshShapeName(builder, meshShapeNameOffset);
            MeshShape.addMaterials(builder, materialsOffset);
            MeshShape.addAttributes(builder, attributesOffset);
            MeshShape.addInfluence(builder, influenceOffset); // NOTE: S/V needs this for moving or something. maybe fill it with junk data. Something about deform in blender
            MeshShape.addVisShapes(builder, visShapesOffset);
            MeshShape.addMorphShape(builder, morphShapeOffset);
            MeshShape.addBounds(builder, bbOffset);
            MeshShape.addClipSphere(builder, Sphere.createSphere(builder, 0, 0, 0, 2));
            MeshShape.addPolygonType(builder, SVModel.IndexLayout.UINT16.ordinal());
            MeshShape.addRes0(builder, 0);
            MeshShape.addRes1(builder, 0);
            MeshShape.addRes2(builder, 0);
            MeshShape.addRes3(builder, 0);
            MeshShape.addUnk13(builder, 0);
            meshes.add(MeshShape.endMeshShape(builder));
        }
        var meshesOffset = TRMSH.createMeshesVector(builder, meshes.stream().mapToInt(value -> value).toArray());

        var trmshOffset = TRMSH.createTRMSH(builder, 0, meshesOffset, meshBufferFileNameOffset);
        TRMSH.finishTRMSHBuffer(builder, trmshOffset);
        return builder.dataBuffer().duplicate();
    }

    private static ByteBuffer createTRMDL(String commonName, SVExportSettings settings) {
        var builder = new FlatBufferBuilder(1);

        // Bounds
        Bounds.startBounds(builder);
        Bounds.addMin(builder, Vec3.createVec3(builder, settings.minBounds().x(), settings.minBounds().y(), settings.minBounds().z()));
        Bounds.addMax(builder, Vec3.createVec3(builder, settings.minBounds().x(), settings.minBounds().y(), settings.minBounds().z()));
        var boundsOffset = Bounds.endBounds(builder);

        // Lods (Blank if possible for now)
        var lodIndexOffset = LodIndex.createLodIndex(builder, 0); // unk0 is obv the quality index???
        var lodIndexes = Lod.createIndexVector(builder, new int[]{lodIndexOffset});
        var lodTypeOffset = builder.createString("Custom");
        var lodOffset = Lod.createLod(builder, lodIndexes, lodTypeOffset);
        var lodVector = TRMDL.createLodsVector(builder, new int[]{lodOffset});

        // Materials TODO: material sets like rare
        var materialFileOffset = builder.createString(commonName + ".trmtl");
        var materialsVector = TRMDL.createMaterialsVector(builder, new int[]{materialFileOffset});

        // Skeleton
        var fileNameOffset = builder.createString(commonName + ".trskl");
        var skeletonOffset = trskeleton.createtrskeleton(builder, fileNameOffset);

        // Meshes
        fileNameOffset = builder.createString(commonName + ".trmsh");
        var meshFileOffset = trmeshes.createtrmeshes(builder, fileNameOffset);
        var meshesVector = TRMDL.createMeshesVector(builder, new int[]{meshFileOffset});

        TRMDL.startTRMDL(builder);
        TRMDL.addUnk0(builder, 0);
        TRMDL.addMeshes(builder, meshesVector);
        TRMDL.addSkeleton(builder, skeletonOffset);
        TRMDL.addMaterials(builder, materialsVector);
        TRMDL.addLods(builder, lodVector);
        TRMDL.addBounds(builder, boundsOffset);
        TRMDL.addUnkVec(builder, Vec4.createVec4(builder, 0, 0, 0, 0));
        TRMDL.addUnk7(builder, 0);
        TRMDL.addUnk8(builder, 0);
        TRMDL.addUnk9(builder, 2);
        var trmdlOffset = TRMDL.endTRMDL(builder);
        TRMDL.finishTRMDLBuffer(builder, trmdlOffset);

        return builder.dataBuffer().duplicate();
    }

    private static int createSVAttributes(FlatBufferBuilder builder, int stride, List<AttributeType> layout) {
        var svAttribs = new ArrayList<Integer>();
        for (var i = 0; i < layout.size(); i++) {
            var attrib = layout.get(i);
            var type = switch (attrib) {
                case POSITION, BINORMAL -> SVModel.AttributeSize.RGB_32_FLOAT.id;
                case NORMAL, TANGENT -> SVModel.AttributeSize.RGBA_16_FLOAT.id;
                case COLOR -> SVModel.AttributeSize.RGBA_8_UNORM.id;
                case TEXCOORD -> SVModel.AttributeSize.RG_32_FLOAT.id;
                case BLEND_INDICES -> SVModel.AttributeSize.RGBA_8_UNSIGNED.id;
                case BLEND_WEIGHTS -> SVModel.AttributeSize.RGBA_16_UNORM.id;
            };

            svAttribs.add(VertexAccessor.createVertexAccessor(builder, -1, attrib.ordinal() + 1, 0, type, i));
        }

        var attrsOffset = VertexAccessors.createAttrsVector(builder, svAttribs.stream().mapToInt(value -> value).toArray());
        var sizeOffset = VertexAccessors.createAttrsVector(builder, new int[]{VertexSize.createVertexSize(builder, stride)});
        return VertexAccessors.createVertexAccessors(builder, attrsOffset, sizeOffset);
    }

    private static int calculateStride(SVExportSettings settings) {
        var vertexSize = 0;
        for (var attrib : settings.layout()) {
            switch (attrib) {
                case POSITION -> vertexSize += Float.BYTES * 3;
                case NORMAL -> vertexSize += HALF_FLOAT_BYTES * 3;
                case TEXCOORD -> vertexSize += Float.BYTES * 2;
                case COLOR, BLEND_INDICES -> vertexSize += Byte.BYTES * 4;
                case BLEND_WEIGHTS -> vertexSize += Short.BYTES * 4;
                default -> throw new RuntimeException("Unsupported Attribute: " + attrib);
            }
        }

        return vertexSize;
    }

    private record MeshData(
            ByteBuffer vertexBuffer,
            List<Integer> indexBuffer,
            int vertexStride,
            HashMap<Mesh, Integer> meshOffsetMap
    ) {
    }
}
