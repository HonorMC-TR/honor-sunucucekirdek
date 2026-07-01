package org.purpurmc.purpur;

import com.destroystokyo.paper.util.VersionFetcher;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import io.papermc.paper.ServerBuildInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

import static net.kyori.adventure.text.Component.text;
import static io.papermc.paper.ServerBuildInfo.StringRepresentation.VERSION_SIMPLE;

/**
 * Modified version of Paper's Version Fetcher before Fill API implementation.
 */
@DefaultQualifier(NonNull.class)
public class PurpurVersionFetcher implements VersionFetcher {
    private static final Logger LOGGER = LogUtils.getClassLogger();
    private static final int DISTANCE_ERROR = -1;
    private static final int DISTANCE_UNKNOWN = -2;
    private static final String REPOSITORY = "HonorMC-TR/honor-sunucucekirdek";
    private static final String RELEASES_PAGE = "https://github.com/" + REPOSITORY + "/releases";
    private static final String LATEST_RELEASE_API = "https://api.github.com/repos/" + REPOSITORY + "/releases/latest";
    private static final String RELEASE_TAG_PREFIX = "Honor-";
    private static final ServerBuildInfo BUILD_INFO;
    private static final String USER_AGENT;
    private static final Gson GSON = new Gson();

    static {
        BUILD_INFO = ServerBuildInfo.buildInfo();
        USER_AGENT = BUILD_INFO.brandName() + "/" + BUILD_INFO.asString(VERSION_SIMPLE) + " (HonorMC)";
    }

    private static int distance = DISTANCE_UNKNOWN;
    public int distance() {
        return distance;
    }

    @Override
    public long getCacheTime() {
        return 720000;
    }

    @Override
    public Component getVersionMessage() {
        return getUpdateStatusMessage();
    }

    private static Component getUpdateStatusMessage() {
        final String localTag = localReleaseTag();
        final @Nullable ReleaseInfo latestRelease = fetchLatestRelease();
        if (latestRelease == null || latestRelease.tagName == null || latestRelease.tagName.isBlank()) {
            distance = DISTANCE_ERROR;
            return text("* HonorMC GitHub release bilgisi alinamadi.", NamedTextColor.RED)
                .append(Component.newline())
                .append(text("Kontrol edilen repo: " + REPOSITORY, NamedTextColor.GRAY));
        }

        if (latestRelease.tagName.equalsIgnoreCase(localTag)) {
            distance = 0;
            return text("* HonorMC guncel: " + localTag, NamedTextColor.GREEN);
        }

        distance = 1;
        final String releaseUrl = latestRelease.htmlUrl != null && !latestRelease.htmlUrl.isBlank()
            ? latestRelease.htmlUrl
            : RELEASES_PAGE;
        return text("* Yeni HonorMC release'i var: " + latestRelease.tagName, NamedTextColor.YELLOW)
            .append(Component.newline())
            .append(text("Yerel surum: " + localTag, NamedTextColor.GRAY))
            .append(Component.newline())
            .append(text("Indirme: ")
                .append(text(releaseUrl, NamedTextColor.GOLD)
                    .hoverEvent(text("Ac", NamedTextColor.WHITE))
                    .clickEvent(ClickEvent.openUrl(releaseUrl))));
    }

    private static String localReleaseTag() {
        return RELEASE_TAG_PREFIX + BUILD_INFO.minecraftVersionId();
    }

    private static @Nullable ReleaseInfo fetchLatestRelease() {
        try {
            final HttpURLConnection connection = (HttpURLConnection) URI.create(LATEST_RELEASE_API).toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                LOGGER.debug("HonorMC release bilgisi alinamadi. HTTP {}", connection.getResponseCode());
                return null;
            }

            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                final JsonObject json = GSON.fromJson(reader, JsonObject.class);
                final String tagName = json.has("tag_name") && !json.get("tag_name").isJsonNull()
                    ? json.get("tag_name").getAsString()
                    : null;
                final String htmlUrl = json.has("html_url") && !json.get("html_url").isJsonNull()
                    ? json.get("html_url").getAsString()
                    : RELEASES_PAGE;
                return new ReleaseInfo(tagName, htmlUrl);
            } catch (final JsonSyntaxException | NumberFormatException e) {
                LOGGER.error("HonorMC GitHub release bilgisi okunamadi", e);
                return null;
            }
        } catch (final IOException e) {
            LOGGER.error("HonorMC surum denetimi yapilamadi", e);
            return null;
        }
    }

    private record ReleaseInfo(@Nullable String tagName, @Nullable String htmlUrl) {
    }
}
