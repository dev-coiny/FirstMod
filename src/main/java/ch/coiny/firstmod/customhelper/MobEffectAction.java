package ch.coiny.firstmod.customhelper;

import net.minecraft.world.entity.Mob;

@FunctionalInterface
public interface MobEffectAction {
    void apply(Mob mob);
}
