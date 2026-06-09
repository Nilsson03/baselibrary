package ru.nilsson03.library.file.util;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FileUtils {

    public static Path getOrCreateDirectory(Path path) throws IOException {
        validatePath(path);

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        } else if (!Files.isDirectory(path)) {
            throw new IOException("Path is not a directory: " + path);
        }
        return path;
    }

    public static Path createFileOrLoad(Path directory, String fileName) {
        Objects.requireNonNull(directory, "directory cannot be null");
        validateFileName(fileName);

        Path file = directory.resolve(fileName);
        if (!Files.exists(file)) {
            try {
                Files.createFile(file);
            } catch (IOException | SecurityException e) {
                ConsoleLogger.warn("baselibrary", "Failed to create %s due to %s", fileName, e.getMessage());
            }
        }
        return file;
    }

    public static Path copyResourceIfNotExists(NPlugin plugin, Path targetPath, String resourcePath) throws IOException {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(targetPath, "targetPath cannot be null");
        validateFileName(resourcePath);

        if (!Files.exists(targetPath.getParent())) {
            Files.createDirectories(targetPath.getParent());
        }

        if (!Files.exists(targetPath) || Files.size(targetPath) == 0) {
            try (InputStream input = plugin.getResource(resourcePath)) {
                if (input != null) {
                    Files.copy(input, targetPath);
                    ConsoleLogger.info(plugin, "Successfully copied default resource: %s", targetPath);
                } else {
                    Files.createFile(targetPath);
                    ConsoleLogger.info(plugin, "Created EMPTY file: %s", targetPath);
                }
            }
        }

        return targetPath;
    }

    private static void validateFileName(String fileName) throws IllegalArgumentException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("fileName cannot be null or empty");
        }
        if (fileName.contains("..") || fileName.startsWith("/")) {
            throw new IllegalArgumentException("fileName contains invalid characters");
        }
    }

    private static void validatePath(Path path) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }
    }
}
