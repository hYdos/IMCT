// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.imct.la.flatbuffers.Hayabusa.Model;

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
public final class TRSKL extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static TRSKL getRootAsTRSKL(ByteBuffer _bb) { return getRootAsTRSKL(_bb, new TRSKL()); }
  public static TRSKL getRootAsTRSKL(ByteBuffer _bb, TRSKL obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public TRSKL __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public long res0() { int o = __offset(4); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.TransformNode transformNodes(int j) { return transformNodes(new gg.generations.imct.la.flatbuffers.Hayabusa.Model.TransformNode(), j); }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.TransformNode transformNodes(gg.generations.imct.la.flatbuffers.Hayabusa.Model.TransformNode obj, int j) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int transformNodesLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.TransformNode.Vector transformNodesVector() { return transformNodesVector(new gg.generations.imct.la.flatbuffers.Hayabusa.Model.TransformNode.Vector()); }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.TransformNode.Vector transformNodesVector(gg.generations.imct.la.flatbuffers.Hayabusa.Model.TransformNode.Vector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.Bone bones(int j) { return bones(new gg.generations.imct.la.flatbuffers.Hayabusa.Model.Bone(), j); }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.Bone bones(gg.generations.imct.la.flatbuffers.Hayabusa.Model.Bone obj, int j) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int bonesLength() { int o = __offset(8); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.Bone.Vector bonesVector() { return bonesVector(new gg.generations.imct.la.flatbuffers.Hayabusa.Model.Bone.Vector()); }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.Bone.Vector bonesVector(gg.generations.imct.la.flatbuffers.Hayabusa.Model.Bone.Vector obj) { int o = __offset(8); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.IKControl iks(int j) { return iks(new gg.generations.imct.la.flatbuffers.Hayabusa.Model.IKControl(), j); }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.IKControl iks(gg.generations.imct.la.flatbuffers.Hayabusa.Model.IKControl obj, int j) { int o = __offset(10); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int iksLength() { int o = __offset(10); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.IKControl.Vector iksVector() { return iksVector(new gg.generations.imct.la.flatbuffers.Hayabusa.Model.IKControl.Vector()); }
  public gg.generations.imct.la.flatbuffers.Hayabusa.Model.IKControl.Vector iksVector(gg.generations.imct.la.flatbuffers.Hayabusa.Model.IKControl.Vector obj) { int o = __offset(10); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public long rigOffset() { int o = __offset(12); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }

  public static int createTRSKL(FlatBufferBuilder builder,
      long res0,
      int transformNodesOffset,
      int bonesOffset,
      int iksOffset,
      long rigOffset) {
    builder.startTable(5);
    TRSKL.addRigOffset(builder, rigOffset);
    TRSKL.addIks(builder, iksOffset);
    TRSKL.addBones(builder, bonesOffset);
    TRSKL.addTransformNodes(builder, transformNodesOffset);
    TRSKL.addRes0(builder, res0);
    return TRSKL.endTRSKL(builder);
  }

  public static void startTRSKL(FlatBufferBuilder builder) { builder.startTable(5); }
  public static void addRes0(FlatBufferBuilder builder, long res0) { builder.addInt(0, (int) res0, (int) 0L); }
  public static void addTransformNodes(FlatBufferBuilder builder, int transformNodesOffset) { builder.addOffset(1, transformNodesOffset, 0); }
  public static int createTransformNodesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startTransformNodesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addBones(FlatBufferBuilder builder, int bonesOffset) { builder.addOffset(2, bonesOffset, 0); }
  public static int createBonesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startBonesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addIks(FlatBufferBuilder builder, int iksOffset) { builder.addOffset(3, iksOffset, 0); }
  public static int createIksVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startIksVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addRigOffset(FlatBufferBuilder builder, long rigOffset) { builder.addInt(4, (int) rigOffset, (int) 0L); }
  public static int endTRSKL(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }
  public static void finishTRSKLBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
  public static void finishSizePrefixedTRSKLBuffer(FlatBufferBuilder builder, int offset) { builder.finishSizePrefixed(offset); }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public TRSKL get(int j) { return get(new TRSKL(), j); }
    public TRSKL get(TRSKL obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

