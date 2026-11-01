package net.gruposeis.mullet.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ProfesorMulletEntity extends PathfinderMob {

    private int talkCooldown = 0;

    public ProfesorMulletEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.literal("Profesor Mullet"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            if (this.talkCooldown <= 0) {
                this.talkCooldown = 40; // 2 segundos de enfriamiento entre interacciones

                String[] dialogos = {
                        "§6§l[Profesor Mullet]§r §f¡Muchas gracias por salvarme! Ese malware casi acaba conmigo... ¡Te debo la vida!",
                        "§6§l[Profesor Mullet]§r §f¡Por fin libre de la corrupción de las Tierras Profundas! Gracias por rescatarme.",
                        "§6§l[Profesor Mullet]§r §f¡Eres un verdadero héroe! Pensé que nunca volvería a ver el Overworld.",
                        "§6§l[Profesor Mullet]§r §fPintaluba no se saldrá con la suya ahora que estoy a salvo."
                };

                String mensaje = dialogos[this.random.nextInt(dialogos.length)];
                player.sendSystemMessage(Component.literal(mensaje));

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.8D, this.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.05D);
                }
                this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.talkCooldown > 0) {
            this.talkCooldown--;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // NPC pacífico protegido contra ataques accidentales de jugadores en supervivencia
        if (source.getEntity() instanceof Player player && !player.isCreative()) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}

