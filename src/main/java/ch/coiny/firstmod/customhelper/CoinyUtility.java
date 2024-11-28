package ch.coiny.firstmod.customhelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class CoinyUtility {
    public static void createSphereWithEffect(Level level,
                                              Player player,
                                              float baseRadius,
                                              float power,
                                              float scaling,
                                              int growthDuration,
                                              int maxDuration,
                                              MobEffectAction effectAction) {
        if (level.isClientSide()) {
            return; // Verhindert, dass der Code auf der Client-Seite ausgeführt wird
        }

        ServerLevel serverLevel = (ServerLevel) level;

        double maxRadius = baseRadius + power * scaling;
        int totalDuration = growthDuration + maxDuration;

        double initialX = player.getX();
        double initialY = player.getY();
        double initialZ = player.getZ();

        final int[] tickCounter = {0};

        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onServerTick(TickEvent.ServerTickEvent event) {
                if (tickCounter[0] < totalDuration) {
                    double currentRadius;
                    if (tickCounter[0] < growthDuration) {
                        currentRadius = maxRadius * (tickCounter[0] / (double) growthDuration);
                    } else {
                        currentRadius = maxRadius;
                    }

                    // Partikel generieren
                    for (int i = 0; i < 100; i++) { // Reduzierte Partikelanzahl
                        double theta = Math.random() * Math.PI;
                        double phi = Math.random() * 2 * Math.PI;

                        double x = initialX + currentRadius * Math.sin(theta) * Math.cos(phi);
                        double y = initialY + currentRadius * Math.cos(theta);
                        double z = initialZ + currentRadius * Math.sin(theta) * Math.sin(phi);

                        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 0, 0, 0, 0, 0);
                    }

                    // Effekte auf Mobs anwenden
                    AABB sphereBounds = new AABB(
                            initialX - currentRadius, initialY - currentRadius, initialZ - currentRadius,
                            initialX + currentRadius, initialY + currentRadius, initialZ + currentRadius
                    );

                    List<Mob> mobsInSphere = serverLevel.getEntitiesOfClass(Mob.class, sphereBounds,
                            mob -> mob.distanceToSqr(initialX, initialY, initialZ) <= currentRadius * currentRadius);

                    for (Mob mob : mobsInSphere) {
                        effectAction.apply(mob); // Die übergebene Aktion auf den Mob anwenden
                    }

                    tickCounter[0]++;
                } else {
                    MinecraftForge.EVENT_BUS.unregister(this);
                }
            }
        });
    }


}
