package me.ztowne13.customcrates.crates.types.animations.block;

import me.ztowne13.customcrates.utils.ReflectionUtilities;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class NMSChestState {

    public void playChestAction(Block chest, boolean open) {
        try {
            Location location = chest.getLocation();

            Class<?> craftWorldClass = ReflectionUtilities.getOBCClass("CraftWorld");
            Object craftWorld = craftWorldClass.cast(location.getWorld());
            Object world = ReflectionUtilities.getHandle(craftWorld);

            Class<?> blockPositionClass = ReflectionUtilities.getNMSClass("BlockPosition");
            Object blockPosition = blockPositionClass.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE)
                    .newInstance(location.getX(), location.getY(), location.getZ());

            Object iBlockData =
                    ReflectionUtilities.getMethod(world.getClass(), "getType", blockPosition.getClass())
                            .invoke(world, blockPosition);
            Object block =
                    ReflectionUtilities.getMethod(iBlockData.getClass(), "getBlock").invoke(iBlockData);


            ReflectionUtilities.getMethod(world.getClass(), "playBlockAction",
                    blockPosition.getClass(), ReflectionUtilities.getNMSClass("Block"), Integer.TYPE,
                    Integer.TYPE).invoke(world, blockPosition, block, 1, open ? 1 : 0);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

}
