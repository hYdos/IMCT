// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.imct.scvi.flatbuffers.Titan.Model;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class TRMSH extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static TRMSH getRootAsTRMSH(ByteBuffer _bb) { return getRootAsTRMSH(_bb, new TRMSH()); }
  public static TRMSH getRootAsTRMSH(ByteBuffer _bb, TRMSH obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public TRMSH __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public long unk0() { int o = __offset(4); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MeshShape meshes(int j) { return meshes(new gg.generations.imct.scvi.flatbuffers.Titan.Model.MeshShape(), j); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MeshShape meshes(gg.generations.imct.scvi.flatbuffers.Titan.Model.MeshShape obj, int j) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int meshesLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MeshShape.Vector meshesVector() { return meshesVector(new gg.generations.imct.scvi.flatbuffers.Titan.Model.MeshShape.Vector()); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MeshShape.Vector meshesVector(gg.generations.imct.scvi.flatbuffers.Titan.Model.MeshShape.Vector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public String bufferName() { int o = __offset(8); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer bufferNameAsByteBuffer() { return __vector_as_bytebuffer(8, 1); }
  public ByteBuffer bufferNameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 8, 1); }

  public static int createTRMSH(FlatBufferBuilder builder,
      long unk0,
      int meshesOffset,
      int bufferNameOffset) {
    builder.startTable(3);
    TRMSH.addBufferName(builder, bufferNameOffset);
    TRMSH.addMeshes(builder, meshesOffset);
    TRMSH.addUnk0(builder, unk0);
    return TRMSH.endTRMSH(builder);
  }

  public static void startTRMSH(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addUnk0(FlatBufferBuilder builder, long unk0) { builder.addInt(0, (int) unk0, (int) 0L); }
  public static void addMeshes(FlatBufferBuilder builder, int meshesOffset) { builder.addOffset(1, meshesOffset, 0); }
  public static int createMeshesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startMeshesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addBufferName(FlatBufferBuilder builder, int bufferNameOffset) { builder.addOffset(2, bufferNameOffset, 0); }
  public static int endTRMSH(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }
  public static void finishTRMSHBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
  public static void finishSizePrefixedTRMSHBuffer(FlatBufferBuilder builder, int offset) { builder.finishSizePrefixed(offset); }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public TRMSH get(int j) { return get(new TRMSH(), j); }
    public TRMSH get(TRMSH obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

