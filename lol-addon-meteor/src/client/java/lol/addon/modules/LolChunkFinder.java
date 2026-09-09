package lol.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;

/**
 * Clean-room implementation.
 *
 * This is intentionally NOT a copy of the Bon Client class.
 * It provides the same high-level idea: scan nearby loaded chunks,
 * keep a set of chunks that match a user-defined heuristic, and
 * expose rendering data for later extension.
 */
public class LolChunkFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> radius = sgGeneral.add(
        new IntSetting.Builder()
            .name("scan-radius")
            .description("Chunk radius around the player.")
            .defaultValue(5)
            .min(1)
            .max(32)
            .sliderMax(32)
            .build()
    );

    private final Setting<Integer> minMatches = sgGeneral.add(
        new IntSetting.Builder()
            .name("min-matches")
            .description("Minimum number of suspicious block matches required.")
            .defaultValue(3)
            .min(1)
            .max(64)
            .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(
        new ColorSetting.Builder()
            .name("chunk-color")
            .defaultValue(new SettingColor(255, 70, 70, 90))
            .build()
    );

    private final Set<ChunkPos> matches = new HashSet<>();

    public LolChunkFinder() {
        super(LolAddon.CATEGORY, "LOL Chunk Finder",
            "Finds nearby chunks using a clean-room suspicious-structure heuristic.");
    }

    @Override
    public void onActivate() {
        matches.clear();
    }

    @Override
    public void onDeactivate() {
        matches.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;

        ChunkPos center = mc.player.getChunkPos();
        int r = radius.get();

        matches.removeIf(pos ->
            Math.abs(pos.x - center.x) > r || Math.abs(pos.z - center.z) > r
        );

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                if (matches.size() >= 1024) return;

                if (matches(pos)) matches.add(pos);
            }
        }
    }

    private boolean matches(ChunkPos pos) {
        var chunk = mc.world.getChunkManager().getChunk(pos.x, pos.z, false);
        if (chunk == null) return false;

        // Clean-room placeholder heuristic.
        // Extend this method with the exact gameplay rule you want.
        int count = 0;

        int minY = mc.world.getBottomY();
        int maxY = Math.min(mc.world.getTopYInclusive(), minY + 48);

        for (int y = minY; y <= maxY; y += 2) {
            for (int x = 0; x < 16; x += 2) {
                for (int z = 0; z < 16; z += 2) {
                    var state = chunk.getBlockState(
                        new net.minecraft.util.math.BlockPos(
                            (pos.x << 4) + x, y, (pos.z << 4) + z
                        )
                    );

                    if (!state.isAir() && state.getBlock().getDefaultState() == state) {
                        count++;
                        if (count >= minMatches.get()) return true;
                    }
                }
            }
        }

        return false;
    }

    public Set<ChunkPos> getMatches() {
        return Set.copyOf(matches);
    }

    public SettingColor getColor() {
        return color.get();
    }
}
