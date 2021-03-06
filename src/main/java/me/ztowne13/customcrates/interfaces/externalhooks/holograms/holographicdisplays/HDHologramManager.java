package me.ztowne13.customcrates.interfaces.externalhooks.holograms.holographicdisplays;

import me.ztowne13.customcrates.SpecializedCrates;
import me.ztowne13.customcrates.interfaces.externalhooks.holograms.Hologram;
import me.ztowne13.customcrates.interfaces.externalhooks.holograms.HologramManager;
import org.bukkit.Location;

public class HDHologramManager extends HologramManager {
    public HDHologramManager(SpecializedCrates instance) {
        super(instance);
    }

    @Override
    public Hologram newHologram(Location location) {
        return new HDHologram(instance, location);
    }
}
