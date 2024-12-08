package ch.coiny.firstmod.item.custom;

import ch.coiny.firstmod.customhelper.CoinyUtility;
import ch.coiny.firstmod.entity.custom.HourGlassProjectileEntity;
import ch.coiny.firstmod.util.EntityTickEventHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.function.Predicate;

public class HourGlass extends ProjectileWeaponItem {
    public static final int MAX_DRAW_DURATION = 20;
    public static final int DEFAULT_RANGE = 15;

    public HourGlass(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        if (pEntityLiving instanceof Player player) {
            int useDuration = this.getUseDuration(pStack, pEntityLiving) - pTimeLeft;

            // Mindestnutzungsdauer prüfen
            if (useDuration >= 10) {
                if (!pLevel.isClientSide) {
                    // Neue Instanz der HourGlassProjectileEntity erstellen
                    HourGlassProjectileEntity projectile = new HourGlassProjectileEntity(pLevel, player, calculatePower(pTimeLeft));

                    // Flugbahn des Projektils setzen
                    projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);

                    // Projektil zur Welt hinzufügen
                    pLevel.addFreshEntity(projectile);

                    // Sound abspielen (optional)
                    pLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

                    // Gegenstand entfernen
                    if (!player.getAbilities().instabuild) { // Nur entfernen, wenn der Spieler keinen Kreativmodus hat
                        pStack.shrink(1); // Reduziert den Stack um 1; wenn der Stack leer ist, wird das Item entfernt
                    }
                }

                // Statistik aktualisieren
                player.awardStat(Stats.ITEM_USED.get(this));
            }
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
        return UseAnim.SPEAR;
    }
/*
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

 */
@Override
public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    player.startUsingItem(hand); // Startet die Nutzung
    return InteractionResultHolder.consume(stack); // Zeigt an, dass das Item benutzt wird
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
