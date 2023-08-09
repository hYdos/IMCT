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
public final class RGB extends Struct {
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public RGB __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public float r() { return bb.getFloat(bb_pos + 0); }
  public float g() { return bb.getFloat(bb_pos + 4); }
  public float b() { return bb.getFloat(bb_pos + 8); }

  public static int createRGB(FlatBufferBuilder builder, float r, float g, float b) {
    builder.prep(4, 12);
    builder.putFloat(b);
    builder.putFloat(g);
    builder.putFloat(r);
    return builder.offset();
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public RGB get(int j) { return get(new RGB(), j); }
    public RGB get(RGB obj, int j) {  return obj.__assign(__element(j), bb); }
  }
}

