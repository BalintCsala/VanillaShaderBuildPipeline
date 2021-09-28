package me.balintcsala.build;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderBuilder {

    private static final Pattern IMPORT_EXTRACTOR = Pattern.compile("^#\\s*vsbp_import\\s*<\\s*([^\\s<>]+)\\s*>");

    private static final HashMap<Path, String> cache = new HashMap<>();

    public static String buildShaderFile(Path path, Path shaderFolder, boolean cacheFile) {
        if (cacheFile && cache.containsKey(path))
            return cache.get(path);

        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            int lineIndex = 1;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = IMPORT_EXTRACTOR.matcher(line);
                if (!matcher.find()) {
                    builder.append(line).append("\n");
                    continue;
                }
                Path included = shaderFolder.resolve(matcher.group(1));
                if (Files.notExists(included)) {
                    System.out.println("Missing included file: " + included);
                    System.out.println("Included in: " + path + ":" + lineIndex);
                    builder.append("#error Missing file: ").append(path);
                }
                builder.append(buildShaderFile(included, shaderFolder));
                lineIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String res = builder.toString();
        if (cacheFile) {
            cache.put(path, res);
        }
        return res;
    }

    public static String buildShaderFile(Path path, Path shaderFolder) {
        return buildShaderFile(path, shaderFolder, false);
    }

    public static void clearCache() {
        cache.clear();
    }

}
