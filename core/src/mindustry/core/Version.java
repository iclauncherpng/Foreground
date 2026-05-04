package mindustry.core;

import arc.*;
import arc.Files.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;

public class Version{
    /** Build type. 'official' for official releases; 'custom' or 'bleeding edge' are also used. */
    public static String type = "unknown";
    /** Build modifier, e.g. 'alpha' or 'release' */
    public static String modifier = "unknown";
    /** Git commit hash (short) */
    public static String commitHash = "unknown";
    /** Date that this version was built. */
    public static String buildDate = "unknown";
    /** Number specifying the major version, e.g. '4' */
    public static int number;
    /** Build number, e.g. '43'. set to '-1' for custom builds. */
    public static String build;
    /** Revision number. Used for hotfixes. Does not affect server compatibility. */
    public static int revision = 0;
    /** Build sign by 1057 */
    public static String sign = "unknown";
    /** Whether the Steam version of the game is requested. This is different from Vars.steam (Steam initialization can fail) */
    public static boolean isSteam = false;
    /** Whether version loading is enabled. */
    public static boolean enabled = true;

    public static void init(){
        if(!enabled) return;

        Fi file = OS.isAndroid || OS.isIos ? Core.files.internal("version.properties") : new Fi("version.properties", FileType.internal);

        ObjectMap<String, String> map = new ObjectMap<>();
        PropertiesUtils.load(map, file.reader());

        type = map.get("type");
        number = Integer.parseInt(map.get("number", "4"));
        modifier = map.get("modifier");
        commitHash = map.get("commitHash", "unknown");
        buildDate = map.get("buildDate", "unknown");
        sign = map.get("sign", "unknown");
        isSteam = modifier.contains("steam");
        build = map.get("build", "0");
        revision = Integer.parseInt(map.get("revision", "0"));
    }

    /** @return whether the current game version is greater than the specified version string, e.g. "120.1"*/
    public static boolean isAtLeast(String str){
        if(str == null || str.isEmpty() || str.equals("0")) return true;
        if(build.equals("-1")) return true;
        if(build.equals(str)) return true;

        try{
            String[] current = build.split("\\.");
            String[] target = str.split("\\.");

            int length = Math.max(current.length, target.length);
            for(int i = 0; i < length; i++){
                int c = i < current.length ? Strings.parseInt(current[i], 0) : 0;
                int t = i < target.length ? Strings.parseInt(target[i], 0) : 0;

                if(c > t) return true;
                if(c < t) return false;
            }
        }catch(Exception e){
            return build.equals(str);
        }

        return true;
    }

    public static String buildString(){
        return build;
    }

    public static int buildNumber(){
        return number;
    }

    public static String signString(){
        return sign == null || sign.equals("unknown") || sign.isEmpty() ? "" : " [" + sign + "]";
    }

    /** get menu version without colors */
    public static String combined() {
        if (build.equals("-1")) return "Custom Build";
        String modSuffix = switch (modifier) {
            case "devtest" -> "-devtest-" + revision;
            case "alpha" -> "a";
            case "beta" -> "b";
            case "snapshot" -> "-snapshot-" + revision;
            case "release-candidate" -> "rc";
            case "release" -> "";
            default -> " " + Strings.capitalize(modifier);
        };
        String result = build + modSuffix;
        if (!type.equals("official")) {
            String typePrefix = type.equals("custom") ? "[#fc8140aa]Custom Build[]" : Strings.capitalize(type);
            result = typePrefix + " " + result;
        }
        String hash = commitHash.equals("unknown") ? "" : " (" + commitHash + ")";

        return result + hash + signString();
    }
}
