package ch.coiny.firstmod.entity.rendering;

import ch.coiny.firstmod.entity.custom.HourGlassProjectileEntity;
import com.ibm.icu.text.DisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class RotatingRenderer extends EntityRenderer<HourGlassProjectileEntity> {
    private final ItemRenderer itemRenderer;

    public RotatingRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(HourGlassProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Setze die Position in der Welt
        poseStack.translate(0.0D, 0.0D, 0.0D);

        // Rotation basierend auf der Flugzeit
        float rotation = entity.tickCount + partialTicks; // Tick-basierte Drehung
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation * 20)); // Drehung um die Y-Achse
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation * 10)); // Drehung um die X-Achse

        // Hole das ItemStack, das gerendert werden soll
        ItemStack stack = entity.getItem();

        // Rendere das Item
        itemRenderer.renderStatic(stack,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                0);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HourGlassProjectileEntity entity) {
        return null; // Kein spezifischer Texturpfad, da das Item gerendert wird
    }
}
