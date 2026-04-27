package mindustry.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;

import static mindustry.type.ItemStack.with;

public class TantrosTurrets {
    public static Block piercing;

    public static void load() {
        piercing = new PowerTurret("piercing"){{
            requirements(Category.experiments, with(Items.copper, 50, Items.lead, 25));

            size = 1;
            health = 250;
            range = 160f;

            consumePower(1.2f);

            reload = 50f;
            rotateSpeed = 7f;

            shootSound = Sounds.shoot;
            shootEffect = Fx.shootLiquid;
            smokeEffect = Fx.none;

            shootType = new BasicBulletType(17f, 15){{
                width = 2.5f;
                height = 16f;

                pierce = true;
                pierceCap = 5;
                pierceBuilding = false;

                sprite = "bullet";
                frontColor = Color.valueOf("ffffff");
                backColor = Color.valueOf("ffffff");

                lightRadius = 0f;
                lightOpacity = 0f;
                trailWidth = 1.8f;
                trailLength = 20;
                trailColor = Color.valueOf("ffffff").a(0.6f);
                hitEffect = Fx.hitLiquid;
                despawnEffect = Fx.bubble;

                knockback = 1.5f;
                status = StatusEffects.wet;
                statusDuration = 60f * 4f;
            }};
        }};
    }
}