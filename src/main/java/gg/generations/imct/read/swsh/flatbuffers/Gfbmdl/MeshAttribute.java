// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.imct.read.swsh.flatbuffers.Gfbmdl;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class MeshAttribute extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static MeshAttribute getRootAsMeshAttribute(ByteBuffer _bb) { return getRootAsMeshAttribute(_bb, new MeshAttribute()); }
  public static MeshAttribute getRootAsMeshAttribute(ByteBuffer _bb, MeshAttribute obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public MeshAttribute __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public long vertexType() { int o = __offset(4); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long bufferFormat() { int o = __offset(6); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long elementCount() { int o = __offset(8); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }

  public static int createMeshAttribute(FlatBufferBuilder builder,
      long vertexType,
      long bufferFormat,
      long elementCount) {
    builder.startTable(3);
    MeshAttribute.addElementCount(builder, elementCount);
    MeshAttribute.addBufferFormat(builder, bufferFormat);
    MeshAttribute.addVertexType(builder, vertexType);
    return MeshAttribute.endMeshAttribute(builder);
  }

  public static void startMeshAttribute(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addVertexType(FlatBufferBuilder builder, long vertexType) { builder.addInt(0, (int) vertexType, (int) 0L); }
  public static void addBufferFormat(FlatBufferBuilder builder, long bufferFormat) { builder.addInt(1, (int) bufferFormat, (int) 0L); }
  public static void addElementCount(FlatBufferBuilder builder, long elementCount) { builder.addInt(2, (int) elementCount, (int) 0L); }
  public static int endMeshAttribute(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public MeshAttribute get(int j) { return get(new MeshAttribute(), j); }
    public MeshAttribute get(MeshAttribute obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}
