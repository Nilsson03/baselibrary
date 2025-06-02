package ru.nilsson03.library.load;

import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

public class LibraryLoader {
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.00");
    private final File dataFolder;
    private final Set<File> loadedLibs = new LinkedHashSet<>();

    public LibraryLoader(File dataFolder) {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IllegalStateException("Cannot create libs directory");
        }
        this.dataFolder = dataFolder;
    }

    /**
     * Загружает библиотеку из URL
     * @param jarUrl Прямая ссылка на JAR-файл
     * @param fileName Имя файла для сохранения (если null, берется из URL)
     */
    public boolean loadLibrary(String jarUrl, String fileName) {
        long startTime = System.nanoTime();
        String displayName = (fileName != null) ? fileName : jarUrl.substring(jarUrl.lastIndexOf('/') + 1);

        try {
            ConsoleLogger.info("baselibrary", "Loading the library %s", displayName);

            URL url = new URL(jarUrl);
            File outputFile = new File(dataFolder, (fileName != null) ? fileName : getFileNameFromUrl(jarUrl));

            try (InputStream in = url.openStream();
                 ReadableByteChannel rbc = Channels.newChannel(in);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {

                long totalBytes = getFileSize(url);
                ConsoleLogger.info("baselibrary", "File size to upload %s.", formatSize(totalBytes));

                long bytesRead = fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                if (bytesRead != totalBytes && totalBytes > 0) {
                    throw new IOException("Incomplete download (" + bytesRead + "/" + totalBytes + " bytes)");
                }
            }

            long duration = System.nanoTime() - startTime;
            ConsoleLogger.info("baselibrary", "%s file uploaded successfully in %s ms", displayName, TimeUnit.NANOSECONDS.toMillis(duration));

            loadedLibs.add(outputFile);
            return true;

        } catch (Exception e) {
            ConsoleLogger.error("baselibrary", "An error occurred while loading the required file %s (ex %s).",
                    displayName,
                    e.getMessage());
            return false;
        }
    }

    public void loadLibraries(String... jarUrls) {
        for (String url : jarUrls) {
            if (!loadLibrary(url, null)) {
                ConsoleLogger.warn("baselibrary","The download of the necessary files was interrupted due to an error.");
                break;
            }
        }
    }

    private boolean isValidJar(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Set<File> getLoadedLibs() {
        return new LinkedHashSet<>(loadedLibs);
    }

    private String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private long getFileSize(URL url) throws IOException {
        return url.openConnection().getContentLengthLong();
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) return "? MB";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return SIZE_FORMAT.format(bytes / 1024.0) + " KB";
        return SIZE_FORMAT.format(bytes / (1024.0 * 1024.0)) + " MB";
    }
}