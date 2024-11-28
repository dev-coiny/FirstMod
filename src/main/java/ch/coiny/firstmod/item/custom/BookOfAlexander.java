package ch.coiny.firstmod.item.custom;

import ch.coiny.firstmod.block.ModBlocks;
import ch.coiny.firstmod.component.ModDataComponentTypes;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class BookOfAlexander extends Item {
    private static final Map<Block, Block> CHISEL_MAP =
            Map.of(
                    Blocks.STONE, Blocks.STONE_BRICKS,
                    Blocks.END_STONE, Blocks.END_STONE_BRICKS,
                    Blocks.DEEPSLATE, Blocks.DEEPSLATE_BRICKS,
                    Blocks.IRON_BLOCK, Blocks.DIAMOND_BLOCK,
                    Blocks.DIRT, ModBlocks.ALEXANDRITE_BLOCK.get()
            );

    public BookOfAlexander(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        Block clickedBlock = level.getBlockState(pContext.getClickedPos()).getBlock();

        if(CHISEL_MAP.containsKey(clickedBlock)){
            if(!level.isClientSide){
                level.setBlockAndUpdate(pContext.getClickedPos(), CHISEL_MAP.get(clickedBlock).defaultBlockState());

                pContext.getItemInHand().hurtAndBreak(1, ((ServerLevel) level), ((ServerPlayer) pContext.getPlayer()),
                        item -> pContext.getPlayer().onEquippedItemBroken(item, EquipmentSlot.MAINHAND));

                level.playSound(null, pContext.getClickedPos(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS);

                pContext.getItemInHand().set(ModDataComponentTypes.COORDINATES.get(), pContext.getClickedPos());
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            // Ausgangspunkt (Augenposition des Spielers)
            Vec3 start = player.getEyePosition(1.0F);
            Vec3 direction = player.getLookAngle().normalize();
            double maxDistance = 50.0;

            // Ziel berechnen
            HitResult hitResult = level.clip(new ClipContext(
                    start,
                    start.add(direction.scale(maxDistance)),
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (hitResult.getType() != HitResult.Type.MISS) {
                // Zielposition ermitteln
                Vec3 hitPos = hitResult.getLocation();
                double targetX = hitPos.x;
                double targetY = hitPos.y;
                double targetZ = hitPos.z;

                // Startpunkt des Strahls (Maximale Weltbauhöhe)
                double startY = level.getMaxBuildHeight() - 1;

                // Starte den Partikelstrahl
                new Thread(() -> {
                    for (double currentY = startY; currentY >= targetY; currentY -= 0.5) {
                        Vec3 currentPos = new Vec3(targetX, currentY, targetZ);

                        // Partikel hinzufügen
                        level.getServer().getPlayerList().getPlayers().forEach(serverPlayer -> {
                            serverPlayer.connection.send(new ClientboundLevelParticlesPacket(
                                    new DustParticleOptions(new Vector3f(0.0F, 1.0F, 1.0F), 1.0F), // Türkisfarbene Partikel
                                    false, // Absolutposition
                                    currentPos.x, currentPos.y, currentPos.z, // Position
                                    0.5F, 0.5F, 0.5F, // Streuung
                                    50F, // Anzahl der Partikel
                                    50 // Geschwindigkeit
                            ));
                        });
                    }

                    // Explosion am Ziel erzeugen
                    level.explode(
                            null, targetX, targetY, targetZ,
                            4.0F, Level.ExplosionInteraction.BLOCK
                    );
                }).start();
            }
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }




    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
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
}
