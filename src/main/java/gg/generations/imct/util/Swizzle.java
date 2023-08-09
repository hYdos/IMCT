package gg.generations.imct.util;

import static gg.generations.imct.util.BntxExtractor.DIV_ROUND_UP;

public class Swizzle {
    public static int roundUp(int x, int y) {
        return ((x - 1) | (y - 1)) + 1;
    }

    public static byte[] deswizzle(int width, int height, Integer blkWidth, Integer blkHeight, Integer bpp, byte tileMode, int alignment, int sizeRange, byte[] data) {
        assert 0 <= sizeRange && sizeRange <= 5;
        var block_height = 1 << sizeRange;
        width = DIV_ROUND_UP(width, blkWidth);
        height = DIV_ROUND_UP(height, blkHeight);

        int pitch;
        int surfSize;
        if (tileMode == 0) {
            pitch = roundUp(width * bpp, 32);
            surfSize = roundUp(pitch * height * 8, alignment);
        } else {
            pitch = roundUp(width * bpp, 64);
            surfSize = roundUp(pitch * roundUp(height, block_height * 8), alignment);
        }

        var result = new byte[surfSize];

        int pos;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tileMode == 0) {
                    pos = y * pitch + x * bpp;
                } else {
                    pos = getAddrBlockLinear(x, y, width, bpp, 0, block_height);
                }

                var pos_ = (y * width + x) * bpp;

                if(pos + bpp <= surfSize) {
                    System.arraycopy(data, pos, result, pos_, bpp);
                }
            }
        }

        System.out.println(surfSize);

        return result;
    }

    public static int getAddrBlockLinear(int x, int y, int imageWidth, int bytesPerPixel, int baseAddress, int blockHeight) {
        int imageWidthInGOBs = DIV_ROUND_UP(imageWidth * bytesPerPixel, 64);

        var GOBAddress = (baseAddress
                + (y / (8 * blockHeight)) * 512 * blockHeight * imageWidthInGOBs
                + (x * bytesPerPixel / 64) * 512 * blockHeight
                + (y % (8 * blockHeight) / 8) * 512);

        x *= bytesPerPixel;

        var address = (GOBAddress + ((x % 64) / 32) * 256 + ((y % 8) / 2) * 64
                + ((x % 32) / 16) * 32 + (y % 2) * 16 + (x % 16));

        return address;
    }
}