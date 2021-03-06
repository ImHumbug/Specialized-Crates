package me.ztowne13.customcrates.crates.options.particles;

import me.ztowne13.customcrates.SpecializedCrates;
import me.ztowne13.customcrates.utils.LocationUtils;
import me.ztowne13.customcrates.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * Created by ztowne13 on 6/24/16.
 */
public class BukkitParticleEffect extends ParticleData {
    private final Random random;
    private Particle particle;

    public BukkitParticleEffect(SpecializedCrates sc, String particleName, String name, boolean hasAnimation) {
        this(sc, Particle.valueOf(particleName.toUpperCase()), name, hasAnimation);
    }

    public BukkitParticleEffect(SpecializedCrates sc, Particle particle, String name, boolean hasAnimation) {
        super(sc, name, hasAnimation);
        this.particle = particle;
        random = new Random();
    }

    @Override
    public void display(Location l) {
        Location centered;
        int amnt;
        float offX;
        float offY;
        float offZ;
        float speed;

        if (isHasAnimation()) {
            centered = LocationUtils.getLocationCentered(l);
            amnt = 1;
            offX = offY = offZ = 0;
            speed = 0;
        } else {
            centered = LocationUtils.getLocationCentered(l).add(getCenterX(), getCenterY(), getCenterZ());
            amnt = getAmount();
            offX = getRangeX();
            offY = getRangeY();
            offZ = getRangeZ();
            speed = getSpeed();
        }

        if ((particle.equals(Particle.SPELL_MOB) || particle.equals(Particle.SPELL_MOB_AMBIENT) ||
                particle.equals(Particle.NOTE)) && isHasColor() && isColorEnabled()) {
            // Artificial 'offset'
            if (!isHasAnimation()) {
                float randX = (random.nextFloat() >= .5 ? 1 : -1) * random.nextFloat() * offX;
                float randY = (random.nextFloat() >= .5 ? 1 : -1) * random.nextFloat() * offY;
                float randZ = (random.nextFloat() >= .5 ? 1 : -1) * random.nextFloat() * offZ;

                centered.add(randX, randY, randZ);
            }

            for (int i = 0; i < amnt; i++)
                spawnParticle(centered, 0, getColorRed() / 255.0, getColorGreen() / 255.0, getColorBlue() / 255.0, 1);
        } else
            spawnParticle(centered, amnt, offX, offY, offZ, speed);
    }

    @Override
    public boolean setParticle(String particleName) {
        try {
            particle = Particle.valueOf(particleName.toUpperCase());
            return true;
        } catch (Exception exc) {
            return false;
        }
    }

    @Override
    public String getParticleName() {
        return particle.name();
    }

    public void spawnParticle(Location centeredLoc, int amnt, double offX, double offY, double offZ, float speed) {

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Utils.isPlayerInRange(instance, player, centeredLoc)) {
                if (particle.equals(Particle.REDSTONE)) {
                    player.spawnParticle(particle, centeredLoc, amnt, offX, offY, offZ, speed, getDustOptions());
                } else {
                    player.spawnParticle(particle, centeredLoc, amnt, offX, offY, offZ, speed);
                }
            }
        }
    }
}
