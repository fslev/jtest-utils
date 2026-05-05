package io.jtest.utils.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads files from either the classpath or an absolute filesystem path, with helpers for
 * common test-fixture formats (plain text, properties, YAML) and bulk directory reads.
 *
 * <p>Every {@code filePath} parameter accepts both forms:
 * <ul>
 *   <li>A <b>relative path</b> resolved against the current thread's context classloader,
 *       e.g. {@code "fixtures/config.yaml"}.</li>
 *   <li>An <b>absolute filesystem path</b>, e.g. {@code "/var/tmp/data.json"}.</li>
 * </ul>
 *
 * <p>If the path does not resolve to a readable file, an {@link IOException} is thrown
 * with the offending path included in the message.
 */
public class ResourceUtils {

    private ResourceUtils() {

    }

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Reads the file at {@code filePath} as a UTF-8 string.
     *
     * @param filePath classpath-relative or absolute path
     * @return file contents as a UTF-8 string
     * @throws IOException if the file is not found or cannot be read
     */
    public static String read(String filePath) throws IOException {
        return readFromPath(filePath);
    }

    /**
     * Reads the file at {@code filePath} as a Java {@link Properties} document.
     * The file is decoded as UTF-8 before being parsed.
     *
     * @param filePath classpath-relative or absolute path to a {@code .properties} file
     * @return parsed properties
     * @throws IOException if the file is not found, cannot be read, or is not valid
     *                     properties syntax
     */
    public static Properties readProps(String filePath) throws IOException {
        Properties props = new Properties();
        props.load(new StringReader(read(filePath)));
        return props;
    }

    /**
     * Reads the file at {@code filePath} as a YAML document and returns it as a tree
     * of nested {@link Map}s and {@link java.util.List}s, mirroring the YAML structure.
     *
     * @param filePath classpath-relative or absolute path to a {@code .yaml} / {@code .yml} file
     * @return the parsed YAML root, or {@code null} if the file is empty
     * @throws IOException if the file is not found or cannot be read
     */
    public static Map<String, Object> readYaml(String filePath) throws IOException {
        try (InputStream is = getInputStream(filePath)) {
            return new Yaml().load(is);
        }
    }

    /**
     * Recursively reads every file under {@code dirPath} whose extension matches one of
     * {@code fileExtensionPatterns}, returning a map from the file's relative path to its
     * UTF-8 content.
     *
     * <p>If no extensions are given, every regular file under the directory is read.
     * Files with non-matching extensions are skipped and logged at WARN level.
     *
     * @param dirPath               classpath-relative or absolute path to a directory
     * @param fileExtensionPatterns extensions to include, each starting with a dot
     *                              (e.g. {@code ".json", ".txt"}); empty for "all files"
     * @return relative file path → file contents
     * @throws IOException        if {@code dirPath} is not a directory, or a file cannot
     *                            be read
     * @throws URISyntaxException if a classpath URL cannot be converted to a file path
     */
    public static Map<String, String> readDirectory(String dirPath, String... fileExtensionPatterns) throws IOException, URISyntaxException {
        Set<String> files = getFilesFromDir(dirPath, fileExtensionPatterns);
        return files.stream().collect(Collectors.toMap(filePath -> filePath,
                filePath -> {
                    try {
                        return readFromPath(filePath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
    }

    /**
     * Recursively lists every file under {@code dirPath} whose extension matches one of
     * {@code fileExtensionPatterns}.
     *
     * <p>Returned paths are <em>relative to {@code dirPath}</em>, prefixed with
     * {@code dirPath} itself — they can be passed straight back to {@link #read(String)}.
     * If no extensions are given, every regular file under the directory is returned.
     *
     * @param dirPath               classpath-relative or absolute path to a directory
     * @param fileExtensionPatterns extensions to include, each starting with a dot
     *                              (e.g. {@code ".json", ".txt"}); empty for "all files"
     * @return set of file paths matching the extension filter
     * @throws IOException        if {@code dirPath} is not a directory
     * @throws URISyntaxException if a classpath URL cannot be converted to a file path
     */
    public static Set<String> getFilesFromDir(String dirPath, String... fileExtensionPatterns) throws IOException, URISyntaxException {
        Path rootPath = getPath(dirPath);
        if (!Files.isDirectory(rootPath)) {
            throw new IOException("Not a directory " + rootPath);
        }
        Set<String> extensions = new HashSet<>(Arrays.asList(fileExtensionPatterns));
        try (Stream<Path> stream = Files.walk(rootPath).filter(path -> isMatchingFile(path, extensions))) {
            return stream.map(path -> dirPath + (!dirPath.isEmpty() ? File.separator : "") + rootPath.relativize(path))
                    .collect(Collectors.toSet());
        }
    }

    private static boolean isMatchingFile(Path path, Set<String> extensions) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        if (extensions.isEmpty()) {
            return true;
        }
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0 && extensions.contains(fileName.substring(dotIndex))) {
            return true;
        }
        LOG.warn("Ignore file '{}'." + System.lineSeparator() + "It has none of the following extensions: {}",
                fileName, extensions);
        return false;
    }

    private static String readFromPath(String filePath) throws IOException {
        try (InputStream is = getInputStream(filePath); ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8.name());
        }
    }

    /**
     * Returns the file name component (the last path segment) of {@code filePath},
     * resolving classpath references the same way as {@link #read(String)}.
     *
     * @param filePath classpath-relative or absolute path
     * @return the file's name (no directories, with extension if any)
     * @throws IOException        if the file is not found
     * @throws URISyntaxException if a classpath URL cannot be converted to a file path
     */
    public static String getFileName(String filePath) throws IOException, URISyntaxException {
        return getPath(filePath).getFileName().toString();
    }

    private static Path getPath(String filePath) throws IOException, URISyntaxException {
        Path path;
        if (!isAbsolute(filePath)) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
            if (url == null) {
                throw new IOException("File " + filePath + " not found or directory might be empty");
            }
            path = Paths.get(url.toURI());
        } else {
            path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new IOException("File " + filePath + " not found");
            }
        }
        return path;
    }

    private static InputStream getInputStream(String filePath) throws IOException {
        InputStream is = !isAbsolute(filePath) ? Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath)
                : Files.newInputStream(Paths.get(filePath));
        if (is == null) {
            throw new IOException("File " + filePath + " not found or directory might be empty");
        }
        return is;
    }

    private static boolean isAbsolute(String path) {
        return Paths.get(path).isAbsolute();
    }
}
