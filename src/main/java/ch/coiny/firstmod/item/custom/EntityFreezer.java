package ch.coiny.firstmod.item.custom;

import ch.coiny.firstmod.customhelper.AreaHelper;
import ch.coiny.firstmod.customhelper.CoinyUtility;
import ch.coiny.firstmod.customhelper.ParticleHelper;
import ch.coiny.firstmod.util.MobTickEventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class EntityFreezer extends ProjectileWeaponItem {
    public static final int MAX_DRAW_DURATION = 20;
    public static final int DEFAULT_RANGE = 15;

    public EntityFreezer(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!level.isClientSide && entity instanceof Player player) {
            int charge = this.getUseDuration(stack, entity) - timeCharged;
            float power = calculatePower(charge);
            CoinyUtility.createSphereWithEffect(
                    level,
                    player,
                    3,
                    power,
                    5,
                    20,
                    1000,
                    mob -> MobTickEventHandler.disableMobTick(mob, 200));
        }
    }
    private float calculatePower(int charge) {
        float f = (float) charge / 20.0F; // Ladezeit in Sekunden
        f = (f * f + f * 2.0F) / 3.0F;   // Skaliere Ladeprogression
        return Math.min(f, 1.0F);        // Maximal 1.0
    }

    @Override
    protected void shootProjectile(
            LivingEntity pShooter, Projectile pProjectile, int pIndex, float pVelocity, float pInaccuracy, float pAngle, @Nullable LivingEntity pTarget) {
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        return 72000;
    }


    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        boolean flag = !pPlayer.getProjectile(itemstack).isEmpty();
        var ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, pLevel, pPlayer, pHand, flag);
        if (ret != null) return ret;
        if (!pPlayer.hasInfiniteMaterials() && !flag) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            pPlayer.startUsingItem(pHand);
            return InteractionResultHolder.consume(itemstack);
        }
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }
}
