package net.inklinggamer.shopsandtools.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.CarrotsBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.PotatoesBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class CelestiumHoeHelper {
    public static final int EFFICIENCY_LEVEL = 10;
    public static final int UNBREAKING_LEVEL = 5;
    public static final int FORTUNE_LEVEL = 5;
    public static final double GROWTH_BOOST_RADIUS = 50.0D;
    public static final int EXTRA_RANDOM_TICKS = 19;

    public enum SupportedCropType {
        WHEAT("minecraft:wheat", 7, "minecraft:wheat_seeds"),
        CARROT("minecraft:carrots", 7, "minecraft:carrot"),
        POTATO("minecraft:potatoes", 7, "minecraft:potato"),
        BEETROOT("minecraft:beetroots", 3, "minecraft:beetroot_seeds"),
        NETHER_WART("minecraft:nether_wart", 3, "minecraft:nether_wart");

        private final String blockId;
        private final int matureAge;
        private final String replantCostItemId;

        SupportedCropType(String blockId, int matureAge, String replantCostItemId) {
            this.blockId = blockId;
            this.matureAge = matureAge;
            this.replantCostItemId = replantCostItemId;
        }

        public String blockId() {
            return this.blockId;
        }

        public int matureAge() {
            return this.matureAge;
        }

        public String replantCostItemId() {
            return this.replantCostItemId;
        }
    }

    private CelestiumHoeHelper() {
    }

    public static boolean isCelestiumHoe(ItemStack stack) {
        return stack.isOf(ModItems.CELESTIUM_HOE);
    }

    public static void initializeSmithingResult(ItemStack stack, DynamicRegistryManager registryManager) {
        if (!isCelestiumHoe(stack)) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> efficiency = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.EFFICIENCY);
        RegistryEntry<Enchantment> unbreaking = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.UNBREAKING);
        RegistryEntry<Enchantment> fortune = shopsandtools$getEnchantment(enchantmentRegistry, Enchantments.FORTUNE);

        EnchantmentHelper.apply(stack, builder -> {
            builder.set(efficiency, EFFICIENCY_LEVEL);
            builder.set(unbreaking, UNBREAKING_LEVEL);
            builder.set(fortune, FORTUNE_LEVEL);
        });
    }

    public static boolean isSupportedCrop(BlockState state) {
        return getCropType(state) != null;
    }

    public static boolean isMatureCrop(BlockState state) {
        SupportedCropType cropType = getCropType(state);
        if (cropType == null) {
            return false;
        }

        return isMatureAge(cropType, getCropAge(state, cropType));
    }

    public static boolean isSupportedMatureCrop(BlockState state) {
        return isSupportedCrop(state) && isMatureCrop(state);
    }

    public static boolean isGrowthBoostedCrop(BlockState state) {
        return isSupportedCrop(state) && !isMatureCrop(state);
    }

    public static List<BlockPos> getHarvestTargets(BlockPos center, Function<BlockPos, BlockState> stateProvider) {
        if (!isSupportedMatureCrop(stateProvider.apply(center))) {
            return List.of();
        }

        List<BlockPos> positions = new ArrayList<>(9);
        for (BlockPos pos : getHorizontalArea(center)) {
            if (isSupportedMatureCrop(stateProvider.apply(pos))) {
                positions.add(pos.toImmutable());
            }
        }

        return positions;
    }

    public static List<BlockPos> getHorizontalArea(BlockPos center) {
        List<BlockPos> positions = new ArrayList<>(9);
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                positions.add(center.add(offsetX, 0, offsetZ));
            }
        }
        return positions;
    }

    public static BlockState getReplantState(BlockState harvestedState) {
        return harvestedState.getBlock().getDefaultState();
    }

    public static Item getReplantCostItem(BlockState harvestedState) {
        SupportedCropType cropType = getCropType(harvestedState);
        if (cropType == SupportedCropType.WHEAT) {
            return Items.WHEAT_SEEDS;
        }

        if (cropType == SupportedCropType.BEETROOT) {
            return Items.BEETROOT_SEEDS;
        }

        if (cropType == SupportedCropType.CARROT) {
            return Items.CARROT;
        }

        if (cropType == SupportedCropType.POTATO) {
            return Items.POTATO;
        }

        if (cropType == SupportedCropType.NETHER_WART) {
            return Items.NETHER_WART;
        }

        return Items.AIR;
    }

    public static void consumeReplantItem(List<ItemStack> drops, Item replantItem) {
        if (replantItem == Items.AIR) {
            return;
        }

        for (ItemStack drop : drops) {
            if (!drop.isEmpty() && drop.isOf(replantItem)) {
                drop.setCount(getRemainingCountAfterReplant(drop.getCount()));
                return;
            }
        }
    }

    public static SupportedCropType getCropType(String blockId) {
        for (SupportedCropType cropType : SupportedCropType.values()) {
            if (cropType.blockId().equals(blockId)) {
                return cropType;
            }
        }

        return null;
    }

    public static boolean isMatureAge(SupportedCropType cropType, int age) {
        return age >= cropType.matureAge();
    }

    public static int getRemainingCountAfterReplant(int originalCount) {
        return Math.max(0, originalCount - 1);
    }

    private static SupportedCropType getCropType(BlockState state) {
        if (state.isAir()) {
            return null;
        }

        if (state.isOf(Blocks.WHEAT)) {
            return SupportedCropType.WHEAT;
        }

        if (state.isOf(Blocks.CARROTS)) {
            return SupportedCropType.CARROT;
        }

        if (state.isOf(Blocks.POTATOES)) {
            return SupportedCropType.POTATO;
        }

        if (state.isOf(Blocks.BEETROOTS)) {
            return SupportedCropType.BEETROOT;
        }

        if (state.isOf(Blocks.NETHER_WART)) {
            return SupportedCropType.NETHER_WART;
        }

        return null;
    }

    private static int getCropAge(BlockState state, SupportedCropType cropType) {
        return switch (cropType) {
            case WHEAT -> state.get(CropBlock.AGE);
            case CARROT -> state.get(CarrotsBlock.AGE);
            case POTATO -> state.get(PotatoesBlock.AGE);
            case BEETROOT -> state.get(BeetrootsBlock.AGE);
            case NETHER_WART -> state.get(NetherWartBlock.AGE);
        };
    }

    private static RegistryEntry<Enchantment> shopsandtools$getEnchantment(Registry<Enchantment> registry, net.minecraft.registry.RegistryKey<Enchantment> key) {
        Enchantment enchantment = registry.getValueOrThrow(key);
        return registry.getEntry(enchantment);
    }
}
