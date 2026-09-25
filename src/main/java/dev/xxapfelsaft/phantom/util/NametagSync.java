package dev.xxapfelsaft.phantom.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.xxapfelsaft.phantom.PhantomAddon;
import dev.xxapfelsaft.phantom.feature.modules.CustomNameTagModule;
import net.minecraft.client.Minecraft;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NametagSync {

    private static final String API_URL = "https://phantomapi.xxapfelsaft.xyz/api/nametags";
    private static ScheduledExecutorService executor;

    public record TagData(String text, double yOffset) {}

    // Thread-safe map of UUID string to custom nametag data
    public static final Map<String, TagData> globalTags = new ConcurrentHashMap<>();

    public static synchronized void start() {
        if (executor != null) return;
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NametagSync");
            t.setDaemon(true);
            return t;
        });

        executor.scheduleAtFixedRate(NametagSync::sync, 0, 5, TimeUnit.SECONDS);
    }

    public static synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private static void sync() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                String uuid = mc.player.getUUID().toString();
                String text = "";
                double yOffset = 0.35;

                // If module is enabled OR broadcastWhenDisabled is true, send our tag.
                if (CustomNameTagModule.instance != null && (CustomNameTagModule.instance.active() || CustomNameTagModule.instance.broadcastWhenDisabledSetting.get())) {
                    text = CustomNameTagModule.getCurrentText();
                    yOffset = CustomNameTagModule.getCurrentYOffset();
                }

                // Push
                HttpURLConnection postConn = (HttpURLConnection) URI.create(API_URL + "/update").toURL().openConnection();
                postConn.setRequestMethod("POST");
                postConn.setRequestProperty("Content-Type", "application/json");
                postConn.setConnectTimeout(5000);
                postConn.setReadTimeout(5000);
                postConn.setDoOutput(true);

                JsonObject body = new JsonObject();
                body.addProperty("uuid", uuid);
                body.addProperty("text", text);
                body.addProperty("yOffset", yOffset);

                try (OutputStream os = postConn.getOutputStream()) {
                    byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                postConn.getResponseCode();
                postConn.disconnect();
            }

            // Pull
            HttpURLConnection getConn = (HttpURLConnection) URI.create(API_URL).toURL().openConnection();
            getConn.setRequestMethod("GET");
            getConn.setConnectTimeout(5000);
            getConn.setReadTimeout(5000);

            if (getConn.getResponseCode() == 200) {
                try (InputStreamReader reader = new InputStreamReader(getConn.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();
                    globalTags.clear();
                    for (String key : response.keySet()) {
                        JsonObject data = response.getAsJsonObject(key);
                        if (data.has("text")) {
                            double offset = data.has("yOffset") ? data.get("yOffset").getAsDouble() : 0.35;
                            globalTags.put(key, new TagData(data.get("text").getAsString(), offset));
                        }
                    }
                }
            }
            getConn.disconnect();
        } catch (Exception e) {
            PhantomAddon.LOGGER.debug("Nametag sync cycle skipped: {}", e.getMessage());
        }
    }
}
