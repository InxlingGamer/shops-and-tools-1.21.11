package net.inklinggamer.shopsandtools.item;

import net.inklinggamer.shopsandtools.util.ModTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public final class CelestiumShovelHelper {
    public static final String AREA_MINING_ENABLED_KEY = "AreaMiningEnabled";
    public static final int EFFICIENCY_LEVEL = 10;
    public static final int UNBREAKING_LEVEL = 5;

    private CelestiumShovelHelper() {
    }

    public static boolean isCelestiumShovel(ItemStack stack) {
        return stack.isOf(ModItems.CELESTIUM_SHOVEL);
    }

    public static boolean isAreaMiningEnabled(ItemStack stack) {
        return shopsandtools$getBoolean(stack, AREA_MINING_ENABLED_KEY);
    }

    public static boolean toggleAreaMining(ItemStack stack) {
        boolean enabled = !isAreaMiningEnabled(stack);
        setAreaMiningEnabled(stack, enabled);
        return enabled;
    }

    public static void setAreaMiningEnabled(ItemStack stack, boolean enabled) {
        shopsandtools$setBoolean(stack, AREA_MINING_ENABLED_KEY, enabled);
    }

    public static void initializeSmithingResult(ItemStack stack, DynamicRegistryManager registryManager) {
        if (!isCelestiumShovel(stack)) {
            return;
        }

        setAreaMiningEnabled(stack, false);

        Registry<Enchantment> enchantmentRegistry = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> efficiency = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.EFFICIENCY);
        RegistryEntry<Enchantment> unbreaking = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.UNBREAKING);

        EnchantmentHelper.apply(stack, builder -> {
            builder.set(efficiency, EFFICIENCY_LEVEL);
            builder.set(unbreaking, UNBREAKING_LEVEL);
        });
    }

    public static boolean canToggleAreaMining(PlayerEntity player, World world, HitResult hitResult, GameMode gameMode) {
        if (!isCelestiumShovel(player.getMainHandStack())) {
            return false;
        }

        boolean entityTarget = hitResult != null && hitResult.getType() == HitResult.Type.ENTITY;
        boolean interactiveTarget = isInteractiveBlockTarget(player, world, hitResult);
        boolean offhandPlacementWouldSucceed = false;

        ItemStack offhandStack = player.getOffHandStack();
        if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof BlockItem blockItem) {
            offhandPlacementWouldSucceed = canPlaceOffhandBlock(player, offhandStack, blockItem, hitResult);
        }

        boolean validMiningTarget = isValidMiningTarget(player, world, hitResult, gameMode);
        boolean preservesNormalShovelUse = false;
        if (validMiningTarget && hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHitResult.getBlockPos();
            if (player.canInteractWithBlockAt(pos, 1.0D)) {
                preservesNormalShovelUse = preservesNormalShovelUse(world, pos, world.getBlockState(pos));
            }
        }

        return shouldAllowAreaToggle(
                entityTarget,
                interactiveTarget,
                offhandPlacementWouldSucceed,
                validMiningTarget,
                preservesNormalShovelUse
        );
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

    public static boolean isLooseEarthBlock(BlockState state) {
        return state.isIn(ModTags.Blocks.CELESTIUM_SHOVEL_AREA_MINEABLE) && state.isIn(BlockTags.SHOVEL_MINEABLE);
    }

    public static boolean shouldAllowAreaToggle(
            boolean entityTarget,
            boolean interactiveTarget,
            boolean offhandPlacementWouldSucceed,
            boolean validMiningTarget,
            boolean preservesNormalShovelUse
    ) {
        if (entityTarget || interactiveTarget || offhandPlacementWouldSucceed) {
            return false;
        }

        if (!validMiningTarget) {
            return true;
        }

        return !preservesNormalShovelUse;
    }

    public static boolean isPathConvertibleShovelBlock(BlockState state) {
        return isPathConvertibleShovelBlockId(Registries.BLOCK.getId(state.getBlock()).toString());
    }

    public static boolean isPathConvertibleShovelBlockId(String blockId) {
        return switch (blockId) {
            case "minecraft:dirt",
                    "minecraft:grass_block",
                    "minecraft:coarse_dirt",
                    "minecraft:podzol",
                    "minecraft:rooted_dirt",
                    "minecraft:mycelium" -> true;
            default -> false;
        };
    }

    public static boolean preservesNormalShovelUse(World world, BlockPos pos, BlockState state) {
        if (state.isOf(Blocks.DIRT_PATH)) {
            return true;
        }

        if (isPathConvertibleShovelBlock(state)) {
            return world.getBlockState(pos.up()).isAir();
        }

        return state.getBlock() instanceof CampfireBlock
                && state.contains(CampfireBlock.LIT)
                && state.get(CampfireBlock.LIT);
    }

    public static boolean canUseGroundSlam(PlayerEntity player) {
        return isCelestiumShovel(player.getMainHandStack())
                && !player.hasVehicle()
                && !player.isClimbing()
                && !player.isTouchingWater()
                && !player.isSubmergedInWater()
                && !player.isSwimming()
                && !player.isGliding();
    }

    public static boolean canArmSlam(PlayerEntity player, World world, HitResult hitResult) {
        if (!canUseGroundSlam(player) || player.isOnGround()) {
            return false;
        }

        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        if (!player.canInteractWithBlockAt(pos, 1.0D)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        if (state.isAir() || state.getCollisionShape(world, pos).isEmpty()) {
            return false;
        }

        return blockHitResult.getPos().y <= player.getY() - 0.25D || pos.getY() < player.getBlockY();
    }

    public static AreaMiningTargets getAreaMiningTargets(PlayerEntity player, World world, BlockPos center, Direction face, GameMode gameMode) {
        List<BlockPos> breakablePositions = new ArrayList<>(9);
        List<BlockPos> outlinePositions = new ArrayList<>(9);
        float effectiveBreakingDelta = Float.MAX_VALUE;

        for (BlockPos pos : getMiningPlane(center, face)) {
            BlockState state = world.getBlockState(pos);
            if (state.isAir()) {
                outlinePositions.add(pos.toImmutable());
                continue;
            }

            float miningDelta = getMiningDelta(player, world, pos, gameMode);
            if (miningDelta <= 0.0F) {
                continue;
            }

            BlockPos immutablePos = pos.toImmutable();
            breakablePositions.add(immutablePos);
            outlinePositions.add(immutablePos);
            effectiveBreakingDelta = Math.min(effectiveBreakingDelta, miningDelta);
        }

        return new AreaMiningTargets(
                breakablePositions,
                outlinePositions,
                effectiveBreakingDelta == Float.MAX_VALUE ? 0.0F : effectiveBreakingDelta
        );
    }

    public static float getMiningDelta(PlayerEntity player, World world, BlockPos pos, GameMode gameMode) {
        if (!player.canInteractWithBlockAt(pos, 1.0D) || player.isBlockBreakingRestricted(world, pos, gameMode)) {
            return 0.0F;
        }

        BlockState state = world.getBlockState(pos);
        if (state.isAir() || state.getHardness(world, pos) < 0.0F || !isLooseEarthBlock(state)) {
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

    public record AreaMiningTargets(List<BlockPos> breakablePositions, List<BlockPos> outlinePositions, float effectiveBreakingDelta) {
    }
}
