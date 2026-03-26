package net.inklinggamer.shopsandtools.client;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public final class CelestiumFroglightIrisCompat {
    private static final String WORLD_RENDERING_SETTINGS_CLASS =
            "net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings";
    private static final String IRIS_CLASS = "net.irisshaders.iris.Iris";
    private static final String PEARLESCENT_FROGLIGHT_ID = "minecraft:pearlescent_froglight";
    private static final String CELESTIUM_BLOCK_ID = "shopsandtools:celestium_block";
    private static final String CELESTIUM_ID = "shopsandtools:celestium";

    private static boolean registrationAttempted;
    private static boolean reflectionUnavailable;
    private static boolean mappingApplied;
    private static Field worldRenderingSettingsInstanceField;
    private static Method getBlockStateIdsMethod;
    private static Method setBlockStateIdsMethod;
    private static Method getItemIdsMethod;
    private static Method setItemIdsMethod;
    private static Method getCurrentPackMethod;
    private static Method getCurrentDimensionMethod;
    private static Method getPipelineManagerMethod;
    private static Method getIdMapMethod;
    private static Method destroyPipelineMethod;
    private static Method preparePipelineMethod;
    private static Field idMapItemIdMapField;
    private static Constructor<?> namespacedIdConstructor;

    private CelestiumFroglightIrisCompat() {
    }

    public static synchronized void register() {
        if (registrationAttempted) {
            return;
        }

        registrationAttempted = true;
        ClientTickEvents.END_CLIENT_TICK.register(CelestiumFroglightIrisCompat::tick);
    }

    private static void tick(MinecraftClient client) {
        if (reflectionUnavailable) {
            return;
        }

        try {
            Object worldRenderingSettings = getWorldRenderingSettings();
            if (worldRenderingSettings == null) {
                return;
            }

            Object2IntMap<BlockState> blockStateIds = asBlockStateIds(getBlockStateIds(worldRenderingSettings));
            Object2IntMap<BlockState> patchedBlockStateIds = createPatchedBlockStateIds(blockStateIds);
            boolean blockMappingUpdated = patchedBlockStateIds != null && patchedBlockStateIds != blockStateIds;
            if (blockMappingUpdated) {
                setBlockStateIds(worldRenderingSettings, patchedBlockStateIds);
            }

            boolean packItemMappingUpdated = patchHeldLightItemIdsInShaderPack(client);

            Object2IntMap<Object> itemIds = asItemIds(getItemIds(worldRenderingSettings));
            Object2IntMap<Object> patchedItemIds = itemIds == null ? null : createPatchedItemIds(
                    itemIds,
                    createNamespacedId(CELESTIUM_BLOCK_ID),
                    createNamespacedId(PEARLESCENT_FROGLIGHT_ID),
                    createNamespacedId(CELESTIUM_ID),
                    createNamespacedId(PEARLESCENT_FROGLIGHT_ID)
            );
            boolean heldMappingUpdated = patchedItemIds != null && patchedItemIds != itemIds;
            if (heldMappingUpdated) {
                setItemIds(worldRenderingSettings, patchedItemIds);
            }

            if ((blockMappingUpdated || packItemMappingUpdated) && client.worldRenderer != null) {
                client.worldRenderer.reload();
            }

            if (!mappingApplied && (blockMappingUpdated || heldMappingUpdated || packItemMappingUpdated)) {
                ShopsAndTools.LOGGER.info("Aliased Celestium block and item to Complimentary/Iris pearlescent froglight material mappings");
            }
            mappingApplied = mappingApplied || blockMappingUpdated || heldMappingUpdated || packItemMappingUpdated;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            reflectionUnavailable = true;
            ShopsAndTools.LOGGER.warn("Failed to apply Celestium Iris material aliasing; shader lighting will stay on the default color", exception);
        }
    }

    static Object2IntMap<BlockState> createPatchedBlockStateIds(Object2IntMap<BlockState> blockStateIds) {
        if (blockStateIds == null) {
            return null;
        }

        return createPatchedAliasIds(
                blockStateIds,
                ModBlocks.CELESTIUM_BLOCK.getDefaultState(),
                Blocks.PEARLESCENT_FROGLIGHT.getDefaultState()
        );
    }

    static <T> Object2IntMap<T> createPatchedItemIds(
            Object2IntMap<T> itemIds,
            T celestiumBlockItemId,
            T pearlescentFroglightItemId,
            T celestiumItemId,
            T soulTorchItemId
    ) {
        if (itemIds == null) {
            return null;
        }

        Object2IntMap<T> patchedItemIds = createPatchedAliasIds(itemIds, celestiumBlockItemId, pearlescentFroglightItemId);
        return createPatchedAliasIds(patchedItemIds, celestiumItemId, soulTorchItemId);
    }

    static <T> Object2IntMap<T> createPatchedAliasIds(Object2IntMap<T> ids, T targetId, T sourceId) {
        if (ids == null) {
            return null;
        }

        int sourceShaderId = ids.getInt(sourceId);
        if (sourceShaderId < 0 || ids.getOrDefault(targetId, -1) == sourceShaderId) {
            return ids;
        }

        Object2IntOpenHashMap<T> patchedIds = copyIds(ids);
        patchedIds.put(targetId, sourceShaderId);
        return patchedIds;
    }

    private static <T> Object2IntOpenHashMap<T> copyIds(Object2IntMap<T> source) {
        Object2IntOpenHashMap<T> copy = new Object2IntOpenHashMap<>(source);
        copy.defaultReturnValue(source.defaultReturnValue());
        return copy;
    }

    private static boolean patchHeldLightItemIdsInShaderPack(MinecraftClient client) throws ReflectiveOperationException {
        Object shaderPack = getCurrentShaderPack();
        if (shaderPack == null) {
            return false;
        }

        Object idMap = getIdMapMethod.invoke(shaderPack);
        if (idMap == null) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Object2IntMap<Object> currentItemIds = (Object2IntMap<Object>) idMapItemIdMapField.get(idMap);
        if (currentItemIds == null) {
            return false;
        }

        Object2IntMap<Object> patchedItemIds = createPatchedItemIds(
                currentItemIds,
                createNamespacedId(CELESTIUM_BLOCK_ID),
                createNamespacedId(PEARLESCENT_FROGLIGHT_ID),
                createNamespacedId(CELESTIUM_ID),
                createNamespacedId(PEARLESCENT_FROGLIGHT_ID)
        );
        if (patchedItemIds == currentItemIds) {
            return false;
        }

        idMapItemIdMapField.set(idMap, patchedItemIds);

        Object worldRenderingSettings = getWorldRenderingSettings();
        if (worldRenderingSettings != null) {
            setItemIds(worldRenderingSettings, patchedItemIds);
        }

        if (client.world != null) {
            Object pipelineManager = getPipelineManagerMethod.invoke(null);
            destroyPipelineMethod.invoke(pipelineManager);
            Object currentDimension = getCurrentDimensionMethod.invoke(null);
            preparePipelineMethod.invoke(pipelineManager, currentDimension);
        }

        return true;
    }

    private static Object getCurrentShaderPack() throws ReflectiveOperationException {
        resolveReflectionMembers();
        @SuppressWarnings("unchecked")
        Optional<Object> currentPack = (Optional<Object>) getCurrentPackMethod.invoke(null);
        return currentPack.orElse(null);
    }

    private static Object getWorldRenderingSettings() throws ReflectiveOperationException {
        resolveReflectionMembers();
        return worldRenderingSettingsInstanceField.get(null);
    }

    private static Object getBlockStateIds(Object worldRenderingSettings) throws ReflectiveOperationException {
        resolveReflectionMembers();
        return getBlockStateIdsMethod.invoke(worldRenderingSettings);
    }

    private static void setBlockStateIds(Object worldRenderingSettings, Object2IntMap<BlockState> blockStateIds)
            throws ReflectiveOperationException {
        resolveReflectionMembers();
        setBlockStateIdsMethod.invoke(worldRenderingSettings, blockStateIds);
    }

    private static Object getItemIds(Object worldRenderingSettings) throws ReflectiveOperationException {
        resolveReflectionMembers();
        return getItemIdsMethod.invoke(worldRenderingSettings);
    }

    private static void setItemIds(Object worldRenderingSettings, Object2IntMap<Object> itemIds)
            throws ReflectiveOperationException {
        resolveReflectionMembers();
        setItemIdsMethod.invoke(worldRenderingSettings, itemIds);
    }

    @SuppressWarnings("unchecked")
    private static Object2IntMap<BlockState> asBlockStateIds(Object blockStateIds) {
        if (!(blockStateIds instanceof Object2IntMap<?> map)) {
            return null;
        }

        return (Object2IntMap<BlockState>) map;
    }

    @SuppressWarnings("unchecked")
    private static Object2IntMap<Object> asItemIds(Object itemIds) {
        if (!(itemIds instanceof Object2IntMap<?> map)) {
            return null;
        }

        return (Object2IntMap<Object>) map;
    }

    private static Object createNamespacedId(String combinedId) throws ReflectiveOperationException {
        resolveReflectionMembers();
        return namespacedIdConstructor.newInstance(combinedId);
    }

    private static void resolveReflectionMembers() throws ReflectiveOperationException {
        if (worldRenderingSettingsInstanceField != null
                && getBlockStateIdsMethod != null
                && setBlockStateIdsMethod != null
                && getItemIdsMethod != null
                && setItemIdsMethod != null
                && getCurrentPackMethod != null
                && getCurrentDimensionMethod != null
                && getPipelineManagerMethod != null
                && getIdMapMethod != null
                && destroyPipelineMethod != null
                && preparePipelineMethod != null
                && idMapItemIdMapField != null
                && namespacedIdConstructor != null) {
            return;
        }

        Class<?> worldRenderingSettingsClass = Class.forName(WORLD_RENDERING_SETTINGS_CLASS);
        worldRenderingSettingsInstanceField = worldRenderingSettingsClass.getField("INSTANCE");
        getBlockStateIdsMethod = worldRenderingSettingsClass.getMethod("getBlockStateIds");
        setBlockStateIdsMethod = worldRenderingSettingsClass.getMethod("setBlockStateIds", Object2IntMap.class);
        getItemIdsMethod = worldRenderingSettingsClass.getMethod("getItemIds");
        setItemIdsMethod = worldRenderingSettingsClass.getMethod("setItemIds", Object2IntFunction.class);

        Class<?> irisClass = Class.forName(IRIS_CLASS);
        getCurrentPackMethod = irisClass.getMethod("getCurrentPack");
        getCurrentDimensionMethod = irisClass.getMethod("getCurrentDimension");
        getPipelineManagerMethod = irisClass.getMethod("getPipelineManager");

        Class<?> shaderPackClass = Class.forName("net.irisshaders.iris.shaderpack.ShaderPack");
        getIdMapMethod = shaderPackClass.getMethod("getIdMap");

        Class<?> pipelineManagerClass = Class.forName("net.irisshaders.iris.pipeline.PipelineManager");
        destroyPipelineMethod = pipelineManagerClass.getMethod("destroyPipeline");
        Class<?> namespacedIdClass = Class.forName("net.irisshaders.iris.shaderpack.materialmap.NamespacedId");
        preparePipelineMethod = pipelineManagerClass.getMethod("preparePipeline", namespacedIdClass);

        Class<?> idMapClass = Class.forName("net.irisshaders.iris.shaderpack.IdMap");
        idMapItemIdMapField = idMapClass.getDeclaredField("itemIdMap");
        idMapItemIdMapField.setAccessible(true);

        namespacedIdConstructor = namespacedIdClass.getConstructor(String.class);
    }
}
