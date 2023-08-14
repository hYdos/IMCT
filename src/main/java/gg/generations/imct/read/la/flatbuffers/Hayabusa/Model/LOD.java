// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.imct.read.la.flatbuffers.Hayabusa.Model;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class LOD extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static LOD getRootAsLOD(ByteBuffer _bb) { return getRootAsLOD(_bb, new LOD()); }
  public static LOD getRootAsLOD(ByteBuffer _bb, LOD obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public LOD __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public gg.generations.imct.read.la.flatbuffers.Hayabusa.Model.LODIndex entries(int j) { return entries(new gg.generations.imct.read.la.flatbuffers.Hayabusa.Model.LODIndex(), j); }
  public gg.generations.imct.read.la.flatbuffers.Hayabusa.Model.LODIndex entries(gg.generations.imct.read.la.flatbuffers.Hayabusa.Model.LODIndex obj, int j) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int entriesLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.read.la.flatbuffers.Hayabusa.Model.LODIndex.Vector entriesVector() { return entriesVector(new gg.generations.imct.read.la.flatbuffers.Hayabusa.Model.LODIndex.Vector()); }
  public gg.generations.imct.read.la.flatbuffers.Hayabusa.Model.LODIndex.Vector entriesVector(gg.generations.imct.read.la.flatbuffers.Hayabusa.Model.LODIndex.Vector obj) { int o = __offset(4); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public String type() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer typeAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer typeInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }

  public static int createLOD(FlatBufferBuilder builder,
      int entriesOffset,
      int typeOffset) {
    builder.startTable(2);
    LOD.addType(builder, typeOffset);
    LOD.addEntries(builder, entriesOffset);
    return LOD.endLOD(builder);
  }

  public static void startLOD(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addEntries(FlatBufferBuilder builder, int entriesOffset) { builder.addOffset(0, entriesOffset, 0); }
  public static int createEntriesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startEntriesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addType(FlatBufferBuilder builder, int typeOffset) { builder.addOffset(1, typeOffset, 0); }
  public static int endLOD(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public LOD get(int j) { return get(new LOD(), j); }
    public LOD get(LOD obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}
