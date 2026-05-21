package mindustry.world;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.struct.*;
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
    public static byte[][] territoryMap;

    public static void load() {
        Core.settings.defaults("territoryopacity", 15);
        Events.on(WorldLoadEvent.class, event -> {
            territoryMap = new byte[world.width()][world.height()];
            if(state.rules.diplomacy){
                state.rules.polygonCoreProtection = false;
                state.rules.enemyCoreBuildRadius = 0f;
                state.rules.placeRangeCheck = false;
            }
            renderer.setDirty();
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
                renderer.setDirty();
            }
        });

        Events.on(TileChangeEvent.class, e -> {
            renderer.setDirty();
        });

        Events.run(Trigger.draw, () -> {
            if (state.isMenu() || !state.rules.diplomacy || !initialized || !Core.settings.getBool("territoryrender", !mobile)) return;

            Draw.z(Layer.territory);

            if (renderer.isDirty()) {
                renderer.clearZones();

                if (drawn.length() != world.width() * world.height()) {
                    drawn = new Bits(world.width() * world.height());
                }
                drawn.clear();
                for(int x = 0; x < world.width(); x++){
                    for(int y = 0; y < world.height(); y++){
                        territoryMap[x][y] = 0;
                    }
                }

                float alpha = Core.settings.getInt("territoryopacity", 15) / 100f;

                for (Teams.TeamData data : state.teams.present) {
                    if (data.team == Team.derelict || data.buildings.isEmpty()) continue;

                    byte teamId = (byte) data.team.id;
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

                                territoryMap[tx][ty] = teamId;

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

    public static boolean isEnemyTerritory(int tileX, int tileY, Team team) {
        if (!state.rules.diplomacy || territoryMap == null) return false;
        if (tileX < 0 || tileY < 0 || tileX >= world.width() || tileY >= world.height()) return false;

        byte teamId = territoryMap[tileX][tileY];
        return teamId != 0 && teamId != team.id && teamId != Team.derelict.id;
    }
}