package net.willo678.nodami;

//import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Handler {

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onEntityHurt(LivingHurtEvent event) {
		if (!event.isCanceled()) {
			LivingEntity entity = event.getEntity();
			if (entity.getLevel().isClientSide()) {
				return;
			}

			DamageSource source = event.getSource();
			Entity trueSource = source.getDirectEntity();
			ResourceLocation trueSourceLoc = trueSource != null ? EntityType.getKey(trueSource.getType()) : null;

			if (Config.CORE.excludePlayers && entity instanceof Player) {
				return;
			}

			if (Config.CORE.excludeAllMobs && !(entity instanceof Player)) {
				return;
			}

			ResourceLocation loc = EntityType.getKey(entity.getType());
			if (Config.EXCLUSIONS.dmgReceiveExcludedEntities.contains(loc.toString())) {
				return;
			}

			if (Config.EXCLUSIONS.damageSrcWhitelist.contains(source.getMsgId())) {
				return;
			}

			if (trueSource != null) {
				if (Config.EXCLUSIONS.attackExcludedEntities.contains(trueSourceLoc.toString())) {
					return;
				}

			}
			entity.invulnerableTime = Config.CORE.iFrameInterval;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPlayerAttack(AttackEntityEvent event) {
		if (!event.isCanceled()) {
			Player player = event.getEntity();

			if (player.getLevel().isClientSide() || player instanceof FakePlayer) {
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
