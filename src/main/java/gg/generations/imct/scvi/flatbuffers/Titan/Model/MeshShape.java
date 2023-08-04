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
public final class MeshShape extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static MeshShape getRootAsMeshShape(ByteBuffer _bb) { return getRootAsMeshShape(_bb, new MeshShape()); }
  public static MeshShape getRootAsMeshShape(ByteBuffer _bb, MeshShape obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public MeshShape __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String meshShapeName() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer meshShapeNameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer meshShapeNameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.BoundingBox bounds() { return bounds(new gg.generations.imct.scvi.flatbuffers.Titan.Model.BoundingBox()); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.BoundingBox bounds(gg.generations.imct.scvi.flatbuffers.Titan.Model.BoundingBox obj) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  public long polygonType() { int o = __offset(8); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessors attributes(int j) { return attributes(new gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessors(), j); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessors attributes(gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessors obj, int j) { int o = __offset(10); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int attributesLength() { int o = __offset(10); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessors.Vector attributesVector() { return attributesVector(new gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessors.Vector()); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessors.Vector attributesVector(gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessors.Vector obj) { int o = __offset(10); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MaterialInfo materials(int j) { return materials(new gg.generations.imct.scvi.flatbuffers.Titan.Model.MaterialInfo(), j); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MaterialInfo materials(gg.generations.imct.scvi.flatbuffers.Titan.Model.MaterialInfo obj, int j) { int o = __offset(12); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int materialsLength() { int o = __offset(12); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MaterialInfo.Vector materialsVector() { return materialsVector(new gg.generations.imct.scvi.flatbuffers.Titan.Model.MaterialInfo.Vector()); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MaterialInfo.Vector materialsVector(gg.generations.imct.scvi.flatbuffers.Titan.Model.MaterialInfo.Vector obj) { int o = __offset(12); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public long res0() { int o = __offset(14); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long res1() { int o = __offset(16); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long res2() { int o = __offset(18); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long res3() { int o = __offset(20); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.Sphere clipSphere() { return clipSphere(new gg.generations.imct.scvi.flatbuffers.Titan.Model.Sphere()); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.Sphere clipSphere(gg.generations.imct.scvi.flatbuffers.Titan.Model.Sphere obj) { int o = __offset(22); return o != 0 ? obj.__assign(o + bb_pos, bb) : null; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.Influence influence(int j) { return influence(new gg.generations.imct.scvi.flatbuffers.Titan.Model.Influence(), j); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.Influence influence(gg.generations.imct.scvi.flatbuffers.Titan.Model.Influence obj, int j) { int o = __offset(24); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int influenceLength() { int o = __offset(24); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.Influence.Vector influenceVector() { return influenceVector(new gg.generations.imct.scvi.flatbuffers.Titan.Model.Influence.Vector()); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.Influence.Vector influenceVector(gg.generations.imct.scvi.flatbuffers.Titan.Model.Influence.Vector obj) { int o = __offset(24); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VisShape visShapes(int j) { return visShapes(new gg.generations.imct.scvi.flatbuffers.Titan.Model.VisShape(), j); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VisShape visShapes(gg.generations.imct.scvi.flatbuffers.Titan.Model.VisShape obj, int j) { int o = __offset(26); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int visShapesLength() { int o = __offset(26); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VisShape.Vector visShapesVector() { return visShapesVector(new gg.generations.imct.scvi.flatbuffers.Titan.Model.VisShape.Vector()); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VisShape.Vector visShapesVector(gg.generations.imct.scvi.flatbuffers.Titan.Model.VisShape.Vector obj) { int o = __offset(26); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public String meshName() { int o = __offset(28); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer meshNameAsByteBuffer() { return __vector_as_bytebuffer(28, 1); }
  public ByteBuffer meshNameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 28, 1); }
  public long unk13() { int o = __offset(30); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MorphShape morphShape(int j) { return morphShape(new gg.generations.imct.scvi.flatbuffers.Titan.Model.MorphShape(), j); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MorphShape morphShape(gg.generations.imct.scvi.flatbuffers.Titan.Model.MorphShape obj, int j) { int o = __offset(32); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int morphShapeLength() { int o = __offset(32); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MorphShape.Vector morphShapeVector() { return morphShapeVector(new gg.generations.imct.scvi.flatbuffers.Titan.Model.MorphShape.Vector()); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.MorphShape.Vector morphShapeVector(gg.generations.imct.scvi.flatbuffers.Titan.Model.MorphShape.Vector obj) { int o = __offset(32); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }

  public static void startMeshShape(FlatBufferBuilder builder) { builder.startTable(15); }
  public static void addMeshShapeName(FlatBufferBuilder builder, int meshShapeNameOffset) { builder.addOffset(0, meshShapeNameOffset, 0); }
  public static void addBounds(FlatBufferBuilder builder, int boundsOffset) { builder.addOffset(1, boundsOffset, 0); }
  public static void addPolygonType(FlatBufferBuilder builder, long polygonType) { builder.addInt(2, (int) polygonType, (int) 0L); }
  public static void addAttributes(FlatBufferBuilder builder, int attributesOffset) { builder.addOffset(3, attributesOffset, 0); }
  public static int createAttributesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startAttributesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addMaterials(FlatBufferBuilder builder, int materialsOffset) { builder.addOffset(4, materialsOffset, 0); }
  public static int createMaterialsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startMaterialsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addRes0(FlatBufferBuilder builder, long res0) { builder.addInt(5, (int) res0, (int) 0L); }
  public static void addRes1(FlatBufferBuilder builder, long res1) { builder.addInt(6, (int) res1, (int) 0L); }
  public static void addRes2(FlatBufferBuilder builder, long res2) { builder.addInt(7, (int) res2, (int) 0L); }
  public static void addRes3(FlatBufferBuilder builder, long res3) { builder.addInt(8, (int) res3, (int) 0L); }
  public static void addClipSphere(FlatBufferBuilder builder, int clipSphereOffset) { builder.addStruct(9, clipSphereOffset, 0); }
  public static void addInfluence(FlatBufferBuilder builder, int influenceOffset) { builder.addOffset(10, influenceOffset, 0); }
  public static int createInfluenceVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startInfluenceVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addVisShapes(FlatBufferBuilder builder, int visShapesOffset) { builder.addOffset(11, visShapesOffset, 0); }
  public static int createVisShapesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startVisShapesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addMeshName(FlatBufferBuilder builder, int meshNameOffset) { builder.addOffset(12, meshNameOffset, 0); }
  public static void addUnk13(FlatBufferBuilder builder, long unk13) { builder.addInt(13, (int) unk13, (int) 0L); }
  public static void addMorphShape(FlatBufferBuilder builder, int morphShapeOffset) { builder.addOffset(14, morphShapeOffset, 0); }
  public static int createMorphShapeVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startMorphShapeVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endMeshShape(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public MeshShape get(int j) { return get(new MeshShape(), j); }
    public MeshShape get(MeshShape obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

