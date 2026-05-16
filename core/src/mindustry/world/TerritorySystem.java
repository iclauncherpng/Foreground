package mindustry.world;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.struct.*;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.content.*;
import mindustry.world.blocks.diplomacy.TradeConveyor;

import static mindustry.Vars.*;

public class TerritorySystem {
    public static ObjectFloatMap<Block> territoryBlocks = new ObjectFloatMap<>();
    public static boolean initialized = false;
    private static Bits drawn = new Bits();
    public static TerritoryRenderer renderer = new TerritoryRenderer();
    public static final int CHUNK_SIZE = 16;
    public static byte[][] territoryChunks;
    public static int chunksX, chunksY;

    public static void load() {
        Core.settings.defaults("territoryopacity", 15);
        Events.on(WorldLoadEvent.class, event -> {
            chunksX = Mathf.ceil((float) world.width() / CHUNK_SIZE);
            chunksY = Mathf.ceil((float) world.height() / CHUNK_SIZE);
            territoryChunks = new byte[chunksX][chunksY];

            if(state.rules.diplomacy){
                state.rules.polygonCoreProtection = false;
                state.rules.enemyCoreBuildRadius = 0f;
                state.rules.placeRangeCheck = false;
            }
            invalidateTerritory();
        });

        Events.on(ContentInitEvent.class, event -> {
            territoryBlocks.clear();
            territoryBlocks.put(Blocks.coreShard, 320f);
            territoryBlocks.put(Blocks.coreFoundation, 480f);
            territoryBlocks.put(Blocks.coreNucleus, 640f);
            territoryBlocks.put(Blocks.coreCitadel, 640f);
            territoryBlocks.put(Blocks.coreBastion, 640f);
            territoryBlocks.put(Blocks.coreAcropolis, 640f);
            territoryBlocks.put(Blocks.dipTowerTear1, 96f);
            territoryBlocks.put(Blocks.dipTowerTear2, 140f);
            territoryBlocks.put(Blocks.dipTowerTear3, 240f);
            initialized = true;
        });

        Events.on(BlockBuildEndEvent.class, e -> {
            if(e.tile.build != null && territoryBlocks.containsKey(e.tile.block())){
                invalidateTerritory();
            }
        });

        Events.on(TileChangeEvent.class, e -> {
            invalidateTerritory();
        });

        Events.run(Trigger.draw, () -> {
            if (state.isMenu() || !state.rules.diplomacy || !initialized || !Core.settings.getBool("territoryrender", !mobile)) return;

            Draw.z(Layer.territory);
            if(Vars.devMode) drawDebugChunks();

            if (renderer.isDirty()) {
                renderer.clearZones();

                if (drawn.length() != world.width() * world.height()) {
                    drawn = new Bits(world.width() * world.height());
                }
                drawn.clear();

                float alpha = Core.settings.getInt("territoryopacity", 15) / 100f;

                for (Teams.TeamData data : state.teams.present) {
                    if (data.team == Team.derelict || data.buildings.isEmpty()) continue;

                    Color col = Tmp.c1.set(data.team.color).a(alpha);

                    data.buildings.each(b -> territoryBlocks.containsKey(b.block), b -> {
                        float radius = territoryBlocks.get(b.block, 0f);
                        float side = radius * 0.7071f;
                        float bx = b.x - (b.block.size % 2 == 0 ? 4f : 0f);
                        float by = b.y - (b.block.size % 2 == 0 ? 4f : 0f);

                        int tileSide = (int)(side / 8f);
                        int centerX = b.tileX();
                        int centerY = b.tileY();

                        for(int x = -tileSide; x <= tileSide; x++){
                            for(int y = -tileSide; y <= tileSide; y++){
                                int tx = centerX + x;
                                int ty = centerY + y;

                                if(tx < 0 || ty < 0 || tx >= world.width() || ty >= world.height()) continue;

                                int index = tx + ty * world.width();
                                if(drawn.get(index)) continue;

                                float worldX = tx * 8f + 4f;
                                float worldY = ty * 8f + 4f;

                                if(Math.abs(worldX - bx) <= side && Math.abs(worldY - by) <= side){
                                    renderer.addZone(worldX, worldY, 8f, col);
                                    drawn.set(index);
                                }
                            }
                        }
                    });
                }
                renderer.setNotDirty();
            }

            renderer.render();
        });
    }
    private static void invalidateTerritory() {
        renderer.setDirty();
        updateChunks();
    }
    private static void updateChunks() {
        if (territoryChunks == null || !initialized) return;

        for (int x = 0; x < chunksX; x++) {
            for (int y = 0; y < chunksY; y++) {
                territoryChunks[x][y] = 0;
            }
        }

        for (Teams.TeamData data : state.teams.present) {
            if (data.team == Team.derelict || data.buildings.isEmpty()) continue;

            byte teamId = (byte) data.team.id;

            data.buildings.each(b -> territoryBlocks.containsKey(b.block), b -> {
                float radius = territoryBlocks.get(b.block, 0f);
                float side = radius * 0.7071f;
                int tileSide = (int)(side / 8f);

                int centerX = b.tileX();
                int centerY = b.tileY();

                int minChunkX = Math.max(0, (centerX - tileSide) / CHUNK_SIZE);
                int maxChunkX = Math.min(chunksX - 1, (centerX + tileSide) / CHUNK_SIZE);
                int minChunkY = Math.max(0, (centerY - tileSide) / CHUNK_SIZE);
                int maxChunkY = Math.min(chunksY - 1, (centerY + tileSide) / CHUNK_SIZE);

                for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                    for (int cy = minChunkY; cy <= maxChunkY; cy++) {
                        territoryChunks[cx][cy] = teamId;
                    }
                }
            });
        }
    }

    public static boolean isEnemyTerritory(int tileX, int tileY, Team team) {
        if (!state.rules.diplomacy || territoryChunks == null) return false;
        if (tileX < 0 || tileY < 0 || tileX >= world.width() || tileY >= world.height()) return false;

        int cx = tileX / CHUNK_SIZE;
        int cy = tileY / CHUNK_SIZE;

        if (cx >= chunksX || cy >= chunksY) return false;

        byte teamId = territoryChunks[cx][cy];
        return teamId != 0 && teamId != team.id && teamId != Team.derelict.id;
    }

    public static void drawDebugChunks() {
        if (state.isMenu() || territoryChunks == null || !initialized) return;

        Draw.z(Layer.max);
        float chunkSizeWorld = CHUNK_SIZE * 8f;

        for (int cx = 0; cx < chunksX; cx++) {
            for (int cy = 0; cy < chunksY; cy++) {
                byte teamId = territoryChunks[cx][cy];
                float worldX = cx * chunkSizeWorld + chunkSizeWorld / 2f;
                float worldY = cy * chunkSizeWorld + chunkSizeWorld / 2f;

                Draw.color(Color.white, 0.3f);
                Lines.stroke(1f);
                Lines.rect(worldX - chunkSizeWorld / 2f, worldY - chunkSizeWorld / 2f, chunkSizeWorld, chunkSizeWorld);

                if (teamId != 0) {
                    Draw.color(Team.get(teamId).color, 0.7f);
                    Fill.crect(worldX - 6f, worldY - 6f, 12f, 12f);
                }
            }
        }
        Draw.reset();
    }

}
