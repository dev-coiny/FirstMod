package ch.coiny.firstmod.customhelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ParticleHelper {

    public static void createSphere(Level level, Player player, float baseRadius, float power, float scaling, int growthDuration, int maxDuration) {
        if (level.isClientSide()) {
            return;  // Verhindert, dass der Code auf der Client-Seite ausgeführt wird
        }

        ServerLevel serverLevel = (ServerLevel) level;

        // Maximale Reichweite der Schockwelle basierend auf dem Power-Wert
        double maxRadius = baseRadius + power * scaling;
        int particleCount = (int) (400 * (power * 3));  // Anzahl der Partikel in der Schockwelle

        // Gesamtzeit, die Partikel erzeugt werden (1 Sekunde Wachstum + 5 Sekunden Stabilität)
        int totalDuration = growthDuration + maxDuration;

        // Speichern der ursprünglichen Position des Spielers, um die Schockwelle an diesem Punkt zu behalten
        double initialX = player.getX();
        double initialY = player.getY();
        double initialZ = player.getZ();

        final int[] tickCounter = {0};

        // Registrieren eines Event-Handlers, um die Partikel in jedem Tick zu erzeugen
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onServerTick(TickEvent.ServerTickEvent event) {
                if (tickCounter[0] < totalDuration) {
                    // Berechne den aktuellen Radius
                    double currentRadius;
                    if (tickCounter[0] < growthDuration) {
                        currentRadius = maxRadius * (tickCounter[0] / (double) growthDuration);
                    } else {
                        currentRadius = maxRadius;
                    }

                    // Generiere Partikel
                    for (int i = 0; i < particleCount; i++) {
                        // Erzeuge zufällige Richtung auf der Kugeloberfläche
                        double theta = Math.random() * Math.PI;  // Vertikaler Winkel [0, PI]
                        double phi = Math.random() * 2 * Math.PI;  // Horizontaler Winkel [0, 2*PI]

                        // Berechne die Position der Partikel auf der Kugeloberfläche Spieler koordinaten angeben um verfolgende kugel zu machen (evtl neue methode)
                        double x = initialX + currentRadius * Math.sin(theta) * Math.cos(phi);  // X-Position
                        double y = initialY + currentRadius * Math.cos(theta);                    // Y-Position
                        double z = initialZ + currentRadius * Math.sin(theta) * Math.sin(phi);  // Z-Position

                        // Alle Spieler im Server-Level erreichen und Partikel für sie erzeugen
                        for (Player otherPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
                            if (otherPlayer.level() == serverLevel) {  // Überprüfen, ob der Spieler im selben Level ist
                                // Sende Partikel an alle Clients im selben Level (Welt)
                                serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 0, 0, 0, 0, 0); // Silbrige Partikel
                            }
                        }
                    }

                    // Erhöhe den Tick-Zähler
                    tickCounter[0]++;
                } else {
                    // Entferne den Event-Handler, wenn die Schockwelle abgeschlossen ist
                    MinecraftForge.EVENT_BUS.unregister(this);
                }
            }
        });
    }
}
