package net.gruposeis.mullet.entity.custom;

import net.gruposeis.mullet.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MrMulletEntity extends PathfinderMob {

    private boolean combatMusicStarted = false;

    public MrMulletEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("Mr Mullet"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void registerGoals() {
        // Que no haga nada por ahora: solo mantenerse a flote y mirar alrededor
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.JEFE_GOLPE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.JEFE_MUERTE.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.JEFE_ATAQUE.get();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.playSound(ModSounds.JEFE_ATAQUE.get(), 1.0F, 1.0F);
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean wasHurt = super.hurt(source, amount);
        if (wasHurt && !this.level().isClientSide() && source.getEntity() instanceof Player) {
            if (!this.combatMusicStarted) {
                this.combatMusicStarted = true;
                // Dispara la música de combate del jefe
                this.level().playSound(null, this.blockPosition(), ModSounds.DUNGEON_COMBATE.get(), SoundSource.RECORDS, 1.5F, 1.0F);
            }
        }
        return wasHurt;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // Evitar que despawnee al alejarse
        return false;
    }
}
