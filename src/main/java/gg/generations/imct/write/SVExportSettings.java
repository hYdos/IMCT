package gg.generations.imct.write;

import org.joml.Vector3f;

public record SVExportSettings(
        Vector3f minBounds,
        Vector3f maxBounds
) {

    public SVExportSettings() {
        this(
                new Vector3f(-1, -1, -1),
                new Vector3f(1, 1, 1)
        );
    }
}
