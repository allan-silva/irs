package br.dev.contrib;

import java.io.InputStream;

public class ResourcesUtils {
    public static InputStream getResourceFileStream(String path) {
        assert path != null;
        return ResourcesUtils.class.getClassLoader().getResourceAsStream(path);
    }
}
