// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.imct.scvi.flatbuffers.Titan.Model;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.StringVector;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class MeshEntry extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static MeshEntry getRootAsMeshEntry(ByteBuffer _bb) { return getRootAsMeshEntry(_bb, new MeshEntry()); }
  public static MeshEntry getRootAsMeshEntry(ByteBuffer _bb, MeshEntry obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public MeshEntry __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public String slotName(int j) { int o = __offset(6); return o != 0 ? __string(__vector(o) + j * 4) : null; }
  public int slotNameLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public StringVector slotNameVector() { return slotNameVector(new StringVector()); }
  public StringVector slotNameVector(StringVector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }

  public static int createMeshEntry(FlatBufferBuilder builder,
      int nameOffset,
      int slotNameOffset) {
    builder.startTable(2);
    MeshEntry.addSlotName(builder, slotNameOffset);
    MeshEntry.addName(builder, nameOffset);
    return MeshEntry.endMeshEntry(builder);
  }

  public static void startMeshEntry(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static void addSlotName(FlatBufferBuilder builder, int slotNameOffset) { builder.addOffset(1, slotNameOffset, 0); }
  public static int createSlotNameVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startSlotNameVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endMeshEntry(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public MeshEntry get(int j) { return get(new MeshEntry(), j); }
    public MeshEntry get(MeshEntry obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

