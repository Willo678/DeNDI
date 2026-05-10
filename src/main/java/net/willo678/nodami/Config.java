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
            builder.comment("Core functionality settings").push("core");

            iFrameIntervalTemp = builder.comment(
                            "How many ticks of i-frames does an entity get when damaged, from 0 (default), to 2^31-1 (nothing can take damage)")
                    .defineInRange("iFrameInterval", 0, 0, Integer.MAX_VALUE);
            excludePlayersTemp = builder.comment(
                            "Are players excluded from this mod (if true, players will always get 10 ticks of i-frames on being damaged")
                    .define("excludePlayers", false);
            excludeAllMobsTemp = builder.comment(
                            "Are players excluded from this mod (if true, players will always get 10 ticks of i-frames on being damaged")
                    .define("excludeAllMobs", false);

            builder.pop();
        }
    }

    public static class Thresholds {
        public final ModConfigSpec.DoubleValue attackCancelThresholdTemp, knockbackCancelThresholdTemp;
        public double attackCancelThreshold, knockbackCancelThreshold;

        public Thresholds(ModConfigSpec.Builder builder) {
            builder.comment("Threshold values for certain features").push("threshold");

            attackCancelThresholdTemp = builder.comment(
                            "How weak a player's attack can be before it gets nullified, from 0 (0%, cancels multiple attacks on the same tick) to 1 (100%, players cannot attack), or -0.1 (disables this feature)")
                    .defineInRange("attackCancelThreshold", 0.1, -0.1, 1);
            knockbackCancelThresholdTemp = builder.comment(
                            "How weak a player's attack can be before the knockback gets nullified, from 0 (0%, cancels multiple attacks on the same tick) to 1 (100%, no knockback), or -0.1 (disables this feature)")
                    .defineInRange("knockbackCancelThreshold", 0.75, -0.1, 1);

            builder.pop();
        }
    }

    public static class Exclusions {
        public final ModConfigSpec.ConfigValue<List<? extends String>> attackExcludedEntitiesTemp, dmgReceiveExcludedEntitiesTemp,
                damageSrcWhitelistTemp;

        public HashSet<String> attackExcludedEntities, dmgReceiveExcludedEntities, damageSrcWhitelist;

        public Exclusions(ModConfigSpec.Builder builder) {
            List<String> AtkExcEnt = Arrays.asList("minecraft:slime", "minecraft:magma_cube", "tconstruct:earth_slime", "tconstruct:sky_slime", "tconstruct:ender_slime", "tconstruct:terracube", "twilightforest:maze_slime");
            List<String> DmgRecExcEnt = List.of();
            List<String> DmgSrcWhtLst = Arrays.asList("inFire", "lava", "sweetBerryBush", "cactus", "lightningBolt", "inWall", "hotFloor", "outOfWorld");
            builder.comment("Exclusion lists for certain features").push("exclusions");

            Predicate<Object> dummyPredicate = t -> true;

            attackExcludedEntitiesTemp = builder
                    .comment("List of entities that need to give i-frames on attacking")
                    .define("attackExcludedEntities", AtkExcEnt, dummyPredicate);

            dmgReceiveExcludedEntitiesTemp = builder
                    .comment("List of entities that need to receive i-frames on receiving attacks or relies on i-frames")
                    .define("dmgReceiveExcludedEntities", DmgRecExcEnt, dummyPredicate);

            damageSrcWhitelistTemp = builder
                    .comment("List of damage sources that need to give i-frames on doing damage (ex: lava)")
                    .define("damageSrcWhitelist", DmgSrcWhtLst, dummyPredicate);

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
        EXCLUSIONS.dmgReceiveExcludedEntities = new HashSet<>(EXCLUSIONS.dmgReceiveExcludedEntitiesTemp.get());
        EXCLUSIONS.damageSrcWhitelist = new HashSet<>(EXCLUSIONS.damageSrcWhitelistTemp.get());
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
