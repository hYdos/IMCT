// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.imct.swsh.flatbuffers.GFLib.Model;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.BooleanVector;
import com.google.flatbuffers.ByteVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.DoubleVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.FloatVector;
import com.google.flatbuffers.IntVector;
import com.google.flatbuffers.LongVector;
import com.google.flatbuffers.ShortVector;
import com.google.flatbuffers.StringVector;
import com.google.flatbuffers.Struct;
import com.google.flatbuffers.Table;
import com.google.flatbuffers.UnionVector;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Flag extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static Flag getRootAsFlag(ByteBuffer _bb) { return getRootAsFlag(_bb, new Flag()); }
  public static Flag getRootAsFlag(ByteBuffer _bb, Flag obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Flag __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String flagName() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer flagNameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer flagNameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public boolean flagEnable() { int o = __offset(6); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }

  public static int createFlag(FlatBufferBuilder builder,
      int flagNameOffset,
      boolean flagEnable) {
    builder.startTable(2);
    Flag.addFlagName(builder, flagNameOffset);
    Flag.addFlagEnable(builder, flagEnable);
    return Flag.endFlag(builder);
  }

  public static void startFlag(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addFlagName(FlatBufferBuilder builder, int flagNameOffset) { builder.addOffset(0, flagNameOffset, 0); }
  public static void addFlagEnable(FlatBufferBuilder builder, boolean flagEnable) { builder.addBoolean(1, flagEnable, false); }
  public static int endFlag(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Flag get(int j) { return get(new Flag(), j); }
    public Flag get(Flag obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

