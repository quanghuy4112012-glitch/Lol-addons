package lol.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;

/**
 * Clean-room Sus Chunk Finder.
 *
 * Heuristic idea: naturally generated terrain is dominated by a small set of
 * "natural" blocks (stone variants, dirt, sand, water, ores, etc). A chunk
 * that contains an unusually high count of player-placed/manufactured blocks
 * (planks, cobblestone outside of naturally-generated structures, glass,
 * doors, chests, redstone components, etc.) below the surface is a decent
 * signal that the chunk has been modified by a player - hence "sus".
 *
 * This is an original heuristic/implementation, not decompiled or copied
 * from any third-party client.
 */
public class SusChunkFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> radius = sgGeneral.add(
        new IntSetting.Builder()
            .name("scan-radius")
            .description("Chunk radius around the player to scan.")
            .defaultValue(6)
            .min(1)
            .max(32)
            .sliderMax(32)
            .build()
    );

    private final Setting<Integer> minSus = sgGeneral.add(
        new IntSetting.Builder()
            .name("min-sus-blocks")
            .description("Minimum count of suspicious blocks required to flag a chunk.")
            .defaultValue(6)
            .min(1)
            .max(256)
            .build()
    );

    private final Setting<Integer> maxY = sgGeneral.add(
        new IntSetting.Builder()
            .name("max-y")
            .description("Only scan below this Y level (surface builds are usually visible anyway).")
            .defaultValue(60)
            .sliderMin(-64)
            .sliderMax(320)
            .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(
        new ColorSetting.Builder()
            .name("chunk-color")
            .defaultValue(new SettingColor(255, 170, 0, 90))
            .build()
    );

    // Blocks that essentially never occur underground from natural world-gen
    // and strongly indicate a player-made structure when found there.
    private static final Set<Block> SUS_BLOCKS = Set.of(
        Blocks.COBBLESTONE,
        Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS,
        Blocks.STONE_BRICKS, Blocks.GLASS,
        Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL,
        Blocks.FURNACE, Blocks.CRAFTING_TABLE,
        Blocks.TORCH, Blocks.WALL_TORCH,
        Blocks.OAK_DOOR, Blocks.IRON_DOOR,
        Blocks.REDSTONE_WIRE, Blocks.REPEATER, Blocks.COMPARATOR,
        Blocks.OBSIDIAN, Blocks.LADDER, Blocks.SCAFFOLDING
    );

    private final Set<ChunkPos> matches = new HashSet<>();

    public SusChunkFinder() {
        super(LolAddon.CATEGORY, "Sus Chunk Finder",
            "Flags nearby chunks that contain an unusual amount of player-placed blocks underground.");
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

        int susCount = 0;
        int minY = mc.world.getBottomY();
        int top = Math.min(maxY.get(), mc.world.getTopYInclusive());

        for (int y = minY; y <= top; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos bp = new BlockPos((pos.x << 4) + x, y, (pos.z << 4) + z);
                    Block block = chunk.getBlockState(bp).getBlock();

                    if (SUS_BLOCKS.contains(block)) {
                        susCount++;
                        if (susCount >= minSus.get()) return true;
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
