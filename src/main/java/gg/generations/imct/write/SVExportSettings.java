package gg.generations.imct.write;

import gg.generations.imct.api.AttributeType;
import org.joml.Vector3f;

import java.util.List;

public record SVExportSettings(
        Vector3f minBounds,
        Vector3f maxBounds,
        List<AttributeType> layout
) {

    public SVExportSettings() {
        this(
                new Vector3f(-1, -1, -1),
                new Vector3f(1, 1, 1),
                List.of(AttributeType.POSITION, AttributeType.NORMAL, AttributeType.TEXCOORD, AttributeType.BLEND_INDICES, AttributeType.BLEND_WEIGHTS)
        );
    }
}
