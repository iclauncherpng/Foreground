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
            
            t.add("[accent]Mod API:[] [lightgray]v26.1[]").left().pad(5f).row();
            t.add("[accent]ARC:[] [lightgray]v1[]").left().pad(5f).row();
        }).pad(20f).row();

        addCloseButton();
    }
}