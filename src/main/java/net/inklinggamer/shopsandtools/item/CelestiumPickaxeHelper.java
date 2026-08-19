package net.inklinggamer.shopsandtools.item;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CelestiumPickaxeHelper {
    public static final String AREA_MINING_ENABLED_KEY = "AreaMiningEnabled";
    public static final String SILK_MODE_ENABLED_KEY = "SilkModeEnabled";
    public static final int EFFICIENCY_LEVEL = 10;
    public static final int UNBREAKING_LEVEL = 5;
    public static final int FORTUNE_LEVEL = 5;
    private static final List<OreFamily> VEIN_ORE_FAMILIES = List.of(
            new OreFamily("ores/iron", ConventionalBlockTags.IRON_ORES),
            new OreFamily("ores/gold", ConventionalBlockTags.GOLD_ORES),
            new OreFamily("ores/copper", ConventionalBlockTags.COPPER_ORES),
            new OreFamily("ores/diamond", ConventionalBlockTags.DIAMOND_ORES),
            new OreFamily("ores/emerald", ConventionalBlockTags.EMERALD_ORES),
            new OreFamily("ores/lapis", ConventionalBlockTags.LAPIS_ORES),
            new OreFamily("ores/redstone", ConventionalBlockTags.REDSTONE_ORES),
            new OreFamily("ores/coal", ConventionalBlockTags.COAL_ORES),
            new OreFamily("ores/quartz", ConventionalBlockTags.QUARTZ_ORES),
            new OreFamily("ores/netherite_scrap", ConventionalBlockTags.NETHERITE_SCRAP_ORES)
    );

    private CelestiumPickaxeHelper() {
    }

    public static boolean isCelestiumPickaxe(ItemStack stack) {
        return stack.is(ModItems.CELESTIUM_PICKAXE);
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

    public static void synchronizeEnchantAndModeComponents(ItemStack targetStack, ItemStack sourceStack) {
        if (!isCelestiumPickaxe(targetStack) || !isCelestiumPickaxe(sourceStack)) {
            return;
        }

        targetStack.copyFrom(DataComponents.ENCHANTMENTS, sourceStack);
        targetStack.copyFrom(DataComponents.CUSTOM_DATA, sourceStack);
    }

    public static void initializeSmithingResult(ItemStack stack, RegistryAccess registryManager) {
        if (!isCelestiumPickaxe(stack)) {
            return;
        }

        setAreaMiningEnabled(stack, false);
        applyEnchantMode(stack, registryManager, false);
    }

    public static boolean toggleEnchantMode(ItemStack stack, RegistryAccess registryManager) {
        boolean silkModeEnabled = !isSilkModeEnabled(stack);
        applyEnchantMode(stack, registryManager, silkModeEnabled);
        return silkModeEnabled;
    }

    public static boolean canToggleAreaMining(Player player, Level world, HitResult hitResult, GameType gameMode) {
        if (!isCelestiumPickaxe(player.getMainHandItem())) {
            return false;
        }

        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            return false;
        }

        if (isInteractiveBlockTarget(player, world, hitResult)) {
            return false;
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (offhandStack.isEmpty()) {
            return true;
        }

        if (offhandStack.getItem() instanceof BlockItem blockItem) {
            return !canPlaceOffhandBlock(player, offhandStack, blockItem, hitResult);
        }

        return isValidMiningTarget(player, world, hitResult, gameMode);
    }

    public static boolean isValidMiningTarget(Player player, Level world, HitResult hitResult, GameType gameMode) {
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        return isAreaMiningCenterEligible(player, world, blockHitResult.getBlockPos(), gameMode);
    }

    public static boolean isValidMiningTarget(Player player, Level world, BlockPos pos, GameType gameMode) {
        return isAreaMiningCenterEligible(player, world, pos, gameMode);
    }

    public static boolean isAreaMiningCenterEligible(Player player, Level world, BlockPos pos, GameType gameMode) {
        return getMiningDelta(player, world, pos, gameMode) > 0.0F;
    }

    public static boolean shouldApplyAreaMining(boolean areaMiningEnabled, boolean centerEligible) {
        return areaMiningEnabled && centerEligible;
    }

    public static boolean shouldDeferBreakPrediction(boolean areaMiningEnabled, boolean breakingSelectionMatches, boolean centerEligible) {
        return shouldApplyAreaMining(areaMiningEnabled, centerEligible) && breakingSelectionMatches;
    }

    public static boolean shouldProcessStoredAreaBreak(boolean areaMiningEnabled, boolean breakingSelectionMatches) {
        return areaMiningEnabled && breakingSelectionMatches;
    }

    public static boolean shouldApplyVeinMining(boolean sneaking) {
        return !sneaking;
    }

    public static float resolveAreaMiningDelta(boolean areaMiningEnabled, boolean centerEligible, float areaMiningDelta, float fallbackDelta) {
        return shouldApplyAreaMining(areaMiningEnabled, centerEligible) && areaMiningDelta > 0.0F ? areaMiningDelta : fallbackDelta;
    }

    public static AreaMiningTargets getAreaMiningTargets(Player player, Level world, BlockPos center, Direction face, GameType gameMode) {
        if (!isAreaMiningCenterEligible(player, world, center, gameMode)) {
            return new AreaMiningTargets(List.of(), 0.0F);
        }

        List<BlockPos> positions = new ArrayList<>(9);
        float effectiveBreakingDelta = Float.MAX_VALUE;

        for (BlockPos pos : getMiningPlane(center, face)) {
            float miningDelta = getMiningDelta(player, world, pos, gameMode);
            if (miningDelta <= 0.0F) {
                continue;
            }

            positions.add(pos.immutable());
            effectiveBreakingDelta = Math.min(effectiveBreakingDelta, miningDelta);
        }

        return new AreaMiningTargets(
                positions,
                effectiveBreakingDelta == Float.MAX_VALUE ? 0.0F : effectiveBreakingDelta
        );
    }

    public static boolean isVeinMiningOre(BlockState state) {
        return state.is(ConventionalBlockTags.ORES);
    }

    public static List<BlockPos> getVeinMiningTargets(Player player, Level world, BlockPos centerPos, BlockState centerState, GameType gameMode) {
        if (!canVeinMineOre(player, world, centerPos, centerState, gameMode)) {
            return List.of();
        }

        List<BlockPos> positions = new ArrayList<>();
        ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(centerPos.immutable());

        while (!frontier.isEmpty()) {
            BlockPos current = frontier.removeFirst();
            BlockPos immutableCurrent = current.immutable();
            if (!visited.add(immutableCurrent)) {
                continue;
            }

            BlockState currentState = immutableCurrent.equals(centerPos) ? centerState : world.getBlockState(immutableCurrent);
            if (!canVeinMineOre(player, world, immutableCurrent, currentState, gameMode)
                    || !areMatchingVeinOres(centerState, currentState)) {
                continue;
            }

            positions.add(immutableCurrent);
            frontier.addAll(getVeinMiningNeighbors(immutableCurrent));
        }

        return positions;
    }

    public static List<BlockPos> combineSecondaryBreakTargets(BlockPos centerPos, Iterable<BlockPos> initialTargets, Iterable<BlockPos> veinTargets) {
        LinkedHashSet<BlockPos> combinedTargets = new LinkedHashSet<>();
        BlockPos immutableCenter = centerPos.immutable();

        for (BlockPos targetPos : initialTargets) {
            combinedTargets.add(targetPos.immutable());
        }

        for (BlockPos targetPos : veinTargets) {
            combinedTargets.add(targetPos.immutable());
        }

        combinedTargets.remove(immutableCenter);
        return combinedTargets.stream().toList();
    }

    public static float getMiningDelta(Player player, Level world, BlockPos pos, GameType gameMode) {
        if (!player.isWithinBlockInteractionRange(pos, 1.0D) || player.blockActionRestricted(world, pos, gameMode)) {
            return 0.0F;
        }

        BlockState state = world.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(world, pos) < 0.0F || !state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
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

    static List<BlockPos> getVeinMiningNeighbors(BlockPos center) {
        List<BlockPos> positions = new ArrayList<>(26);

        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
                        continue;
                    }

                    positions.add(center.offset(offsetX, offsetY, offsetZ));
                }
            }
        }

        return positions;
    }

    static boolean matchesResolvedOreFamily(String seedFamilyId, Block seedBlock, String candidateFamilyId, Block candidateBlock) {
        return matchesResolvedOreFamily(seedFamilyId, (Object) seedBlock, candidateFamilyId, candidateBlock);
    }

    static boolean matchesResolvedOreFamily(String seedFamilyId, String seedBlockId, String candidateFamilyId, String candidateBlockId) {
        return matchesResolvedOreFamily(seedFamilyId, (Object) seedBlockId, candidateFamilyId, candidateBlockId);
    }

    private static boolean matchesResolvedOreFamily(String seedFamilyId, Object seedIdentity, String candidateFamilyId, Object candidateIdentity) {
        if (seedFamilyId != null) {
            return seedFamilyId.equals(candidateFamilyId);
        }

        return seedIdentity.equals(candidateIdentity);
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

    private static void applyEnchantMode(ItemStack stack, RegistryAccess registryManager, boolean silkModeEnabled) {
        Registry<Enchantment> enchantmentRegistry = registryManager.lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> efficiency = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.EFFICIENCY);
        Holder<Enchantment> unbreaking = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.UNBREAKING);
        Holder<Enchantment> fortune = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.FORTUNE);
        Holder<Enchantment> silkTouch = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.SILK_TOUCH);

        EnchantmentHelper.updateEnchantments(stack, builder -> {
            builder.set(efficiency, EFFICIENCY_LEVEL);
            builder.set(unbreaking, UNBREAKING_LEVEL);
            builder.removeIf(entry -> entry.is(Enchantments.FORTUNE) || entry.is(Enchantments.SILK_TOUCH));

            if (silkModeEnabled) {
                builder.set(silkTouch, 1);
            } else {
                builder.set(fortune, FORTUNE_LEVEL);
            }
        });
        setSilkModeEnabled(stack, silkModeEnabled);
    }

    private static Holder<Enchantment> shopsandtools$getEnchantment(Registry<Enchantment> registry, net.minecraft.resources.ResourceKey<Enchantment> key) {
        Enchantment enchantment = registry.getValueOrThrow(key);
        return registry.wrapAsHolder(enchantment);
    }

    private static boolean canVeinMineOre(Player player, Level world, BlockPos pos, BlockState state, GameType gameMode) {
        if (player.blockActionRestricted(world, pos, gameMode)) {
            return false;
        }

        if (state.isAir()
                || state.getDestroySpeed(world, pos) < 0.0F
                || !state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || !isVeinMiningOre(state)) {
            return false;
        }

        ItemStack tool = player.getMainHandItem();
        return !tool.isEmpty() && tool.canDestroyBlock(state, world, pos, player);
    }

    private static boolean areMatchingVeinOres(BlockState seedState, BlockState candidateState) {
        if (!isVeinMiningOre(seedState) || !isVeinMiningOre(candidateState)) {
            return false;
        }

        return matchesResolvedOreFamily(
                resolveOreFamilyId(seedState),
                seedState.getBlock(),
                resolveOreFamilyId(candidateState),
                candidateState.getBlock()
        );
    }

    private static String resolveOreFamilyId(BlockState state) {
        if (!isVeinMiningOre(state)) {
            return null;
        }

        for (OreFamily oreFamily : VEIN_ORE_FAMILIES) {
            if (state.is(oreFamily.tag())) {
                return oreFamily.id();
            }
        }

        return null;
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

    public record AreaMiningTargets(List<BlockPos> positions, float effectiveBreakingDelta) {
    }

    private record OreFamily(String id, TagKey<Block> tag) {
    }
}
