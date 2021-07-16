package ru.zsc.zoo;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Pavel Perepech
 */
public interface ZooAccessor {

    void init(String connectionString, String user, String password);

    Map<String, byte[]> readLevel(String path);

    Properties readNodeAsProperty(String path, List<String> excludedPatch);

    void storeProperty(String path, Properties properties, List<String> excludedPatch);
}
