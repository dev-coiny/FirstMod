package ch.coiny.firstmod.item.custom;

import ch.coiny.firstmod.block.ModBlocks;
import ch.coiny.firstmod.component.ModDataComponentTypes;
import net.minecraft.client.gui.screens.Screen;
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        return InteractionResultHolder.success(player.getItemInHand(hand));
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
}
