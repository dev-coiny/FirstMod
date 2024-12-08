package ch.coiny.firstmod.util;

import ch.coiny.firstmod.item.custom.MagicStaff;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EntityTickEventHandler {

    // Eine Map, um die Entitäten und deren Zeitstempel zu speichern, wann der Tick wieder aktiviert wird.
    private static final Map<Entity, Long> disabledTicks = new HashMap<>();

    // Registriere die Events
    public static void register(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.register(EntityTickEventHandler.class);
    }

    // Methode, um den Tick einer Entität zu deaktivieren
    public static void disableEntityTick(Entity entity, int disabledTime) {
        long currentTime = System.currentTimeMillis();
        disabledTicks.put(entity, currentTime + disabledTime);
    }

    // Event-Handler für LivingEntity-Ticks
    @SubscribeEvent
    public static void onLivingEntityTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        // Überprüfe, ob die Entität deaktiviert ist
        if (disabledTicks.containsKey(entity)) {
            long disabledUntil = disabledTicks.get(entity);
            long currentTime = System.currentTimeMillis();

            if (currentTime < disabledUntil) {
                event.setCanceled(true); // Stoppe den Tick
                entity.setDeltaMovement(0, 0, 0); // Bewegung stoppen
            } else {
                // Entferne die Entität aus der Map, wenn die Zeit abgelaufen ist
                disabledTicks.remove(entity);
            }
        }
    }

    // Zusätzliche Methode: Tick-Überwachung für andere Entitäten
    public static void handleNonLivingEntityTicks(ServerLevel level) {
        // Iteriere über alle Entitäten in der Welt
        Iterator<Map.Entry<Entity, Long>> iterator = disabledTicks.entrySet().iterator();
        long currentTime = System.currentTimeMillis();

        while (iterator.hasNext()) {
            Map.Entry<Entity, Long> entry = iterator.next();
            Entity entity = entry.getKey();
            long disabledUntil = entry.getValue();

            // Überprüfen, ob die Entität in der aktuellen Welt ist
            if (entity.level() != level) {
                continue;
            }

            if (currentTime < disabledUntil) {
                // Solange die Zeit nicht abgelaufen ist, verhindere das Ticken
                entity.setDeltaMovement(0, 0, 0); // Bewegung stoppen
                entity.setNoGravity(true); // Optional: Schwerkraft deaktivieren
            } else {
                // Entferne die Entität aus der Map, wenn die Zeit abgelaufen ist
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            Player player = event.player;
            ServerLevel serverLevel = (ServerLevel) player.level();
            ItemStack heldItem = player.getMainHandItem();

            // Prüfe, ob der Spieler das Portal-Item hält
            if (heldItem.getItem() instanceof MagicStaff portalItem) {
                // Zeige Portal-Partikel und überprüfe Kollision
                portalItem.showPortalParticles(serverLevel);
                portalItem.checkPlayerCollision(player, serverLevel);
            }
        }
    }
}
