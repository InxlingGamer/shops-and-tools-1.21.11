package net.inklinggamer.shopsandtools.client.xray;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CelestiumXrayController {
    private static final int SCAN_RADIUS = 16;
    private static final int PERIODIC_RESCAN_TICKS = 20;

    private static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.shopsandtools.celestium_xray",
            GLFW.GLFW_KEY_X,
            KeyMapping.Category.GAMEPLAY
    );

    private static final Set<OreOutlineEntry> renderQueue = Collections.synchronizedSet(new HashSet<>());
    private static final AtomicBoolean scanInProgress = new AtomicBoolean(false);

    private static boolean helmetEquipped;
    private static boolean xrayEnabledByPlayer;
    private static boolean scanDirty = true;
    private static BlockPos lastScanOrigin;
    private static long lastScanWorldTime = Long.MIN_VALUE;

    private CelestiumXrayController() {
    }

    public static void initialize() {
        KeyMappingHelper.registerKeyMapping(TOGGLE_KEY);
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            clearState();
            return;
        }

        updateHelmetState(client.player);
        handleToggleInput(client);

        if (!isActive()) {
            return;
        }

        if (shouldRescan(client.level, client.player)) {
            scheduleScan(client.level, client.player.blockPosition(), client);
        }
    }

    public static void render(LevelRenderContext context) {
        if (!isActive()) {
            return;
        }
        CelestiumXrayRenderer.render(context, renderQueue);
    }

    private static void handleToggleInput(Minecraft client) {
        while (TOGGLE_KEY.consumeClick()) {
            if (!helmetEquipped) {
                xrayEnabledByPlayer = false;
                clearOutlines();
                sendActionBar(client, "message.shopsandtools.celestium_xray_requires_helmet");
                continue;
            }

            xrayEnabledByPlayer = !xrayEnabledByPlayer;
            scanDirty = true;
            if (!xrayEnabledByPlayer) {
                clearOutlines();
            }

            sendActionBar(
                    client,
                    xrayEnabledByPlayer
                            ? "message.shopsandtools.celestium_xray_enabled"
                            : "message.shopsandtools.celestium_xray_disabled"
            );
        }
    }

    private static void updateHelmetState(Player player) {
        ItemStack equipped = player.getItemBySlot(EquipmentSlot.HEAD);
        boolean nowEquipped = equipped.is(ModItems.CELESTIUM_HELMET);
        if (nowEquipped == helmetEquipped) {
            return;
        }

        helmetEquipped = nowEquipped;
        scanDirty = true;
        if (!helmetEquipped) {
            xrayEnabledByPlayer = false;
            clearOutlines();
        }
    }

    private static boolean shouldRescan(ClientLevel world, Player player) {
        if (scanDirty) {
            return true;
        }

        BlockPos currentPos = player.blockPosition();
        if (!currentPos.equals(lastScanOrigin)) {
            return true;
        }

        return world.getGameTime() - lastScanWorldTime >= PERIODIC_RESCAN_TICKS;
    }

    private static void scheduleScan(ClientLevel world, BlockPos origin, Minecraft client) {
        if (!scanInProgress.compareAndSet(false, true)) {
            return;
        }

        scanDirty = false;
        BlockPos immutableOrigin = origin.immutable();

        Util.backgroundExecutor().execute(() -> {
            Set<OreOutlineEntry> scanned = scanWorld(world, immutableOrigin);
            client.execute(() -> {
                renderQueue.clear();
                renderQueue.addAll(scanned);
                CelestiumXrayRenderer.markDirty();
                lastScanOrigin = immutableOrigin;
                lastScanWorldTime = world.getGameTime();
                scanInProgress.set(false);
            });
        });
    }

    private static Set<OreOutlineEntry> scanWorld(Level world, BlockPos origin) {
        Set<OreOutlineEntry> found = new HashSet<>();

        for (int x = origin.getX() - SCAN_RADIUS; x <= origin.getX() + SCAN_RADIUS; x++) {
            for (int y = origin.getY() - SCAN_RADIUS; y <= origin.getY() + SCAN_RADIUS; y++) {
                if (y < world.getMinY() || y > world.getMaxY()) {
                    continue;
                }

                for (int z = origin.getZ() - SCAN_RADIUS; z <= origin.getZ() + SCAN_RADIUS; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
                    if (!world.hasChunk(chunkPos.x(), chunkPos.z())) {
                        continue;
                    }

                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    OreColor color = CelestiumOrePalette.getColor(block);
                    if (color != null) {
                        found.add(new OreOutlineEntry(pos.immutable(), color));
                    }
                }
            }
        }

        return found;
    }

    private static boolean isActive() {
        return helmetEquipped && xrayEnabledByPlayer;
    }

    private static void sendActionBar(Minecraft client, String translationKey) {
        if (client.player != null) {
            client.player.sendOverlayMessage(Component.translatable(translationKey));
        }
    }

    private static void clearOutlines() {
        renderQueue.clear();
        scanDirty = true;
        lastScanOrigin = null;
        lastScanWorldTime = Long.MIN_VALUE;
        CelestiumXrayRenderer.clear();
    }

    private static void clearState() {
        helmetEquipped = false;
        xrayEnabledByPlayer = false;
        scanDirty = true;
        lastScanOrigin = null;
        lastScanWorldTime = Long.MIN_VALUE;
        renderQueue.clear();
        scanInProgress.set(false);
        CelestiumXrayRenderer.clear();
    }
}
