// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.imct.scvi.flatbuffers.Titan.Model;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class MaterialMapper extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static MaterialMapper getRootAsMaterialMapper(ByteBuffer _bb) { return getRootAsMaterialMapper(_bb, new MaterialMapper()); }
  public static MaterialMapper getRootAsMaterialMapper(ByteBuffer _bb, MaterialMapper obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public MaterialMapper __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String meshName() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer meshNameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer meshNameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public String materialName() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer materialNameAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer materialNameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }
  public String layerName() { int o = __offset(8); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer layerNameAsByteBuffer() { return __vector_as_bytebuffer(8, 1); }
  public ByteBuffer layerNameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 8, 1); }

  public static int createMaterialMapper(FlatBufferBuilder builder,
      int meshNameOffset,
      int materialNameOffset,
      int layerNameOffset) {
    builder.startTable(3);
    MaterialMapper.addLayerName(builder, layerNameOffset);
    MaterialMapper.addMaterialName(builder, materialNameOffset);
    MaterialMapper.addMeshName(builder, meshNameOffset);
    return MaterialMapper.endMaterialMapper(builder);
  }

  public static void startMaterialMapper(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addMeshName(FlatBufferBuilder builder, int meshNameOffset) { builder.addOffset(0, meshNameOffset, 0); }
  public static void addMaterialName(FlatBufferBuilder builder, int materialNameOffset) { builder.addOffset(1, materialNameOffset, 0); }
  public static void addLayerName(FlatBufferBuilder builder, int layerNameOffset) { builder.addOffset(2, layerNameOffset, 0); }
  public static int endMaterialMapper(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public MaterialMapper get(int j) { return get(new MaterialMapper(), j); }
    public MaterialMapper get(MaterialMapper obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

