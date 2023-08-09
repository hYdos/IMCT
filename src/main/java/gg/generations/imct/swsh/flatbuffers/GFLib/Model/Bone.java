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
public final class Bone extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static Bone getRootAsBone(ByteBuffer _bb) { return getRootAsBone(_bb, new Bone()); }
  public static Bone getRootAsBone(ByteBuffer _bb, Bone obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Bone __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public long type() { int o = __offset(6); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public int parent() { int o = __offset(8); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public long effect() { int o = __offset(10); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public boolean segmentScaleCompensate() { int o = __offset(12); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 vecScale() { return vecScale(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3()); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 vecScale(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 obj) { int o = __offset(14); return o != 0 ? obj.__assign(o + bb_pos, bb) : null; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 vecRot() { return vecRot(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3()); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 vecRot(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 obj) { int o = __offset(16); return o != 0 ? obj.__assign(o + bb_pos, bb) : null; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 vecTranslate() { return vecTranslate(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3()); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 vecTranslate(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 obj) { int o = __offset(18); return o != 0 ? obj.__assign(o + bb_pos, bb) : null; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 vecScalePivot() { return vecScalePivot(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3()); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 vecScalePivot(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 obj) { int o = __offset(20); return o != 0 ? obj.__assign(o + bb_pos, bb) : null; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 vecRotatePivot() { return vecRotatePivot(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3()); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 vecRotatePivot(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Vec3 obj) { int o = __offset(22); return o != 0 ? obj.__assign(o + bb_pos, bb) : null; }
  public int isSkin() { int o = __offset(24); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 1; }

  public static void startBone(FlatBufferBuilder builder) { builder.startTable(11); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static void addType(FlatBufferBuilder builder, long type) { builder.addInt(1, (int) type, (int) 0L); }
  public static void addParent(FlatBufferBuilder builder, int parent) { builder.addInt(2, parent, 0); }
  public static void addEffect(FlatBufferBuilder builder, long effect) { builder.addInt(3, (int) effect, (int) 0L); }
  public static void addSegmentScaleCompensate(FlatBufferBuilder builder, boolean segmentScaleCompensate) { builder.addBoolean(4, segmentScaleCompensate, false); }
  public static void addVecScale(FlatBufferBuilder builder, int vecScaleOffset) { builder.addStruct(5, vecScaleOffset, 0); }
  public static void addVecRot(FlatBufferBuilder builder, int vecRotOffset) { builder.addStruct(6, vecRotOffset, 0); }
  public static void addVecTranslate(FlatBufferBuilder builder, int vecTranslateOffset) { builder.addStruct(7, vecTranslateOffset, 0); }
  public static void addVecScalePivot(FlatBufferBuilder builder, int vecScalePivotOffset) { builder.addStruct(8, vecScalePivotOffset, 0); }
  public static void addVecRotatePivot(FlatBufferBuilder builder, int vecRotatePivotOffset) { builder.addStruct(9, vecRotatePivotOffset, 0); }
  public static void addIsSkin(FlatBufferBuilder builder, int isSkin) { builder.addByte(10, (byte) isSkin, (byte) 1); }
  public static int endBone(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Bone get(int j) { return get(new Bone(), j); }
    public Bone get(Bone obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

