package me.balintcsala.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class Arguments {

    public enum ArgumentType {
        VALUE,
        LIST,
    }

    private static class Argument {
        private final String name;
        private final String shorthand;
        private final ArgumentType type;

        public Argument(String name, String shorthand, ArgumentType type) {
            this.name = name;
            this.shorthand = shorthand;
            this.type = type;
        }

        public boolean matches(String key) {
            return key.equals("--" + name) || key.equals("-" + shorthand);
        }
    }

    private final ArrayList<Argument> registeredArguments = new ArrayList<>();
    private final HashMap<String, String> arguments = new HashMap<>();
    private final HashMap<String, ArrayList<String>> listArguments = new HashMap<>();

    public Arguments registerArgument(String name, String shorthand) {
        registeredArguments.add(new Argument(name, shorthand, ArgumentType.VALUE));
        return this;
    }

    public Arguments registerListArgument(String name, String shorthand) {
        registeredArguments.add(new Argument(name, shorthand, ArgumentType.LIST));
        return this;
    }

    public Arguments parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            for (Argument registeredArgument : registeredArguments) {
                if (!registeredArgument.matches(args[i]))
                    continue;

                switch (registeredArgument.type) {
                    case VALUE:
                        arguments.put(registeredArgument.name, args[i + 1]);
                        break;
                    case LIST:
                        if (!listArguments.containsKey(registeredArgument.name)) {
                            ArrayList<String> list = new ArrayList<>();
                            list.add(args[i + 1]);
                            listArguments.put(registeredArgument.name, list);
                        } else {
                            listArguments.get(registeredArgument.name).add(args[i + 1]);
                        }
                        break;
                }
            }
        }
        return this;
    }

    public String getArgument(String name) {
        return arguments.get(name);
    }

    public ArrayList<String> getListArgument(String name) {
        if (!listArguments.containsKey(name))
            return new ArrayList<>();
        return listArguments.get(name);
    }
}
