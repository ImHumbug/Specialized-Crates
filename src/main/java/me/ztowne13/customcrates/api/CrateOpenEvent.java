package me.ztowne13.customcrates.api;

import me.ztowne13.customcrates.crates.Crate;
import me.ztowne13.customcrates.crates.options.rewards.Reward;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public class CrateOpenEvent extends Event
{
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private Crate crate;
    private ArrayList<Reward> rewards;
    private Player player;

    public CrateOpenEvent(Player player, ArrayList<Reward> rewards, Crate crate)
    {
        this.player = player;
        this.rewards = rewards;
        this.crate = crate;
    }

    @Override
    public HandlerList getHandlers()
    {
        return HANDLERS_LIST;
    }

    public Crate getCrate()
    {
        return crate;
    }

    public ArrayList<Reward> getRewards()
    {
        return rewards;
    }

    public Player getPlayer()
    {
        return player;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLERS_LIST;
    }
}