package gg.generations.imct.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record ApiMaterial(
        String name,
        List<ApiTexture> textures,
        Map<String, Object> properties
) {

    public ApiTexture getTexture(String type) {
        for (var texture : textures) {
            if (texture != null && texture.type().endsWith(type)) return texture;
        }
        return null;
//        throw new RuntimeException("Texture of type " + type + " doesn't exist");
    }

    public String getShader() {
        return (String) properties.get("shader");
    }

    public ApiMaterial with(Map<String, String> map) {
        var newTextures = new ArrayList<ApiTexture>();

        textures().forEach(apiTexture -> {
            var t = map.get(apiTexture.filePath());
            if(t != null) {
                newTextures.add(new ApiTexture(apiTexture.type(), t));
            }
        });

        return newTextures.isEmpty() ? null : new ApiMaterial(name, newTextures, properties);
    }
}