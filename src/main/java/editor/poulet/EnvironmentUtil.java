package editor.poulet;

import poulet.superficial.imports.ImportHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentUtil {
    public static List<File> directories = new ArrayList<>();
    public static List<Boolean> recursive = new ArrayList<>();

    public static void setEnvironment(List<File> directories, List<Boolean> recursive) {
        EnvironmentUtil.directories = directories;
        EnvironmentUtil.recursive = recursive;
        ImportHandler.directories = directories.stream().map(x -> x.getAbsolutePath()).collect(Collectors.toList());
        ImportHandler.recursive = recursive;
    }
}
