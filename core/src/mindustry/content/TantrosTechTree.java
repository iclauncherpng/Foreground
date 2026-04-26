package mindustry.content;

import arc.struct.*;
import mindustry.game.Objectives.*;
import mindustry.type.*;
import static mindustry.content.Blocks.*;
import static mindustry.content.Items.*;
import static mindustry.content.TantrosBlocks.*;
import static mindustry.content.TechTree.*;
import static mindustry.content.SectorPresets.*;

public class TantrosTechTree {

    public static void load() {

        Planets.tantros.techTree = nodeRoot("tantros", evacuationCapsule, () -> {
            node(underWaterDrill, () -> {
                node(underWaterDrillT3, () -> {

                });
            });
            node(underWaterDuct, () -> {
                node(underwaterRouter, () -> {
                    node(underwaterJunction, () -> {
                        node(underwaterBridge, () -> {
                            node(underWaterDuctv2, () -> {

                            });
                        });
                    });
                });
            });
            node(siliconSmelter, () -> {
                node(graphitePress);
            });
            node(landing, () -> {
                /*node(temple, () -> {
                    node(powerWire, () -> {

                    });
                    node(steamTurbine, () -> {

                    });
                });*/
            });
            node(boiling, () -> {
                node(harpoon, () -> {

                });
            });
            node(Blocks.copperWall, () -> {
                node(Blocks.copperWallLarge, () -> {
                    node(Blocks.titaniumWall, () -> {
                        node(Blocks.titaniumWallLarge, () -> {

                        });
                    });
                });
            });





        });
    }
}
