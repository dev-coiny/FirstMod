package ch.coiny.firstmod.customhelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class CoinyUtility {
    ServerTickHandler tickHandler = new ServerTickHandler();

    public static void createSphereWithMobEffect(Level level,
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

    public static void createSphereWithEntityEffect(
            Level level, Player player, float baseRadius, float power, float scaling, int growthDuration, int maxDuration,
            EntityEffectAction effectAction, List<Entity> excludeEntities) {

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

                    // Effekte auf alle Entitäten anwenden
                    AABB sphereBounds = new AABB(
                            initialX - currentRadius, initialY - currentRadius, initialZ - currentRadius,
                            initialX + currentRadius, initialY + currentRadius, initialZ + currentRadius
                    );

                    List<Entity> entitiesInSphere = serverLevel.getEntitiesOfClass(Entity.class, sphereBounds,
                            entity -> entity.distanceToSqr(initialX, initialY, initialZ) <= currentRadius * currentRadius);

                    for (Entity entity : entitiesInSphere) {
                        // Wenn die Entität nicht in der Ausschlussliste ist, den Effekt anwenden
                        if (!excludeEntities.contains(entity)) {
                            effectAction.apply(entity);  // Die übergebene Aktion auf die Entität anwenden
                        }
                    }

                    tickCounter[0]++;
                } else {
                    MinecraftForge.EVENT_BUS.unregister(this);
                }
            }
        });
    }

    public static void createSphereWithCoordinatesAndEntityEffect(
            Level level, Vec3 coordinates, float baseRadius, float power, float scaling, int growthDuration, int maxDuration,
            EntityEffectAction effectAction, List<Entity> excludeEntities) {

        if (level.isClientSide()) {
            return; // Verhindert, dass der Code auf der Client-Seite ausgeführt wird
        }

        ServerLevel serverLevel = (ServerLevel) level;

        double maxRadius = baseRadius + power * scaling;
        int totalDuration = growthDuration + maxDuration;

        double initialX = coordinates.x;
        double initialY = coordinates.y;
        double initialZ = coordinates.z;

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

                    // Effekte auf alle Entitäten anwenden
                    AABB sphereBounds = new AABB(
                            initialX - currentRadius, initialY - currentRadius, initialZ - currentRadius,
                            initialX + currentRadius, initialY + currentRadius, initialZ + currentRadius
                    );

                    List<Entity> entitiesInSphere = serverLevel.getEntitiesOfClass(Entity.class, sphereBounds,
                            entity -> entity.distanceToSqr(initialX, initialY, initialZ) <= currentRadius * currentRadius);

                    for (Entity entity : entitiesInSphere) {
                        // Wenn die Entität nicht in der Ausschlussliste ist, den Effekt anwenden
                        if (!excludeEntities.contains(entity)) {
                            effectAction.apply(entity);  // Die übergebene Aktion auf die Entität anwenden
                        }
                    }

                    tickCounter[0]++;
                } else {
                    MinecraftForge.EVENT_BUS.unregister(this);
                }
            }
        });
    }

}
