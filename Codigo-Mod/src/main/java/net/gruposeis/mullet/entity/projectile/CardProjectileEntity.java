package net.gruposeis.mullet.entity.projectile;

import net.gruposeis.mullet.entity.ModEntities;
import net.gruposeis.mullet.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

public class CardProjectileEntity extends ThrowableItemProjectile {

    public CardProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public CardProjectileEntity(Level level, LivingEntity shooter, boolean isRedCard) {
        super(ModEntities.CARD_PROJECTILE.get(), shooter, level);
        this.setItem(new ItemStack(isRedCard ? ModItems.TARJETA_ROJA.get() : ModItems.TARJETA_AMARILLA.get()));
    }

    public CardProjectileEntity(Level level, double x, double y, double z, boolean isRedCard) {
        super(ModEntities.CARD_PROJECTILE.get(), x, y, z, level);
        this.setItem(new ItemStack(isRedCard ? ModItems.TARJETA_ROJA.get() : ModItems.TARJETA_AMARILLA.get()));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.TARJETA_AMARILLA.get();
    }

    public boolean isRedCard() {
        return this.getItem().is(ModItems.TARJETA_ROJA.get());
    }

    @Override
    protected float getGravity() {
        // Vuela recto sin caer por gravedad, similar a los proyectiles del Wither
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();

        // Estela de partículas durante el vuelo según la tarjeta
        if (this.level().isClientSide()) {
            if (this.isRedCard()) {
                this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.0D, 0.0D);
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.0D, 0.0D);
            } else {
                this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

        // Si vuela durante más de 10 segundos (200 ticks) sin colisionar, se descarta
        if (!this.level().isClientSide() && this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();

            // No dañar al lanzador
            if (target == owner) {
                return;
            }

            DamageSource damageSource = owner instanceof LivingEntity livingOwner
                    ? this.damageSources().mobProjectile(this, livingOwner)
                    : this.damageSources().magic();

            boolean isRed = this.isRedCard();
            // Tarjeta roja: 12 puntos de daño (6 corazones), Tarjeta amarilla: 7 puntos (3.5 corazones)
            float damage = isRed ? 12.0F : 7.0F;

            if (target.hurt(damageSource, damage) && target instanceof LivingEntity livingTarget) {
                if (isRed) {
                    // Tarjeta roja: Expulsión severa -> Veneno del Wither y fuego
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1), owner);
                    livingTarget.setSecondsOnFire(4);
                } else {
                    // Tarjeta amarilla: Advertencia / Falta -> Lentitud
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1), owner);
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            boolean isRed = this.isRedCard();
            // Explosión como el wither: respeta la regla mobGriefing del servidor
            boolean mobGriefing = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode(
                    this,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    isRed ? 1.6F : 1.1F,
                    mobGriefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE
            );

            this.discard();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

