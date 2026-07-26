package dev.xxapfelsaft.phantom.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MediaMetadataFetcher {

    private static String currentTitle = "No Media";
    private static String currentArtist = "";
    private static boolean isPlaying = false;
    private static long currentLength = 0;
    private static long currentPosition = 0;
    private static long lastFetchTime = 0;
    public enum Platform { NONE, YOUTUBE_MUSIC, SPOTIFY, SOUNDCLOUD, APPLE_MUSIC, DEEZER }
    private static Platform currentPlatform = Platform.NONE;
    private static ScheduledExecutorService executor;

    public static void start() {
        if (executor != null) return;
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MediaMetadataFetcher");
            t.setDaemon(true);
            return t;
        });

        executor.scheduleAtFixedRate(MediaMetadataFetcher::fetch, 0, 3, TimeUnit.SECONDS);
    }

    public static void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public static String getTitle() {
        return currentTitle;
    }

    public static String getArtist() {
        return currentArtist;
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static long getLength() {
        return currentLength;
    }

    public static long getPosition() {
        return currentPosition;
    }

    public static long getLastFetchTime() {
        return lastFetchTime;
    }

    public static Platform getPlatform() {
        return currentPlatform;
    }

    private static void fetch() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                fetchWindows();
            } else if (os.contains("nux")) {
                fetchLinux();
            } else if (os.contains("mac")) {
                fetchMac();
            }
        } catch (Exception e) {
            // Silently fail if native commands are missing
        }
    }

    private static void fetchWindows() throws Exception {
        String script = "[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media, ContentType = WindowsRuntime] | Out-Null;" +
                "$manager = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync().GetResults();" +
                "$session = $manager.GetCurrentSession();" +
                "if ($session) {" +
                "    $props = $session.TryGetMediaPropertiesAsync().GetResults();" +
                "    $appId = ''; if ($session.SourceAppUserModelId) { $appId = $session.SourceAppUserModelId; }" +
                "    Write-Output ($props.Title + '|||' + $props.Artist + '|||' + $appId);" +
                "}";

        ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", script);
        runAndParse(pb);
    }

    private static void fetchLinux() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("sh", "-c",
            "PLAYER=$(dbus-send --session --dest=org.freedesktop.DBus --type=method_call --print-reply /org/freedesktop/DBus org.freedesktop.DBus.ListNames | grep -o 'org\\.mpris\\.MediaPlayer2\\.[^\"]*' | head -n 1); " +
            "if [ -n \"$PLAYER\" ]; then " +
            "  echo \"---PLAYER---\"; " +
            "  echo \"$PLAYER\"; " +
            "  dbus-send --session --print-reply --dest=\"$PLAYER\" /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:\"org.mpris.MediaPlayer2.Player\" string:\"Metadata\"; " +
            "  echo \"---POSITION---\"; " +
            "  dbus-send --session --print-reply --dest=\"$PLAYER\" /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:\"org.mpris.MediaPlayer2.Player\" string:\"Position\"; " +
            "fi"
        );
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        String title = "";
        String artist = "";
        String url = "";
        String playerName = "";
        long lengthMs = 0;
        long positionMs = 0;
        boolean inTitle = false;
        boolean inArtist = false;
        boolean inLength = false;
        boolean inUrl = false;
        boolean parsingPosition = false;
        boolean parsingPlayer = false;
        
        while ((line = reader.readLine()) != null) {
            if (line.equals("---PLAYER---")) {
                parsingPlayer = true;
                continue;
            } else if (line.equals("---POSITION---")) {
                parsingPosition = true;
                parsingPlayer = false;
                continue;
            }

            if (parsingPlayer) {
                playerName = line.trim();
                parsingPlayer = false;
                continue;
            }

            if (!parsingPosition) {
                if (line.contains("string \"xesam:title\"")) {
                    inTitle = true;
                } else if (inTitle && line.contains("variant")) {
                    try {
                        title = line.split("string \"")[1];
                        title = title.substring(0, title.lastIndexOf("\""));
                    } catch (Exception ignored) {}
                    inTitle = false;
                } else if (line.contains("string \"xesam:artist\"")) {
                    inArtist = true;
                } else if (inArtist && line.contains("string \"")) {
                    try {
                        artist = line.split("string \"")[1];
                        artist = artist.substring(0, artist.lastIndexOf("\""));
                    } catch (Exception ignored) {}
                    inArtist = false;
                } else if (line.contains("string \"xesam:url\"")) {
                    inUrl = true;
                } else if (inUrl && line.contains("variant")) {
                    try {
                        url = line.split("string \"")[1];
                        url = url.substring(0, url.lastIndexOf("\""));
                    } catch (Exception ignored) {}
                    inUrl = false;
                } else if (line.contains("string \"mpris:length\"")) {
                    inLength = true;
                } else if (inLength && line.contains("variant")) {
                    try {
                        String val = line.split("int64 ")[1].trim();
                        lengthMs = Long.parseLong(val) / 1000L;
                    } catch (Exception ignored) {}
                    inLength = false;
                }
            } else {
                if (line.contains("int64")) {
                    try {
                        String val = line.split("int64 ")[1].trim();
                        positionMs = Long.parseLong(val) / 1000L;
                    } catch (Exception ignored) {}
                }
            }
        }
        p.waitFor(2, TimeUnit.SECONDS);

        if (!title.isEmpty()) {
            if (title.endsWith(" | YouTube Music")) {
                title = title.substring(0, title.length() - " | YouTube Music".length());
                currentPlatform = Platform.YOUTUBE_MUSIC;
            } else if (url.contains("music.youtube.com")) {
                currentPlatform = Platform.YOUTUBE_MUSIC;
            } else if (playerName.toLowerCase().contains("spotify") || url.contains("spotify") || title.toLowerCase().contains("spotify") || artist.toLowerCase().contains("spotify")) {
                currentPlatform = Platform.SPOTIFY;
            } else if (playerName.toLowerCase().contains("soundcloud") || url.contains("soundcloud") || title.toLowerCase().contains("soundcloud")) {
                currentPlatform = Platform.SOUNDCLOUD;
            } else if (playerName.toLowerCase().contains("apple") || url.contains("apple") || title.toLowerCase().contains("apple")) {
                currentPlatform = Platform.APPLE_MUSIC;
            } else if (playerName.toLowerCase().contains("deezer") || url.contains("deezer") || title.toLowerCase().contains("deezer")) {
                currentPlatform = Platform.DEEZER;
            } else {
                currentPlatform = Platform.NONE;
            }
            
            currentTitle = title;
            currentArtist = artist;
            currentLength = lengthMs;
            currentPosition = positionMs;
            lastFetchTime = System.currentTimeMillis();
            isPlaying = true;
        } else {
            currentTitle = "No Media";
            currentArtist = "";
            currentLength = 0;
            currentPosition = 0;
            currentPlatform = Platform.NONE;
            isPlaying = false;
        }
    }

    private static void fetchMac() throws Exception {
        // Simple fallback for Spotify on Mac
        String script = "tell application \"System Events\"\n" +
                "if exists process \"Spotify\" then\n" +
                "tell application \"Spotify\"\n" +
                "if player state is playing then\n" +
                "return name of current track & \"|||\" & artist of current track\n" +
                "end if\n" +
                "end tell\n" +
                "end if\n" +
                "end tell";
        ProcessBuilder pb = new ProcessBuilder("osascript", "-e", script);
        runAndParse(pb);
    }

    private static void runAndParse(ProcessBuilder pb) throws Exception {
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = reader.readLine();
        p.waitFor(2, TimeUnit.SECONDS);

        if (line != null && line.contains("|||")) {
            String[] parts = line.split("\\|\\|\\|");
            currentTitle = parts.length > 0 ? parts[0].trim() : "";
            currentArtist = parts.length > 1 ? parts[1].trim() : "";
            String appId = parts.length > 2 ? parts[2].trim().toLowerCase() : "";
            currentLength = 0;
            currentPosition = 0;
            lastFetchTime = System.currentTimeMillis();
            
            if (appId.contains("spotify") || currentTitle.toLowerCase().contains("spotify")) {
                currentPlatform = Platform.SPOTIFY;
            } else if (appId.contains("soundcloud") || currentTitle.toLowerCase().contains("soundcloud")) {
                currentPlatform = Platform.SOUNDCLOUD;
            } else if (appId.contains("apple") || appId.contains("itunes") || currentTitle.toLowerCase().contains("apple music")) {
                currentPlatform = Platform.APPLE_MUSIC;
            } else if (appId.contains("deezer") || currentTitle.toLowerCase().contains("deezer")) {
                currentPlatform = Platform.DEEZER;
            } else if (currentTitle.endsWith(" | YouTube Music")) {
                currentTitle = currentTitle.substring(0, currentTitle.length() - " | YouTube Music".length());
                currentPlatform = Platform.YOUTUBE_MUSIC;
            } else if (appId.contains("chrome") || appId.contains("edge") || appId.contains("firefox")) {
                currentPlatform = Platform.NONE; // Will just show ♫
            } else {
                currentPlatform = Platform.NONE;
            }
            
            isPlaying = !currentTitle.isEmpty();
        } else {
            currentTitle = "No Media";
            currentArtist = "";
            currentLength = 0;
            currentPosition = 0;
            currentPlatform = Platform.NONE;
            isPlaying = false;
        }
    }
}
