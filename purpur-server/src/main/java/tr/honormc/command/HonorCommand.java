package tr.honormc.command;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.purpurmc.purpur.PurpurConfig;

public final class HonorCommand extends Command {

    public HonorCommand(final String name) {
        super(name);
        this.description = "HonorMC komut merkezi";
        this.usageMessage = "/honor [yardim | surum | eklentiler | tps | mspt | yenile]";
        this.setAliases(List.of("hmc"));
    }

    @Override
    public boolean execute(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final String[] args) {
        if (args.length == 0) {
            HonorHelpCommand.sendHelp(sender);
            return true;
        }

        final String subCommand = args[0].toLowerCase(Locale.ROOT);
        return switch (subCommand) {
            case "yardim", "help", "komutlar" -> {
                HonorHelpCommand.sendHelp(sender);
                yield true;
            }
            case "surum", "version" -> dispatch(sender, "surum");
            case "eklentiler", "plugins" -> dispatch(sender, "eklentiler");
            case "tps" -> dispatch(sender, "tps");
            case "mspt" -> dispatch(sender, "mspt");
            case "yenile", "reload" -> reloadHonor(sender);
            default -> {
                sender.sendMessage(Component.text("Bilinmeyen HonorMC komutu: " + args[0], NamedTextColor.RED));
                sender.sendMessage(Component.text("Kullan: /honor yardim", NamedTextColor.YELLOW));
                yield true;
            }
        };
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String alias, @NotNull final String[] args) {
        if (args.length == 1) {
            final String prefix = args[0].toLowerCase(Locale.ROOT);
            return Stream.of("yardim", "surum", "eklentiler", "tps", "mspt", "yenile")
                .filter(command -> command.startsWith(prefix))
                .toList();
        }
        return Collections.emptyList();
    }

    private static boolean dispatch(final CommandSender sender, final String commandLine) {
        if (!Bukkit.dispatchCommand(sender, commandLine)) {
            sender.sendMessage(Component.text("HonorMC komutu su an hazir degil: /" + commandLine, NamedTextColor.RED));
        }
        return true;
    }

    private static boolean reloadHonor(final CommandSender sender) {
        if (!sender.hasPermission("honor.command.reload") && !sender.isOp()) {
            sender.sendMessage(Bukkit.permissionMessage());
            return true;
        }

        final MinecraftServer console = MinecraftServer.getServer();
        PurpurConfig.init((File) console.options.valueOf("purpur-settings"));
        for (final ServerLevel level : console.getAllLevels()) {
            level.purpurConfig.init();
            level.resetBreedingCooldowns();
        }
        console.server.reloadCount++;

        Command.broadcastCommandMessage(sender, Component.text("HonorMC ayarlari yenilendi.", NamedTextColor.GREEN));
        return true;
    }
}
