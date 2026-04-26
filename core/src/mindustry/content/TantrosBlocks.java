package mindustry.content;

import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.Core;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.DrawPart.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.campaign.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import mindustry.ui.Bar;
import arc.util.Strings;

import static mindustry.Vars.*;
import static mindustry.type.ItemStack.*;

public class TantrosBlocks {
    public static Block
     underWaterDrill, underWaterDrillT3,
    underWaterDuct, underWaterDuctv2, underwaterJunction, underwaterRouter, underwaterBridge,
    boiling, harpoon,
    evacuationCapsule,
    steamTurbine, powerWire,
    aluminumSmelter
    ;

    public static void load() {

        evacuationCapsule = new CoreBlock("evacuation-capsule"){{
            requirements(Category.effect, with(Items.copper, 790, Items.titanium, 680));
            size = 2;
            isFirstTier = true;
            alwaysUnlocked = true;
            landSpawn = true;
            health = 230;
            itemCapacity = 1400;
            buildCostMultiplier = 3;
            unitCapModifier = 18;
            unitType = UnitTypes.daggerTypeA;
            envEnabled |= Env.terrestrial | Env.underwater;
            envDisabled = Env.none;
            squareSprite = false;
        }};

        /*underWaterDrill = new BurstDrill("underwater-drill"){{
            requirements(Category.production, with(Items.copper, 55, Items.lead, 35));
            drillTime = 470f;
            size = 2;
            hasPower = true;
            tier = 2;
            drillEffect = new MultiEffect(Fx.drillSteam);
            shake = 0.5f;
            itemCapacity = 15;
            alwaysUnlocked = true;
            hasLiquids = false;

            fogRadius = 1;

            //consumePower(35f / 60f);
        }};*/

        underWaterDrill = new utypeDrill("underwater-drill"){{
            requirements(Category.production, with(Items.copper, 18, Items.lead, 7));
            tier = 2;
            drillTime = 400;
            size = 2;
            hasLiquids = false;
            researchCost = with(Items.copper, 120, Items.scrap, 50);

            //consumeLiquid(Liquids.water, 3.5f / 60f).boost();
            addBar("health", (DrillBuild e) -> new Bar("health", Pal.health, () -> e.healthf()));
            addBar("drillspeed", (DrillBuild e) ->
                    new Bar(
                            () -> Core.bundle.format("bar.drillspeed", Strings.fixed(e.lastDrillSpeed * 60 * e.timeScale(), 1)),
                            () -> Pal.ammo,
                            () -> e.warmup
                    )
            );
            addBar("pressure", (DrillBuild e) ->
                    new Bar(
                            () -> Core.bundle.format("bar.pressure", 0),
                            () -> Pal.accent,
                            () -> 0f
                    )
            );

        }};

        underWaterDrillT3 = new BurstDrill("underwater-drillt3"){{
            requirements(Category.production, with(Items.copper, 140, Items.lead, 75));
            researchCost = with(Items.copper, 350, Items.lead, 210);
            drillTime = 340f;
            size = 3;
            hasPower = true;
            tier = 3;
            drillEffect = new MultiEffect(Fx.mineImpact, Fx.drillSteam);
            shake = 1f;
            itemCapacity = 30;
            hasLiquids = false;

            fogRadius = 3;

            //consumePower(35f / 60f);
        }};

        underWaterDuct = new Duct("underwater-duct"){{
            requirements(Category.distribution, with(Items.copper, 1));
            health = 10;
            speed = 500f / 60f; // В Mindustry скорость обычно считается так (4 предмета в сек)
            researchCost = with(Items.copper, 5);
        }};

        underwaterJunction = new Junction("underwater-junction"){{
            requirements(Category.distribution, with(Items.copper, 3));
            speed = 26;
            capacity = 6;
            health = 30;
            buildCostMultiplier = 6f;
        }};

        underWaterDuctv2 = new Duct("underwater-ductv2"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.titanium, 2));
            health = 35;
            speed = 280f / 60f; // В Mindustry скорость обычно считается так (4 предмета в сек)
            researchCost = with(Items.copper, 95, Items.titanium, 50);
        }};

        underwaterRouter = new DuctRouter("underwater-router"){{
            requirements(Category.distribution, with(Items.copper, 15));
            health = 90;
            speed = 4f;
            regionRotated1 = 1;
            solid = false;
            researchCost = with(Items.copper, 30);
        }};

        underwaterBridge = new DuctBridge("underwater-bridge"){{
            requirements(Category.distribution, with(Items.beryllium, 15));
            health = 90;
            speed = 4f;
            buildCostMultiplier = 2f;
            researchCostMultiplier = 0.3f;
            crushFragile = true;
            researchCost = with(Items.copper, 70);
        }};

        powerWire = new PowerWire("power-wire") {{
            requirements(Category.power, with(Items.copper, 1, Items.lead, 1));
            health = 40;
            size = 1;
            destructible = true;

            hasPower = true;
            outputsPower = true;
            consumesPower = false;


            drawDisabled = false;
            researchCost = with(Items.copper, 35, Items.aluminum, 40);

            group = BlockGroup.power;
        }};

        steamTurbine = new ThermalGenerator("steam-turbine"){{
            requirements(Category.power, with(Items.aluminum, 60, Items.copper, 70));
            attribute = Attribute.steam;
            group = BlockGroup.power;
            displayEfficiencyScale = 1f / 9f;
            minEfficiency = 9f - 0.0001f;
            powerProduction = 3f / 9f;
            displayEfficiency = false;
            generateEffect = Fx.turbinegenerate;
            effectChance = 0.04f;
            size = 3;
            ambientSound = Sounds.loopHum;
            ambientSoundVolume = 0.06f;

            drawer = new DrawMulti(new DrawDefault(), new DrawBlurSpin("-rotator", 0.6f * 9f){{
                blurThresh = 0.01f;
            }});

            hasLiquids = false;
            //outputLiquid = new LiquidStack(Liquids.water, 5f / 60f / 9f);
            //liquidCapacity = 20f;
            fogRadius = 3;
            researchCost = with(Items.copper, 35, Items.aluminum, 40);
        }};

        aluminumSmelter = new GenericCrafter("aluminum-smelter"){{
            requirements(Category.crafting, with(Items.copper, 60, Items.lead, 45));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(Items.aluminum, 2);
            craftTime = 120f;
            size = 2;
            hasPower = false;
            hasLiquids = false;
            drawer = new DrawMulti(new DrawDefault(), new DrawFlame(Color.valueOf("ffef99")));
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.07f;

            consumeItems(with(Items.lead, 4, Items.copper, 4));
            //consumePower(0.50f);
        }};
        boiling = new PowerTurret("boiling"){{
            requirements(Category.turret, with(Items.copper, 60));
            range = 155f;

            shoot.firstShotDelay = 40f;

            recoil = 2f;
            reload = 80f;
            shake = 2f;
            shootEffect = Fx.lancerLaserShoot;
            smokeEffect = Fx.none;
            heatColor = Color.red;
            size = 2;
            scaledHealth = 120;
            targetAir = false;
            moveWhileCharging = false;
            accurateDelay = false;
            shootSound = Sounds.shootLancer;
            chargeSound = Sounds.chargeLancer;

            consumePower(5f / 60f);

            researchCost = with(Items.copper, 105);

            shootType = new LaserBulletType(45){{
                colors = new Color[]{Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white};
                chargeEffect = new MultiEffect(Fx.lancerLaserCharge, Fx.lancerLaserChargeBegin);

                buildingDamageMultiplier = 0.25f;
                hitEffect = Fx.hitLancer;
                hitSize = 2;
                lifetime = 16f;
                drawSize = 400f;
                collidesAir = false;
                length = 164f;
                ammoMultiplier = 1f;
                pierceCap = 4;
            }};
        }};


        harpoon = new ItemTurret("harpoon"){{
            requirements(Category.turret, with(Items.copper, 150, Items.lead, 90));
            researchCost = with(Items.copper, 280, Items.lead, 125);
            size = 2;
            health = 800;
            range = 160f;
            reload = 80f;
            recoil = 3f;
            shake = 2f;
            shootSound = Sounds.shoot;

            ammo(
                    Items.titanium, new BasicBulletType(8f, 30f){{
                        width = 6f;
                        height = 20f;
                        shrinkY = 0f;

                        knockback = -4.5f;

                        drag = 0.01f;
                        lifetime = 20f;

                        trailWidth = 1.5f;
                        trailLength = 15;
                        trailColor = Color.lightGray;

                        backColor = Color.lightGray;
                        frontColor = Color.white;

                        hitEffect = Fx.hitLancer;
                        despawnEffect = Fx.none;
                    }}
            );
        }};

    }
}
