package ru.nilsson03.library.menu.command.factory;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.menu.command.MenuAction;
import ru.nilsson03.library.menu.command.impl.CloseMenuAction;
import ru.nilsson03.library.menu.command.impl.ConsoleMenuAction;
import ru.nilsson03.library.menu.command.impl.PlayerMenuAction;
import ru.nilsson03.library.menu.command.impl.PreviousMenuAction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuActionFactory {

    private static final Map<Pattern, Function<String, MenuAction>> PARSERS = new LinkedHashMap<>();

    static {
        registerParser(Pattern.compile("^\\[console\\]\\s*(.+)$", Pattern.CASE_INSENSITIVE),
                ConsoleMenuAction::new);
        registerParser(Pattern.compile("^\\[player\\]\\s*(.+)$", Pattern.CASE_INSENSITIVE),
                PlayerMenuAction::new);
        registerParser(Pattern.compile("^\\[previous_menu\\]\\s*$", Pattern.CASE_INSENSITIVE),
                command -> new PreviousMenuAction());
        registerParser(Pattern.compile("^\\[close\\]\\s*$", Pattern.CASE_INSENSITIVE),
                command -> new CloseMenuAction());
        registerDefaultParser(command -> new PlayerMenuAction(command) {
        });
    }

    public static void registerParser(Pattern pattern, Function<String, MenuAction> parser) {
        PARSERS.put(pattern, parser);
    }

    public static void registerDefaultParser(Function<String, MenuAction> defaultParser) {
        PARSERS.put(null, defaultParser);
    }

    /**
     * Создать список командных действий из секции конфигурации
     * @param section секция конфигурации
     * @return список действий
     */
    public static List<MenuAction> createFromSection(ConfigurationSection section) {
        List<MenuAction> actions = new ArrayList<>();

        if (!section.contains("commands")) {
            return actions;
        }

        List<String> commands = section.getStringList("commands");
        for (String commandLine : commands) {
            MenuAction action = parseCommand(commandLine);
            if (action != null) {
                actions.add(action);
            }
        }

        return actions;
    }

    private static MenuAction parseCommand(String commandLine) {
        for (Map.Entry<Pattern, Function<String, MenuAction>> entry : PARSERS.entrySet()) {
            Pattern pattern = entry.getKey();
            Function<String, MenuAction> parser = entry.getValue();

            if (pattern == null) {
                return parser.apply(commandLine);
            }

            Matcher matcher = pattern.matcher(commandLine);
            if (matcher.matches()) {
                String argument = null;
                try {
                    argument = matcher.group(1);
                } catch (IndexOutOfBoundsException e) {
                    argument = "";
                }
                return parser.apply(argument);
            }
        }

        return new PlayerMenuAction(commandLine);
    }
}
