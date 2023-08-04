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
public final class VertexAccessors extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static VertexAccessors getRootAsVertexAccessors(ByteBuffer _bb) { return getRootAsVertexAccessors(_bb, new VertexAccessors()); }
  public static VertexAccessors getRootAsVertexAccessors(ByteBuffer _bb, VertexAccessors obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public VertexAccessors __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessor attrs(int j) { return attrs(new gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessor(), j); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessor attrs(gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessor obj, int j) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int attrsLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessor.Vector attrsVector() { return attrsVector(new gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessor.Vector()); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessor.Vector attrsVector(gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexAccessor.Vector obj) { int o = __offset(4); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexSize size(int j) { return size(new gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexSize(), j); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexSize size(gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexSize obj, int j) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int sizeLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexSize.Vector sizeVector() { return sizeVector(new gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexSize.Vector()); }
  public gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexSize.Vector sizeVector(gg.generations.imct.scvi.flatbuffers.Titan.Model.VertexSize.Vector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }

  public static int createVertexAccessors(FlatBufferBuilder builder,
      int attrsOffset,
      int sizeOffset) {
    builder.startTable(2);
    VertexAccessors.addSize(builder, sizeOffset);
    VertexAccessors.addAttrs(builder, attrsOffset);
    return VertexAccessors.endVertexAccessors(builder);
  }

  public static void startVertexAccessors(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addAttrs(FlatBufferBuilder builder, int attrsOffset) { builder.addOffset(0, attrsOffset, 0); }
  public static int createAttrsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startAttrsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addSize(FlatBufferBuilder builder, int sizeOffset) { builder.addOffset(1, sizeOffset, 0); }
  public static int createSizeVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startSizeVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endVertexAccessors(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public VertexAccessors get(int j) { return get(new VertexAccessors(), j); }
    public VertexAccessors get(VertexAccessors obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

