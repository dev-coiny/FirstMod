package ch.coiny.firstmod.item.custom;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SoulLantern extends ProjectileWeaponItem {
    public static final int MAX_DRAW_DURATION = 20;
    public static final int DEFAULT_RANGE = 15;

    public SoulLantern(Item.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!level.isClientSide && entity instanceof Player player) {
            int charge = this.getUseDuration(stack, entity) - timeCharged; // Ladezeit
            float power = calculatePower(charge); // Skaliere Stärke
            createShockwave(level, player, power); // Schockwelle erstellen
            affectMobs(level, player, power); // Gegner beeinflussen
        }
    }
    private float calculatePower(int charge) {
        float f = (float) charge / 20.0F; // Ladezeit in Sekunden
        f = (f * f + f * 2.0F) / 3.0F;   // Skaliere Ladeprogression
        return Math.min(f, 1.0F);        // Maximal 1.0
    }

    private void createShockwave(Level level, Player player, float power) {
        // Sicherstellen, dass dieser Code nur auf der Server-Seite ausgeführt wird
        if (level.isClientSide()) {
            return;  // Verhindert, dass der Code auf der Client-Seite ausgeführt wird
        }

        // Casten des Levels zu ServerLevel, da wir auf der Server-Seite arbeiten
        ServerLevel serverLevel = (ServerLevel) level;

        // Maximale Reichweite der Schockwelle basierend auf dem Power-Wert
        double maxRadius = 3.0 + power * 5.0;
        int particleCount = (int) (400 * (power * 3));  // Anzahl der Partikel in der Schockwelle

        // Generiere Partikel entlang einer zufälligen Verteilung auf der Oberfläche einer Kugel
        for (int i = 0; i < particleCount; i++) {

            // Erzeuge zufällige Richtung auf der Kugeloberfläche
            double theta = Math.random() * Math.PI;  // Vertikaler Winkel [0, PI]
            double phi = Math.random() * 2 * Math.PI;  // Horizontaler Winkel [0, 2*PI]

            // Berechne die Position der Partikel auf der Kugeloberfläche
            double x = player.getX() + maxRadius * Math.sin(theta) * Math.cos(phi);  // X-Position
            double y = player.getY() + maxRadius * Math.cos(theta);                    // Y-Position
            double z = player.getZ() + maxRadius * Math.sin(theta) * Math.sin(phi);  // Z-Position

            // Alle Spieler im Server-Level erreichen und Partikel für sie erzeugen
            for (Player otherPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
                if (otherPlayer.level() == serverLevel) {  // Überprüfen, ob der Spieler im selben Level ist
                    // Sende das erste Partikel an alle Clients im selben Level (Welt)
                    serverLevel.sendParticles(ParticleTypes.SOUL, x, y, z, 0, 0, 0, 0, 0);
                    // Sende das zweite Partikel an alle Clients im selben Level (Welt)
                    serverLevel.sendParticles(ParticleTypes.TRIAL_OMEN, x, y, z, 0, 0, 0, 0, 0);
                    // Sende das zweite Partikel an alle Clients im selben Level (Welt)
                    serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, x, y, z, 0, 0, 0, 0, 0);
                }
            }
        }

        // Optional: Ausgabe zur Bestätigung der Ausführung
        System.out.println("Schockwelle auf Server-Seite ausgelöst");
    }




    private void affectMobs(Level level, Player player, float power) {
        double maxRadius = 3.0 + power * 5.0; // Gleicher Radius wie die Schockwelle

        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(maxRadius));

        for (Mob mob : mobs) {
            // Berechne Fluchtrichtung
            Vec3 direction = mob.position().subtract(player.position()).normalize().scale(100.0); // Distanz erhöhen
            double targetX = mob.getX() + direction.x;
            double targetZ = mob.getZ() + direction.z;

            // Setze Zielposition und erhöhe Bewegungsgeschwindigkeit
            mob.getNavigation().moveTo(targetX, mob.getY(), targetZ, 1.0); // Schnelleres Weglaufen

            // Temporärer Effekt für "Angst"
            //mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1)); // Schneller laufen (10 Sekunden)
        }
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
