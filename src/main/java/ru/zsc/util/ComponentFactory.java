package ru.zsc.util;

import static java.text.MessageFormat.format;

import java.util.Objects;
import ru.zsc.util.ArgumentParser.Option;
import ru.zsc.zoo.ZooAccessor;
import ru.zsc.zoo.impl.ZooAccessorImpl;

/**
 * Created by Pavel Perepech.
 */
public class ComponentFactory {
    private static volatile ComponentFactory INSTANCE = null;

    private final ArgumentParser argumentParser;

    private volatile ZooAccessor zooAccessor;

    private ComponentFactory(final ArgumentParser argumentParser) {
        this.argumentParser = argumentParser;
    }

    public static void init(final ArgumentParser argumentParser) {
        INSTANCE = new ComponentFactory(Objects.requireNonNull(argumentParser));
    }

    public static ComponentFactory getInstance() {
        return Objects.requireNonNull(INSTANCE, "Component factory was not initialized");
    }

    public ZooAccessor zooAccessor() {
        if (Objects.isNull(zooAccessor)) {
            synchronized (ZooAccessor.class) {
                if (Objects.isNull(zooAccessor)) {
                    zooAccessor = new ZooAccessorImpl();
                    zooAccessor.init(buildConnectionString());
                }
            }
        }

        return zooAccessor;
    }

    private String buildConnectionString() {
        final String hosts[] = Objects.requireNonNull(
                argumentParser.getOptionValue(Option.ZOO_HOST), "Zookeeper host must be specified")
                .split(",");
        final String ports[] = Objects.requireNonNull(
                argumentParser.getOptionValue(Option.ZOO_PORT), "Zookeeper port must be specified")
                .split(",");

        final StringBuilder result = new StringBuilder();
        if (hosts.length == ports.length) {
            for(int i = 0; i < hosts.length; i++) {
                if (result.length() > 0) {
                    result.append(',');
                }

                result.append(hosts[i]).append(':').append(ports[i]);
            }
        } else if (ports.length == 1) {
            for(int i = 0; i < hosts.length; i++) {
                if (result.length() > 0) {
                    result.append(',');
                }
                
                result.append(hosts[i]).append(':').append(ports[0]);
            }
        } else {
            throw new IllegalArgumentException(
                    format("Different count between hosts {0} and ports {1}", hosts.length, ports.length));
        }

        return result.toString();
    }

}
