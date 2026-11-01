package net.gruposeis.mullet.entity.custom;

import net.gruposeis.mullet.DungeonProtectionData;
import net.gruposeis.mullet.entity.projectile.CardProjectileEntity;
import net.gruposeis.mullet.sound.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MrMulletEntity extends PathfinderMob {

    // Modificador de velocidad x5 durante el modo frenesí (+400% de la velocidad base -> 1.0 + 4.0 = 5.0x)
    private static final UUID FRENZY_SPEED_MODIFIER_UUID = UUID.fromString("d8029d28-8d48-4cb5-b427-46369c9b1428");
    private static final AttributeModifier FRENZY_SPEED_MODIFIER = new AttributeModifier(
            FRENZY_SPEED_MODIFIER_UUID, "Mr Mullet Frenzy Speed Boost", 4.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private static final EntityDataAccessor<Boolean> DATA_FRENZIED =
            SynchedEntityData.defineId(MrMulletEntity.class, EntityDataSerializers.BOOLEAN);

    // Barra de jefe
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.literal("§4§lMr Mullet§r"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
    ).setDarkenScreen(false);

    // Estado del combate
    private boolean combatMusicStarted = false;
    private boolean hasSpawnedMinions = false;
    private final java.util.List<Rabbit> activeCamejitos = new java.util.ArrayList<>();

    // Frenesí: cooldown entre 15 y 20 segundos (300-400 ticks), duración 3 a 4 segundos (60-80 ticks)
    private int frenzyCooldown = 200; // Primer frenesí a los 10 segundos de iniciar combate
    private int frenzyDuration = 0;

    // Cooldown de lanzamiento de cartas
    private int cardShootCooldown = 40;

    // Control de golpes consecutivos para aplicar veneno del wither
    private int consecutiveHits = 0;
    private UUID lastHitTargetId = null;
    private long lastHitTick = 0;

    public MrMulletEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("Mr Mullet"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FRENZIED, false);
    }

    public boolean isFrenzied() {
        return this.entityData.get(DATA_FRENZIED);
    }

    public void setFrenzied(boolean frenzied) {
        this.entityData.set(DATA_FRENZIED, frenzied);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                // 4.5 corazones por golpe cuerpo a cuerpo (1 corazón = 2 puntos de daño -> 4.5 corazones = 9.0)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Ataque cuerpo a cuerpo persiguiendo al jugador
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Selección de objetivos hostiles
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        // En una entidad de 5 bloques, la altura de los ojos está a 4.25 bloques (0.85 * 5.0)
        return 4.25F;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Efectos visuales de partículas cuando está en modo frenesí
        if (this.level().isClientSide() && this.isFrenzied()) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(
                        ParticleTypes.ANGRY_VILLAGER,
                        this.getRandomX(0.8D),
                        this.getY() + this.random.nextDouble() * 4.5D,
                        this.getRandomZ(0.8D),
                        0.0D, 0.0D, 0.0D
                );
                this.level().addParticle(
                        ParticleTypes.FLAME,
                        this.getRandomX(0.6D),
                        this.getY() + this.random.nextDouble() * 2.5D,
                        this.getRandomZ(0.6D),
                        (this.random.nextDouble() - 0.5D) * 0.1D,
                        0.05D,
                        (this.random.nextDouble() - 0.5D) * 0.1D
                );
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        // Actualizar barra de jefe
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        // 1. Manejo del modo Frenesí
        if (this.isFrenzied()) {
            this.frenzyDuration--;
            if (this.frenzyDuration <= 0) {
                this.setFrenzied(false);
                if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                    this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(FRENZY_SPEED_MODIFIER_UUID);
                }
            }
        } else {
            // Se activa periódicamente cada 15 a 20 segundos (300 a 400 ticks) mientras esté en combate
            if (this.getTarget() != null || this.combatMusicStarted) {
                this.frenzyCooldown--;
                if (this.frenzyCooldown <= 0) {
                    this.setFrenzied(true);
                    // Duración de frenesí: 3 a 4 segundos (60 a 80 ticks)
                    this.frenzyDuration = 60 + this.random.nextInt(21);
                    this.frenzyCooldown = 300 + this.random.nextInt(100);

                    if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                        if (!this.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(FRENZY_SPEED_MODIFIER)) {
                            this.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(FRENZY_SPEED_MODIFIER);
                        }
                    }

                    this.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 1.2F);
                    this.playSound(ModSounds.JEFE_ATAQUE.get(), 1.5F, 1.3F);

                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 2.5D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        }

        // 2. Lanzamiento de proyectiles estilo Wither (tarjeta amarilla y 1/3 tarjeta roja)
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.cardShootCooldown--;
            if (this.cardShootCooldown <= 0) {
                double distSq = this.distanceToSqr(target);
                if (distSq <= 35.0D * 35.0D && this.getSensing().hasLineOfSight(target)) {
                    shootCard(target);
                    // En frenesí dispara más rápido (cada 1.5 a 2.5s), normalmente cada 3 a 4.5s
                    this.cardShootCooldown = this.isFrenzied()
                            ? (30 + this.random.nextInt(20))
                            : (60 + this.random.nextInt(30));
                } else {
                    this.cardShootCooldown = 20;
                }
            }
        }

        // 3. Spawneo de 5 conejos asesinos ("camejitos") tras bajar de la mitad de vida
        if (!this.hasSpawnedMinions && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            this.hasSpawnedMinions = true;
            spawnCamejitos();
        }

        // En la fase de camejitos: cuando queden solo tres mobs (o menos), dejarlos con efecto Luminiscencia
        if (this.hasSpawnedMinions) {
            this.activeCamejitos.removeIf(r -> !r.isAlive() || r.isRemoved());

            // Recuperar referencias si se recargó el mundo
            if (this.activeCamejitos.isEmpty() && this.level().getGameTime() % 40L == 0L) {
                net.minecraft.world.phys.AABB arena = this.getBoundingBox().inflate(35.0D);
                java.util.List<Rabbit> nearby = this.level().getEntitiesOfClass(Rabbit.class, arena,
                        r -> r.isAlive() && r.getVariant() == Rabbit.Variant.EVIL && "camejitos".equals(r.getCustomName() != null ? r.getCustomName().getString() : ""));
                this.activeCamejitos.addAll(nearby);
            }

            if (this.activeCamejitos.size() > 0 && this.activeCamejitos.size() <= 3) {
                for (Rabbit camejito : this.activeCamejitos) {
                    camejito.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false, true));
                }
            }
        }
    }

    private void shootCard(LivingEntity target) {
        // 1 de cada 3, de manera aleatoria, es tarjeta roja
        boolean isRed = this.random.nextInt(3) == 0;

        // Lanzar desde la altura del torso/manos de MrMullet (~3.2 bloques en altura 5.0)
        double shootX = this.getX();
        double shootY = this.getY() + 3.2D;
        double shootZ = this.getZ();

        CardProjectileEntity projectile = new CardProjectileEntity(this.level(), shootX, shootY, shootZ, isRed);
        projectile.setOwner(this);

        double targetX = target.getX() - shootX;
        double targetY = target.getEyeY() - shootY;
        double targetZ = target.getZ() - shootZ;

        // Vuela directo al objetivo con velocidad como un proyectil del wither
        projectile.shoot(targetX, targetY, targetZ, 1.4F, 1.0F);

        this.playSound(SoundEvents.WITHER_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(projectile);
    }

    private void spawnCamejitos() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.5F, 0.8F);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0D, this.getZ(), 5, 0.5D, 0.5D, 0.5D, 0.1D);

        for (int i = 0; i < 5; i++) {
            Rabbit camejito = EntityType.RABBIT.create(serverLevel);
            if (camejito != null) {
                double angle = (2.0D * Math.PI / 5.0D) * i;
                double distance = 2.5D;
                double spawnX = this.getX() + Math.cos(angle) * distance;
                double spawnZ = this.getZ() + Math.sin(angle) * distance;
                double spawnY = this.getY();

                camejito.moveTo(spawnX, spawnY, spawnZ, this.random.nextFloat() * 360.0F, 0.0F);
                camejito.setVariant(Rabbit.Variant.EVIL);
                camejito.setCustomName(Component.literal("camejitos"));
                camejito.setCustomNameVisible(true);
                camejito.setPersistenceRequired();

                if (this.getTarget() != null) {
                    camejito.setTarget(this.getTarget());
                }

                this.activeCamejitos.add(camejito);
                serverLevel.addFreshEntity(camejito);
                serverLevel.sendParticles(ParticleTypes.POOF, spawnX, spawnY + 0.5D, spawnZ, 10, 0.2D, 0.2D, 0.2D, 0.05D);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.playSound(ModSounds.JEFE_ATAQUE.get(), 1.0F, 1.0F);

            if (target instanceof LivingEntity livingTarget) {
                long currentTime = this.level().getGameTime();
                // Si da dos golpes seguidos al mismo objetivo (en una ventana de hasta 5 segundos / 100 ticks)
                if (livingTarget.getUUID().equals(this.lastHitTargetId) && (currentTime - this.lastHitTick) <= 100L) {
                    this.consecutiveHits++;
                } else {
                    this.consecutiveHits = 1;
                }

                this.lastHitTargetId = livingTarget.getUUID();
                this.lastHitTick = currentTime;

                if (this.consecutiveHits >= 2) {
                    // Segundo golpe consecutivo: aplica veneno del wither
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1), this);
                    this.consecutiveHits = 0; // Reiniciar tras activar el efecto

                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.SQUID_INK, livingTarget.getX(), livingTarget.getY() + 1.0D, livingTarget.getZ(), 15, 0.3D, 0.5D, 0.3D, 0.05D);
                        serverLevel.sendParticles(ParticleTypes.SMOKE, livingTarget.getX(), livingTarget.getY() + 1.0D, livingTarget.getZ(), 10, 0.2D, 0.4D, 0.2D, 0.02D);
                    }
                    this.playSound(SoundEvents.WITHER_SHOOT, 1.2F, 0.8F);
                }
            }
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Inmune a las explosiones o impactos de sus propios proyectiles de tarjeta
        if (source.getDirectEntity() instanceof CardProjectileEntity) {
            return false;
        }

        boolean wasHurt = super.hurt(source, amount);
        if (wasHurt && !this.level().isClientSide() && source.getEntity() instanceof Player) {
            if (!this.combatMusicStarted) {
                this.combatMusicStarted = true;
                this.level().playSound(null, this.blockPosition(), ModSounds.DUNGEON_COMBATE.get(), SoundSource.RECORDS, 1.5F, 1.0F);
            }
        }
        return wasHurt;
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
    public void die(DamageSource source) {
        super.die(source);
        if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(FRENZY_SPEED_MODIFIER_UUID);
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            DungeonProtectionData data = DungeonProtectionData.get(serverLevel);
            data.setMrMulletDefeated(true);
            for (ServerPlayer player : serverLevel.players()) {
                player.sendSystemMessage(Component.literal("§6§l[Mr Mullet]§r §a¡El jefe ha sido derrotado! La puerta de salida se ha desbloqueado."));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("SpawnedMinions", this.hasSpawnedMinions);
        tag.putBoolean("CombatMusicStarted", this.combatMusicStarted);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.hasSpawnedMinions = tag.getBoolean("SpawnedMinions");
        this.combatMusicStarted = tag.getBoolean("CombatMusicStarted");
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
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
