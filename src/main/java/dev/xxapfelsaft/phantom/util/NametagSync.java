package dev.xxapfelsaft.phantom.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.xxapfelsaft.phantom.feature.modules.CustomNameTagModule;
import net.minecraft.client.Minecraft;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NametagSync {

    private static final String API_URL = "https://phantomapi.xxapfelsaft.xyz/api/nametags";
    private static ScheduledExecutorService executor;
    private static final Gson GSON = new Gson();

    // Map of UUID string to custom nametag text
    public static final Map<String, String> globalTags = new HashMap<>();

    public static void start() {
        if (executor != null) return;
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NametagSync");
            t.setDaemon(true);
            return t;
        });

        executor.scheduleAtFixedRate(NametagSync::sync, 0, 5, TimeUnit.SECONDS);
    }

    public static void stop() {
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
                
                // If module is enabled, send our tag. Otherwise send empty to clear it.
                if (CustomNameTagModule.instance != null && CustomNameTagModule.instance.active()) {
                    text = CustomNameTagModule.customText;
                }

                // Push
                URL postUrl = new URL(API_URL + "/update");
                HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
                postConn.setRequestMethod("POST");
                postConn.setRequestProperty("Content-Type", "application/json");
                postConn.setDoOutput(true);

                JsonObject body = new JsonObject();
                body.addProperty("uuid", uuid);
                body.addProperty("text", text);

                try (OutputStream os = postConn.getOutputStream()) {
                    byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                postConn.getResponseCode(); // Execute request
            }

            // Pull
            URL getUrl = new URL(API_URL);
            HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
            getConn.setRequestMethod("GET");

            if (getConn.getResponseCode() == 200) {
                try (InputStreamReader reader = new InputStreamReader(getConn.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();
                    synchronized (globalTags) {
                        globalTags.clear();
                        for (String key : response.keySet()) {
                            JsonObject data = response.getAsJsonObject(key);
                            if (data.has("text")) {
                                globalTags.put(key, data.get("text").getAsString());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Silently fail if API is down
        }
    }
}
