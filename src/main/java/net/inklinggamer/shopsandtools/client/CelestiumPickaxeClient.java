package net.inklinggamer.shopsandtools.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.inklinggamer.shopsandtools.item.CelestiumPickaxeHelper;
import net.inklinggamer.shopsandtools.mixin.client.ClientPlayerInteractionManagerAccessor;
import net.inklinggamer.shopsandtools.mixin.client.HandledScreenAccessor;
import net.inklinggamer.shopsandtools.network.ToggleCelestiumPickaxeAreaModePayload;
import net.inklinggamer.shopsandtools.network.ToggleCelestiumPickaxeEnchantModePayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class CelestiumPickaxeClient {
    private static final int BREAKING_INFO_ID_BASE = 9000;

    private static final List<BlockPos> outlinePositions = new ArrayList<>();
    private static final List<BlockPos> breakingAnimationPositions = new ArrayList<>();

    private static boolean areaToggleHeld;
    private static BlockPos breakingCenter;
    private static Direction breakingFace;
    private static int lastBreakingStage = -1;

    private CelestiumPickaxeClient() {
    }

    public static void initialize() {
        ScreenEvents.AFTER_INIT.register(CelestiumPickaxeClient::registerInventoryToggleInput);
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            areaToggleHeld = false;
            clearOutline();
            clearBreakingAnimation(client);
            return;
        }

        if (!client.options.useKey.isPressed()) {
            areaToggleHeld = false;
        }

        updateOutline(client);
        updateBreakingAnimation(client);
    }

    public static void render(WorldRenderContext context) {
        CelestiumPickaxeOutlineRenderer.render(context, outlinePositions);
    }

    public static boolean handleRightClickToggle(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) {
            return false;
        }

        if (areaToggleHeld) {
            return true;
        }

        if (!CelestiumPickaxeHelper.canToggleAreaMining(
                client.player,
                client.world,
                client.crosshairTarget,
                client.interactionManager.getCurrentGameMode()
        )) {
            return false;
        }

        areaToggleHeld = true;
        ToggleCelestiumPickaxeAreaModePayload.send();
        return true;
    }

    public static void onBreakingAttempt(BlockPos pos, Direction direction) {
        breakingCenter = pos.toImmutable();
        breakingFace = direction;
    }

    public static void clearBreakingState() {
        breakingCenter = null;
        breakingFace = null;
    }

    private static void registerInventoryToggleInput(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof InventoryScreen inventoryScreen)) {
            return;
        }

        ScreenKeyboardEvents.allowKeyPress(screen).register((currentScreen, context) -> !handleInventoryKeyPress(client, inventoryScreen, context));
    }

    private static boolean handleInventoryKeyPress(MinecraftClient client, InventoryScreen screen, KeyInput context) {
        if (client.player == null || !shopsandtools$isShiftKey(context.key())) {
            return false;
        }

        Slot slot = ((HandledScreenAccessor) screen).shopsandtools$getFocusedSlot();
        if (slot == null || !slot.hasStack() || !CelestiumPickaxeHelper.isCelestiumPickaxe(slot.getStack())) {
            return false;
        }

        ToggleCelestiumPickaxeEnchantModePayload.send(slot.id);
        return true;
    }

    private static void updateOutline(MinecraftClient client) {
        outlinePositions.clear();
        if (!shopsandtools$canUseAreaMining(client) || client.interactionManager == null) {
            return;
        }

        if (!(client.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult hitResult)) {
            return;
        }

        if (!CelestiumPickaxeHelper.isValidMiningTarget(
                client.player,
                client.world,
                hitResult.getBlockPos(),
                client.interactionManager.getCurrentGameMode()
        )) {
            return;
        }

        outlinePositions.addAll(CelestiumPickaxeHelper.getMiningPlane(hitResult.getBlockPos(), hitResult.getSide()));
    }

    private static void updateBreakingAnimation(MinecraftClient client) {
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        if (!shopsandtools$canUseAreaMining(client) || interactionManager == null) {
            clearBreakingAnimation(client);
            return;
        }

        ClientPlayerInteractionManagerAccessor accessor = (ClientPlayerInteractionManagerAccessor) interactionManager;
        if (!accessor.shopsandtools$isBreakingBlock()) {
            clearBreakingAnimation(client);
            return;
        }

        BlockPos currentBreakingPos = accessor.shopsandtools$getCurrentBreakingPos();
        if (currentBreakingPos == null) {
            clearBreakingAnimation(client);
            return;
        }

        if (!CelestiumPickaxeHelper.isValidMiningTarget(
                client.player,
                client.world,
                currentBreakingPos,
                interactionManager.getCurrentGameMode()
        )) {
            clearBreakingAnimation(client);
            return;
        }

        if (breakingCenter == null || !breakingCenter.equals(currentBreakingPos) || breakingFace == null) {
            breakingCenter = currentBreakingPos.toImmutable();
            if (breakingFace == null) {
                clearBreakingAnimation(client);
                return;
            }
        }

        int currentStage = Math.max(-1, Math.min(9, (int) (accessor.shopsandtools$getCurrentBreakingProgress() * 10.0F) - 1));
        List<BlockPos> targets = CelestiumPickaxeHelper.getMiningPlane(currentBreakingPos, breakingFace).stream()
                .filter(pos -> !pos.equals(currentBreakingPos))
                .filter(pos -> !client.world.getBlockState(pos).isAir())
                .toList();

        if (currentStage == lastBreakingStage && breakingAnimationPositions.equals(targets)) {
            return;
        }

        clearBreakingAnimation(client);
        lastBreakingStage = currentStage;
        breakingAnimationPositions.addAll(targets);
        if (currentStage < 0) {
            return;
        }

        for (int index = 0; index < breakingAnimationPositions.size(); index++) {
            client.worldRenderer.setBlockBreakingInfo(BREAKING_INFO_ID_BASE + index, breakingAnimationPositions.get(index), currentStage);
        }
    }

    private static void clearOutline() {
        outlinePositions.clear();
    }

    private static void clearBreakingAnimation(MinecraftClient client) {
        for (int index = 0; index < breakingAnimationPositions.size(); index++) {
            client.worldRenderer.setBlockBreakingInfo(BREAKING_INFO_ID_BASE + index, breakingAnimationPositions.get(index), -1);
        }
        breakingAnimationPositions.clear();
        lastBreakingStage = -1;
    }

    private static boolean shopsandtools$canUseAreaMining(MinecraftClient client) {
        return client.player != null
                && CelestiumPickaxeHelper.isCelestiumPickaxe(client.player.getMainHandStack())
                && CelestiumPickaxeHelper.isAreaMiningEnabled(client.player.getMainHandStack());
    }

    private static boolean shopsandtools$isShiftKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT;
    }
}
