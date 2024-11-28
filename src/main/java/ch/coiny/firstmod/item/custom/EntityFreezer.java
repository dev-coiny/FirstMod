package ch.coiny.firstmod.item.custom;

import ch.coiny.firstmod.fearbehavior.FearUtils;
import ch.coiny.firstmod.util.MobTickEventHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        if (level.isClientSide()) {
            return;  // Verhindert, dass der Code auf der Client-Seite ausgeführt wird
        }

        ServerLevel serverLevel = (ServerLevel) level;

        // Maximale Reichweite der Schockwelle basierend auf dem Power-Wert
        double maxRadius = 3.0 + power * 5.0;
        int particleCount = (int) (400 * (power * 3));  // Anzahl der Partikel in der Schockwelle

        // Zeit, wie lange die Kugel bestehen soll: 1 Sekunde für Wachstum + 5 Sekunden für Bestehen
        int growthDuration = 20;  // Dauer des Wachstums in Ticks (1 Sekunde)
        int maxDuration = 1000;    // Dauer, wie lange die Kugel ihre maximale Größe behält (5 Sekunden)

        // Gesamtzeit, die Partikel erzeugt werden (1 Sekunde Wachstum + 5 Sekunden Stabilität)
        int totalDuration = growthDuration + maxDuration;

        // Speichern der ursprünglichen Position des Spielers, um die Schockwelle an diesem Punkt zu behalten
        double initialX = player.getX();
        double initialY = player.getY();
        double initialZ = player.getZ();

        final int[] tickCounter = {0};

        // Registrieren eines Event-Handlers, um die Partikel in jedem Tick zu erzeugen
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onServerTick(TickEvent.ServerTickEvent event) {
                if (tickCounter[0] < totalDuration) {
                    // Berechne den aktuellen Radius
                    double currentRadius;
                    if (tickCounter[0] < growthDuration) {
                        currentRadius = maxRadius * (tickCounter[0] / (double) growthDuration);
                    } else {
                        currentRadius = maxRadius;
                    }

                    // Generiere Partikel
                    for (int i = 0; i < particleCount; i++) {
                        // Erzeuge zufällige Richtung auf der Kugeloberfläche
                        double theta = Math.random() * Math.PI;  // Vertikaler Winkel [0, PI]
                        double phi = Math.random() * 2 * Math.PI;  // Horizontaler Winkel [0, 2*PI]

                        // Berechne die Position der Partikel auf der Kugeloberfläche
                        double x = initialX + currentRadius * Math.sin(theta) * Math.cos(phi);  // X-Position
                        double y = initialY + currentRadius * Math.cos(theta);                    // Y-Position
                        double z = initialZ + currentRadius * Math.sin(theta) * Math.sin(phi);  // Z-Position

                        // Alle Spieler im Server-Level erreichen und Partikel für sie erzeugen
                        for (Player otherPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
                            if (otherPlayer.level() == serverLevel) {  // Überprüfen, ob der Spieler im selben Level ist
                                // Sende Partikel an alle Clients im selben Level (Welt)
                                serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 0, 0, 0, 0, 0); // Silbrige Partikel
                            }
                        }
                    }

                    // Erhöhe den Tick-Zähler
                    tickCounter[0]++;
                } else {
                    // Entferne den Event-Handler, wenn die Schockwelle abgeschlossen ist
                    MinecraftForge.EVENT_BUS.unregister(this);
                }
            }
        });
    }







    private void affectMobs(Level level, Player player, float power) {
        double maxRadius = 3.0 + power * 5.0; // Gleicher Radius wie die Schockwelle

        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(maxRadius));

        for (Mob mob : mobs) {
            //mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1)); // Schneller laufen (10 Sekunden)

            MobTickEventHandler.disableMobTick(mob, 10000);
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
