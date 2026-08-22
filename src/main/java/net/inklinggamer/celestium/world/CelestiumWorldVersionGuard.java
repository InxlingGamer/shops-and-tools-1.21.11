package net.inklinggamer.celestium.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CelestiumWorldVersionGuard {
    private CelestiumWorldVersionGuard() {
    }

    public static void rejectNewerWorld(Path worldDirectory, int supportedDataVersion) throws IOException {
        Path levelDat = worldDirectory.resolve("level.dat");
        if (!Files.isRegularFile(levelDat)) {
            return;
        }

        NbtCompound root = NbtIo.readCompressed(levelDat, NbtSizeTracker.of(16L * 1024L * 1024L));
        NbtCompound data = root.getCompound("Data");
        if (!data.contains("DataVersion")) {
            return;
        }

        int worldDataVersion = data.getInt("DataVersion");
        if (worldDataVersion > supportedDataVersion) {
            throw new IOException(
                    "Celestium 1.21.1 refused to open world '" + worldDirectory.getFileName()
                            + "': its DataVersion " + worldDataVersion
                            + " is newer than Minecraft 1.21.1 DataVersion " + supportedDataVersion
                            + ". Use the Minecraft 26.2 Celestium release for that world; downgrading is unsupported."
            );
        }
    }
}
