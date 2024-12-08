package ch.coiny.firstmod.item.custom;

import ch.coiny.firstmod.customhelper.ParticleCage;
import ch.coiny.firstmod.util.EntityTickEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import ch.coiny.firstmod.component.ModDataComponentTypes;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SoulCrystal extends Item {

    public SoulCrystal(Properties pProperties) {
        super(pProperties);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            shootBeam((ServerLevel) level, player);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    private void shootBeam(ServerLevel level, Player player) {
        Vec3 start = player.getEyePosition(); // Startpunkt des Strahls
        Vec3 direction = player.getLookAngle(); // Richtung des Strahls
        double range = 20.0; // Maximale Reichweite

        for (double i = 0; i < range; i += 0.5) { // Schrittweite des Strahls
            Vec3 currentPos = start.add(direction.scale(i)); // Position entlang des Strahls
            level.sendParticles(ParticleTypes.END_ROD, currentPos.x, currentPos.y, currentPos.z, 1, 0, 0, 0, 0);

            // Block-Treffer prüfen
            BlockPos blockPos = new BlockPos((int) currentPos.x, (int) currentPos.y, (int) currentPos.z); // Konvertiere Vec3 zu BlockPos (Rundung)
            if (!level.getBlockState(blockPos).isAir()) {
                onBlockHit(level, blockPos);
                break;
            }

            // Entity-Treffer prüfen
            EntityHitResult entityHit = findEntityOnPath(level, player, start, currentPos);
            if (entityHit != null) {
                onEntityHit(level, entityHit);
                break;
            }
        }
    }

    private void onBlockHit(ServerLevel level, BlockPos blockPos) {
        level.sendParticles(ParticleTypes.SMOKE, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.01);
        // TODO: Aktion hinzufügen, z.B. Block zerstören, Sound abspielen, etc.
    }

    private void onEntityHit(ServerLevel level, EntityHitResult entityHit) {
        Entity entity = entityHit.getEntity();
        level.sendParticles(ParticleTypes.CRIT, entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ(), 5, 0.2, 0.2, 0.2, 0.01);
        // TODO: Aktion hinzufügen, z.B. Schaden zufügen, Effekte anwenden, etc.
        EntityTickEventHandler.disableEntityTick(entityHit.getEntity(), 5000);
        new ParticleCage(level, entity, 50);
    }

    private EntityHitResult findEntityOnPath(Level level, Player player, Vec3 start, Vec3 end) {
        List<Entity> entities = level.getEntities(player, player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1));
        for (Entity entity : entities) {
            if (entity.getBoundingBox().intersects(start, end)) {
                return new EntityHitResult(entity);
            }
        }
        return null;
    }
}
