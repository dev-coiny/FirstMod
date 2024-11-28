package ch.coiny.firstmod.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;

public class MobTickEventHandler {

    // Eine Map, um die Mobs und deren Zeitstempel zu speichern, wann der Tick wieder aktiviert wird.
    private static final Map<LivingEntity, Long> disabledTicks = new HashMap<>();


    // Registriere die Events
    public static void register(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.register(MobTickEventHandler.class);
    }

    // Methode, um den Tick eines Mobs zu deaktivieren
    public static void disableMobTick(LivingEntity mob, int disabledTime) {
        long currentTime = System.currentTimeMillis();
        disabledTicks.put(mob, currentTime + disabledTime);
    }

    // Event-Handler für das LivingEntity-Event (wird für das Ticken von Mobs genutzt)
    @SubscribeEvent
    public static void onLivingEntityTick(LivingEvent.LivingTickEvent event) {
        LivingEntity mob = event.getEntity();

        // Überprüfen, ob der Mob in der Map ist und der Tick gestoppt werden muss
        if (disabledTicks.containsKey(mob)) {
            long disabledUntil = disabledTicks.get(mob);
            if (System.currentTimeMillis() < disabledUntil) {
                event.setCanceled(true); // Stoppt den Tick für diesen Mob
                return;
            } else {
                disabledTicks.remove(mob); // Entferne den Mob, wenn der Timer abgelaufen ist
            }
        }
    }
}
