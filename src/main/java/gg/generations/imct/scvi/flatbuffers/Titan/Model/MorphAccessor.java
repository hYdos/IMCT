// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.imct.scvi.flatbuffers.Titan.Model;

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
public final class MorphAccessor extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static MorphAccessor getRootAsMorphAccessor(ByteBuffer _bb) { return getRootAsMorphAccessor(_bb, new MorphAccessor()); }
  public static MorphAccessor getRootAsMorphAccessor(ByteBuffer _bb, MorphAccessor obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public MorphAccessor __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public long unk0() { int o = __offset(4); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long attribute() { int o = __offset(6); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long unk1() { int o = __offset(8); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long type() { int o = __offset(10); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long position() { int o = __offset(12); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }

  public static int createMorphAccessor(FlatBufferBuilder builder,
      long unk0,
      long attribute,
      long unk1,
      long type,
      long position) {
    builder.startTable(5);
    MorphAccessor.addPosition(builder, position);
    MorphAccessor.addType(builder, type);
    MorphAccessor.addUnk1(builder, unk1);
    MorphAccessor.addAttribute(builder, attribute);
    MorphAccessor.addUnk0(builder, unk0);
    return MorphAccessor.endMorphAccessor(builder);
  }

  public static void startMorphAccessor(FlatBufferBuilder builder) { builder.startTable(5); }
  public static void addUnk0(FlatBufferBuilder builder, long unk0) { builder.addInt(0, (int) unk0, (int) 0L); }
  public static void addAttribute(FlatBufferBuilder builder, long attribute) { builder.addInt(1, (int) attribute, (int) 0L); }
  public static void addUnk1(FlatBufferBuilder builder, long unk1) { builder.addInt(2, (int) unk1, (int) 0L); }
  public static void addType(FlatBufferBuilder builder, long type) { builder.addInt(3, (int) type, (int) 0L); }
  public static void addPosition(FlatBufferBuilder builder, long position) { builder.addInt(4, (int) position, (int) 0L); }
  public static int endMorphAccessor(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public MorphAccessor get(int j) { return get(new MorphAccessor(), j); }
    public MorphAccessor get(MorphAccessor obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

