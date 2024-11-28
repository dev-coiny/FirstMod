package ch.coiny.firstmod.customhelper;

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

public class AreaHelper {
    public static void applyMobsInSphere(Level level, Player player, float baseRadius, float power, float scaling, int growthDuration, int maxDuration) {
        if (level.isClientSide()) {
            return; // Verhindert, dass der Code auf der Client-Seite ausgeführt wird
        }

        ServerLevel serverLevel = (ServerLevel) level;

        // Maximale Reichweite der Schockwelle basierend auf dem Power-Wert
        double maxRadius = baseRadius + power * scaling;

        // Gesamtzeit, die die Sphäre aktiv ist
        int totalDuration = growthDuration + maxDuration;

        // Speichern der ursprünglichen Position des Spielers, um die Sphäre an diesem Punkt zu behalten
        double initialX = player.getX();
        double initialY = player.getY();
        double initialZ = player.getZ();

        final int[] tickCounter = {0};

        // Registrieren eines Event-Handlers, um die Wirkung in jedem Tick zu überprüfen
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onServerTick(TickEvent.ServerTickEvent event) {
                if (tickCounter[0] < totalDuration) {
                    // Berechne den aktuellen Radius der Sphäre
                    double currentRadius;
                    if (tickCounter[0] < growthDuration) {
                        currentRadius = maxRadius * (tickCounter[0] / (double) growthDuration);
                    } else {
                        currentRadius = maxRadius;
                    }

                    // Suche nach allen Mobs innerhalb der Sphäre
                    AABB sphereBounds = new AABB(
                            initialX - currentRadius, initialY - currentRadius, initialZ - currentRadius,
                            initialX + currentRadius, initialY + currentRadius, initialZ + currentRadius
                    );

                    List<Mob> mobsInSphere = serverLevel.getEntitiesOfClass(Mob.class, sphereBounds,
                            mob -> mob.distanceToSqr(initialX, initialY, initialZ) <= currentRadius * currentRadius);

                    // Wende den "test"-Effekt auf alle Mobs in der Sphäre an
                    for (Mob mob : mobsInSphere) {
                        test(mob);
                    }

                    // Erhöhe den Tick-Zähler
                    tickCounter[0]++;
                } else {
                    // Entferne den Event-Handler, wenn die Sphäre abgeschlossen ist
                    MinecraftForge.EVENT_BUS.unregister(this);
                }
            }

            // Beispielmethode "test" zum Anwenden eines Effekts auf Mobs
            private void test(Mob mob) {
                // Hier wird ein einfacher Effekt auf den Mob angewendet (z. B. Schaden, Trank-Effekte usw.)
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0)); // Beispiel: Mob leuchtet kurz auf
            }
        });
    }

}
