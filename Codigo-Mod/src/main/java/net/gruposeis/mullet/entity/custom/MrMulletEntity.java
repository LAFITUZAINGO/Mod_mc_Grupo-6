package net.gruposeis.mullet.entity.custom;

import net.gruposeis.mullet.DungeonProtectionData;
import net.gruposeis.mullet.sound.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
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
import org.jetbrains.annotations.Nullable;

public class MrMulletEntity extends PathfinderMob {

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.literal("§4§lMr Mullet§r"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
    ).setDarkenScreen(false);

    private boolean combatMusicStarted = false;

    public MrMulletEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("Mr Mullet"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        // AI base mientras el usuario define el move set completo
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        if (name != null) {
            this.bossEvent.setName(name);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
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
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (this.level() instanceof ServerLevel serverLevel) {
            DungeonProtectionData data = DungeonProtectionData.get(serverLevel);
            data.setMrMulletDefeated(true);
            for (ServerPlayer player : serverLevel.players()) {
                player.sendSystemMessage(Component.literal("§6§l[Mr Mullet]§r §a¡El jefe ha sido derrotado! La puerta de salida se ha desbloqueado."));
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // Evitar que despawnee al alejarse
        return false;
    }
}
