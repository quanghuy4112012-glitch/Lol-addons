package lol.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;

/**
 * Clean-room Tuff Chunk Finder.
 *
 * It looks for chunks containing a configurable amount of tuff and
 * optional redstone-repeaters. This is an original implementation,
 * not copied bytecode/source from Bon Client.
 */
public class TuffChunkFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> radius = sgGeneral.add(
        new IntSetting.Builder()
            .name("scan-radius")
            .defaultValue(5)
            .min(1)
            .max(32)
            .sliderMax(32)
            .build()
    );

    private final Setting<Integer> minTuff = sgGeneral.add(
        new IntSetting.Builder()
            .name("min-tuff")
            .defaultValue(8)
            .min(1)
            .max(128)
            .build()
    );

    private final Setting<Boolean> requireRepeater = sgGeneral.add(
        new BoolSetting.Builder()
            .name("require-repeater")
            .defaultValue(false)
            .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(
        new ColorSetting.Builder()
            .name("chunk-color")
            .defaultValue(new SettingColor(80, 180, 255, 90))
            .build()
    );

    private final Set<ChunkPos> matches = new HashSet<>();

    public TuffChunkFinder() {
        super(LolAddon.CATEGORY, "Tuff Chunk Finder",
            "Finds nearby chunks containing configurable tuff/repeater signatures.");
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

        matches.clear();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                if (scan(pos)) matches.add(pos);
            }
        }
    }

    private boolean scan(ChunkPos pos) {
        var chunk = mc.world.getChunkManager().getChunk(pos.x, pos.z, false);
        if (chunk == null) return false;

        int tuff = 0;
        boolean repeater = false;

        int minY = mc.world.getBottomY();
        int maxY = mc.world.getTopYInclusive();

        for (int y = minY; y <= maxY; y += 2) {
            for (int x = 0; x < 16; x += 2) {
                for (int z = 0; z < 16; z += 2) {
                    BlockPos bp = new BlockPos((pos.x << 4) + x, y, (pos.z << 4) + z);
                    var block = chunk.getBlockState(bp).getBlock();

                    if (block == Blocks.TUFF) {
                        tuff++;
                        if (tuff >= minTuff.get()) {
                            if (!requireRepeater.get() || repeater) return true;
                        }
                    } else if (block == Blocks.REPEATER) {
                        repeater = true;
                        if (requireRepeater.get() && tuff >= minTuff.get()) return true;
                    }
                }
            }
        }

        return !requireRepeater.get() && tuff >= minTuff.get();
    }

    public Set<ChunkPos> getMatches() {
        return Set.copyOf(matches);
    }

    public SettingColor getColor() {
        return color.get();
    }
}
