package ch.coiny.firstmod.customhelper;

import ch.coiny.firstmod.util.EntityTickEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ServerTickHandler {

    // Liste von dynamisch hinzugefügten Methoden (Runnable)
    private final List<Runnable> dynamicMethods = new ArrayList<>();

    // Event-Handler für den Server-Tick
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        // Alle dynamisch hinzugefügten Methoden ausführen
        for (Runnable method : dynamicMethods) {
            method.run();
        }
    }

    // Methode zum Hinzufügen von dynamischen Methoden
    public void addDynamicMethod(Runnable method) {
        dynamicMethods.add(method);
    }

    // Methode zum Entfernen von dynamischen Methoden
    public void removeDynamicMethod(Runnable method) {
        dynamicMethods.remove(method);
    }

    // Registriere die Events
    public static void register(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.register(ServerTickHandler.class);
    }
}