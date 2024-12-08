package ch.coiny.firstmod.customhelper;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public class ParticleCage {
    private final ServerLevel level;
    private final Entity entity;
    private final int durationTicks;
    private int ticksElapsed = 0;

    public ParticleCage(ServerLevel level, Entity entity, int durationTicks) {
        this.level = level;
        this.entity = entity;
        this.durationTicks = durationTicks;
        MinecraftForge.EVENT_BUS.register(this); // Tick-Handler registrieren
    }

    @SubscribeEvent
    public void onTick(TickEvent.LevelTickEvent event) {
        if (event.level != level || event.phase != TickEvent.Phase.END) {
            return; // Nur für die entsprechende Welt und am Ende des Ticks ausführen
        }

        // Partikel erzeugen
        createCageParticles();

        ticksElapsed++;
        if (ticksElapsed >= durationTicks) {
            MinecraftForge.EVENT_BUS.unregister(this); // Tick-Handler entfernen
        }
    }

    private void createCageParticles() {
        // Hitbox-Dimensionen der Entität
        double width = entity.getBbWidth();
        double height = entity.getBbHeight();
        Vec3 center = entity.position(); // Mittelpunkt der Entität

        // Käfig-Ränder berechnen
        double minX = center.x - width / 2 - 0.2;
        double maxX = center.x + width / 2 + 0.2;
        double minY = center.y;
        double maxY = center.y + height + 0.2;
        double minZ = center.z - width / 2 - 0.2;
        double maxZ = center.z + width / 2 + 0.2;

        // Partikel entlang der Kanten des Käfigs erzeugen
        for (double x = minX; x <= maxX; x += 0.2) {
            for (double z = minZ; z <= maxZ; z += 0.2) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, minY, z, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, maxY, z, 1, 0, 0, 0, 0);
            }
        }
        for (double y = minY; y <= maxY; y += 0.2) {
            for (double x = minX; x <= maxX; x += 0.2) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, minZ, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, maxZ, 1, 0, 0, 0, 0);
            }
            for (double z = minZ; z <= maxZ; z += 0.2) {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, minX, y, z, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, maxX, y, z, 1, 0, 0, 0, 0);
            }
        }
    }
}
