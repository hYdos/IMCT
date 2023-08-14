// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.imct.read.swsh.flatbuffers.Gfbmdl;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.ByteVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Mesh extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static Mesh getRootAsMesh(ByteBuffer _bb) { return getRootAsMesh(_bb, new Mesh()); }
  public static Mesh getRootAsMesh(ByteBuffer _bb, Mesh obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Mesh __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshPolygon polygons(int j) { return polygons(new gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshPolygon(), j); }
  public gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshPolygon polygons(gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshPolygon obj, int j) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int polygonsLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshPolygon.Vector polygonsVector() { return polygonsVector(new gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshPolygon.Vector()); }
  public gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshPolygon.Vector polygonsVector(gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshPolygon.Vector obj) { int o = __offset(4); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshAttribute attributes(int j) { return attributes(new gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshAttribute(), j); }
  public gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshAttribute attributes(gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshAttribute obj, int j) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int attributesLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshAttribute.Vector attributesVector() { return attributesVector(new gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshAttribute.Vector()); }
  public gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshAttribute.Vector attributesVector(gg.generations.imct.read.swsh.flatbuffers.Gfbmdl.MeshAttribute.Vector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public int data(int j) { int o = __offset(8); return o != 0 ? bb.get(__vector(o) + j * 1) & 0xFF : 0; }
  public int dataLength() { int o = __offset(8); return o != 0 ? __vector_len(o) : 0; }
  public ByteVector dataVector() { return dataVector(new ByteVector()); }
  public ByteVector dataVector(ByteVector obj) { int o = __offset(8); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer dataAsByteBuffer() { return __vector_as_bytebuffer(8, 1); }
  public ByteBuffer dataInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 8, 1); }

  public static int createMesh(FlatBufferBuilder builder,
      int polygonsOffset,
      int attributesOffset,
      int dataOffset) {
    builder.startTable(3);
    Mesh.addData(builder, dataOffset);
    Mesh.addAttributes(builder, attributesOffset);
    Mesh.addPolygons(builder, polygonsOffset);
    return Mesh.endMesh(builder);
  }

  public static void startMesh(FlatBufferBuilder builder) { builder.startTable(3); }
  public static void addPolygons(FlatBufferBuilder builder, int polygonsOffset) { builder.addOffset(0, polygonsOffset, 0); }
  public static int createPolygonsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startPolygonsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addAttributes(FlatBufferBuilder builder, int attributesOffset) { builder.addOffset(1, attributesOffset, 0); }
  public static int createAttributesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startAttributesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addData(FlatBufferBuilder builder, int dataOffset) { builder.addOffset(2, dataOffset, 0); }
  public static int createDataVector(FlatBufferBuilder builder, byte[] data) { return builder.createByteVector(data); }
  public static int createDataVector(FlatBufferBuilder builder, ByteBuffer data) { return builder.createByteVector(data); }
  public static void startDataVector(FlatBufferBuilder builder, int numElems) { builder.startVector(1, numElems, 1); }
  public static int endMesh(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Mesh get(int j) { return get(new Mesh(), j); }
    public Mesh get(Mesh obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

