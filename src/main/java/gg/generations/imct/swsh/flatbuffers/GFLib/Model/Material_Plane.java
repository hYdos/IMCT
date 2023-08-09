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
public final class Material_Plane extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static Material_Plane getRootAsMaterial_Plane(ByteBuffer _bb) { return getRootAsMaterial_Plane(_bb, new Material_Plane()); }
  public static Material_Plane getRootAsMaterial_Plane(ByteBuffer _bb, Material_Plane obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Material_Plane __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public String shader() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer shaderAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer shaderInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }
  public long renderpass() { int o = __offset(8); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public int b3() { int o = __offset(10); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0; }
  public int b4() { int o = __offset(12); return o != 0 ? bb.get(o + bb_pos) & 0xFF : 0; }
  public long i5() { int o = __offset(14); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long i6() { int o = __offset(16); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long i7() { int o = __offset(18); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long i8() { int o = __offset(20); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long i9() { int o = __offset(22); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public long iA() { int o = __offset(24); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Texture iB(int j) { return iB(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Texture(), j); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Texture iB(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Texture obj, int j) { int o = __offset(26); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int iBLength() { int o = __offset(26); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Texture.Vector iBVector() { return iBVector(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Texture.Vector()); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Texture.Vector iBVector(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Texture.Vector obj) { int o = __offset(26); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Flag iC(int j) { return iC(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Flag(), j); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Flag iC(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Flag obj, int j) { int o = __offset(28); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int iCLength() { int o = __offset(28); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Flag.Vector iCVector() { return iCVector(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Flag.Vector()); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Flag.Vector iCVector(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Flag.Vector obj) { int o = __offset(28); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Value iD(int j) { return iD(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Value(), j); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Value iD(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Value obj, int j) { int o = __offset(30); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int iDLength() { int o = __offset(30); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Value.Vector iDVector() { return iDVector(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Value.Vector()); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Value.Vector iDVector(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Value.Vector obj) { int o = __offset(30); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Color iE(int j) { return iE(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Color(), j); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Color iE(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Color obj, int j) { int o = __offset(32); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int iELength() { int o = __offset(32); return o != 0 ? __vector_len(o) : 0; }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Color.Vector iEVector() { return iEVector(new gg.generations.imct.swsh.flatbuffers.GFLib.Model.Color.Vector()); }
  public gg.generations.imct.swsh.flatbuffers.GFLib.Model.Color.Vector iEVector(gg.generations.imct.swsh.flatbuffers.GFLib.Model.Color.Vector obj) { int o = __offset(32); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public long i15() { int o = __offset(34); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }

  public static int createMaterial_Plane(FlatBufferBuilder builder,
      int nameOffset,
      int shaderOffset,
      long renderpass,
      int b3,
      int b4,
      long i5,
      long i6,
      long i7,
      long i8,
      long i9,
      long iA,
      int iBOffset,
      int iCOffset,
      int iDOffset,
      int iEOffset,
      long i15) {
    builder.startTable(16);
    Material_Plane.addI15(builder, i15);
    Material_Plane.addIE(builder, iEOffset);
    Material_Plane.addID(builder, iDOffset);
    Material_Plane.addIC(builder, iCOffset);
    Material_Plane.addIB(builder, iBOffset);
    Material_Plane.addIA(builder, iA);
    Material_Plane.addI9(builder, i9);
    Material_Plane.addI8(builder, i8);
    Material_Plane.addI7(builder, i7);
    Material_Plane.addI6(builder, i6);
    Material_Plane.addI5(builder, i5);
    Material_Plane.addRenderpass(builder, renderpass);
    Material_Plane.addShader(builder, shaderOffset);
    Material_Plane.addName(builder, nameOffset);
    Material_Plane.addB4(builder, b4);
    Material_Plane.addB3(builder, b3);
    return Material_Plane.endMaterial_Plane(builder);
  }

  public static void startMaterial_Plane(FlatBufferBuilder builder) { builder.startTable(16); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static void addShader(FlatBufferBuilder builder, int shaderOffset) { builder.addOffset(1, shaderOffset, 0); }
  public static void addRenderpass(FlatBufferBuilder builder, long renderpass) { builder.addInt(2, (int) renderpass, (int) 0L); }
  public static void addB3(FlatBufferBuilder builder, int b3) { builder.addByte(3, (byte) b3, (byte) 0); }
  public static void addB4(FlatBufferBuilder builder, int b4) { builder.addByte(4, (byte) b4, (byte) 0); }
  public static void addI5(FlatBufferBuilder builder, long i5) { builder.addInt(5, (int) i5, (int) 0L); }
  public static void addI6(FlatBufferBuilder builder, long i6) { builder.addInt(6, (int) i6, (int) 0L); }
  public static void addI7(FlatBufferBuilder builder, long i7) { builder.addInt(7, (int) i7, (int) 0L); }
  public static void addI8(FlatBufferBuilder builder, long i8) { builder.addInt(8, (int) i8, (int) 0L); }
  public static void addI9(FlatBufferBuilder builder, long i9) { builder.addInt(9, (int) i9, (int) 0L); }
  public static void addIA(FlatBufferBuilder builder, long iA) { builder.addInt(10, (int) iA, (int) 0L); }
  public static void addIB(FlatBufferBuilder builder, int iBOffset) { builder.addOffset(11, iBOffset, 0); }
  public static int createIBVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startIBVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addIC(FlatBufferBuilder builder, int iCOffset) { builder.addOffset(12, iCOffset, 0); }
  public static int createICVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startICVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addID(FlatBufferBuilder builder, int iDOffset) { builder.addOffset(13, iDOffset, 0); }
  public static int createIDVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startIDVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addIE(FlatBufferBuilder builder, int iEOffset) { builder.addOffset(14, iEOffset, 0); }
  public static int createIEVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startIEVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addI15(FlatBufferBuilder builder, long i15) { builder.addInt(15, (int) i15, (int) 0L); }
  public static int endMaterial_Plane(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Material_Plane get(int j) { return get(new Material_Plane(), j); }
    public Material_Plane get(Material_Plane obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

