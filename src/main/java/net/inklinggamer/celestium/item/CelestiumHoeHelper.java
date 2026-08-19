package net.inklinggamer.celestium.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PotatoBlock;
import net.minecraft.world.level.block.state.BlockState;

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
        return stack.is(ModItems.CELESTIUM_HOE);
    }

    public static void initializeSmithingResult(ItemStack stack, RegistryAccess registryManager) {
        if (!isCelestiumHoe(stack)) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = registryManager.lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> efficiency = celestium$getEnchantment(enchantmentRegistry, Enchantments.EFFICIENCY);
        Holder<Enchantment> unbreaking = celestium$getEnchantment(enchantmentRegistry, Enchantments.UNBREAKING);
        Holder<Enchantment> fortune = celestium$getEnchantment(enchantmentRegistry, Enchantments.FORTUNE);

        EnchantmentHelper.updateEnchantments(stack, builder -> {
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
                positions.add(pos.immutable());
            }
        }

        return positions;
    }

    public static List<BlockPos> getHorizontalArea(BlockPos center) {
        List<BlockPos> positions = new ArrayList<>(9);
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                positions.add(center.offset(offsetX, 0, offsetZ));
            }
        }
        return positions;
    }

    public static BlockState getReplantState(BlockState harvestedState) {
        return harvestedState.getBlock().defaultBlockState();
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
            if (!drop.isEmpty() && drop.is(replantItem)) {
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

        if (state.is(Blocks.WHEAT)) {
            return SupportedCropType.WHEAT;
        }

        if (state.is(Blocks.CARROTS)) {
            return SupportedCropType.CARROT;
        }

        if (state.is(Blocks.POTATOES)) {
            return SupportedCropType.POTATO;
        }

        if (state.is(Blocks.BEETROOTS)) {
            return SupportedCropType.BEETROOT;
        }

        if (state.is(Blocks.NETHER_WART)) {
            return SupportedCropType.NETHER_WART;
        }

        return null;
    }

    private static int getCropAge(BlockState state, SupportedCropType cropType) {
        return switch (cropType) {
            case WHEAT -> state.getValue(CropBlock.AGE);
            case CARROT -> state.getValue(CarrotBlock.AGE);
            case POTATO -> state.getValue(PotatoBlock.AGE);
            case BEETROOT -> state.getValue(BeetrootBlock.AGE);
            case NETHER_WART -> state.getValue(NetherWartBlock.AGE);
        };
    }

    private static Holder<Enchantment> celestium$getEnchantment(Registry<Enchantment> registry, net.minecraft.resources.ResourceKey<Enchantment> key) {
        Enchantment enchantment = registry.getValueOrThrow(key);
        return registry.wrapAsHolder(enchantment);
    }
}
