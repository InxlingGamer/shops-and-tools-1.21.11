package net.inklinggamer.celestium.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.inklinggamer.celestium.item.CelestiumPickaxeHelper;
import net.inklinggamer.celestium.mixin.client.ClientPlayerInteractionManagerAccessor;
import net.inklinggamer.celestium.mixin.client.HandledScreenAccessor;
import net.inklinggamer.celestium.network.ToggleCelestiumPickaxeAreaModePayload;
import net.inklinggamer.celestium.network.ToggleCelestiumPickaxeEnchantModePayload;
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

    public static void render(LevelRenderContext context) {
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
        CelestiumClientNetworking.send(new ToggleCelestiumPickaxeAreaModePayload());
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
        boolean centerEligible = celestium$isAreaMiningCenterEligible(client, pos);
        if (!CelestiumPickaxeHelper.shouldDeferBreakPrediction(
                celestium$canUseAreaMining(client),
                breakingFace != null && breakingCenter != null && breakingCenter.equals(pos),
                centerEligible
        )) {
            return fallbackDelta;
        }

        float areaMiningDelta = celestium$getAreaMiningTargets(client, pos, breakingFace).effectiveBreakingDelta();
        return CelestiumPickaxeHelper.resolveAreaMiningDelta(
                celestium$canUseAreaMining(client),
                centerEligible,
                areaMiningDelta,
                fallbackDelta
        );
    }

    public static boolean shouldDeferBreakPrediction(Minecraft client, BlockPos pos) {
        return CelestiumPickaxeHelper.shouldDeferBreakPrediction(
                celestium$canUseAreaMining(client),
                breakingCenter != null && breakingCenter.equals(pos) && breakingFace != null,
                celestium$isAreaMiningCenterEligible(client, pos)
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
        if (client.player == null || !celestium$isShiftKey(context.key())) {
            return false;
        }

        Slot slot = ((HandledScreenAccessor) screen).celestium$getFocusedSlot();
        if (slot == null || !slot.hasItem() || !CelestiumPickaxeHelper.isCelestiumPickaxe(slot.getItem())) {
            return false;
        }

        CelestiumClientNetworking.send(new ToggleCelestiumPickaxeEnchantModePayload(slot.index));
        return true;
    }

    private static void updateOutline(Minecraft client) {
        outlinePositions.clear();
        if (!celestium$canUseAreaMining(client) || client.gameMode == null) {
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

        outlinePositions.addAll(celestium$getAreaMiningTargets(client, hitResult.getBlockPos(), hitResult.getDirection()).positions());
    }

    private static void updateBreakingAnimation(Minecraft client) {
        MultiPlayerGameMode interactionManager = client.gameMode;
        if (!celestium$canUseAreaMining(client) || interactionManager == null) {
            clearBreakingAnimation(client);
            return;
        }

        ClientPlayerInteractionManagerAccessor accessor = (ClientPlayerInteractionManagerAccessor) interactionManager;
        if (!accessor.celestium$isBreakingBlock()) {
            clearBreakingAnimation(client);
            return;
        }

        BlockPos currentBreakingPos = accessor.celestium$getCurrentBreakingPos();
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

        int currentStage = Math.max(-1, Math.min(9, (int) (accessor.celestium$getCurrentBreakingProgress() * 10.0F) - 1));
        List<BlockPos> targets = celestium$getAreaMiningTargets(client, currentBreakingPos, breakingFace).positions().stream()
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
            client.level.destroyBlockProgress(BREAKING_INFO_ID_BASE + index, breakingAnimationPositions.get(index), currentStage);
        }
    }

    private static void clearOutline() {
        outlinePositions.clear();
    }

    private static void clearBreakingAnimation(Minecraft client) {
        for (int index = 0; index < breakingAnimationPositions.size(); index++) {
            client.level.destroyBlockProgress(BREAKING_INFO_ID_BASE + index, breakingAnimationPositions.get(index), -1);
        }
        breakingAnimationPositions.clear();
        lastBreakingStage = -1;
    }

    private static boolean celestium$canUseAreaMining(Minecraft client) {
        return client.player != null
                && CelestiumPickaxeHelper.isCelestiumPickaxe(client.player.getMainHandItem())
                && CelestiumPickaxeHelper.isAreaMiningEnabled(client.player.getMainHandItem());
    }

    private static CelestiumPickaxeHelper.AreaMiningTargets celestium$getAreaMiningTargets(Minecraft client, BlockPos centerPos, Direction face) {
        if (!celestium$canUseAreaMining(client) || client.player == null || client.level == null || client.gameMode == null) {
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

    private static boolean celestium$isAreaMiningCenterEligible(Minecraft client, BlockPos pos) {
        return celestium$canUseAreaMining(client)
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

    private static boolean celestium$isShiftKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT;
    }
}
