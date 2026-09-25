package dev.xxapfelsaft.phantom.util;

import com.dwarslooper.cactus.client.systems.params.PlaceholderHandler;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.xxapfelsaft.phantom.PhantomAddon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PhantomPlaceholders {

    @FunctionalInterface
    public interface PlaceholderRegistrar {
        void register(String name, PlaceholderHandler.PlaceholderGetter getter, String fallback);
    }

    private static volatile boolean registered = false;

    private static final Map<String, String> RESOLVE_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> PENDING_RESOLVES = ConcurrentHashMap.newKeySet();

    public static synchronized void init() {
        if (registered) return;
        try {
            PlaceholderHandler handler = PlaceholderHandler.get();
            if (!registered) {
                registerViaReflection(handler);
            }
        } catch (Throwable t) {
            PhantomAddon.LOGGER.error("Failed to initialize Phantom placeholders: ", t);
        }
    }

    public static synchronized void registerAll(PlaceholderRegistrar r) {
        if (registered) return;
        registered = true;

        PhantomAddon.LOGGER.info("Registering custom Phantom HUD placeholders...");

        // === Resolved Server IP & Port ===
        r.register("server.ip.resolved", PhantomPlaceholders::getResolvedServerIp, "None");
        r.register("server.resolved_ip", PhantomPlaceholders::getResolvedServerIp, "None");
        r.register("server.resolvedip", PhantomPlaceholders::getResolvedServerIp, "None");
        r.register("server.real_ip", PhantomPlaceholders::getResolvedServerIp, "None");

        r.register("server.ip.resolved.port", PhantomPlaceholders::getResolvedServerIpWithPort, "None");
        r.register("server.resolved_ip_port", PhantomPlaceholders::getResolvedServerIpWithPort, "None");

        r.register("server.port", PhantomPlaceholders::getServerPort, "25565");

        // === F3 Client & Server Brand ===
        r.register("client.brand.f3", ClientBrandRetriever::getClientModName, "fabric");
        r.register("client.f3.brand", ClientBrandRetriever::getClientModName, "fabric");
        r.register("client.mod_brand", ClientBrandRetriever::getClientModName, "fabric");

        r.register("client.brand.full", PhantomPlaceholders::getFullClientBrand, "Minecraft");
        r.register("client.f3.full", PhantomPlaceholders::getFullClientBrand, "Minecraft");

        r.register("server.brand.f3", PhantomPlaceholders::getServerBrandF3, "vanilla");
        r.register("server.f3.brand", PhantomPlaceholders::getServerBrandF3, "vanilla");

        // === Protocol ===
        r.register("client.protocol.id", () -> SharedConstants.getCurrentVersion().protocolVersion(), "0");
        r.register("client.protocol_id", () -> SharedConstants.getCurrentVersion().protocolVersion(), "0");
        r.register("client.protocol.number", () -> SharedConstants.getCurrentVersion().protocolVersion(), "0");
        r.register("client.protocol_version", () -> SharedConstants.getCurrentVersion().protocolVersion(), "0");

        r.register("server.protocol.id", PhantomPlaceholders::getServerProtocol, "0");
        r.register("server.protocol", PhantomPlaceholders::getServerProtocol, "0");

        // === GPU Information ===
        r.register("gpu.name", PhantomPlaceholders::getGpuRenderer, "Unknown GPU");
        r.register("gpu", PhantomPlaceholders::getGpuRenderer, "Unknown GPU");
        r.register("system.gpu.name", PhantomPlaceholders::getGpuRenderer, "Unknown GPU");
        r.register("system.gpu", PhantomPlaceholders::getGpuRenderer, "Unknown GPU");

        r.register("gpu.vendor", PhantomPlaceholders::getGpuVendor, "Unknown Vendor");
        r.register("system.gpu.vendor", PhantomPlaceholders::getGpuVendor, "Unknown Vendor");

        r.register("gpu.version", PhantomPlaceholders::getGpuVersion, "Unknown Version");
        r.register("system.gpu.version", PhantomPlaceholders::getGpuVersion, "Unknown Version");
        r.register("gpu.driver", PhantomPlaceholders::getGpuVersion, "Unknown Version");

        r.register("gpu.backend", PhantomPlaceholders::getGpuBackend, "OpenGL");
        r.register("system.gpu.backend", PhantomPlaceholders::getGpuBackend, "OpenGL");

        // === RAM Usage ===
        r.register("ram.percent", PhantomPlaceholders::getRamPercentString, "0%");
        r.register("system.ram.percent", PhantomPlaceholders::getRamPercentString, "0%");
        r.register("system.memory.used.percent", PhantomPlaceholders::getRamPercentString, "0%");
        r.register("ram.percent.num", PhantomPlaceholders::getRamPercentNumber, "0");

        r.register("ram.usage", PhantomPlaceholders::getRamUsageSummary, "0MB / 0MB (0%)");
        r.register("ram.summary", PhantomPlaceholders::getRamUsageSummary, "0MB / 0MB (0%)");
        r.register("system.ram.usage", PhantomPlaceholders::getRamUsageSummary, "0MB / 0MB (0%)");
        r.register("system.ram", PhantomPlaceholders::getRamUsageSummary, "0MB / 0MB (0%)");

        r.register("ram.used", PhantomPlaceholders::getRamUsedMb, "0MB");
        r.register("system.ram.used", PhantomPlaceholders::getRamUsedMb, "0MB");

        r.register("ram.max", PhantomPlaceholders::getRamMaxMb, "0MB");
        r.register("system.ram.max", PhantomPlaceholders::getRamMaxMb, "0MB");

        r.register("ram.free", PhantomPlaceholders::getRamFreeMb, "0MB");
        r.register("system.ram.free", PhantomPlaceholders::getRamFreeMb, "0MB");

        r.register("ram.total", PhantomPlaceholders::getRamTotalMb, "0MB");
        r.register("system.ram.total", PhantomPlaceholders::getRamTotalMb, "0MB");

        r.register("ram.used.gb", PhantomPlaceholders::getRamUsedGb, "0.00 GB");
        r.register("system.ram.used.gb", PhantomPlaceholders::getRamUsedGb, "0.00 GB");

        r.register("ram.max.gb", PhantomPlaceholders::getRamMaxGb, "0.00 GB");
        r.register("system.ram.max.gb", PhantomPlaceholders::getRamMaxGb, "0.00 GB");

        // === CPU & Other Hardware/System ===
        r.register("cpu.name", PhantomPlaceholders::getCpuName, "Unknown CPU");
        r.register("cpu", PhantomPlaceholders::getCpuName, "Unknown CPU");
        r.register("system.cpu.name", PhantomPlaceholders::getCpuName, "Unknown CPU");
        r.register("cpu.cores", () -> Runtime.getRuntime().availableProcessors(), "1");

        r.register("client.display.resolution", PhantomPlaceholders::getDisplayResolution, "N/A");
        r.register("display.resolution", PhantomPlaceholders::getDisplayResolution, "N/A");
        r.register("screen.resolution", PhantomPlaceholders::getDisplayResolution, "N/A");

        r.register("client.mods.count", () -> FabricLoader.getInstance().getAllMods().size(), "0");
        r.register("mods.count", () -> FabricLoader.getInstance().getAllMods().size(), "0");

        r.register("server.ping.ms", PhantomPlaceholders::getServerPingMs, "0ms");

        // === Phantom Media (from MediaHudElement) ===
        r.register("phantom.media.title", MediaMetadataFetcher::getTitle, "");
        r.register("phantom.media.artist", MediaMetadataFetcher::getArtist, "");
        r.register("phantom.media", PhantomPlaceholders::getFormattedMedia, "");

        PhantomAddon.LOGGER.info("Phantom HUD placeholders registered successfully!");
    }

    public static void registerViaReflection(PlaceholderHandler handler) {
        if (registered) return;
        try {
            Method method = PlaceholderHandler.class.getDeclaredMethod(
                    "registerPlaceholder",
                    String.class,
                    PlaceholderHandler.PlaceholderGetter.class,
                    String.class
            );
            method.setAccessible(true);
            registerAll((name, getter, fallback) -> {
                try {
                    method.invoke(handler, name, getter, fallback);
                } catch (Exception e) {
                    PhantomAddon.LOGGER.error("Failed to register placeholder '{}': {}", name, e.getMessage());
                }
            });
        } catch (Exception e) {
            PhantomAddon.LOGGER.error("Failed to reflect registerPlaceholder method on PlaceholderHandler: ", e);
        }
    }

    // --- Helper Methods ---

    private static String getResolvedServerIp() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isLocalServer()) {
            return "Singleplayer";
        }
        if (mc.getConnection() != null && mc.getConnection().getConnection() != null) {
            SocketAddress sa = mc.getConnection().getConnection().getRemoteAddress();
            if (sa instanceof InetSocketAddress inet) {
                if (inet.getAddress() != null) {
                    return inet.getAddress().getHostAddress();
                }
                return inet.getHostString();
            }
        }
        ServerData data = mc.getCurrentServer();
        if (data != null && data.ip != null) {
            return getCachedOrResolve(data.ip, false);
        }
        return "None";
    }

    private static String getResolvedServerIpWithPort() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isLocalServer()) {
            return "Singleplayer";
        }
        if (mc.getConnection() != null && mc.getConnection().getConnection() != null) {
            SocketAddress sa = mc.getConnection().getConnection().getRemoteAddress();
            if (sa instanceof InetSocketAddress inet) {
                String host = inet.getAddress() != null ? inet.getAddress().getHostAddress() : inet.getHostString();
                return host + ":" + inet.getPort();
            }
        }
        ServerData data = mc.getCurrentServer();
        if (data != null && data.ip != null) {
            return getCachedOrResolve(data.ip, true);
        }
        return "None";
    }

    private static String getServerPort() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isLocalServer()) {
            return "25565";
        }
        if (mc.getConnection() != null && mc.getConnection().getConnection() != null) {
            SocketAddress sa = mc.getConnection().getConnection().getRemoteAddress();
            if (sa instanceof InetSocketAddress inet) {
                return String.valueOf(inet.getPort());
            }
        }
        ServerData data = mc.getCurrentServer();
        if (data != null && data.ip != null && data.ip.contains(":")) {
            try {
                return data.ip.substring(data.ip.lastIndexOf(':') + 1);
            } catch (Exception ignored) {}
        }
        return "25565";
    }

    private static String getCachedOrResolve(String hostAndPort, boolean includePort) {
        String host = hostAndPort;
        int port = 25565;
        if (hostAndPort.contains(":")) {
            String[] parts = hostAndPort.split(":", 2);
            host = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }

        String cached = RESOLVE_CACHE.get(host);
        if (cached != null) {
            return includePort ? (cached + ":" + port) : cached;
        }

        final String targetHost = host;
        if (PENDING_RESOLVES.add(targetHost)) {
            CompletableFuture.runAsync(() -> {
                try {
                    InetAddress addr = InetAddress.getByName(targetHost);
                    RESOLVE_CACHE.put(targetHost, addr.getHostAddress());
                } catch (Exception e) {
                    RESOLVE_CACHE.put(targetHost, targetHost);
                } finally {
                    PENDING_RESOLVES.remove(targetHost);
                }
            });
        }
        return includePort ? hostAndPort : host;
    }

    private static String getFullClientBrand() {
        Minecraft mc = Minecraft.getInstance();
        return "Minecraft " + SharedConstants.getCurrentVersion().name() + " (" + mc.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")";
    }

    private static String getServerBrandF3() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            String brand = mc.getConnection().serverBrand();
            if (brand != null && !brand.isEmpty()) {
                return brand;
            }
        }
        if (mc.isLocalServer()) {
            return "integrated";
        }
        return "vanilla";
    }

    private static int getServerProtocol() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCurrentServer() != null) {
            return mc.getCurrentServer().protocol;
        }
        return SharedConstants.getCurrentVersion().protocolVersion();
    }

    private static String getGpuRenderer() {
        GpuDevice device = RenderSystem.getDevice();
        return (device != null && device.getRenderer() != null) ? device.getRenderer() : "Unknown GPU";
    }

    private static String getGpuVendor() {
        GpuDevice device = RenderSystem.getDevice();
        return (device != null && device.getVendor() != null) ? device.getVendor() : "Unknown Vendor";
    }

    private static String getGpuVersion() {
        GpuDevice device = RenderSystem.getDevice();
        return (device != null && device.getVersion() != null) ? device.getVersion() : "Unknown Version";
    }

    private static String getGpuBackend() {
        GpuDevice device = RenderSystem.getDevice();
        return (device != null && device.getBackendName() != null) ? device.getBackendName() : "OpenGL";
    }

    private static String getCpuName() {
        String cpu = GLX._getCpuInfo();
        return (cpu != null && !cpu.isEmpty()) ? cpu : "Unknown CPU";
    }

    private static String getRamPercentString() {
        return getRamPercentNumber() + "%";
    }

    private static long getRamPercentNumber() {
        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long used = total - free;
        return max > 0 ? (used * 100L / max) : 0;
    }

    private static String getRamUsageSummary() {
        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long used = total - free;
        long percent = max > 0 ? (used * 100L / max) : 0;
        return (used / 1048576L) + "MB / " + (max / 1048576L) + "MB (" + percent + "%)";
    }

    private static String getRamUsedMb() {
        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory();
        return (used / 1048576L) + "MB";
    }

    private static String getRamMaxMb() {
        return (Runtime.getRuntime().maxMemory() / 1048576L) + "MB";
    }

    private static String getRamFreeMb() {
        return (Runtime.getRuntime().freeMemory() / 1048576L) + "MB";
    }

    private static String getRamTotalMb() {
        return (Runtime.getRuntime().totalMemory() / 1048576L) + "MB";
    }

    private static String getRamUsedGb() {
        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return String.format(Locale.ROOT, "%.2f GB", used / 1073741824.0);
    }

    private static String getRamMaxGb() {
        return String.format(Locale.ROOT, "%.2f GB", Runtime.getRuntime().maxMemory() / 1073741824.0);
    }

    private static String getDisplayResolution() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() != null) {
            return mc.getWindow().getWidth() + "x" + mc.getWindow().getHeight();
        }
        return "N/A";
    }

    private static String getServerPingMs() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCurrentServer() != null) {
            return mc.getCurrentServer().ping + "ms";
        }
        return "0ms";
    }

    private static String getFormattedMedia() {
        String title = MediaMetadataFetcher.getTitle();
        String artist = MediaMetadataFetcher.getArtist();
        if (title.isEmpty()) return "";
        if (artist.isEmpty()) return title;
        return title + " - " + artist;
    }
}
