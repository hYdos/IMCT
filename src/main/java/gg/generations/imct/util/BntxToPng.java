package gg.generations.imct.util;

import org.im4java.utils.BaseFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;

public class BntxToPng {
    public static void main(String[] args) throws IOException, InterruptedException {
        Path exe = Paths.get("C:\\Users\\water\\Downloads\\quickbms\\quickbms.exe");
        Path texcon_exe = Paths.get("C:\\Users\\water\\Downloads\\texconv.exe");
        Path folder = Paths.get("C:\\Users\\water\\Downloads\\test");
        Path bms = Paths.get("C:\\Users\\water\\Downloads\\bnt_dds.bms");
        convert(exe, bms, texcon_exe, folder);
    }

    public static void convert(Path exe, Path bms, Path texcon_exe, Path folder) throws IOException, InterruptedException {
        var builder = new ProcessBuilder();
        builder.inheritIO().directory(folder.toFile()).command(exe.toString(), bms.toString(), folder.toString()).start().waitFor();

        var builder1 = new ProcessBuilder();
        builder1.inheritIO().directory(folder.toFile()).command(
                texcon_exe.toString(), "-r", folder.toString() + "\\*.dds", "-ft", "png"
        ).start().waitFor();



        System.out.println("Rawr!");
    }
}
