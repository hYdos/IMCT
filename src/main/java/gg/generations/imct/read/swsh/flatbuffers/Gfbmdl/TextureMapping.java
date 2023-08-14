// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.imct.read.swsh.flatbuffers.Gfbmdl;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class TextureMapping extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static TextureMapping getRootAsTextureMapping(ByteBuffer _bb) { return getRootAsTextureMapping(_bb, new TextureMapping()); }
  public static TextureMapping getRootAsTextureMapping(ByteBuffer _bb, TextureMapping obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public TextureMapping __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public long unknown1() { int o = __offset(4); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long wrapModeX() { int o = __offset(6); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long wrapModeY() { int o = __offset(8); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long wrapModeZ() { int o = __offset(10); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public float unknown5() { int o = __offset(12); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  public float unknown6() { int o = __offset(14); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  public float unknown7() { int o = __offset(16); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  public float unknown8() { int o = __offset(18); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  public float lodBias() { int o = __offset(20); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }

  public static int createTextureMapping(FlatBufferBuilder builder,
      long unknown1,
      long wrapModeX,
      long wrapModeY,
      long wrapModeZ,
      float unknown5,
      float unknown6,
      float unknown7,
      float unknown8,
      float lodBias) {
    builder.startTable(9);
    TextureMapping.addLodBias(builder, lodBias);
    TextureMapping.addUnknown8(builder, unknown8);
    TextureMapping.addUnknown7(builder, unknown7);
    TextureMapping.addUnknown6(builder, unknown6);
    TextureMapping.addUnknown5(builder, unknown5);
    TextureMapping.addWrapModeZ(builder, wrapModeZ);
    TextureMapping.addWrapModeY(builder, wrapModeY);
    TextureMapping.addWrapModeX(builder, wrapModeX);
    TextureMapping.addUnknown1(builder, unknown1);
    return TextureMapping.endTextureMapping(builder);
  }

  public static void startTextureMapping(FlatBufferBuilder builder) { builder.startTable(9); }
  public static void addUnknown1(FlatBufferBuilder builder, long unknown1) { builder.addInt(0, (int) unknown1, (int) 0L); }
  public static void addWrapModeX(FlatBufferBuilder builder, long wrapModeX) { builder.addInt(1, (int) wrapModeX, (int) 0L); }
  public static void addWrapModeY(FlatBufferBuilder builder, long wrapModeY) { builder.addInt(2, (int) wrapModeY, (int) 0L); }
  public static void addWrapModeZ(FlatBufferBuilder builder, long wrapModeZ) { builder.addInt(3, (int) wrapModeZ, (int) 0L); }
  public static void addUnknown5(FlatBufferBuilder builder, float unknown5) { builder.addFloat(4, unknown5, 0.0f); }
  public static void addUnknown6(FlatBufferBuilder builder, float unknown6) { builder.addFloat(5, unknown6, 0.0f); }
  public static void addUnknown7(FlatBufferBuilder builder, float unknown7) { builder.addFloat(6, unknown7, 0.0f); }
  public static void addUnknown8(FlatBufferBuilder builder, float unknown8) { builder.addFloat(7, unknown8, 0.0f); }
  public static void addLodBias(FlatBufferBuilder builder, float lodBias) { builder.addFloat(8, lodBias, 0.0f); }
  public static int endTextureMapping(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public TextureMapping get(int j) { return get(new TextureMapping(), j); }
    public TextureMapping get(TextureMapping obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

