package net.inklinggamer.shopsandtools.client.xray;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.inklinggamer.shopsandtools.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CelestiumXrayController {
    private static final int SCAN_RADIUS = 16;
    private static final int PERIODIC_RESCAN_TICKS = 20;

    private static final KeyBinding TOGGLE_KEY = new KeyBinding(
            "key.shopsandtools.celestium_xray",
            GLFW.GLFW_KEY_X,
            KeyBinding.Category.GAMEPLAY
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
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            clearState();
            return;
        }

        updateHelmetState(client.player);
        handleToggleInput(client);

        if (!isActive()) {
            return;
        }

        if (shouldRescan(client.world, client.player)) {
            scheduleScan(client.world, client.player.getBlockPos(), client);
        }
    }

    public static void render(WorldRenderContext context) {
        if (!isActive()) {
            return;
        }
        CelestiumXrayRenderer.render(context, renderQueue);
    }

    private static void handleToggleInput(MinecraftClient client) {
        while (TOGGLE_KEY.wasPressed()) {
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

    private static void updateHelmetState(PlayerEntity player) {
        ItemStack equipped = player.getEquippedStack(EquipmentSlot.HEAD);
        boolean nowEquipped = equipped.isOf(ModItems.CELESTIUM_HELMET);
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

    private static boolean shouldRescan(ClientWorld world, PlayerEntity player) {
        if (scanDirty) {
            return true;
        }

        BlockPos currentPos = player.getBlockPos();
        if (!currentPos.equals(lastScanOrigin)) {
            return true;
        }

        return world.getTime() - lastScanWorldTime >= PERIODIC_RESCAN_TICKS;
    }

    private static void scheduleScan(ClientWorld world, BlockPos origin, MinecraftClient client) {
        if (!scanInProgress.compareAndSet(false, true)) {
            return;
        }

        scanDirty = false;
        BlockPos immutableOrigin = origin.toImmutable();

        Util.getMainWorkerExecutor().execute(() -> {
            Set<OreOutlineEntry> scanned = scanWorld(world, immutableOrigin);
            client.execute(() -> {
                renderQueue.clear();
                renderQueue.addAll(scanned);
                lastScanOrigin = immutableOrigin;
                lastScanWorldTime = world.getTime();
                scanInProgress.set(false);
            });
        });
    }

    private static Set<OreOutlineEntry> scanWorld(World world, BlockPos origin) {
        Set<OreOutlineEntry> found = new HashSet<>();

        for (int x = origin.getX() - SCAN_RADIUS; x <= origin.getX() + SCAN_RADIUS; x++) {
            for (int y = origin.getY() - SCAN_RADIUS; y <= origin.getY() + SCAN_RADIUS; y++) {
                if (y < world.getBottomY() || y > world.getTopYInclusive()) {
                    continue;
                }

                for (int z = origin.getZ() - SCAN_RADIUS; z <= origin.getZ() + SCAN_RADIUS; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    ChunkPos chunkPos = new ChunkPos(pos);
                    if (!world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
                        continue;
                    }

                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    OreColor color = CelestiumOrePalette.getColor(block);
                    if (color != null) {
                        found.add(new OreOutlineEntry(pos.toImmutable(), color));
                    }
                }
            }
        }

        return found;
    }

    private static boolean isActive() {
        return helmetEquipped && xrayEnabledByPlayer;
    }

    private static void sendActionBar(MinecraftClient client, String translationKey) {
        if (client.player != null) {
            client.player.sendMessage(Text.translatable(translationKey), true);
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
