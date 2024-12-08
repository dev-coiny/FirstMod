package ch.coiny.firstmod.entity;

import ch.coiny.firstmod.FirstMod;
import ch.coiny.firstmod.entity.custom.HourGlassProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FirstMod.MOD_ID);


    public static final RegistryObject<EntityType<HourGlassProjectileEntity>> HOURGLASS_PROJECTILE =
            ENTITY_TYPES.register("hourglass_projectile", () -> EntityType.Builder.<HourGlassProjectileEntity>of(HourGlassProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("hourglass_projectile"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
