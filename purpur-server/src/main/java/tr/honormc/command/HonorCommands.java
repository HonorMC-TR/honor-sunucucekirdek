package tr.honormc.command;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

public final class HonorCommands {
    private static final Set<String> LEGACY_NAMESPACES = Set.of("bukkit", "spigot", "paper", "purpur");
    private static final Set<String> LEGACY_DIRECT_COMMANDS = Set.of(
        "bukkit",
        "spigot",
        "paper",
        "purpur",
        "timings",
        "reload",
        "rl",
        "help",
        "?",
        "version",
        "ver",
        "about",
        "plugins",
        "pl",
        "tps",
        "mspt",
        "spark"
    );

    private HonorCommands() {
    }

    public static void registerCommands(final MinecraftServer server) {
        final SimpleCommandMap commandMap = server.server.getCommandMap();
        removeLegacyForkCommands(commandMap);
        registerOrReplace(commandMap, "honor", new HonorCommand("honor"));
        registerOrReplace(commandMap, "yardim", new HonorHelpCommand("yardim"));
        registerOrReplace(commandMap, "surum", new HonorInfoCommand("surum", HonorInfoCommand.Mode.SURUM, "version", "ver", "about"));
        registerOrReplace(commandMap, "eklentiler", new HonorInfoCommand("eklentiler", HonorInfoCommand.Mode.EKLENTILER, "plugins", "pl"));
        registerOrReplace(commandMap, "tps", new HonorInfoCommand("tps", HonorInfoCommand.Mode.TPS));
        registerOrReplace(commandMap, "mspt", new HonorInfoCommand("mspt", HonorInfoCommand.Mode.MSPT));
        registerOrReplace(commandMap, "durum", new HonorInfoCommand("durum", HonorInfoCommand.Mode.DURUM, "status"));
        registerOrReplace(commandMap, "dosyalar", new HonorInfoCommand("dosyalar", HonorInfoCommand.Mode.DOSYALAR, "klasorler"));
        registerOrReplace(commandMap, "telemetri", new HonorInfoCommand("telemetri", HonorInfoCommand.Mode.TELEMETRI, "olcum"));
    }

    private static void registerOrReplace(final SimpleCommandMap commandMap, final String label, final Command command) {
        removeLabel(commandMap, label);
        removeLabel(commandMap, command.getName());
        removeLabels(commandMap, command.getAliases());
        commandMap.register(label, "honor", command);
    }

    private static void removeLegacyForkCommands(final SimpleCommandMap commandMap) {
        final Map<String, Command> knownCommands = commandMap.getKnownCommands();
        knownCommands.keySet().removeIf(HonorCommands::isLegacyCommandKey);
    }

    private static boolean isLegacyCommandKey(final String key) {
        final String normalized = key.toLowerCase(Locale.ROOT);
        final int namespaceIndex = normalized.indexOf(':');
        if (namespaceIndex > 0 && LEGACY_NAMESPACES.contains(normalized.substring(0, namespaceIndex))) {
            return true;
        }
        return LEGACY_DIRECT_COMMANDS.contains(normalized);
    }

    private static void removeLabels(final SimpleCommandMap commandMap, final Collection<String> labels) {
        for (final String label : labels) {
            removeLabel(commandMap, label);
        }
    }

    private static void removeLabel(final SimpleCommandMap commandMap, final String label) {
        final Map<String, Command> knownCommands = commandMap.getKnownCommands();
        final String normalized = label.toLowerCase(Locale.ROOT);
        knownCommands.remove(normalized);
        knownCommands.remove("honor:" + normalized);
    }
}
