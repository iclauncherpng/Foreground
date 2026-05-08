package mindustry.ui.dialogs;

import arc.graphics.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class ApiDialog extends BaseDialog{

    public ApiDialog(){
        super("API Information");

        cont.margin(30f);
        cont.table(t -> {
            t.background(Tex.button);

            t.add("[accent]ARC:[] [lightgray]v2026.1.0.4-alpha-0[]").left().pad(5f).row();
        }).pad(20f).row();

        addCloseButton();
    }
}