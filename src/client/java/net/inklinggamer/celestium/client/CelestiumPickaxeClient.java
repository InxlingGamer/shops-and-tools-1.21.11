package net.inklinggamer.celestium.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.inklinggamer.celestium.item.CelestiumPickaxeHelper;
import net.inklinggamer.celestium.mixin.client.ClientPlayerInteractionManagerAccessor;
import net.inklinggamer.celestium.mixin.client.HandledScreenAccessor;
import net.inklinggamer.celestium.network.ToggleCelestiumPickaxeAreaModePayload;
import net.inklinggamer.celestium.network.ToggleCelestiumPickaxeEnchantModePayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.sound.BlockSoundGroup;
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
        CelestiumClientNetworking.send(new ToggleCelestiumPickaxeAreaModePayload());
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

    public static float getAreaMiningDelta(MinecraftClient client, BlockPos pos, float fallbackDelta) {
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

    public static boolean shouldDeferBreakPrediction(MinecraftClient client, BlockPos pos) {
        return CelestiumPickaxeHelper.shouldDeferBreakPrediction(
                celestium$canUseAreaMining(client),
                breakingCenter != null && breakingCenter.equals(pos) && breakingFace != null,
                celestium$isAreaMiningCenterEligible(client, pos)
        );
    }

    public static void playDeferredBreakSound(MinecraftClient client, BlockPos pos) {
        if (client.player == null || client.world == null) {
            return;
        }

        BlockSoundGroup soundGroup = client.world.getBlockState(pos).getSoundGroup();
        if (soundGroup == null || soundGroup.getVolume() <= 0.0F) {
            return;
        }

        client.player.playSound(soundGroup.getBreakSound(), soundGroup.getVolume(), soundGroup.getPitch());
    }

    private static void registerInventoryToggleInput(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof InventoryScreen inventoryScreen)) {
            return;
        }

        ScreenKeyboardEvents.allowKeyPress(screen).register((currentScreen, keyCode, scanCode, modifiers) -> !handleInventoryKeyPress(client, inventoryScreen, keyCode));
    }

    private static boolean handleInventoryKeyPress(MinecraftClient client, InventoryScreen screen, int keyCode) {
        if (client.player == null || !celestium$isShiftKey(keyCode)) {
            return false;
        }

        Slot slot = ((HandledScreenAccessor) screen).celestium$getFocusedSlot();
        if (slot == null || !slot.hasStack() || !CelestiumPickaxeHelper.isCelestiumPickaxe(slot.getStack())) {
            return false;
        }

        CelestiumClientNetworking.send(new ToggleCelestiumPickaxeEnchantModePayload(slot.id));
        return true;
    }

    private static void updateOutline(MinecraftClient client) {
        outlinePositions.clear();
        if (!celestium$canUseAreaMining(client) || client.interactionManager == null) {
            return;
        }

        if (!(client.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult hitResult)) {
            return;
        }

        if (!CelestiumPickaxeHelper.isAreaMiningCenterEligible(
                client.player,
                client.world,
                hitResult.getBlockPos(),
                client.interactionManager.getCurrentGameMode()
        )) {
            return;
        }

        outlinePositions.addAll(celestium$getAreaMiningTargets(client, hitResult.getBlockPos(), hitResult.getSide()).positions());
    }

    private static void updateBreakingAnimation(MinecraftClient client) {
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
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

    private static boolean celestium$canUseAreaMining(MinecraftClient client) {
        return client.player != null
                && CelestiumPickaxeHelper.isCelestiumPickaxe(client.player.getMainHandStack())
                && CelestiumPickaxeHelper.isAreaMiningEnabled(client.player.getMainHandStack());
    }

    private static CelestiumPickaxeHelper.AreaMiningTargets celestium$getAreaMiningTargets(MinecraftClient client, BlockPos centerPos, Direction face) {
        if (!celestium$canUseAreaMining(client) || client.player == null || client.world == null || client.interactionManager == null) {
            return new CelestiumPickaxeHelper.AreaMiningTargets(List.of(), 0.0F);
        }

        return CelestiumPickaxeHelper.getAreaMiningTargets(
                client.player,
                client.world,
                centerPos,
                face,
                client.interactionManager.getCurrentGameMode()
        );
    }

    private static boolean celestium$isAreaMiningCenterEligible(MinecraftClient client, BlockPos pos) {
        return celestium$canUseAreaMining(client)
                && client.player != null
                && client.world != null
                && client.interactionManager != null
                && CelestiumPickaxeHelper.isAreaMiningCenterEligible(
                        client.player,
                        client.world,
                        pos,
                        client.interactionManager.getCurrentGameMode()
                );
    }

    private static boolean celestium$isShiftKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT;
    }
}
