package net.willo678.nodami;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber
public class Handler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityHurt(LivingDamageEvent.Pre event) {
        LivingEntity recipient = event.getEntity();
        if (recipient.level().isClientSide) {
            return;
        }
        if (excludeBasedOnType(recipient)) return;


        // Exclude if damage source is on 'damageSrcWhitelist'
        DamageSource source = event.getEntity().getLastDamageSource();
        if (source != null && Config.EXCLUSIONS.excludedEnvironmentalSources.contains(source.getMsgId())) {
            return;
        }

        // Exclude if damage recipient is on 'damageReceiveExcludedEntities'
        ResourceLocation recipientLoc = EntityType.getKey(recipient.getType());
        if (Config.EXCLUSIONS.iFrameRequiredEntities.contains(recipientLoc.toString())) {
            return;
        }

        // Exclude if damage source is on 'attackExcludedEntities'
        Entity trueSource = source != null ? source.getDirectEntity() : null;
        ResourceLocation trueSourceLoc = trueSource != null ? EntityType.getKey(trueSource.getType()) : null;
        if (trueSource != null) {
            if (Config.EXCLUSIONS.attackExcludedEntities.contains(trueSourceLoc.toString())) {
                return;
            }
        }

        recipient.invulnerableTime = Config.CORE.iFrameInterval;
    }

    private static boolean excludeBasedOnType(LivingEntity entity) {
        if (entity instanceof Player) {
            return Config.CORE.excludePlayers;
        } else {
            return Config.CORE.excludeAllMobs;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (!event.isCanceled()) {
            Player player = event.getEntity();

            if (player.level().isClientSide() || player instanceof FakePlayer) {
                return;
            }

            float str = player.getAttackStrengthScale(0);
            if (str <= Config.THRESHOLDS.attackCancelThreshold) {
                event.setCanceled(true);
                return;
            }

            if (str <= Config.THRESHOLDS.knockbackCancelThreshold) {
                Entity target = event.getTarget();
                // Don't worry, it's only magic
                if (target instanceof LivingEntity livingEntity) {
                    livingEntity.swinging = true;
                }
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (!event.isCanceled()) {
            LivingEntity entity = event.getEntity();
            if (entity.swinging) {
                event.setCanceled(true);
                entity.swinging = false;
            }
        }
    }
}
