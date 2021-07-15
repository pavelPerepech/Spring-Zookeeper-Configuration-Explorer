package ru.zsc.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by Pavel Perepech.
 */
public class ArgumentParser {
    private static final String FULL_PREFIX = "--";
    private static final String SHORT_PREFIX = "-";

    public enum Option {
        ZOO_HOST("host", "h", "localhost", "Zookeeper host"),
        ZOO_PORT("port", "p", "2181", "Zookeeper port");

        private final String fullName;

        private final String shortName;

        private final String defaultValue;

        private final String description;

        Option(String fullName, String shortName, String defaultValue, String description) {
            this.fullName = fullName;
            this.shortName = shortName;
            this.defaultValue = defaultValue;
            this.description = description;
        }

        public String getFullName() {
            return fullName;
        }

        public String getShortName() {
            return shortName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getDescription() {
            return description;
        }
    }

    private final Map<Option, String> optionMap = new HashMap<>();


    public ArgumentParser(final String[] args) {
        Option currentOption = null;

        for(String arg: args) {
            if (Objects.isNull(currentOption)) {
                currentOption = findOption(arg);
            } else {
                if (arg.startsWith(FULL_PREFIX) || arg.startsWith(SHORT_PREFIX)) {
                    optionMap.put(currentOption, null);
                    currentOption = findOption(arg);
                } else {
                    optionMap.put(currentOption, arg);
                    currentOption = null;
                }
            }
        }

        if (Objects.nonNull(currentOption)) {
            optionMap.put(currentOption, null);
        }
    }

    public boolean isOptionPresent(final Option option) {
        if (Objects.isNull(option)) {
            return false;
        }

        return optionMap.keySet().contains(option);
    }

    public String getOptionValue(final Option option) {
        return Optional.ofNullable(optionMap.get(option))
                .orElseGet(() -> option.getDefaultValue());
    }

    public static void printOptionsHelp() {
        for(Option option: Option.values()) {
            System.out.println("  " + FULL_PREFIX + option.getFullName() + " value " +
                    getDefaultForPrint(option) + " (" + SHORT_PREFIX + option.getShortName() + " value) - " +
                    option.getDescription());
        }
    }

    private static String getDefaultForPrint(final Option option) {
        return Optional.ofNullable(option)
                .map(Option::getDefaultValue)
                .map(val -> "[" + val + "]")
                .orElse("");
    }

    private Option findOption(final String arg) {
        if (arg.startsWith(FULL_PREFIX)) {
            final String fullName = arg.substring(FULL_PREFIX.length());
            return findOptionByFullName(fullName);
        } else if (arg.startsWith(SHORT_PREFIX)) {
            final String shortName = arg.substring(SHORT_PREFIX.length());
            return  findOptionByShortName(shortName);
        }

        return null;
    }

    private Option findOptionByFullName(final String fullName) {
        return Arrays.stream(Option.values())
                .filter(option -> option.getFullName().equals(fullName))
                .findFirst()
                .orElse(null);
    }

    private Option findOptionByShortName(final String shortName) {
        return Arrays.stream(Option.values())
                .filter(option -> option.getShortName().equals(shortName))
                .findFirst()
                .orElse(null);
    }
}
