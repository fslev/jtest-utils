package io.jtest.utils.common;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.json.compare.util.JsonUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceReadTests {

    @Test
    public void testReadFromInvalidFilePath() throws IOException {
        assertTrue(assertThrows(IOException.class, () -> ResourceUtils.read("i/dont/exist"))
                .getMessage().contains("not found"));
    }

    @Test
    public void testStringReadFromRelativePathFile() throws IOException {
        assertEquals("some content 1\nsome content 2\n", ResourceUtils.read("foobar/file1.txt"));
    }

    @Test
    public void testStringReadFromAbsolutePathFile() {
        assertThrowsExactly(NoSuchFileException.class, () -> ResourceUtils.read("/i/dont/exist"));
    }

    @Test
    public void testJsonReadingFromFile() throws IOException {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.putObject("menu").put("show", true);
        assertEquals(json, JsonUtils.toJson(ResourceUtils.read("foobar/file1.json")));
    }

    @Test
    public void testPropertiesReadFromFile() throws IOException {
        assertEquals("some values with white spaces and new lines \n ",
                ResourceUtils.readProps("foobar/foo.properties").get("IAmAProperty"));
    }

    @Test
    public void testPropertiesReadFromFileWithoutExtension() throws IOException {
        assertEquals("some values with white spaces and new lines \n ",
                ResourceUtils.readProps("foobar/dir/foo/foo").get("IAmAProperty"));
    }

    @Test
    public void testInDepthReadFromDirectory() throws IOException, URISyntaxException {
        Map<String, String> actualData = ResourceUtils.readDirectory("foobar/dir");
        assertEquals(7, actualData.size());
        assertEquals("1", actualData.get("foobar/dir/foobar1.json"));
        assertEquals("2", actualData.get("foobar/dir/foo/foo1.json"));
        assertEquals("3", actualData.get("foobar/dir/foo/foo2.json"));
        assertEquals("4", actualData.get("foobar/dir/foo/bar/bar1.json"));
        assertEquals("5", actualData.get("foobar/dir/foo/bar/bar2.json"));
        assertEquals("test", actualData.get("foobar/dir/foo/bar/bar"));
    }

    @Test
    public void testInDepthReadFromDirectoryFilesWithoutExtension() throws IOException, URISyntaxException {
        Map<String, String> actualData = ResourceUtils.readDirectory("foobar/dir1", ".properties");
        assertEquals(1, actualData.size());
        assertEquals("pass", actualData.get("foobar/dir1/test2.properties"));
    }

    @Test
    public void testInDepthReadFromNonExistentDirectory() {
        assertThrows(IOException.class, () -> ResourceUtils.readDirectory("non_existent"));
    }

    @Test
    public void testInDepthReadFromDirectoryWhichIsAFile() {
        assertThrows(IOException.class, () -> ResourceUtils.readDirectory("foobar/file1.txt"));
    }

    @Test
    public void testInDepthReadFromClasspathDir() throws IOException, URISyntaxException {
        Map<String, String> actualData = ResourceUtils.readDirectory("");
        assertTrue(actualData.size() > 0);
        assertEquals("1", actualData.get("foobar/dir/foobar1.json"));
        assertEquals("2", actualData.get("foobar/dir/foo/foo1.json"));
        assertEquals("3", actualData.get("foobar/dir/foo/foo2.json"));
        assertEquals("4", actualData.get("foobar/dir/foo/bar/bar1.json"));
        assertEquals("5", actualData.get("foobar/dir/foo/bar/bar2.json"));
    }

    @Test
    public void readFromInvalidDirectoryPath() {
        assertTrue(assertThrows(IOException.class, () -> ResourceUtils.readDirectory("i/dont/exist"))
                .getMessage().contains("not found"));
    }

    @Test
    public void testReadFromYamlFile() throws IOException {
        Map<String, Object> expected = new HashMap<>();
        List<String> items = new ArrayList<>();
        items.add("test");
        items.add("test1");
        items.add("test2");
        expected.put("list", items);
        assertEquals(expected, ResourceUtils.readYaml("yaml/config.yaml"));
    }

    @Test
    public void testReadFromInvalidYamlFile() {
        assertThrows(ClassCastException.class, () -> ResourceUtils.readYaml("foobar/file1.txt"));
    }

    @Test
    public void testReadFromMissingYamlFile() {
        assertThrows(IOException.class, () -> ResourceUtils.readYaml("i/do/not/exist"));
    }

    @Test
    public void testReadFromEmptyYamlFile() throws IOException {
        assertNull(ResourceUtils.readYaml("yaml/empty.yaml"));
    }

    @Test
    public void testGetFileName() throws IOException, URISyntaxException {
        assertEquals("config.yaml", ResourceUtils.getFileName("yaml/config.yaml"));
    }
}
