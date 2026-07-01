package tr.honormc.command;

import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class HonorHelpCommand extends Command {

    public HonorHelpCommand(final String name) {
        super(name);
        this.description = "HonorMC yardim merkezini gosterir";
        this.usageMessage = "/" + name;
        this.setAliases(List.of("help", "?"));
    }

    @Override
    public boolean execute(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final String[] args) {
        sendHelp(sender);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String alias, @NotNull final String[] args) {
        return Collections.emptyList();
    }

    public static void sendHelp(final CommandSender sender) {
        sender.sendMessage(Component.text("HonorMC Yardim", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/honor", NamedTextColor.YELLOW)
            .append(Component.text(" - HonorMC komut merkezini acar.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/yardim", NamedTextColor.YELLOW)
            .append(Component.text(" veya ", NamedTextColor.GRAY))
            .append(Component.text("/help", NamedTextColor.YELLOW))
            .append(Component.text(" - Bu yardim ekranini gosterir.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/surum", NamedTextColor.YELLOW)
            .append(Component.text(" veya ", NamedTextColor.GRAY))
            .append(Component.text("/version", NamedTextColor.YELLOW))
            .append(Component.text(" - HonorMC surum bilgisini gosterir.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/eklentiler", NamedTextColor.YELLOW)
            .append(Component.text(" veya ", NamedTextColor.GRAY))
            .append(Component.text("/plugins", NamedTextColor.YELLOW))
            .append(Component.text(" - Yuklu eklentileri listeler.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/tps", NamedTextColor.YELLOW)
            .append(Component.text(" / ", NamedTextColor.GRAY))
            .append(Component.text("/mspt", NamedTextColor.YELLOW))
            .append(Component.text(" - Performans durumunu gosterir.", NamedTextColor.WHITE)));
    }
}
