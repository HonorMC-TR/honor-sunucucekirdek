package tr.honormc.command;

import io.papermc.paper.configuration.GlobalConfiguration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class HonorInfoCommand extends Command {

    public enum Mode {
        SURUM,
        EKLENTILER,
        TPS,
        MSPT,
        DURUM,
        DOSYALAR,
        TELEMETRI
    }

    private final Mode mode;

    public HonorInfoCommand(final String name, final Mode mode, final String... aliases) {
        super(name);
        this.mode = mode;
        this.description = "HonorMC " + name + " komutu";
        this.usageMessage = "/" + name;
        this.setAliases(Arrays.asList(aliases));
    }

    @Override
    public boolean execute(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final String[] args) {
        send(sender, this.mode);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String alias, @NotNull final String[] args) {
        return Collections.emptyList();
    }

    public static void send(final CommandSender sender, final Mode mode) {
        switch (mode) {
            case SURUM -> sendVersion(sender);
            case EKLENTILER -> sendPlugins(sender);
            case TPS -> sendTps(sender);
            case MSPT -> sendMspt(sender);
            case DURUM -> sendStatus(sender);
            case DOSYALAR -> sendFiles(sender);
            case TELEMETRI -> sendTelemetry(sender);
        }
    }

    private static void sendVersion(final CommandSender sender) {
        final String implementation = org.bukkit.craftbukkit.Main.class.getPackage().getImplementationVersion();
        title(sender, "HonorMC Surum");
        row(sender, "Cekirdek", "HonorMC " + (implementation == null ? "yerel" : implementation));
        row(sender, "Minecraft", Bukkit.getMinecraftVersion());
        row(sender, "API", Bukkit.getBukkitVersion());
        row(sender, "Release", "https://github.com/HonorMC-TR/honor-sunucucekirdek/releases");
        sender.sendMessage(Component.text("GitHub release kontrolu HonorMC release etiketiyle yapilir.", NamedTextColor.GRAY));
    }

    private static void sendPlugins(final CommandSender sender) {
        final Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        title(sender, "HonorMC Eklentiler");
        if (plugins.length == 0) {
            sender.sendMessage(Component.text("Yuklu eklenti yok.", NamedTextColor.GRAY));
            return;
        }

        row(sender, "Toplam", Integer.toString(plugins.length));
        for (final Plugin plugin : plugins) {
            final NamedTextColor color = plugin.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED;
            sender.sendMessage(Component.text("- ", NamedTextColor.DARK_GRAY)
                .append(Component.text(plugin.getName(), color))
                .append(Component.text(" " + plugin.getPluginMeta().getVersion(), NamedTextColor.GRAY))
                .append(Component.text(plugin.isEnabled() ? " aktif" : " kapali", color)));
        }
    }

    private static void sendTps(final CommandSender sender) {
        final double[] tps = Bukkit.getTPS();
        title(sender, "HonorMC TPS");
        final String[] labels = {"5sn", "1dk", "5dk", "15dk"};
        for (int i = 0; i < tps.length && i < labels.length; i++) {
            row(sender, labels[i], formatTps(tps[i]));
        }
    }

    private static void sendMspt(final CommandSender sender) {
        title(sender, "HonorMC MSPT");
        row(sender, "Ortalama", formatNumber(Bukkit.getAverageTickTime()) + " ms");
        sender.sendMessage(Component.text("20 TPS icin hedef: 50 ms altinda kalmak.", NamedTextColor.GRAY));
    }

    private static void sendStatus(final CommandSender sender) {
        final Runtime runtime = Runtime.getRuntime();
        title(sender, "HonorMC Durum");
        row(sender, "Oyuncu", Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
        row(sender, "Dunya", Integer.toString(Bukkit.getWorlds().size()));
        row(sender, "TPS", formatTps(Bukkit.getTPS()[0]));
        row(sender, "MSPT", formatNumber(Bukkit.getAverageTickTime()) + " ms");
        row(sender, "RAM", memory(runtime.totalMemory() - runtime.freeMemory()) + " / " + memory(runtime.maxMemory()));
    }

    private static void sendFiles(final CommandSender sender) {
        title(sender, "HonorMC Dosya Duzeni");
        row(sender, "Ayarlar", "ayarlar/");
        row(sender, "Komutlar", "ayarlar/komutlar.yml");
        row(sender, "Eklentiler", "eklentiler/");
        row(sender, "Dunyalar", "dunyalar/");
        row(sender, "Kayitlar", "kayitlar/");
        row(sender, "Canli HTML", "kayitlar/canli-konsol.html");
    }

    private static void sendTelemetry(final CommandSender sender) {
        title(sender, "HonorMC Telemetri");
        row(sender, "bStats", Boolean.getBoolean("honormc.telemetri") ? "acik" : "kapali");
        row(sender, "spark modul", Boolean.getBoolean("honormc.spark") ? "izinli" : "kapali");
        row(sender, "spark ayar", GlobalConfiguration.get().spark.enabled ? "acik" : "kapali");
        sender.sendMessage(Component.text("bStats temel calisma icin gerekli degil; spark performans inceleme aracidir.", NamedTextColor.GRAY));
    }

    private static void title(final CommandSender sender, final String text) {
        sender.sendMessage(Component.text("==== " + text + " ====", NamedTextColor.GOLD, TextDecoration.BOLD));
    }

    private static void row(final CommandSender sender, final String key, final String value) {
        sender.sendMessage(Component.text(key + ": ", NamedTextColor.YELLOW)
            .append(Component.text(value, NamedTextColor.WHITE)));
    }

    private static String formatTps(final double value) {
        final double capped = Math.min(value, 20.0D);
        return formatNumber(capped);
    }

    private static String formatNumber(final double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String memory(final long bytes) {
        return (bytes / 1024L / 1024L) + " MB";
    }
}
