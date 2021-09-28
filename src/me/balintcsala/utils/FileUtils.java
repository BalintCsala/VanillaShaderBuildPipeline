package me.balintcsala.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileUtils {

    public static Path getMinecraftPath() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        Path minecraftDir;
        if (os.contains("windows")) {
            // Windows
            minecraftDir = Paths.get(System.getenv("APPDATA"), ".minecraft");
        } else if (os.contains("mac")) {
            // Mac
            minecraftDir = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "minecraft");
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            // Linux/Unix
            minecraftDir = Paths.get(System.getProperty("user.home"), ".minecraft");
        } else {
            return null;
        }

        if (Files.notExists(minecraftDir)) {
            return null;
        }

        return minecraftDir;
    }

    public static void deleteDirectory(Path dir) {
        try {
            Stream<Path> fileStream = Files.list(dir);
            fileStream.forEach(path -> {
                Path file = dir.resolve(path);
                if (Files.isDirectory(file)) {
                    deleteDirectory(file);
                } else {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        System.out.println("Couldn't delete file " + file);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Files.delete(dir);
        } catch (IOException e) {
            System.out.println("Couldn't delete directory");
        }
    }

    public static void copyDirectory(Path dir, Path to) {
        String[] list = dir.toFile().list();
        if (list == null)
            return;
        for (String path : list) {
            Path file = dir.resolve(path);
            Path target = to.resolve(path);
            if (Files.isDirectory(file)) {
                copyDirectory(file, target);
            } else {
                try {
                    Files.createDirectories(target.getParent());
                    Files.copy(file, target, REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
