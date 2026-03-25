package net.inklinggamer.shopsandtools.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public final class CelestiumPickaxeHelper {
    public static final String AREA_MINING_ENABLED_KEY = "AreaMiningEnabled";
    public static final String SILK_MODE_ENABLED_KEY = "SilkModeEnabled";
    public static final int EFFICIENCY_LEVEL = 10;
    public static final int UNBREAKING_LEVEL = 5;
    public static final int FORTUNE_LEVEL = 5;

    private CelestiumPickaxeHelper() {
    }

    public static boolean isCelestiumPickaxe(ItemStack stack) {
        return stack.isOf(ModItems.CELESTIUM_PICKAXE);
    }

    public static boolean isAreaMiningEnabled(ItemStack stack) {
        return shopsandtools$getBoolean(stack, AREA_MINING_ENABLED_KEY);
    }

    public static boolean isSilkModeEnabled(ItemStack stack) {
        return shopsandtools$getBoolean(stack, SILK_MODE_ENABLED_KEY);
    }

    public static boolean toggleAreaMining(ItemStack stack) {
        boolean enabled = !isAreaMiningEnabled(stack);
        setAreaMiningEnabled(stack, enabled);
        return enabled;
    }

    public static void setAreaMiningEnabled(ItemStack stack, boolean enabled) {
        shopsandtools$setBoolean(stack, AREA_MINING_ENABLED_KEY, enabled);
    }

    public static void setSilkModeEnabled(ItemStack stack, boolean enabled) {
        shopsandtools$setBoolean(stack, SILK_MODE_ENABLED_KEY, enabled);
    }

    public static void initializeSmithingResult(ItemStack stack, DynamicRegistryManager registryManager) {
        if (!isCelestiumPickaxe(stack)) {
            return;
        }

        setAreaMiningEnabled(stack, false);
        applyEnchantMode(stack, registryManager, false);
    }

    public static boolean toggleEnchantMode(ItemStack stack, DynamicRegistryManager registryManager) {
        boolean silkModeEnabled = !isSilkModeEnabled(stack);
        applyEnchantMode(stack, registryManager, silkModeEnabled);
        return silkModeEnabled;
    }

    public static boolean canToggleAreaMining(PlayerEntity player, World world, HitResult hitResult, GameMode gameMode) {
        if (!isCelestiumPickaxe(player.getMainHandStack())) {
            return false;
        }

        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            return false;
        }

        if (isInteractiveBlockTarget(player, world, hitResult)) {
            return false;
        }

        ItemStack offhandStack = player.getOffHandStack();
        if (offhandStack.isEmpty()) {
            return true;
        }

        if (offhandStack.getItem() instanceof BlockItem blockItem) {
            return !canPlaceOffhandBlock(player, offhandStack, blockItem, hitResult);
        }

        return isValidMiningTarget(player, world, hitResult, gameMode);
    }

    public static boolean isValidMiningTarget(PlayerEntity player, World world, HitResult hitResult, GameMode gameMode) {
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        return isValidMiningTarget(player, world, blockHitResult.getBlockPos(), gameMode);
    }

    public static boolean isValidMiningTarget(PlayerEntity player, World world, BlockPos pos, GameMode gameMode) {
        return getMiningDelta(player, world, pos, gameMode) > 0.0F;
    }

    public static AreaMiningTargets getAreaMiningTargets(PlayerEntity player, World world, BlockPos center, Direction face, GameMode gameMode) {
        List<BlockPos> positions = new ArrayList<>(9);
        float effectiveBreakingDelta = Float.MAX_VALUE;

        for (BlockPos pos : getMiningPlane(center, face)) {
            float miningDelta = getMiningDelta(player, world, pos, gameMode);
            if (miningDelta <= 0.0F) {
                continue;
            }

            positions.add(pos.toImmutable());
            effectiveBreakingDelta = Math.min(effectiveBreakingDelta, miningDelta);
        }

        return new AreaMiningTargets(
                positions,
                effectiveBreakingDelta == Float.MAX_VALUE ? 0.0F : effectiveBreakingDelta
        );
    }

    public static float getMiningDelta(PlayerEntity player, World world, BlockPos pos, GameMode gameMode) {
        if (!player.canInteractWithBlockAt(pos, 1.0D) || player.isBlockBreakingRestricted(world, pos, gameMode)) {
            return 0.0F;
        }

        BlockState state = world.getBlockState(pos);
        if (state.isAir() || state.getHardness(world, pos) < 0.0F) {
            return 0.0F;
        }

        ItemStack tool = player.getMainHandStack();
        if (tool.isEmpty() || !tool.canMine(state, world, pos, player)) {
            return 0.0F;
        }

        return state.calcBlockBreakingDelta(player, world, pos);
    }

    public static List<BlockPos> getMiningPlane(BlockPos center, Direction face) {
        List<BlockPos> positions = new ArrayList<>(9);

        for (int first = -1; first <= 1; first++) {
            for (int second = -1; second <= 1; second++) {
                positions.add(switch (face) {
                    case DOWN, UP -> center.add(first, 0, second);
                    case NORTH, SOUTH -> center.add(first, second, 0);
                    case WEST, EAST -> center.add(0, second, first);
                });
            }
        }

        return positions;
    }

    private static boolean canPlaceOffhandBlock(PlayerEntity player, ItemStack offhandStack, BlockItem blockItem, HitResult hitResult) {
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        if (!player.canInteractWithBlockAt(blockHitResult.getBlockPos(), 1.0D)) {
            return false;
        }

        ItemPlacementContext placementContext = blockItem.getPlacementContext(new ItemPlacementContext(player, Hand.OFF_HAND, offhandStack, blockHitResult));
        if (placementContext == null || !placementContext.canPlace()) {
            return false;
        }

        return placementContext.canPlace();
    }

    private static boolean isInteractiveBlockTarget(PlayerEntity player, World world, HitResult hitResult) {
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        if (!player.canInteractWithBlockAt(pos, 1.0D)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        if (state.isAir() || state.createScreenHandlerFactory(world, pos) != null) {
            return !state.isAir();
        }

        Block block = state.getBlock();
        return block instanceof ButtonBlock
                || block instanceof LeverBlock
                || block instanceof RepeaterBlock
                || block instanceof ComparatorBlock;
    }

    private static void applyEnchantMode(ItemStack stack, DynamicRegistryManager registryManager, boolean silkModeEnabled) {
        Registry<Enchantment> enchantmentRegistry = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> efficiency = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.EFFICIENCY);
        RegistryEntry<Enchantment> unbreaking = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.UNBREAKING);
        RegistryEntry<Enchantment> fortune = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.FORTUNE);
        RegistryEntry<Enchantment> silkTouch = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.SILK_TOUCH);

        EnchantmentHelper.apply(stack, builder -> {
            builder.set(efficiency, EFFICIENCY_LEVEL);
            builder.set(unbreaking, UNBREAKING_LEVEL);
            builder.remove(entry -> entry.matchesKey(Enchantments.FORTUNE) || entry.matchesKey(Enchantments.SILK_TOUCH));

            if (silkModeEnabled) {
                builder.set(silkTouch, 1);
            } else {
                builder.set(fortune, FORTUNE_LEVEL);
            }
        });
        setSilkModeEnabled(stack, silkModeEnabled);
    }

    private static RegistryEntry<Enchantment> shopsandtools$getEnchantment(Registry<Enchantment> registry, net.minecraft.registry.RegistryKey<Enchantment> key) {
        Enchantment enchantment = registry.getValueOrThrow(key);
        return registry.getEntry(enchantment);
    }

    private static boolean shopsandtools$getBoolean(ItemStack stack, String key) {
        NbtComponent customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        if (customData.isEmpty()) {
            return false;
        }

        NbtCompound nbt = customData.copyNbt();
        return nbt.getBoolean(key).orElse(false);
    }

    private static void shopsandtools$setBoolean(ItemStack stack, String key, boolean value) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            if (value) {
                nbt.putBoolean(key, true);
            } else {
                nbt.remove(key);
            }
        });
    }

    public record AreaMiningTargets(List<BlockPos> positions, float effectiveBreakingDelta) {
    }
}
