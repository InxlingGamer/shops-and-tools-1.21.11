package net.inklinggamer.celestium.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CelestiumWorldVersionGuardTest {
    private CelestiumWorldVersionGuardTest() {
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("celestium-world-version-test-");
        try {
            Path newWorld = root.resolve("new-world");
            CelestiumWorldVersionGuard.rejectNewerWorld(newWorld, 100);

            Path supportedWorld = root.resolve("supported-world");
            writeLevelDat(supportedWorld, 100);
            CelestiumWorldVersionGuard.rejectNewerWorld(supportedWorld, 100);

            Path newerWorld = root.resolve("newer-world");
            writeLevelDat(newerWorld, 101);
            try {
                CelestiumWorldVersionGuard.rejectNewerWorld(newerWorld, 100);
                throw new AssertionError("A world saved by a newer Minecraft version must be rejected");
            } catch (IOException expected) {
                assertTrue(expected.getMessage().contains("downgrading is unsupported"));
            }
        } finally {
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted((left, right) -> right.compareTo(left)).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    private static void writeLevelDat(Path worldDirectory, int dataVersion) throws IOException {
        Files.createDirectories(worldDirectory);
        NbtCompound data = new NbtCompound();
        data.putInt("DataVersion", dataVersion);
        NbtCompound root = new NbtCompound();
        root.put("Data", data);
        NbtIo.writeCompressed(root, worldDirectory.resolve("level.dat"));
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("The rejection should explain that downgrading is unsupported");
        }
    }
}
