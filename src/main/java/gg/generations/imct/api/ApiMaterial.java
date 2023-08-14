package gg.generations.imct.api;

import java.util.List;
import java.util.Map;

public record ApiMaterial(
        String name,
        List<ApiTexture> textures,
        Map<String, Object> properties
) {

    public ApiTexture getTexture(String type) {
        for (var texture : textures) if (texture.type().endsWith(type)) return texture;
        throw new RuntimeException("Texture of type " + type + " doesn't exist");
    }
}