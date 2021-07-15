package ru.zsc.zoo.impl;

import static java.util.Optional.ofNullable;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.utils.ZKPaths;
import ru.zsc.util.ZooAccessException;
import ru.zsc.zoo.ZooAccessor;

/**
 * Created by Pavel Perepech
 */
public class ZooAccessorImpl implements ZooAccessor {

    private CuratorFramework zoo;

    @Override
    public void init(String connectionString) {
        ofNullable(zoo).ifPresent(curatorFramework -> curatorFramework.close());
        zoo = CuratorFrameworkFactory.newClient(
                connectionString,
                new RetryOneTime(100));
        zoo.start();
        try {
            zoo.blockUntilConnected(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Error connect to zookeeper ({0}) caused by: {1}",
                    connectionString, e.getMessage()), e);
        }

        if (!zoo.getZookeeperClient().isConnected()) {
            throw new IllegalStateException(MessageFormat.format(
                    "Error connect to zookeeper ({0})",
                    connectionString));
        }
    }

    @Override
    public Map<String, byte[]> readLevel(String path) {
        final Map<String, byte[]> result = new HashMap<>();
        final List<String> nodes;
        try {
            nodes = zoo.getChildren().forPath(path);
        } catch (Exception e) {
            throw new ZooAccessException(MessageFormat.format("Error reading nodes on {0} caused by: {1}", path, e.getMessage()), e);
        }

        nodes.forEach(node -> {
            final String nodePath = "/".equals(path) ? '/' + node : path + '/' + node;
            final byte[] data;
            try {
                data = zoo.getData().forPath(nodePath);
            } catch (Exception e) {
                throw new ZooAccessException(MessageFormat.format("Error reading node {0} value caused by: {1}", path, e.getMessage()), e);
            }

            result.put(node, data);
        });

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Properties readNodeAsProperty(final String path, final List<String> excludedPatchHolder) {
        final Properties result = new Properties();

        readLevelAsProperties(result, path, path, excludedPatchHolder, null);

        return result;
    }

    private void readLevelAsProperties(
            final Properties properties,
            final String root,
            final String path,
            final List<String> excludedPatchHolder,
            final List<String> childrenNames) {
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid path {0} caused by: it must be start from {1}", path, root));
        }
        String subPath = path.substring(root.length());
        if (subPath.startsWith("/")) {
            if (subPath.length() == 1) {
                subPath = "";
            } else {
                subPath = subPath.substring(1);
            }
        }
        final String prefix = subPath.replaceAll("/", ".");
        final List<String> nodeNames = Objects.nonNull(childrenNames) ? childrenNames: readChildNames(path);

        for(String node: nodeNames) {
            List<String> subNames = null;
            final String nodePath = "/".equals(path) ? path + node: path + "/" + node;
            final String value;
            try {
                value = readPropertyAsString(nodePath);
                final String key = StringUtils.isBlank(prefix) ? node: prefix + "." + node;

                if (value.isEmpty()) {
                    subNames = readChildNames(path + "/" + node);
                    if (subNames.isEmpty()) {
                        properties.setProperty(key, value);
                    }
                } else {
                    properties.setProperty(key, value);
                }
            } catch (Exception e) {
                excludedPatchHolder.add(nodePath);
            }

            readLevelAsProperties(properties, root, path + "/" + node, excludedPatchHolder, subNames);
        }
    }

    private List<String> readChildNames(final String path) {
        try {
            return zoo.getChildren().forPath(path);
        } catch (Exception e) {
            throw new ZooAccessException(MessageFormat.format("Error reading node list on {0} caused by {1}",
                    path, e.getMessage()), e);
        }
    }

    private String readPropertyAsString(final String path) {
        final byte[] data;
        try {
            data = zoo.getData().forPath(path);
        } catch (Exception e) {
            throw new ZooAccessException(MessageFormat.format("Error reading data {0} caused by: {1}",
                    path, e.getMessage()), e);
        }

        if (Objects.isNull(data) || data.length == 0) {
            return "";
        }

        return new String(data);
    }

    @Override
    public void storeProperty(final String path, final Properties properties, final List<String> excludedPath) {
        updatePropertiesValues(path, properties);

        final Set<String> propertiesPaths = getPropertiesPaths(path, properties);
        removeDeletedProperties(path, propertiesPaths, excludedPath);
    }

    private void updatePropertiesValues(final String path, final Properties properties) {
        final String pathPrefix = path.endsWith("/") ? path: path + '/';
        for (Object key : properties.keySet()) {
            final String propertyName = key.toString();

            final String propertyPath = pathPrefix + propertyName.replaceAll("\\.", "/");
            final String propertyValue = properties.get(key).toString();

            try {
                ZKPaths.mkdirs(zoo.getZookeeperClient().getZooKeeper(), propertyPath);
                zoo.setData().forPath(propertyPath, propertyValue.getBytes());
            } catch (Exception e) {
                throw new ZooAccessException(MessageFormat.format("Error update value on path {0} caused by: {1}",
                        path, e.getMessage()), e);

            }
        }
    }

    private void removeDeletedProperties(final String path, final Set<String> propertiesPaths,
            final List<String> excludedPath) {
        final List<String> childNames = readChildNames(path);

        for(String childName: childNames) {
            final String nodePath = path + '/' + childName;
            if (propertiesPaths.contains(nodePath)) {
                removeDeletedProperties(path + "/" + childName, propertiesPaths, excludedPath);
            } else {
                if (!excludedPath.contains(nodePath)) {
                    try {
                        zoo.delete().deletingChildrenIfNeeded().forPath(nodePath);
                    } catch (Exception e) {
                        throw new ZooAccessException(MessageFormat.format("Error remove path {0} caused by: {1}",
                                path, e.getMessage()), e);
                    }
                }
            }
        }
    }

    private Set<String> getPropertiesPaths(final String path, Properties properties) {
        final String root = path.endsWith("/") ? path.substring(0, path.length() - 1): path;
        final Set<String> result = new HashSet<>();
        for(Object key: properties.keySet()) {
            final String propertyName = key.toString();
            final StringBuilder propertyPath = new StringBuilder(root);
            final String[] propertyNameParts = propertyName.split("\\.");
            for(int i = 0; i < propertyNameParts.length; i++) {
                propertyPath.append("/").append(propertyNameParts[i]);
                result.add(propertyPath.toString());
            }
        }

        return Collections.unmodifiableSet(result);
    }
}
