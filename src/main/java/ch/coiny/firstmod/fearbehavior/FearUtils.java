package ch.coiny.firstmod.fearbehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FearUtils {

    /**
     * Lässt ein Mob Angst vor einem Spieler haben.
     * Währenddessen erscheinen Partikel über dem Kopf des Mobs.
     *
     * @param mob            Das Mob, das Angst haben soll.
     * @param player         Der Spieler, vor dem das Mob flieht.
     * @param durationInTicks Dauer der Angst in Ticks (1 Sekunde = 20 Ticks).
     */
    public static void makeMobFearPlayer(Mob mob, LivingEntity player, int durationInTicks) {
        // Speichere die aktuellen Goals
        Set<Goal> originalGoals = mob.goalSelector.getAvailableGoals().stream()
                .map(prioritizedGoal -> prioritizedGoal.getGoal())
                .collect(Collectors.toSet());

        // Entferne alle existierenden Goals
        mob.goalSelector.getAvailableGoals().forEach(prioritizedGoal -> mob.goalSelector.removeGoal(prioritizedGoal.getGoal()));

        // Füge das Flucht-Goal hinzu
        mob.goalSelector.addGoal(1, new FleeFromPlayerWithParticlesGoal(mob, player, durationInTicks, () -> {
            // Stelle die ursprünglichen Goals wieder her
            for (Goal goal : originalGoals) {
                mob.goalSelector.addGoal(2, goal);
            }
        }));
    }

    // Innere Klasse für das Fluchtverhalten mit Partikeln
    private static class FleeFromPlayerWithParticlesGoal extends Goal {
        private final Mob mob;
        private final LivingEntity player;
        private final int durationInTicks;
        private final Runnable onComplete; // Rückruf, wenn die Flucht endet.
        private int ticksRan;

        public FleeFromPlayerWithParticlesGoal(Mob mob, LivingEntity player, int durationInTicks, Runnable onComplete) {
            this.mob = mob;
            this.player = player;
            this.durationInTicks = durationInTicks;
            this.onComplete = onComplete;
            this.ticksRan = 0;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.mob.distanceTo(this.player) < 16.0D && this.ticksRan < this.durationInTicks;
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticksRan < this.durationInTicks;
        }

        @Override
        public void start() {
            this.ticksRan = 0;
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
            this.onComplete.run(); // Rufe die Wiederherstellung der ursprünglichen Goals auf.
        }

        @Override
        public void tick() {
            // Berechne die Fluchtrichtung
            Vec3 mobPosition = this.mob.position();
            Vec3 playerPosition = this.player.position();
            Vec3 fleeDirection = mobPosition.subtract(playerPosition).normalize().scale(1.5);

            Vec3 targetPosition = mobPosition.add(fleeDirection);
            this.mob.getNavigation().moveTo(targetPosition.x, targetPosition.y, targetPosition.z, 1.0);

            // Partikel anzeigen
            if (this.mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.SMOKE, // Partikeltyp
                        mobPosition.x, mobPosition.y + mob.getBbHeight() + 0.5, mobPosition.z, // Position über dem Kopf
                        3, // Anzahl der Partikel
                        0.2, 0.2, 0.2, // Streuung
                        0.02 // Geschwindigkeit
                );
            }

            this.ticksRan++;
        }
    }
}

