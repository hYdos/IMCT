package gg.generations.imct.read.scvi;

import java.util.Map;

public record MaterialProperty(SVModel.TrackConfig config, Map<String, Map<String, SVModel.MaterialTrack>> tracks,
                               java.util.HashMap<String, Map<String, java.util.List<String>>> mappers) {
}
