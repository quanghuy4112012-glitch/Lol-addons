package lol.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.Set;

/**
 * Clean-room Spawner Finder.
 *
 * Scans already-loaded chunks around the player for mob spawner blocks and
 * keeps track of their positions so they can be highlighted/way-pointed.
 * This relies only on chunk data the client has already received from the
 * server for normal rendering purposes - it does not request or reveal
 * anything the server hasn't already sent.
 *
 * Original implementation, not decompiled or copied from any third-party
 * client.
 */
public class SpawnerFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> radius = sgGeneral.add(
        new IntSetting.Builder()
            .name("scan-radius")
            .description("Chunk radius around the player to scan.")
            .defaultValue(8)
            .min(1)
            .max(32)
            .sliderMax(32)
            .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(
        new ColorSetting.Builder()
            .name("spawner-color")
            .defaultValue(new SettingColor(0, 255, 140, 100))
            .build()
    );

    private final Set<BlockPos> spawners = new HashSet<>();

    public SpawnerFinder() {
        super(LolAddon.CATEGORY, "Spawner Finder",
            "Highlights mob spawner blocks in nearby loaded chunks.");
    }

    @Override
    public void onActivate() {
        spawners.clear();
    }

    @Override
    public void onDeactivate() {
        spawners.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;

        ChunkPos center = mc.player.getChunkPos();
        int r = radius.get();

        spawners.clear();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                scan(pos);
            }
        }
    }

    private void scan(ChunkPos pos) {
        var chunk = mc.world.getChunkManager().getChunk(pos.x, pos.z, false);
        if (!(chunk instanceof WorldChunk worldChunk)) return;

        // Spawners are registered as block entities, so we only need to
        // check the (comparatively small) block entity map instead of
        // scanning every block in the chunk column.
        for (BlockPos bp : worldChunk.getBlockEntityPositions()) {
            if (worldChunk.getBlockState(bp).getBlock() == Blocks.SPAWNER) {
                spawners.add(bp.toImmutable());
            }
        }
    }

    public Set<BlockPos> getSpawners() {
        return Set.copyOf(spawners);
    }

    public SettingColor getColor() {
        return color.get();
    }
}
