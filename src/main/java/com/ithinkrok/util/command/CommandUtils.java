package com.ithinkrok.util.command;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 12/01/16.
 */
public class CommandUtils {


    public static List<String> splitStringIntoArguments(String command) {
        if (command.startsWith("/")) command = command.substring(1);

        boolean wasBackslash = false;
        boolean inQuotes = false;

        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int index = 0; index < command.length(); ++index) {
            char c = command.charAt(index);

            if (wasBackslash) {
                current.append(c);
                wasBackslash = false;
                continue;
            }

            switch (c) {
                case '\\':
                    wasBackslash = true;
                    break;
                case '"':
                    inQuotes = !inQuotes;
                    break;
                case ' ':
                    if (inQuotes) {
                        current.append(' ');
                        break;
                    }

                    if (current.length() < 1) break;
                    result.add(current.toString());
                    current = new StringBuilder();
                    break;
                default:
                    current.append(c);
            }
        }

        if (current.length() > 0) result.add(current.toString());
        return result;
    }

    /**
     * Fixes arguments lists that contain quotes, that were parsed by Bukkit instead of
     * splitStringIntoArguments().
     *
     * @param args The arguments to fix
     * @return The corrected arguments
     */
    public static List<String> mergeArgumentsInQuotes(String[] args) {
        List<String> correctedArgs = new ArrayList<>();

        StringBuilder currentArg = new StringBuilder();

        boolean inQuote = false;

        for (String arg : args) {
            if (currentArg.length() > 0) currentArg.append(' ');
            currentArg.append(arg.replace("\"", ""));

            int quoteCount = StringUtils.countMatches(arg, "\"");
            if (((quoteCount & 1) == 1)) inQuote = !inQuote;

            if (!inQuote) {
                correctedArgs.add(currentArg.toString());
                currentArg = new StringBuilder();
            }
        }
        return correctedArgs;
    }

    /**
     * Parses a list of arguments to a parameter map.
     * Arguments that are not part of a parameter will be put in a list, which is put in the returned map with the
     * key "default"
     *
     * @param correctedArgs A quotes corrected argument list
     * @return A parameter map, with the default arguments stored in a list with the key "default"
     */
    public static Map<String, Object> parseArgumentListToMap(List<String> correctedArgs) {
        List<String> defaultArguments = new ArrayList<>();
        Map<String, Object> arguments = new HashMap<>();

        String key = null;

        for (String arg : correctedArgs) {
            if (arg.startsWith("-") && arg.length() > 1 && isValidParameterName(arg.substring(1))) {
                key = arg.substring(1);

                //Support for boolean flags e.g. -w!
                if(key.endsWith("!")) {
                    arguments.put(key.substring(0, key.length() - 1), true);
                    key = null;
                }
            } else {
                //Parameters are still parsed, but arguments are not
                if (key != null) arguments.put(key, parse(arg));
                else defaultArguments.add(arg);
                key = null;
            }
        }

        arguments.put("default", defaultArguments);
        return arguments;
    }

    private static boolean isValidParameterName(String arg) {
        if (arg.isEmpty()) return false;

        char first = arg.charAt(0);

        if (!Character.isLetter(first)) return false;

        for (int index = 0; index < arg.length(); ++index) {
            char c = arg.charAt(index);

            //Make shorthand flags work
            if (!Character.isLetter(c) && !Character.isDigit(c) && c != '_'){
                if(index == arg.length() - 1 && c == '!') continue;
                return false;
            }
        }

        return true;
    }

    private static Object parse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
        }

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ignored) {
        }

        switch (s.toLowerCase()) {
            case "true":
            case "yes":
                return true;
            case "false":
            case "no":
                return false;
        }

        return s;
    }
}
