package me.balintcsala;

import me.balintcsala.build.ShaderBuilder;
import me.balintcsala.utils.Arguments;
import me.balintcsala.utils.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Main {

    public static Scanner scanner;

    private Arguments arguments;
    private Path resourcepackFolder;
    private Path sourceDirectory;

    private void displayHelp() {
        System.out.println("To build the shader into a distributable version:");
        System.out.println("java -jar VSBP.jar build <output directory>");
        System.out.println("  OR");
        System.out.println("To create a development environment with automatically updating files:");
        System.out.println("java -jar VSBP.jar watch <directory> [-e/--excludes <file list>]");
    }

    private void handleFile(Path path, boolean cacheFile) {
        try {
            Path relativePath = sourceDirectory.relativize(path);
            String strPath = path.toString();
            if (relativePath.startsWith("shaders") && strPath.endsWith(".json")) {
                Path fileInResourcepackFolder = resourcepackFolder.resolve(Paths.get("assets", "minecraft")).resolve(relativePath);
                Files.createDirectories(fileInResourcepackFolder.getParent());
                Files.copy(path, fileInResourcepackFolder, REPLACE_EXISTING);
            } else if (strPath.endsWith(".glsl") || strPath.endsWith(".vsh") || strPath.endsWith(".fsh")) {
                // Shader file, process first
                Path fileInResourcepackFolder = resourcepackFolder.resolve(Paths.get("assets", "minecraft")).resolve(relativePath);
                Files.createDirectories(fileInResourcepackFolder.getParent());
                String content = ShaderBuilder.buildShaderFile(path, sourceDirectory.resolve("shaders"), cacheFile);
                FileOutputStream outputStream = new FileOutputStream(fileInResourcepackFolder.toFile());
                outputStream.write(content.getBytes());
                outputStream.close();
            } else {
                // Ordinary file, copy over without change
                Path fileInResourcepackFolder = resourcepackFolder.resolve(relativePath);
                Files.createDirectories(fileInResourcepackFolder.getParent());
                Files.copy(path, fileInResourcepackFolder, REPLACE_EXISTING);
            }
            System.out.println("Processed file: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildAllFiles(boolean cacheFiles) {
        try {
            Files.walk(sourceDirectory).forEach(path -> {
                if (Files.isDirectory(path))
                    return;
                handleFile(path, cacheFiles);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void commandWatch(String[] args) {
        if (Files.notExists(sourceDirectory)) {
            System.err.println("This directory doesn't exist");
            return;
        }

        buildAllFiles(false);

        ArrayList<String> excludes = arguments.getListArgument("exclude");
        new FileWatcher(sourceDirectory, excludes, (path, changeType) -> {
            if (Files.isDirectory(path)) {
                // Skip directories and empty files
                return;
            }

            handleFile(path, false);
        });
    }

    private void commandBuild(String[] args) {
        ShaderBuilder.clearCache();
        buildAllFiles(true);
    }

    private void start(String[] args) {
        if (args.length < 2) {
            displayHelp();
            return;
        }
        sourceDirectory = Paths.get(args[1]);
        arguments = new Arguments()
                .registerListArgument("exclude", "e")
                .parse(args);

        Path minecraftPath = FileUtils.getMinecraftPath();
        while (minecraftPath == null || !Files.exists(minecraftPath)) {
            System.out.println("Couldn't find the .minecraft folder in the " + ((minecraftPath == null) ? "default" : "specified") + " location");
            System.out.println("Please enter it manually:");
            minecraftPath = Paths.get(scanner.nextLine());
        }
        resourcepackFolder = minecraftPath.resolve("resourcepacks").resolve(sourceDirectory.getFileName());

        switch (args[0]) {
            case "watch":
                commandWatch(args);
                break;
            case "build":
                commandBuild(args);
                break;
            default:
                displayHelp();
                break;
        }
    }

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        new Main().start(args);
    }
}
