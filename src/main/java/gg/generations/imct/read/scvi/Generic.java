package gg.generations.imct.read.scvi;

import java.util.List;
import java.util.Map;

public record Generic(Map<String, MaterialProperty> materialProperties, List<Map<String, SVModel.Material>> materials) {
}
