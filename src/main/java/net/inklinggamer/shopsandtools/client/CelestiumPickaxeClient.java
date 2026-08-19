package net.inklinggamer.shopsandtools.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.inklinggamer.shopsandtools.item.CelestiumPickaxeHelper;
import net.inklinggamer.shopsandtools.mixin.client.ClientPlayerInteractionManagerAccessor;
import net.inklinggamer.shopsandtools.mixin.client.HandledScreenAccessor;
import net.inklinggamer.shopsandtools.network.ToggleCelestiumPickaxeAreaModePayload;
import net.inklinggamer.shopsandtools.network.ToggleCelestiumPickaxeEnchantModePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.SoundType;
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

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            areaToggleHeld = false;
            clearOutline();
            clearBreakingAnimation(client);
            return;
        }

        if (!client.options.keyUse.isDown()) {
            areaToggleHeld = false;
        }

        updateOutline(client);
        updateBreakingAnimation(client);
    }

    public static void render(WorldRenderContext context) {
        CelestiumPickaxeOutlineRenderer.render(context, outlinePositions);
    }

    public static boolean handleRightClickToggle(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            return false;
        }

        if (areaToggleHeld) {
            return true;
        }

        if (!CelestiumPickaxeHelper.canToggleAreaMining(
                client.player,
                client.level,
                client.hitResult,
                client.gameMode.getPlayerMode()
        )) {
            return false;
        }

        areaToggleHeld = true;
        ToggleCelestiumPickaxeAreaModePayload.send();
        return true;
    }

    public static void onBreakingAttempt(BlockPos pos, Direction direction) {
        breakingCenter = pos.immutable();
        breakingFace = direction;
    }

    public static void clearBreakingState() {
        breakingCenter = null;
        breakingFace = null;
    }

    public static float getAreaMiningDelta(Minecraft client, BlockPos pos, float fallbackDelta) {
        boolean centerEligible = shopsandtools$isAreaMiningCenterEligible(client, pos);
        if (!CelestiumPickaxeHelper.shouldDeferBreakPrediction(
                shopsandtools$canUseAreaMining(client),
                breakingFace != null && breakingCenter != null && breakingCenter.equals(pos),
                centerEligible
        )) {
            return fallbackDelta;
        }

        float areaMiningDelta = shopsandtools$getAreaMiningTargets(client, pos, breakingFace).effectiveBreakingDelta();
        return CelestiumPickaxeHelper.resolveAreaMiningDelta(
                shopsandtools$canUseAreaMining(client),
                centerEligible,
                areaMiningDelta,
                fallbackDelta
        );
    }

    public static boolean shouldDeferBreakPrediction(Minecraft client, BlockPos pos) {
        return CelestiumPickaxeHelper.shouldDeferBreakPrediction(
                shopsandtools$canUseAreaMining(client),
                breakingCenter != null && breakingCenter.equals(pos) && breakingFace != null,
                shopsandtools$isAreaMiningCenterEligible(client, pos)
        );
    }

    public static void playDeferredBreakSound(Minecraft client, BlockPos pos) {
        if (client.player == null || client.level == null) {
            return;
        }

        SoundType soundGroup = client.level.getBlockState(pos).getSoundType();
        if (soundGroup == null || soundGroup.getVolume() <= 0.0F) {
            return;
        }

        client.player.playSound(soundGroup.getBreakSound(), soundGroup.getVolume(), soundGroup.getPitch());
    }

    private static void registerInventoryToggleInput(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof InventoryScreen inventoryScreen)) {
            return;
        }

        ScreenKeyboardEvents.allowKeyPress(screen).register((currentScreen, context) -> !handleInventoryKeyPress(client, inventoryScreen, context));
    }

    private static boolean handleInventoryKeyPress(Minecraft client, InventoryScreen screen, KeyEvent context) {
        if (client.player == null || !shopsandtools$isShiftKey(context.key())) {
            return false;
        }

        Slot slot = ((HandledScreenAccessor) screen).shopsandtools$getFocusedSlot();
        if (slot == null || !slot.hasItem() || !CelestiumPickaxeHelper.isCelestiumPickaxe(slot.getItem())) {
            return false;
        }

        ToggleCelestiumPickaxeEnchantModePayload.send(slot.index);
        return true;
    }

    private static void updateOutline(Minecraft client) {
        outlinePositions.clear();
        if (!shopsandtools$canUseAreaMining(client) || client.gameMode == null) {
            return;
        }

        if (!(client.hitResult instanceof net.minecraft.world.phys.BlockHitResult hitResult)) {
            return;
        }

        if (!CelestiumPickaxeHelper.isAreaMiningCenterEligible(
                client.player,
                client.level,
                hitResult.getBlockPos(),
                client.gameMode.getPlayerMode()
        )) {
            return;
        }

        outlinePositions.addAll(shopsandtools$getAreaMiningTargets(client, hitResult.getBlockPos(), hitResult.getDirection()).positions());
    }

    private static void updateBreakingAnimation(Minecraft client) {
        MultiPlayerGameMode interactionManager = client.gameMode;
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
                client.level,
                currentBreakingPos,
                interactionManager.getPlayerMode()
        )) {
            clearBreakingAnimation(client);
            return;
        }

        if (breakingCenter == null || !breakingCenter.equals(currentBreakingPos) || breakingFace == null) {
            breakingCenter = currentBreakingPos.immutable();
            if (breakingFace == null) {
                clearBreakingAnimation(client);
                return;
            }
        }

        int currentStage = Math.max(-1, Math.min(9, (int) (accessor.shopsandtools$getCurrentBreakingProgress() * 10.0F) - 1));
        List<BlockPos> targets = shopsandtools$getAreaMiningTargets(client, currentBreakingPos, breakingFace).positions().stream()
                .filter(pos -> !pos.equals(currentBreakingPos))
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
            client.levelRenderer.destroyBlockProgress(BREAKING_INFO_ID_BASE + index, breakingAnimationPositions.get(index), currentStage);
        }
    }

    private static void clearOutline() {
        outlinePositions.clear();
    }

    private static void clearBreakingAnimation(Minecraft client) {
        for (int index = 0; index < breakingAnimationPositions.size(); index++) {
            client.levelRenderer.destroyBlockProgress(BREAKING_INFO_ID_BASE + index, breakingAnimationPositions.get(index), -1);
        }
        breakingAnimationPositions.clear();
        lastBreakingStage = -1;
    }

    private static boolean shopsandtools$canUseAreaMining(Minecraft client) {
        return client.player != null
                && CelestiumPickaxeHelper.isCelestiumPickaxe(client.player.getMainHandItem())
                && CelestiumPickaxeHelper.isAreaMiningEnabled(client.player.getMainHandItem());
    }

    private static CelestiumPickaxeHelper.AreaMiningTargets shopsandtools$getAreaMiningTargets(Minecraft client, BlockPos centerPos, Direction face) {
        if (!shopsandtools$canUseAreaMining(client) || client.player == null || client.level == null || client.gameMode == null) {
            return new CelestiumPickaxeHelper.AreaMiningTargets(List.of(), 0.0F);
        }

        return CelestiumPickaxeHelper.getAreaMiningTargets(
                client.player,
                client.level,
                centerPos,
                face,
                client.gameMode.getPlayerMode()
        );
    }

    private static boolean shopsandtools$isAreaMiningCenterEligible(Minecraft client, BlockPos pos) {
        return shopsandtools$canUseAreaMining(client)
                && client.player != null
                && client.level != null
                && client.gameMode != null
                && CelestiumPickaxeHelper.isAreaMiningCenterEligible(
                        client.player,
                        client.level,
                        pos,
                        client.gameMode.getPlayerMode()
                );
    }

    private static boolean shopsandtools$isShiftKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT;
    }
}
