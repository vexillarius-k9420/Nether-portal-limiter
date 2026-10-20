package vex.npl.config;


// конфиг.
public class ModConfig {
    public static class Center {
        public Integer x = 0;
        public Integer z = 0;
    }
    public static class OverWorld {
        public double guaranteed_teleportation_distance_limit = 1000.0;
        public double guaranteed_destruction_distance_limit = 9000.0;
    }

    public Center Center = new Center();
    public OverWorld OverWorld = new OverWorld();
    public Float ObsidianReplacement = 0.20f;
    public String displayMessage = "The portal is unstable at this distance...";
    public double netherLimitScale = 8.0;

}
