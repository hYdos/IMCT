package gg.generations.imct.util;

import java.util.List;

public class DDS {
    public static String[] dx10_formats = {"BC4U", "BC4S", "BC5U", "BC5S", "BC6H_UF16", "BC6H_SF16", "BC7"};
    public static void generHeader(int num_mipmaps, int width, int height, BntxExtractor.Format format, List<Integer> compSel, boolean isBcn) {
        byte[] hdr = new byte[128];

        boolean luminance = false;
        boolean rgb = false;

        boolean has_alpha = true;

//        if(for)

        if (format.equals(28)) {
            rgb = true;

        }
    }
}
