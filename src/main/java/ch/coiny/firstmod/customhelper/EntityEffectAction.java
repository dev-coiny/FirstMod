package ch.coiny.firstmod.customhelper;

import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface EntityEffectAction {
    void apply(Entity entity);
}