package gg.generations.imct.write;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.flatbuffers.FlatBufferBuilder;
import gg.generations.imct.api.Model;
import gg.generations.imct.read.scvi.flatbuffers.Titan.Model.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class SVWriter {
    // TODO: TEMPORARY. ONLY FOR CHECKING TYPES
    private static final TRMDL tmp = null;

    public static void write(Model model, Path path, SVExportSettings settings) {
        try {
            Files.createDirectories(path);
            var commonName = path.getParent().getFileName().toString();

            Files.write(path.resolve(commonName + ".trmdl"), new ByteBufferBackedInputStream(createTRMDL(commonName, settings)).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create SV model", e);
        }
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
        var lodIndexes = Lod.createIndexVector(builder, new int[] {lodIndexOffset});
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
}
