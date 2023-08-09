package gg.generations.imct.util;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BntxExtractor {
    static Map<Integer, String> formats = Utils.create(new HashMap<>(), obj -> {
        obj.put(0x0b01, "R8_G8_B8_A8_UNORM");
        obj.put(0x0b06, "R8_G8_B8_A8_SRGB");
        obj.put(0x0701, "R5_G6_B5_UNORM");
        obj.put(0x0201, "R8_UNORM");
        obj.put(0x0901, "R8_G8_UNORM");
        obj.put(0x1a01, "BC1_UNORM");
        obj.put(0x1a06, "BC1_SRGB");
        obj.put(0x1b01, "BC2_UNORM");
        obj.put(0x1b06, "BC2_SRGB");
        obj.put(0x1c01, "BC3_UNORM");
        obj.put(0x1c06, "BC3_SRGB");
        obj.put(0x1d01, "BC4_UNORM");
        obj.put(0x1d02, "BC4_SNORM");
        obj.put(0x1e01, "BC5_UNORM");
        obj.put(0x1e02, "BC5_SNORM");
        obj.put(0x1f01, "BC6H_UF16");
        obj.put(0x1f02, "BC6H_SF16");
        obj.put(0x2001, "BC7_UNORM");
        obj.put(0x2006, "BC7_SRGB");
        obj.put(0x2d01, "ASTC4x4");
        obj.put(0x2d06, "ASTC4x4 SRGB");
        obj.put(0x2e01, "ASTC5x4");
        obj.put(0x2e06, "ASTC5x4 SRGB");
        obj.put(0x2f01, "ASTC5x5");
        obj.put(0x2f06, "ASTC5x5 SRGB");
        obj.put(0x3001, "ASTC6x5");
        obj.put(0x3006, "ASTC6x5 SRGB");
        obj.put(0x3101, "ASTC6x6");
        obj.put(0x3106, "ASTC6x6 SRGB");
        obj.put(0x3201, "ASTC8x5");
        obj.put(0x3206, "ASTC8x5 SRGB");
        obj.put(0x3301, "ASTC8x6");
        obj.put(0x3306, "ASTC8x6 SRGB");
        obj.put(0x3401, "ASTC8x8");
        obj.put(0x3406, "ASTC8x8 SRGB");
        obj.put(0x3501, "ASTC10x5");
        obj.put(0x3506, "ASTC10x5 SRGB");
        obj.put(0x3601, "ASTC10x6");
        obj.put(0x3606, "ASTC10x6 SRGB");
        obj.put(0x3701, "ASTC10x8");
        obj.put(0x3706, "ASTC10x8 SRGB");
        obj.put(0x3801, "ASTC10x10");
        obj.put(0x3806, "ASTC10x10 SRGB");
        obj.put(0x3901, "ASTC12x10");
        obj.put(0x3906, "ASTC12x10 SRGB");
        obj.put(0x3a01, "ASTC12x12");
        obj.put(0x3a06, "ASTC12x12 SRGB");
    });

    public static int[] BCn_formats = {
            0x1a, 0x1b, 0x1c, 0x1d,
            0x1e, 0x1f, 0x20
    };

    public static int[] ASTC_formats = {
            0x2d, 0x2e, 0x2f, 0x30,
            0x31, 0x32, 0x33, 0x34,
            0x35, 0x36, 0x37, 0x38,
            0x39, 0x3a
    };

    static Map<Integer, String> compSels = Utils.create(new HashMap<>(), obj -> {
                obj.put(0, "0");
                obj.put(1, "1");
                obj.put(2, "Red");
                obj.put(3, "Green");
                obj.put(4, "Blue");
                obj.put(5, "Alpha");
            });

    public static Map<Integer, Pair<Integer, Integer>> blk_dims = Utils.create(new HashMap<>(), obj -> {
        obj.put(0x1a, new Pair<>(4, 4));
        obj.put(0x1b, new Pair<>(4, 4));
        obj.put(0x1c, new Pair<>(4, 4));
        obj.put(0x1d, new Pair<>(4, 4));
        obj.put(0x1e, new Pair<>(4, 4));
        obj.put(0x1f, new Pair<>(4, 4));
        obj.put(0x20, new Pair<>(4, 4));
        obj.put(0x2d, new Pair<>(4, 4));
        obj.put(0x2e, new Pair<>(5, 4));
        obj.put(0x2f, new Pair<>(5, 5));
        obj.put(0x30, new Pair<>(6, 5));
        obj.put(0x31, new Pair<>(6, 6));
        obj.put(0x32, new Pair<>(8, 5));
        obj.put(0x33, new Pair<>(8, 6));
        obj.put(0x34, new Pair<>(8, 8));
        obj.put(0x35, new Pair<>(10, 5));
        obj.put(0x36, new Pair<>(10, 6));
        obj.put(0x37, new Pair<>(10, 8));
        obj.put(0x38, new Pair<>(10, 10));
        obj.put(0x39, new Pair<>(12, 10));
        obj.put(0x3a, new Pair<>(12, 12));
    });
//            # format -> (blkWidth, blkHeight)

    //# format -> bytes_per_pixel
    public static Map<Integer, Integer> bpps = Utils.create(new HashMap<>(), obj -> {
        obj.put(0x0b, 0x04);
        obj.put(0x07, 0x02);
        obj.put(0x02, 0x01);
        obj.put(0x09, 0x02);
        obj.put(0x1a, 0x08);
        obj.put(0x1b, 0x10);
        obj.put(0x1c, 0x10);
        obj.put(0x1d, 0x08);
        obj.put(0x1e, 0x10);
        obj.put(0x1f, 0x10);
        obj.put(0x20, 0x10);
        obj.put(0x2d, 0x10);
        obj.put(0x2e, 0x10);
        obj.put(0x2f, 0x10);
        obj.put(0x30, 0x10);
        obj.put(0x31, 0x10);
        obj.put(0x32, 0x10);
        obj.put(0x33, 0x10);
        obj.put(0x34, 0x10);
        obj.put(0x35, 0x10);
        obj.put(0x36, 0x10);
        obj.put(0x37, 0x10);
        obj.put(0x38, 0x10);
        obj.put(0x39, 0x10);
        obj.put(0x3a, 0x10);
    });

    public static Map<Integer, String> tileModes = Utils.create(new HashMap<>(), obj -> {
        obj.put(0, "TILING_MODE_PITCH");
        obj.put(1, "TILING_MODE_TILED");
    });

    public static void main(String[] args) {
        String input_ = "C:\\Users\\water\\Downloads\\pm0006_00_00\\pm0006_00_00_body_a_rare_alb.bntx"; // Replace with the actual file path

        try {
            // Open the file for reading in binary mode
            Path inputFile = Paths.get(input_);
            byte[] inb = Files.readAllBytes(inputFile);

            var textures = readBNTX(inb);

            saveTextures(textures);

            var tex = textures.get(0);

            var image = new BufferedImage(tex.width, tex.height, BufferedImage.TYPE_INT_ARGB);

            // Now 'inb' contains the read binary data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<TexInfo> readBNTX(byte[] f) {
        int pos = 0;

        var bom = detectByteOrder(f);

        var header = new BNTXHeader();
        header.parse(f, pos, bom);

        pos += BNTXHeader.STRUCT_SIZE;

        if(!header.magic.equals("BNTX")) {
            throw new IllegalArgumentException("Invalid file header!");
        }

        System.out.println();
        System.out.println("File Name: " + bytesToString(f, header.fileNameAddr, 12));

        var nx = new NXHeader();
        nx.parse(f, pos, bom);
        pos += NXHeader.STRUCT_SIZE;

        System.out.println();
        System.out.println("Textures count: " + nx.count);

        var textures = new ArrayList<TexInfo>();

        for (long i = 0; i < nx.count; i++) {
            pos = (int) (nx.infoPtrAddr + i * 8);

            pos = (int) unpackLong(f, pos, bom);

            var info = new BRTIInfo();
            info.parse(f, pos, bom);

            var nameLen = unpackUnsignedShort(f, (int) (info.nameAddr), bom);
            var name = bytesToString(f, (int) (info.nameAddr + 2), nameLen);

            System.out.println();
            System.out.println("Image " + (i + 1) + " name: " + name);

            var compSel = processCompSel(info.compSel);

            Map<Integer, String> types = Utils.create(new HashMap<>(), obj -> {
                obj.put(0, "1D");
                obj.put(1, "2D");
                obj.put(2, "3D");
                obj.put(3, "Cubemap");
                obj.put(8, "CubemapFar");
            });

            System.out.println("TileMode: " + tileModes.get((int) info.tileMode));

            System.out.println("Dimensions: " + info.dim);
            System.out.println("Flags: " + info.flags);
            System.out.println("Swizzle: " + info.swizzle);
            System.out.println("Number of Mipmaps: " + (info.numMips - 1));

            String formatDescription = formats.get((int)info.format_);

            System.out.println("Format: " + Objects.requireNonNullElseGet(formatDescription, () -> info.format_));

            System.out.println("Width: " + info.width);
            System.out.println("Height: " + info.height);
            System.out.println("Number of faces: " + info.numFaces);
            System.out.println("Size Range: " + info.sizeRange);
            System.out.println("Block Height: " + (1 << info.sizeRange));
            System.out.println("Image Size: " + info.imageSize);
            System.out.println("Alignment: " + info.alignment);
            System.out.println("Channel 1: " + compSels.get(compSel.get(3)));
            System.out.println("Channel 2: " + compSels.get(compSel.get(2)));
            System.out.println("Channel 3: " + compSels.get(compSel.get(1)));
            System.out.println("Channel 4: " + compSels.get(compSel.get(0)));
            System.out.println("Image type: " + types.getOrDefault(info.type_, "Unknown"));

            var dataAddr = unpackLong(f, info.ptrsAddr, bom);
            var mipOffsets = new HashMap<Integer, Long>();

            for (int j = 1; j < info.numMips; j++) {
                var mipOffset = unpackLong(f, info.ptrsAddr + (i * 8), bom);
                mipOffsets.put(j, mipOffset - dataAddr);
            }

            var tex = new TexInfo();
            tex.name = name;
            tex.tileMode = info.tileMode;
            tex.numMips = info.numMips;
            tex.mipOffsets = mipOffsets;
            tex.width = info.width;
            tex.height = info.height;
            tex.format = info.format_;
            tex.numFaces = info.numFaces;
            tex.sizeRange = info.sizeRange;
            tex.compSel = compSel;
            tex.alignment = info.alignment;
            tex.type = info.type_;
            tex.data = Arrays.copyOfRange(f, (int) dataAddr, (int) (dataAddr + info.imageSize));

            textures.add(tex);
        }

        return textures;
    }

    public static int unpackUnsignedShort(byte[] data, int start, ByteOrder bom) {
        ByteBuffer buffer = ByteBuffer.wrap(data, start, 2);
        buffer.order(bom);
        var s = buffer.getShort();

        return Short.toUnsignedInt(s);
    }


    public static ByteOrder detectByteOrder(byte[] data) {
        if (data[0xc] == (byte) 0xFF && data[0xd] == (byte) 0xFE) {
            return ByteOrder.LITTLE_ENDIAN;
        } else if (data[0xc] == (byte) 0xFE && data[0xd] == (byte) 0xFF) {
            return ByteOrder.BIG_ENDIAN;
        } else {
            throw new IllegalArgumentException("Invalid BOM!");
        }
    }

    public static long unpackLong(byte[] data, long pos, ByteOrder bom) {
        ByteBuffer buffer = ByteBuffer.wrap(data, (int) pos, 8);
        buffer.order(bom);
        var s = buffer.getLong();

        return s;
    }


    public static String bytesToString(byte[] data, int start, int length) {
        byte[] subArray = new byte[length];
        System.arraycopy(data, start, subArray, 0, length);
        return new String(subArray, StandardCharsets.US_ASCII);
    }

    public static class BRTIInfo {
        private static final int STRUCT_SIZE = 120;

        private String magic;
        private int size_;
        private long size_2;
        private byte tileMode;
        private byte dim;
        private int flags;
        private int swizzle;
        private int numMips;
        private long unk18;
        private long format_;
        private long unk20;
        private int width;
        private int height;
        private int unk2C;
        private int numFaces;
        private int sizeRange;
        private long unk38;
        private long unk3C;
        private long unk40;
        private long unk44;
        private long unk48;
        private long unk4C;
        private int imageSize;
        private int alignment;
        private int compSel;
        private int type_;
        private long nameAddr;
        private long parentAddr;
        private long ptrsAddr;

        public void parse(byte[] data, int pos, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(data, pos, STRUCT_SIZE);
            buffer.order(byteOrder);

            byte[] magicBytes = new byte[4];
            buffer.get(magicBytes);
            this.magic = new String(magicBytes);

            this.size_ = buffer.getInt();
            this.size_2 = buffer.getLong();
            this.tileMode = buffer.get();
            this.dim = buffer.get();
            this.flags = Short.toUnsignedInt(buffer.getShort());
            this.swizzle = Short.toUnsignedInt(buffer.getShort());
            this.numMips = Short.toUnsignedInt(buffer.getShort());
            this.unk18 = Integer.toUnsignedLong(buffer.getInt());
            this.format_ = Integer.toUnsignedLong(buffer.getInt());
            this.unk20 = Integer.toUnsignedLong(buffer.getInt());
            this.width = buffer.getInt();
            this.height = buffer.getInt();
            this.unk2C = buffer.getInt();
            this.numFaces = buffer.getInt();
            this.sizeRange = buffer.getInt();
            this.unk38 = Integer.toUnsignedLong(buffer.getInt());
            this.unk3C = Integer.toUnsignedLong(buffer.getInt());
            this.unk40 = Integer.toUnsignedLong(buffer.getInt());
            this.unk44 = Integer.toUnsignedLong(buffer.getInt());
            this.unk48 = Integer.toUnsignedLong(buffer.getInt());
            this.unk4C = Integer.toUnsignedLong(buffer.getInt());
            this.imageSize = buffer.getInt();
            this.alignment = buffer.getInt();
            this.compSel = buffer.getInt();
            this.type_ = buffer.getInt();
            this.nameAddr = buffer.getLong();
            this.parentAddr = buffer.getLong();
            this.ptrsAddr = buffer.getLong();
        }
    }

    public static class NXHeader {
        public static final int STRUCT_SIZE = 36;

        private String magic;
        private long count;
        private long infoPtrAddr;
        private long dataBlkAddr;
        private long dictAddr;
        private long strDictSize;

        public void parse(byte[] data, int pos, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(data, pos, STRUCT_SIZE);
            buffer.order(byteOrder);

            byte[] magicBytes = new byte[4];
            buffer.get(magicBytes);
            this.magic = new String(magicBytes);
            this.count = Integer.toUnsignedLong(buffer.getInt());
            this.infoPtrAddr = buffer.getLong();
            this.dataBlkAddr = buffer.getLong();
            this.dictAddr = buffer.getLong();
            this.strDictSize = Integer.toUnsignedLong(buffer.getInt());
        }
    }

    public static class BNTXHeader {
        private static final int STRUCT_SIZE = 32;

        private String magic;
        private int version;
        private int bom;
        private int revision;
        private int fileNameAddr;
        private int strAddr;
        private int relocAddr;
        private int fileSize;

        public BNTXHeader() {
        }

        public void parse(byte[] data, int pos, ByteOrder byteOrder) {
            ByteBuffer buffer = ByteBuffer.wrap(data, pos, STRUCT_SIZE);
            buffer.order(byteOrder);

            byte[] magicBytes = new byte[8];
            buffer.get(magicBytes);
            magic = new String(magicBytes).trim();

            version = buffer.getInt();
            bom = buffer.getShort() & 0xFFFF;
            revision = buffer.getShort() & 0xFFFF;
            fileNameAddr = buffer.getInt();
            buffer.position(buffer.position() + 2); // Skip 2 padding bytes
            strAddr = buffer.getShort();
            relocAddr = buffer.getInt();
            fileSize = buffer.getInt();
        }
    }

    public static List<Integer> processCompSel(int compSel) {
        List<Integer> compSelList = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int value = (compSel >> (8 * (3 - i))) & 0xff;
            if (value == 0) {
                value = compSelList.size() + 2;
            }

            compSelList.add(value);
        }

        return compSelList;
    }

    private static void displayImage(BufferedImage image, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private static class TexInfo {
        public String name;
        public byte tileMode;
        public long numMips;
        public HashMap<Integer, Long> mipOffsets;
        public int width;
        public int height;
        public long format;
        public int numFaces;
        public int sizeRange;
        public List<Integer> compSel;
        public int alignment;
        public int type;
        public byte[] data;
    }

    public static void saveTextures(List<TexInfo> textures) {
        for(var tex : textures) {
            String format_;
            if(formats.containsKey((int) tex.format) && tex.numFaces < 2) {
                if((tex.format >> 8) == 0x20) {
                    format_ = "BC7";
                }

                var pair = blk_dims.getOrDefault((int) tex.format >> 8, new Pair<>(1,1));

                var bpp = bpps.get((int) tex.format >> 8);

                var size = DIV_ROUND_UP(tex.width, pair.a()) * DIV_ROUND_UP(tex.height, pair.b());

                 var data = Swizzle.deswizzle(tex.width, tex.height, pair.a(), pair.b(), bpp, tex.tileMode, tex.alignment, tex.sizeRange, tex.data);

                System.out.println();
            }
        }
    }

    public static int DIV_ROUND_UP(int n, int d) {
        return (n + d - 1) / d;
    }
}
