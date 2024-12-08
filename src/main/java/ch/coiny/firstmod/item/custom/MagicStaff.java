package ch.coiny.firstmod.item.custom;

import ca.weblite.objc.Client;
import ch.coiny.firstmod.block.ModBlocks;
import ch.coiny.firstmod.component.ModDataComponentTypes;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MagicStaff extends Item {
    private static final int PORTAL_LIFETIME_TICKS = 400; // Lebenszeit des Portals (in Ticks, z.B. 40 Ticks = 2 Sekunden)
    private Vec3 activePortalCenter = null; // Speichert die Position des aktiven Portals

    private static final Map<Block, Block> CHISEL_MAP =
            Map.of(
                    Blocks.STONE, Blocks.STONE_BRICKS,
                    Blocks.END_STONE, Blocks.END_STONE_BRICKS,
                    Blocks.DEEPSLATE, Blocks.DEEPSLATE_BRICKS,
                    Blocks.IRON_BLOCK, Blocks.DIAMOND_BLOCK,
                    Blocks.DIRT, ModBlocks.ALEXANDRITE_BLOCK.get()
            );

    public MagicStaff(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        if(Screen.hasShiftDown()){
            pTooltipComponents.add(Component.translatable("tooltip.firstmod.chisel"));
            if(pStack.get(ModDataComponentTypes.COORDINATES.get()) != null){
                pTooltipComponents.add(Component.literal("Last Block changed at " + pStack.get(ModDataComponentTypes.COORDINATES.get())));
            }
        }else{
            pTooltipComponents.add(Component.translatable("tooltip.firstmod.shift_up"));
        }

        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }




    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // Wenn bereits ein Portal existiert, entfernen wir es
            if (activePortalCenter != null) {
                removePortal(serverLevel);
            }

            // Neues Portal erstellen
            createParticlePortal(serverLevel, player);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    private void createParticlePortal(ServerLevel serverLevel, Player player) {
        // Bestimme die Blickrichtung des Spielers
        Vec3 lookDir = player.getLookAngle().normalize();
        Vec3 playerPos = player.position();
        double offsetDistance = 3.0; // Entfernung vor dem Spieler (3 Blöcke)

        // Position des Mittelpunktes des Portals (3 Blöcke vor dem Spieler)
        activePortalCenter = playerPos.add(lookDir.scale(offsetDistance));

        // Partikel für das rechteckige Portal erstellen
        showPortalParticles(serverLevel);
    }

    private void removePortal(ServerLevel serverLevel) {
        if (activePortalCenter != null) {
            // Rauchpartikel beim Entfernen des Portals
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    activePortalCenter.x, activePortalCenter.y, activePortalCenter.z,
                    50, 0.5, 1, 0.5, 0.1);
            activePortalCenter = null; // Position zurücksetzen
        }
    }

    public void showPortalParticles(ServerLevel serverLevel) {
        if (activePortalCenter != null) {
            // Kantenlängen des Rechtecks
            double width = 1.0;  // Breite des Portals
            double height = 2.0; // Höhe des Portals

            // Bestimme die Richtungen
            Vec3 upDirection = new Vec3(0, 1, 0); // Vertikale Richtung
            Vec3 rightDirection = calculateRightDirection(upDirection); // Seitliche Richtung (Rechts-von-Spieler)

            // Berechne die vier Ecken des Rechtecks
            Vec3 topLeft = activePortalCenter.add(upDirection.scale(height / 2)).add(rightDirection.scale(-width / 2));
            Vec3 topRight = activePortalCenter.add(upDirection.scale(height / 2)).add(rightDirection.scale(width / 2));
            Vec3 bottomLeft = activePortalCenter.add(upDirection.scale(-height / 2)).add(rightDirection.scale(-width / 2));
            Vec3 bottomRight = activePortalCenter.add(upDirection.scale(-height / 2)).add(rightDirection.scale(width / 2));

            // Partikel an den vier Ecken senden
            //sendRectangleParticles(serverLevel, topLeft, topRight, bottomLeft, bottomRight);
        }
    }

    private Vec3 calculateRightDirection(Vec3 upDirection) {
        // Kreuzprodukt zwischen der Blickrichtung (lookDir) und der vertikalen Richtung (upDirection)
        // Hier nehmen wir die Z-Achse als Standardbasisvektor (0, 0, 1), um das Kreuzprodukt zu berechnen
        Vec3 forward = new Vec3(0, 0, 1); // Z-Achse als Referenz
        double x = upDirection.y * forward.z - upDirection.z * forward.y;
        double y = upDirection.z * forward.x - upDirection.x * forward.z;
        double z = upDirection.x * forward.y - upDirection.y * forward.x;
        return new Vec3(x, y, z).normalize();
    }

    private void sendRectangleParticles(ClientLevel clientLevel, Vec3 topLeft, Vec3 topRight, Vec3 bottomLeft, Vec3 bottomRight) {
        // Anzahl der Partikel pro Kante
        int particlesPerEdge = 10;

        // Partikel für die obere Kante
        for (int i = 0; i <= particlesPerEdge; i++) {
            double progress = i / (double) particlesPerEdge;
            Vec3 particlePos = lerp(topLeft, topRight, progress);
            // Partikel ohne Schwerkraft und mit benutzerdefinierter Bewegung
            clientLevel.addParticle(ParticleTypes.PORTAL, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }

        // Partikel für die untere Kante
        for (int i = 0; i <= particlesPerEdge; i++) {
            double progress = i / (double) particlesPerEdge;
            Vec3 particlePos = lerp(bottomLeft, bottomRight, progress);
            // Partikel ohne Schwerkraft und mit benutzerdefinierter Bewegung
            clientLevel.addParticle(ParticleTypes.PORTAL, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }

        // Partikel für die linke Kante
        for (int i = 0; i <= particlesPerEdge; i++) {
            double progress = i / (double) particlesPerEdge;
            Vec3 particlePos = lerp(topLeft, bottomLeft, progress);
            // Partikel ohne Schwerkraft und mit benutzerdefinierter Bewegung
            clientLevel.addParticle(ParticleTypes.PORTAL, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }

        // Partikel für die rechte Kante
        for (int i = 0; i <= particlesPerEdge; i++) {
            double progress = i / (double) particlesPerEdge;
            Vec3 particlePos = lerp(topRight, bottomRight, progress);
            // Partikel ohne Schwerkraft und mit benutzerdefinierter Bewegung
            clientLevel.addParticle(ParticleTypes.PORTAL, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }
    }




    private Vec3 lerp(Vec3 start, Vec3 end, double progress) {
        // Lineare Interpolation zwischen den beiden Vektoren
        double x = start.x + (end.x - start.x) * progress;
        double y = start.y + (end.y - start.y) * progress;
        double z = start.z + (end.z - start.z) * progress;
        return new Vec3(x, y, z);
    }

    public void checkPlayerCollision(Player player, ServerLevel serverLevel) {
        if (activePortalCenter != null) {
            double distance = player.position().distanceTo(activePortalCenter);
            if (distance < 1.5) { // Spieler läuft durch das Portal
                removePortal(serverLevel);
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1.0f, 1.0f);
                // Optionale Aktion beim Durchschreiten des Portals (z. B. Teleportation)
            }
        }
    }
}
