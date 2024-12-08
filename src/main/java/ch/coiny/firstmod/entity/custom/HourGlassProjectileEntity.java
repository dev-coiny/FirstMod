package ch.coiny.firstmod.entity.custom;

import ch.coiny.firstmod.block.ModBlocks;
import ch.coiny.firstmod.block.custom.MagicBLock;
import ch.coiny.firstmod.customhelper.CoinyUtility;
import ch.coiny.firstmod.entity.ModEntities;
import ch.coiny.firstmod.item.ModItems;
import ch.coiny.firstmod.util.EntityTickEventHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Collections;

public class HourGlassProjectileEntity extends ThrowableItemProjectile {
    private float Power;
    public HourGlassProjectileEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public HourGlassProjectileEntity(Level pLevel) {
        super(ModEntities.HOURGLASS_PROJECTILE.get(), pLevel);
    }

    public HourGlassProjectileEntity(Level pLevel, LivingEntity livingEntity, float power) {
        super(ModEntities.HOURGLASS_PROJECTILE.get(), livingEntity, pLevel);
        this.Power = power;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.HOUR_GLASS.get();
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        /*
        if(!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, ((byte) 3));
            this.level().setBlock(blockPosition(), ((MagicBLock) ModBlocks.MAGIC_BLOCK.get()).defaultBlockState(), 3);

                CoinyUtility.createSphereWithCoordinatesAndEntityEffect(
                        this.level(),
                        pResult.getBlockPos(),
                        3,
                        power,
                        5,
                        20,
                        1000,
                        e -> EntityTickEventHandler.disableEntityTick(e, 200),
                        Collections.singletonList(null));

        }
         */

        this.discard();
        super.onHitBlock(pResult);
    }

    @Override
    protected void onHit(HitResult pResult) {
        CoinyUtility.createSphereWithCoordinatesAndEntityEffect(
                this.level(),
                pResult.getLocation(),
                3,
                Power,
                5,
                20,
                1000,
                e -> EntityTickEventHandler.disableEntityTick(e, 200),
                Collections.singletonList(null));
        super.onHit(pResult);
    }
}