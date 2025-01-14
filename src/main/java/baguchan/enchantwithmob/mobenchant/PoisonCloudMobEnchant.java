package baguchan.enchantwithmob.mobenchant;

import baguchan.enchantwithmob.EnchantConfig;
import baguchan.enchantwithmob.EnchantWithMob;
import baguchan.enchantwithmob.api.IEnchantCap;
import baguchan.enchantwithmob.registry.MobEnchants;
import baguchan.enchantwithmob.utils.MobEnchantUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber(modid = EnchantWithMob.MODID)
public class PoisonCloudMobEnchant extends MobEnchant {
	public PoisonCloudMobEnchant(Properties properties) {
		super(properties);
	}

	public int getMinEnchantability(int enchantmentLevel) {
		return 20;
	}

	public int getMaxEnchantability(int enchantmentLevel) {
		return 50;
	}

	@Override
	public void tick(LivingEntity entity, int level) {
		super.tick(entity, level);
	}

	@Override
	public boolean isCompatibleMob(LivingEntity livingEntity) {
        return EnchantConfig.COMMON.WHITELIST_SHOOT_ENTITY.get().contains(BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.getType()).toString()) && !(livingEntity instanceof Witch) || super.isCompatibleMob(livingEntity);
	}

	@Override
	protected boolean canApplyTogether(MobEnchant ench) {
		return ench != MobEnchants.POISON.get() && super.canApplyTogether(ench);
	}

	@Override
	public boolean isTresureEnchant() {
		return true;
	}

	@SubscribeEvent
	public static void onImpact(ProjectileImpactEvent event) {
		Projectile projectile = event.getProjectile();
        if (!shooterIsLiving(projectile) || !EnchantConfig.COMMON.ALLOW_POISON_CLOUD_PROJECTILE.get().contains(BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType()).toString()))
			return;
		LivingEntity owner = (LivingEntity) projectile.getOwner();
		if (owner instanceof IEnchantCap cap) {
			int i = MobEnchantUtils.getMobEnchantLevelFromHandler(cap.getEnchantCap().getMobEnchants(), MobEnchants.POISON_CLOUD.get());

			if (cap.getEnchantCap().hasEnchant() && MobEnchantUtils.findMobEnchantFromHandler(cap.getEnchantCap().getMobEnchants(), MobEnchants.POISON_CLOUD.get())) {
                //arrow is different
                if (!(projectile instanceof AbstractArrow) || !projectile.onGround()) {
                    AreaEffectCloud areaeffectcloud = new AreaEffectCloud(owner.level(), event.getRayTraceResult().getLocation().x, event.getRayTraceResult().getLocation().y, event.getRayTraceResult().getLocation().z);
                    areaeffectcloud.setRadius(0.6F);
                    areaeffectcloud.setRadiusOnUse(-0.01F);
                    areaeffectcloud.setWaitTime(10);
                    areaeffectcloud.setDuration(80);
                    areaeffectcloud.setOwner(owner);
                    areaeffectcloud.setRadiusPerTick(-0.001F);

                    areaeffectcloud.addEffect(new MobEffectInstance(MobEffects.POISON, 80, i - 1));
                    owner.level().addFreshEntity(areaeffectcloud);
                }
			}
		}
		;
	}

	public static boolean shooterIsLiving(Projectile projectile) {
		return projectile.getOwner() != null && projectile.getOwner() instanceof LivingEntity;
	}
}
