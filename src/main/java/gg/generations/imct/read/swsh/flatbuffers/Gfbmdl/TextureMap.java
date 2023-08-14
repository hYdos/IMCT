// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.imct.read.swsh.flatbuffers.Gfbmdl;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class TextureMap extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static TextureMap getRootAsTextureMap(ByteBuffer _bb) { return getRootAsTextureMap(_bb, new TextureMap()); }
  public static TextureMap getRootAsTextureMap(ByteBuffer _bb, TextureMap obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public TextureMap __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String sampler() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer samplerAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer samplerInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public int index() { int o = __offset(6); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.TextureMapping params() { return params(new gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.TextureMapping()); }
  public gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.TextureMapping params(gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.TextureMapping obj) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }

  public static int createTextureMap(FlatBufferBuilder builder,
      int samplerOffset,
      int index,
      int paramsOffset) {
    builder.startTable(3);
    TextureMap.addParams(builder, paramsOffset);
    TextureMap.addIndex(builder, index);
    TextureMap.addSampler(builder, samplerOffset);
    return TextureMap.endTextureMap(builder);
  }

  public static void startTextureMap(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addSampler(FlatBufferBuilder builder, int samplerOffset) { builder.addOffset(0, samplerOffset, 0); }
  public static void addIndex(FlatBufferBuilder builder, int index) { builder.addInt(1, index, 0); }
  public static void addParams(FlatBufferBuilder builder, int paramsOffset) { builder.addOffset(2, paramsOffset, 0); }
  public static int endTextureMap(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public TextureMap get(int j) { return get(new TextureMap(), j); }
    public TextureMap get(TextureMap obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

