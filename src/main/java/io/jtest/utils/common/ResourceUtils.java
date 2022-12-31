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

public class ResourceUtils {

    private ResourceUtils() {

    }

    private static final Logger LOG = LogManager.getLogger();

    public static String read(String filePath) throws IOException {
        return readFromPath(filePath);
    }

    public static Properties readProps(String filePath) throws IOException {
        Properties props = new Properties();
        props.load(new StringReader(read(filePath)));
        return props;
    }

    public static Map<String, Object> readYaml(String filePath) throws IOException {
        try (InputStream is = getInputStream(filePath)) {
            return new Yaml().load(is);
        }
    }

    /**
     * @return a Map&lt;String,String&gt; between corresponding relative file paths and file contents
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
     * Returns relative file paths from a directory matching given extensions
     *
     * @param dirPath
     * @param fileExtensionPatterns
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Set<String> getFilesFromDir(String dirPath, String... fileExtensionPatterns) throws IOException, URISyntaxException {
        Path rootPath = getPath(dirPath);
        if (!Files.isDirectory(rootPath)) {
            throw new IOException("Not a directory " + rootPath);
        }
        try (Stream<Path> stream = Files.walk(rootPath).filter(path -> {
            if (!path.toFile().isFile()) {
                return false;
            }
            if (fileExtensionPatterns.length == 0 || (path.getFileName().toString().contains(".")
                    && new HashSet<>(Arrays.asList(fileExtensionPatterns))
                    .contains(path.getFileName().toString().substring(path.getFileName().toString().lastIndexOf("."))))) {
                return true;
            }
            LOG.warn("Ignore file '{}'." + System.lineSeparator() + "It has none of the following extensions: {}",
                    path.getFileName().toString(), fileExtensionPatterns);
            return false;
        })) {
            return stream.map(path -> dirPath + (!dirPath.isEmpty() ? File.separator : "") + rootPath.relativize(path))
                    .collect(Collectors.toSet());
        }
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

    public static String getFileName(String filePath) throws IOException, URISyntaxException {
        return getPath(filePath).getFileName().toString();
    }

    private static Path getPath(String filePath) throws IOException, URISyntaxException {
        Path path;
        if (!isAbsolute(filePath)) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
            if (url == null) {
                throw new IOException("File " + filePath + " not found");
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
            throw new IOException("File " + filePath + " not found");
        }
        return is;
    }

    private static boolean isAbsolute(String path) {
        return Paths.get(path).isAbsolute();
    }
}
