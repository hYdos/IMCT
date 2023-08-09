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
public final class FloatParameter extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static FloatParameter getRootAsFloatParameter(ByteBuffer _bb) { return getRootAsFloatParameter(_bb, new FloatParameter()); }
  public static FloatParameter getRootAsFloatParameter(ByteBuffer _bb, FloatParameter obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public FloatParameter __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String propertyBinding() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer propertyBindingAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer propertyBindingInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public float floatValue() { int o = __offset(6); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }

  public static int createFloatParameter(FlatBufferBuilder builder,
      int propertyBindingOffset,
      float floatValue) {
    builder.startTable(2);
    FloatParameter.addFloatValue(builder, floatValue);
    FloatParameter.addPropertyBinding(builder, propertyBindingOffset);
    return FloatParameter.endFloatParameter(builder);
  }

  public static void startFloatParameter(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addPropertyBinding(FlatBufferBuilder builder, int propertyBindingOffset) { builder.addOffset(0, propertyBindingOffset, 0); }
  public static void addFloatValue(FlatBufferBuilder builder, float floatValue) { builder.addFloat(1, floatValue, 0.0f); }
  public static int endFloatParameter(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public FloatParameter get(int j) { return get(new FloatParameter(), j); }
    public FloatParameter get(FloatParameter obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

