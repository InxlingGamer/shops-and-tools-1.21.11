package net.inklinggamer.shopsandtools.item;

import net.inklinggamer.shopsandtools.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.ArrayList;
import java.util.List;

public final class CelestiumShovelHelper {
    public static final String AREA_MINING_ENABLED_KEY = "AreaMiningEnabled";
    public static final int EFFICIENCY_LEVEL = 10;
    public static final int UNBREAKING_LEVEL = 5;

    private CelestiumShovelHelper() {
    }

    public static boolean isCelestiumShovel(ItemStack stack) {
        return stack.is(ModItems.CELESTIUM_SHOVEL);
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

    public static void initializeSmithingResult(ItemStack stack, RegistryAccess registryManager) {
        if (!isCelestiumShovel(stack)) {
            return;
        }

        setAreaMiningEnabled(stack, false);

        Registry<Enchantment> enchantmentRegistry = registryManager.lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> efficiency = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.EFFICIENCY);
        Holder<Enchantment> unbreaking = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.UNBREAKING);

        EnchantmentHelper.updateEnchantments(stack, builder -> {
            builder.set(efficiency, EFFICIENCY_LEVEL);
            builder.set(unbreaking, UNBREAKING_LEVEL);
        });
    }

    public static boolean canToggleAreaMining(Player player, Level world, HitResult hitResult, GameType gameMode) {
        if (!isCelestiumShovel(player.getMainHandItem())) {
            return false;
        }

        boolean entityTarget = hitResult != null && hitResult.getType() == HitResult.Type.ENTITY;
        boolean interactiveTarget = isInteractiveBlockTarget(player, world, hitResult);
        boolean offhandPlacementWouldSucceed = false;

        ItemStack offhandStack = player.getOffhandItem();
        if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof BlockItem blockItem) {
            offhandPlacementWouldSucceed = canPlaceOffhandBlock(player, offhandStack, blockItem, hitResult);
        }

        boolean validMiningTarget = isValidMiningTarget(player, world, hitResult, gameMode);
        boolean preservesNormalShovelUse = false;
        if (validMiningTarget && hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHitResult.getBlockPos();
            if (player.isWithinBlockInteractionRange(pos, 1.0D)) {
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

    public static boolean isValidMiningTarget(Player player, Level world, HitResult hitResult, GameType gameMode) {
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        return isValidMiningTarget(player, world, blockHitResult.getBlockPos(), gameMode);
    }

    public static boolean isValidMiningTarget(Player player, Level world, BlockPos pos, GameType gameMode) {
        return getMiningDelta(player, world, pos, gameMode) > 0.0F;
    }

    public static boolean isLooseEarthBlock(BlockState state) {
        return state.is(ModTags.Blocks.CELESTIUM_SHOVEL_AREA_MINEABLE) && state.is(BlockTags.MINEABLE_WITH_SHOVEL);
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
        return isPathConvertibleShovelBlockId(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
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

    public static boolean preservesNormalShovelUse(Level world, BlockPos pos, BlockState state) {
        if (state.is(Blocks.DIRT_PATH)) {
            return true;
        }

        if (isPathConvertibleShovelBlock(state)) {
            return world.getBlockState(pos.above()).isAir();
        }

        return state.getBlock() instanceof CampfireBlock
                && state.hasProperty(CampfireBlock.LIT)
                && state.getValue(CampfireBlock.LIT);
    }

    public static boolean canUseGroundSlam(Player player) {
        return isCelestiumShovel(player.getMainHandItem())
                && !player.isPassenger()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.isUnderWater()
                && !player.isSwimming()
                && !player.isFallFlying();
    }

    public static boolean canArmSlam(Player player, Level world, HitResult hitResult) {
        if (!canUseGroundSlam(player) || player.onGround()) {
            return false;
        }

        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        if (!player.isWithinBlockInteractionRange(pos, 1.0D)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        if (state.isAir() || state.getCollisionShape(world, pos).isEmpty()) {
            return false;
        }

        return blockHitResult.getLocation().y <= player.getY() - 0.25D || pos.getY() < player.getBlockY();
    }

    public static AreaMiningTargets getAreaMiningTargets(Player player, Level world, BlockPos center, Direction face, GameType gameMode) {
        List<BlockPos> breakablePositions = new ArrayList<>(9);
        List<BlockPos> outlinePositions = new ArrayList<>(9);
        float effectiveBreakingDelta = Float.MAX_VALUE;

        for (BlockPos pos : getMiningPlane(center, face)) {
            BlockState state = world.getBlockState(pos);
            if (state.isAir()) {
                outlinePositions.add(pos.immutable());
                continue;
            }

            float miningDelta = getMiningDelta(player, world, pos, gameMode);
            if (miningDelta <= 0.0F) {
                continue;
            }

            BlockPos immutablePos = pos.immutable();
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

    public static float getMiningDelta(Player player, Level world, BlockPos pos, GameType gameMode) {
        if (!player.isWithinBlockInteractionRange(pos, 1.0D) || player.blockActionRestricted(world, pos, gameMode)) {
            return 0.0F;
        }

        BlockState state = world.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(world, pos) < 0.0F || !isLooseEarthBlock(state)) {
            return 0.0F;
        }

        ItemStack tool = player.getMainHandItem();
        if (tool.isEmpty() || !tool.canDestroyBlock(state, world, pos, player)) {
            return 0.0F;
        }

        return state.getDestroyProgress(player, world, pos);
    }

    public static List<BlockPos> getMiningPlane(BlockPos center, Direction face) {
        List<BlockPos> positions = new ArrayList<>(9);

        for (int first = -1; first <= 1; first++) {
            for (int second = -1; second <= 1; second++) {
                positions.add(switch (face) {
                    case DOWN, UP -> center.offset(first, 0, second);
                    case NORTH, SOUTH -> center.offset(first, second, 0);
                    case WEST, EAST -> center.offset(0, second, first);
                });
            }
        }

        return positions;
    }

    private static boolean canPlaceOffhandBlock(Player player, ItemStack offhandStack, BlockItem blockItem, HitResult hitResult) {
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        if (!player.isWithinBlockInteractionRange(blockHitResult.getBlockPos(), 1.0D)) {
            return false;
        }

        BlockPlaceContext placementContext = blockItem.updatePlacementContext(new BlockPlaceContext(player, InteractionHand.OFF_HAND, offhandStack, blockHitResult));
        if (placementContext == null || !placementContext.canPlace()) {
            return false;
        }

        return placementContext.canPlace();
    }

    private static boolean isInteractiveBlockTarget(Player player, Level world, HitResult hitResult) {
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        if (!player.isWithinBlockInteractionRange(pos, 1.0D)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        if (state.isAir() || state.getMenuProvider(world, pos) != null) {
            return !state.isAir();
        }

        Block block = state.getBlock();
        return block instanceof ButtonBlock
                || block instanceof LeverBlock
                || block instanceof RepeaterBlock
                || block instanceof ComparatorBlock;
    }

    private static Holder<Enchantment> shopsandtools$getEnchantment(Registry<Enchantment> registry, net.minecraft.resources.ResourceKey<Enchantment> key) {
        Enchantment enchantment = registry.getValueOrThrow(key);
        return registry.wrapAsHolder(enchantment);
    }

    private static boolean shopsandtools$getBoolean(ItemStack stack, String key) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            return false;
        }

        CompoundTag nbt = customData.copyTag();
        return nbt.getBoolean(key).orElse(false);
    }

    private static void shopsandtools$setBoolean(ItemStack stack, String key, boolean value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
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
