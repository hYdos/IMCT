package gg.generations.imct.api;

public enum AttributeType {
    // Normal is here on S/V so ordinal + 1 recreates the id's
    POSITION,
    NORMAL,
    TANGENT,
    BINORMAL,
    COLOR,
    TEXCOORD,
    BLEND_INDICES,
    BLEND_WEIGHTS
}