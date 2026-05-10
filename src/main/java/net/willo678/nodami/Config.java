package net.willo678.nodami;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

public class Config {
    private static final ModConfigSpec.Builder BUILDER;
    public static final Core CORE;
    public static final Thresholds THRESHOLDS;
    public static final Exclusions EXCLUSIONS;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER = new ModConfigSpec.Builder();
        CORE = new Core(BUILDER);
        THRESHOLDS = new Thresholds(BUILDER);
        EXCLUSIONS = new Exclusions(BUILDER);
        SPEC = BUILDER.build();
    }

    public static class Core {
        public final ModConfigSpec.IntValue iFrameIntervalTemp;
        public final ModConfigSpec.BooleanValue excludePlayersTemp, excludeAllMobsTemp;
        public int iFrameInterval;
        public boolean excludePlayers, excludeAllMobs;

        Core(ModConfigSpec.Builder builder) {
            iFrameIntervalTemp = builder
                    .comment("""
                            How many ticks of i-frames does an entity get when damaged
                              - Zero means no i-frames
                              - Super duper high values will mean that nothing can take damage
                            """)
                    .defineInRange("iFrameInterval", 0, 0, Integer.MAX_VALUE);

            excludePlayersTemp = builder
                    .comment("""
                            Are players excluded from this mod?
                              - If true, players will always get 10 ticks of i-frames on being damaged
                              - If false, players will get i-frames specified in the iFrameInterval setting
                            """)
                    .define("excludePlayers", false);

            excludeAllMobsTemp = builder
                    .comment("""
                            Are mobs excluded from this mod
                              - If true, mobs will always get 10 ticks of i-frames on being damaged
                              - If false, mobs will get i-frames specified in the iFrameInterval setting
                            """)
                    .define("excludeAllMobs", false);
        }
    }

    public static class Thresholds {
        public final ModConfigSpec.DoubleValue attackCancelThresholdTemp, knockbackCancelThresholdTemp;
        public double attackCancelThreshold, knockbackCancelThreshold;

        public Thresholds(ModConfigSpec.Builder builder) {
            builder.comment("Threshold values for damage and knockback").push("threshold");

            attackCancelThresholdTemp = builder
                    .comment("""
                            Charge threshold a player's attack must possess, else it gets nullified.
                              - Ranges from 0 (0%, cancels multiple attacks on the same tick) to 1 (100%, players cannot attack)
                              - Negative values disable this feature.
                            """)
                    .comment()
                    .defineInRange("attackCancelThreshold", 0.1, -0.1, 1);

            knockbackCancelThresholdTemp = builder
                    .comment("""
                            Charge threshold a player's attack must possess, else the knockback gets nullified.
                              - Ranges from 0 (0%, cancels multiple attacks on the same tick) to 1 (100%, no knockback)
                              - Negative values disable this feature.
                            """)
                    .defineInRange("knockbackCancelThreshold", 0.75, -0.1, 1);

            builder.pop();
        }
    }

    public static class Exclusions {
        public final ModConfigSpec.ConfigValue<List<? extends String>>
                attackExcludedEntitiesTemp, iFrameRequiredEntitiesTemp, excludedEnvironmentalSourcesTemp;

        public HashSet<String> attackExcludedEntities, iFrameRequiredEntities, excludedEnvironmentalSources;

        public Exclusions(ModConfigSpec.Builder builder) {
            List<String> AtkExcEnt = Arrays.asList("minecraft:slime", "minecraft:magma_cube", "tconstruct:earth_slime", "tconstruct:sky_slime", "tconstruct:ender_slime", "tconstruct:terracube", "twilightforest:maze_slime");
            List<String> DmgRecExcEnt = List.of();
            List<String> DmgSrcWhtLst = Arrays.asList("inFire", "lava", "sweetBerryBush", "cactus", "lightningBolt", "inWall", "hotFloor", "outOfWorld");
            builder.comment("Exclusion lists for damage sources and recipients").push("exclusions");

            Predicate<Object> dummyPredicate = t -> true;

            attackExcludedEntitiesTemp = builder
                    .comment("""
                            List of entities that should give i-frames on attacking
                              - e.g. Slimes, both vanilla and modded
                            """)
                    .defineList("attackExcludedEntities", AtkExcEnt, dummyPredicate);

            excludedEnvironmentalSourcesTemp = builder
                    .comment("""
                            List of non-entity damage sources that need to give i-frames on doing damage
                              - e.g. lava, fire, cactus, etc.
                            """)
                    .defineList("excludedEnvironmentalSources", DmgSrcWhtLst, dummyPredicate);

            iFrameRequiredEntitiesTemp = builder
                    .comment("""
                            List of entities that must receive i-frames on receiving attacks
                              - Add an entity to this list if it relies on i-frames
                            """)
                    .defineList("iFrameRequiredEntities", DmgRecExcEnt, dummyPredicate);

            builder.pop();

        }
    }

    public static void cacheValues() {
        CORE.iFrameInterval = CORE.iFrameIntervalTemp.get();
        CORE.excludePlayers = CORE.excludePlayersTemp.get();
        CORE.excludeAllMobs = CORE.excludeAllMobsTemp.get();

        THRESHOLDS.attackCancelThreshold = THRESHOLDS.attackCancelThresholdTemp.get();
        THRESHOLDS.knockbackCancelThreshold = THRESHOLDS.knockbackCancelThresholdTemp.get();

        EXCLUSIONS.attackExcludedEntities = new HashSet<>(EXCLUSIONS.attackExcludedEntitiesTemp.get());
        EXCLUSIONS.iFrameRequiredEntities = new HashSet<>(EXCLUSIONS.iFrameRequiredEntitiesTemp.get());
        EXCLUSIONS.excludedEnvironmentalSources = new HashSet<>(EXCLUSIONS.excludedEnvironmentalSourcesTemp.get());
    }

    @SubscribeEvent
    public void onModConfigEvent(final ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == Config.SPEC) {
            Config.cacheValues();
        }
    }

    @SubscribeEvent
    public void onLoad(final ModConfigEvent.Loading event) {
        ReNDI.LOGGER.info("Config loaded!");
        Config.cacheValues();
    }

    @SubscribeEvent
    public void onReload(final ModConfigEvent.Reloading event) {
        ReNDI.LOGGER.info("Config reloaded!");
        Config.cacheValues();
    }
}
