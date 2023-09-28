package gg.generations.imct.write;

import java.util.Map;

public record ModelConfig(float scale, Map<String, String> materials, Map<String, VariantReference> defaultVariant, Map<String, Map<String, VariantReference>> variants) {
    public record MaterialReference(String texture, String type) {}
    public record VariantReference(String material) {}
}
