package gg.generations.imct.read;

import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import dev.thecodewarrior.binarysmd.studiomdl.NodesBlock;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import dev.thecodewarrior.binarysmd.studiomdl.TrianglesBlock;
import gg.generations.imct.api.Model;
import gg.generations.rarecandy.renderer.rendering.Bone;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SmdModel extends Model {
    public SmdModel(Path modelDir, Path targetDir) throws IOException {
        var smdFile = new SMDTextReader().read(Files.readString(modelDir));

        NodesBlock nodesBlock = null;
        SkeletonBlock skeletonBlock = null;
        TrianglesBlock trianglesBlock = null;

        for(var block : smdFile.blocks) {
            if(block instanceof NodesBlock nodes) nodesBlock = nodes;
            if(block instanceof SkeletonBlock skeleton1) skeletonBlock = skeleton1;
            if(block instanceof TrianglesBlock triangles) trianglesBlock = triangles;
        }

        if(nodesBlock == null || skeletonBlock == null || trianglesBlock == null) return;

        var hasImplictBone = nodesBlock.bones.get(0).name.equals("blender_implicit");


        if(hasImplictBone) {
            stripImplicit(nodesBlock, skeletonBlock, trianglesBlock);
        }

        for (int i = (hasImplictBone ? 1 : 0); i < nodesBlock.bones.size(); i++) {
            var smdNode = nodesBlock.bones.get(i);

            record Bone(
                    String name,
                    Vector3f translation,
                    Quaternionf rotation,
                    Vector3f scale,
                    int parent
            ) {
            }



        }

    }

    private void stripImplicit(NodesBlock nodesBlock, SkeletonBlock skeletonBlock, TrianglesBlock trianglesBlock) {
        nodesBlock.bones.remove(0);

        for (var node : nodesBlock.bones) {
            node.id -= 1;
            node.parent -= 1;
        }

        for(var keyframe : skeletonBlock.keyframes) {
            keyframe.states.remove(0);

            for(var state : keyframe.states) {
                state.bone -= 1;
            }
        }

        for(var triangle : trianglesBlock.triangles) {
            for(var vertex : triangle.vertices) {
                vertex.parentBone -= 1;
                vertex.links.removeIf(a -> a.bone == 0);


            }
        }

    }
}
