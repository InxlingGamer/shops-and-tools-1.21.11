package net.inklinggamer.celestium.migration;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.block.ModBlocks;
import net.inklinggamer.celestium.item.ModItems;
import net.inklinggamer.celestium.mixin.PlayerAdvancementsAccessor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class LegacyContentBridge {
    public static final String LEGACY_MOD_ID = "shopsandtools";

    private static final Map<Item, Item> LEGACY_ITEMS = new IdentityHashMap<>();
    private static final Queue<PendingChunk> PENDING_CHUNKS = new ConcurrentLinkedQueue<>();
    private static final List<String> ADVANCEMENTS = List.of(
            "celestium/root",
            "celestium/executioner_of_the_deep",
            "celestium/a_strange_energy",
            "celestium/beyond_netherite",
            "celestium/bound_to_the_sky",
            "celestium/touch_grass",
            "celestium/fully_ascended"
    );
    private static final List<String> RECIPES = List.of(
            "celestium", "celestium_block", "celestium_from_block", "celestium_upgrade_template",
            "celestium_upgrade_template_duplication", "celestium_helmet", "celestium_chestplate",
            "celestium_leggings", "celestium_boots", "celestium_sword", "celestium_pickaxe",
            "celestium_axe", "celestium_shovel", "celestium_hoe", "celestium_spear", "celestium_horse_armor"
    );
    private static final List<String> ATTRIBUTE_MODIFIERS = List.of(
            "celestium_chest_bonus_health",
            "celestium_sword_rage_attack_speed",
            "celestium_horse_armor_bonus_health",
            "celestium_horse_armor_step_height"
    );

    private static Block legacyBlock;
    private static int tickCounter;

    private LegacyContentBridge() {
    }

    public static void register() {
        registerLegacyBlock();
        registerLegacyItem("celestium", ModItems.CELESTIUM);
        registerLegacyItem("skulk_venom", ModItems.SKULK_VENOM);
        registerLegacyItem("warden_heart", ModItems.WARDEN_HEART);
        registerLegacyItem("celestium_upgrade_template", ModItems.CELESTIUM_UPGRADE_TEMPLATE);
        registerLegacyItem("celestium_helmet", ModItems.CELESTIUM_HELMET);
        registerLegacyItem("celestium_chestplate", ModItems.CELESTIUM_CHESTPLATE);
        registerLegacyItem("celestium_elytra_chestplate", ModItems.CELESTIUM_ELYTRA_CHESTPLATE);
        registerLegacyItem("celestium_leggings", ModItems.CELESTIUM_LEGGINGS);
        registerLegacyItem("celestium_boots", ModItems.CELESTIUM_BOOTS);
        registerLegacyItem("celestium_horse_armor", ModItems.CELESTIUM_HORSE_ARMOR);
        registerLegacyItem("celestium_sword", ModItems.CELESTIUM_SWORD);
        registerLegacyItem("celestium_pickaxe", ModItems.CELESTIUM_PICKAXE);
        registerLegacyItem("celestium_spear", ModItems.CELESTIUM_SPEAR);
        registerLegacyItem("celestium_shovel", ModItems.CELESTIUM_SHOVEL);
        registerLegacyItem("celestium_axe", ModItems.CELESTIUM_AXE);
        registerLegacyItem("celestium_hoe", ModItems.CELESTIUM_HOE);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> LegacyMigrationState.get(server).logFirstRunWarning());
        ServerEntityEvents.ENTITY_LOAD.register(LegacyContentBridge::onEntityLoad);
        ServerChunkEvents.CHUNK_LOAD.register(LegacyContentBridge::onChunkLoad);
    }

    public static void migratePlayer(ServerPlayer player) {
        int converted = migrateContainer(player.getInventory());
        converted += migrateContainer(player.getEnderChestInventory());
        converted += migrateLivingEntity(player);

        for (Slot slot : player.containerMenu.slots) {
            ItemStack replacement = convertStack(slot.getItem());
            if (replacement != slot.getItem()) {
                slot.set(replacement);
                converted++;
            }
        }

        ItemStack carried = player.containerMenu.getCarried();
        ItemStack replacement = convertStack(carried);
        if (replacement != carried) {
            player.containerMenu.setCarried(replacement);
            converted++;
        }

        migrateAdvancements(player);
        migrateRecipes(player);
        record(player.level().getServer(), converted, 0);
    }

    public static void tickServer(MinecraftServer server) {
        for (int processed = 0; processed < 4; processed++) {
            PendingChunk pending = PENDING_CHUNKS.poll();
            if (pending == null) {
                break;
            }
            migrateChunk(pending.level(), pending.chunk());
        }

        if (++tickCounter < 20) {
            return;
        }
        tickCounter = 0;
        server.getPlayerList().getPlayers().forEach(LegacyContentBridge::migratePlayer);
    }

    public static ItemStack convertStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }

        Item canonical = LEGACY_ITEMS.get(stack.getItem());
        if (canonical == null) {
            return stack;
        }

        return copyToCanonical(stack, canonical);
    }

    static ItemStack copyToCanonical(ItemStack stack, Item canonical) {
        return new ItemStack(canonical.builtInRegistryHolder(), stack.getCount(), stack.getComponentsPatch());
    }

    static net.minecraft.world.level.block.state.BlockState convertBlockState(
            net.minecraft.world.level.block.state.BlockState state, Block legacy, Block canonical) {
        return state.is(legacy) ? canonical.defaultBlockState() : state;
    }

    public static boolean isLegacyItem(Item item) {
        return LEGACY_ITEMS.containsKey(item);
    }

    public static Block legacyBlock() {
        return legacyBlock;
    }

    private static void registerLegacyBlock() {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, legacyId("celestium_block"));
        legacyBlock = Registry.register(
                BuiltInRegistries.BLOCK,
                blockKey,
                new Block(BlockBehaviour.Properties.ofFullCopy(ModBlocks.CELESTIUM_BLOCK).setId(blockKey))
        );

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, legacyId("celestium_block"));
        Item legacyItem = Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                new BlockItem(legacyBlock, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix())
        );
        Item canonicalItem = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "celestium_block"));
        LEGACY_ITEMS.put(legacyItem, canonicalItem);
    }

    private static void registerLegacyItem(String path, Item canonical) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, legacyId(path));
        Item legacy = Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().fireResistant().setId(key)));
        LEGACY_ITEMS.put(legacy, canonical);
    }

    private static void onEntityLoad(Entity entity, ServerLevel level) {
        int converted = 0;
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack oldStack = itemEntity.getItem();
            ItemStack replacement = convertStack(oldStack);
            if (replacement != oldStack) {
                itemEntity.setItem(replacement);
                converted++;
            }
        }
        if (entity instanceof LivingEntity livingEntity) {
            converted += migrateLivingEntity(livingEntity);
        }
        if (entity instanceof Container container) {
            converted += migrateContainer(container);
        }
        if (entity instanceof AbstractHorse horse) {
            for (int index = 0; index < horse.getInventorySize(); index++) {
                SlotAccess access = horse.getSlot(AbstractHorse.INVENTORY_SLOT_OFFSET + index);
                ItemStack stack = access.get();
                ItemStack replacement = convertStack(stack);
                if (replacement != stack && access.set(replacement)) {
                    converted++;
                }
            }
        }
        record(level.getServer(), converted, 0);
    }

    private static void onChunkLoad(ServerLevel level, LevelChunk chunk, boolean newlyGenerated) {
        PENDING_CHUNKS.add(new PendingChunk(level, chunk));
    }

    private static void migrateChunk(ServerLevel level, LevelChunk chunk) {
        int convertedItems = 0;
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof Container container) {
                convertedItems += migrateContainer(container);
            }
        }

        int convertedBlocks = 0;
        LevelChunkSection[] sections = chunk.getSections();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (!section.maybeHas(state -> state.is(legacyBlock))) {
                continue;
            }

            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        if (section.getBlockState(x, y, z).is(legacyBlock)) {
                            section.setBlockState(
                                    x, y, z,
                                    convertBlockState(section.getBlockState(x, y, z), legacyBlock, ModBlocks.CELESTIUM_BLOCK),
                                    false
                            );
                            convertedBlocks++;
                        }
                    }
                }
            }
        }
        if (convertedBlocks > 0) {
            chunk.markUnsaved();
        }
        record(level.getServer(), convertedItems, convertedBlocks);
    }

    private static int migrateContainer(Container container) {
        int converted = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack oldStack = container.getItem(slot);
            ItemStack replacement = convertStack(oldStack);
            if (replacement != oldStack) {
                container.setItem(slot, replacement);
                converted++;
            }
        }
        if (converted > 0) {
            container.setChanged();
        }
        return converted;
    }

    private static int migrateLivingEntity(LivingEntity entity) {
        int converted = 0;
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack oldStack = entity.getItemBySlot(slot);
            ItemStack replacement = convertStack(oldStack);
            if (replacement != oldStack) {
                entity.setItemSlot(slot, replacement);
                converted++;
            }
        }

        for (var attribute : entity.getAttributes().getSyncableAttributes()) {
            for (String path : ATTRIBUTE_MODIFIERS) {
                attribute.removeModifier(legacyId(path));
            }
        }
        return converted;
    }

    private static void migrateAdvancements(ServerPlayer player) {
        var manager = player.level().getServer().getAdvancements();
        var playerAdvancements = player.getAdvancements();
        boolean changed = false;
        for (String path : ADVANCEMENTS) {
            AdvancementHolder legacy = manager.get(legacyId(path));
            AdvancementHolder canonical = manager.get(Identifier.fromNamespaceAndPath(Celestium.MOD_ID, path));
            if (legacy == null || canonical == null) {
                continue;
            }

            AdvancementProgress oldProgress = playerAdvancements.getOrStartProgress(legacy);
            AdvancementProgress newProgress = playerAdvancements.getOrStartProgress(canonical);
            for (String criterion : oldProgress.getCompletedCriteria()) {
                changed |= newProgress.grantProgress(criterion);
            }
            if (changed) {
                ((PlayerAdvancementsAccessor) playerAdvancements).celestium$getProgressChanged().add(canonical);
            }
        }
        if (changed) {
            playerAdvancements.flushDirty(player, true);
        }
    }

    private static void migrateRecipes(ServerPlayer player) {
        boolean changed = false;
        for (String path : RECIPES) {
            ResourceKey<Recipe<?>> legacy = ResourceKey.create(Registries.RECIPE, legacyId(path));
            ResourceKey<Recipe<?>> canonical = ResourceKey.create(
                    Registries.RECIPE, Identifier.fromNamespaceAndPath(Celestium.MOD_ID, path));
            if (player.getRecipeBook().contains(legacy)) {
                player.getRecipeBook().add(canonical);
                player.getRecipeBook().remove(legacy);
                changed = true;
            }
        }
        if (changed) {
            player.getRecipeBook().sendInitialRecipeBook(player);
        }
    }

    private static void record(MinecraftServer server, long items, long blocks) {
        if (server != null) {
            LegacyMigrationState.get(server).record(items, blocks);
        }
    }

    private static Identifier legacyId(String path) {
        return Identifier.fromNamespaceAndPath(LEGACY_MOD_ID, path);
    }

    private record PendingChunk(ServerLevel level, LevelChunk chunk) {
    }
}
