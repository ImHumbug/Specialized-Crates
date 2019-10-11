package me.ztowne13.customcrates.interfaces.igc.fileconfigs;

import me.ztowne13.customcrates.SpecializedCrates;
import me.ztowne13.customcrates.crates.Crate;
import me.ztowne13.customcrates.crates.CrateSettings;
import me.ztowne13.customcrates.crates.options.CRewards;
import me.ztowne13.customcrates.crates.options.ObtainType;
import me.ztowne13.customcrates.crates.types.CrateType;
import me.ztowne13.customcrates.interfaces.InventoryBuilder;
import me.ztowne13.customcrates.interfaces.InventoryUtils;
import me.ztowne13.customcrates.interfaces.igc.IGCDefaultItems;
import me.ztowne13.customcrates.interfaces.igc.IGCMenu;
import me.ztowne13.customcrates.interfaces.igc.crates.IGCCratesMain;
import me.ztowne13.customcrates.interfaces.inputmenus.InputMenu;
import me.ztowne13.customcrates.interfaces.items.DynamicMaterial;
import me.ztowne13.customcrates.interfaces.items.ItemBuilder;
import me.ztowne13.customcrates.utils.ChatUtils;
import me.ztowne13.customcrates.visuals.MaterialPlaceholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ztowne13 on 3/20/16.
 */
public class IGCMenuCrates extends IGCMenu
{
    int page;

    public IGCMenuCrates(SpecializedCrates cc, Player p, IGCMenu lastMenu)
    {
        this(cc, p, lastMenu, 1);
    }

    public IGCMenuCrates(SpecializedCrates specializedCrates, Player p, IGCMenu lastMenu, int page)
    {
        super(specializedCrates, p, lastMenu, "&7&l> &6&lCrates PG " + page);

        this.page = page;
    }

    @Override
    public void open()
    {
        int values = Crate.getLoadedCrates().keySet().size();
        int inThisInv = values - ((page - 1) * 30);

        InventoryBuilder ib = createDefault(InventoryUtils.getRowsFor(4, values, page), 18);
        ib.setItem(0, IGCDefaultItems.EXIT_BUTTON.getIb());
        ib.setItem(8,
                new ItemBuilder(Material.PAPER, 1, 0).setName("&aCreate a new crate").setLore("&7Please set the crate and")
                        .addLore("&7key once you are done configuring").addLore("&7for it to save properly."));

        ItemBuilder nextPage = new ItemBuilder(DynamicMaterial.ARROW, 1);
        nextPage.setDisplayName("&aNext Page");
        nextPage.addLore("").addAutomaticLore("&7", 30, "Click to go to the next page of crates.");

        ItemBuilder lastPage = new ItemBuilder(DynamicMaterial.ARROW, 1);
        lastPage.setDisplayName("&aPrevious Page");
        lastPage.addLore("").addAutomaticLore("&7", 30, "Click to go to the previous page of crates.");

        if(page != 1)
            ib.setItem(9, lastPage);
        if(inThisInv > 30)
            ib.setItem(18, nextPage);


        ArrayList<String> names = new ArrayList<>(Crate.getLoadedCrates().keySet());
        Collections.sort(names);

        int i = 2;
        int toSkip = ((page - 1) * 30);
        int skipped = 0;

        for (String crateName : names)
        {
            if (i % 9 == 7)
            {
                i += 4;
            }

            if(i > 54)
                break;

            if(skipped < toSkip)
            {
                skipped++;
            }
            else
            {
                Crate crate = Crate.getLoadedCrates().get(crateName);
                ib.setItem(i, new ItemBuilder(Material.CHEST, 1, 0).setName((crate.isEnabled() ? "&a" : "&c") + crateName)
                        .setLore("&7Placed crates: &f" + crate.getPlacedCount()).addLore(
                                "&7Errors: " + (crate.getCs().getSl().getFailures() == 0 ? "&f" : "&c") +
                                        crate.getCs().getSl().getFailures()));
                i++;
            }
        }

        getIb().open();
        putInMenu();
    }

    @Override
    public void manageClick(int slot)
    {
        if (slot == 0)
        {
            up();
        }
        else if (slot == 8)
        {
            new InputMenu(getCc(), getP(), "crate name", "null", "Name the crate whatever you want.", String.class, this, true);
        }
        else if (slot == 9)
        {
            new IGCMenuCrates(getCc(), getP(), getLastMenu(), page - 1).open();
        }
        else if(slot == 18)
        {
            new IGCMenuCrates(getCc(), getP(), getLastMenu(), page + 1).open();
        }
        else if (getIb().getInv().getItem(slot) != null && getIb().getInv().getItem(slot).getType() == Material.CHEST)
        {
            String name = ChatUtils.removeColor(getIb().getInv().getItem(slot).getItemMeta().getDisplayName());
            new IGCCratesMain(getCc(), getP(), this, Crate.getCrate(getCc(), name)).open();
        }
    }

    @Override
    public boolean handleInput(String value, String input)
    {
        if (value.equalsIgnoreCase("crate name"))
        {
            if (!Crate.crateAlreadyExist(input))
            {
                if (!input.contains(" "))
                {
                    Crate newCrate = new Crate(getCc(), input, true);
                    CrateSettings cs = newCrate.getCs();
                    cs.setOt(ObtainType.STATIC);
                    cs.setDcp(new MaterialPlaceholder(getCc()));
                    cs.setCt(CrateType.INV_ROULETTE);
                    cs.setRequireKey(true);
                    newCrate.setEnabled(true);
                    newCrate.setCanBeEnabled(false);

                    if (!CRewards.getAllRewards().isEmpty())
                    {
                        cs.getCr().addReward(CRewards.getAllRewards().values().iterator().next().getRewardName());
                    }

                    new IGCCratesMain(getCc(), getP(), this, newCrate).open();
                    ChatUtils.msgSuccess(getP(), "Create a new crate with the name " + input);
                    //	new InputMenu(getCc(), getP(), "crate obtain method", "null", "Available obtain methods: " + Arrays.toString(ObtainType.values()), String.class, this);
                }
                else
                {
                    ChatUtils.msgError(getP(), "Crate names cannot have spaces in their names.");
                }
            }
            else
            {
                ChatUtils.msgError(getP(), "This crate name already exists!");
            }
        }
		/*else if(value.equalsIgnoreCase("crate obtain method"))
		{
			try
			{
				ObtainType ot = ObtainType.valueOf(input.toUpperCase());
				newCrate.getCs().setOt(ot);
				ChatUtils.msgSuccess(getP(), "Set the " + value + " to " + input);
				new InputMenu(getCc(), getP(), "crate display type", "null", "Available display types: block, npc, mob", String.class, this);
			}
			catch(Exception exc)
			{
				ChatUtils.msgError(getP(), "This obtain type doesn't exist. Available options: " + Arrays.toString(ObtainType.values()));
			}
		}
		else if(value.equalsIgnoreCase("crate display type"))
		{
			if(input.equalsIgnoreCase("block"))
			{
				newCrate.getCs().setDcp(new MaterialPlaceholder(getCc()));
				ChatUtils.msgSuccess(getP(), "Set the " + value + " to " + input);
				new InputMenu(getCc(), getP(), "crate key required", "null", "Does the crate require a key to open?", String.class, this);
			}
			else if(input.equalsIgnoreCase("npc"))
			{
				newCrate.getCs().setDcp(new Citizens2NPCPlaceHolder(getCc()));
				ChatUtils.msgSuccess(getP(), "Set the " + value + " to " + input);
				new InputMenu(getCc(), getP(), "npc name", "null", "Set the name of an npc to a player", String.class, this);
			}
			else if(input.equalsIgnoreCase("mob"))
			{
				newCrate.getCs().setDcp(new MobPlaceholder(getCc()));
				ChatUtils.msgSuccess(getP(), "Set the " + value + " to " + input);
				new InputMenu(getCc(), getP(), "mob type", "null", "Available mob types: " + Arrays.toString(EntityTypes.values()), String.class, this);
			}
			else
			{
				ChatUtils.msgError(getP(), "Available display types: block, npc, mob");
			}
		}
		else if(value.equalsIgnoreCase("npc name"))
		{
			newCrate.getCs().getDcp().setType(input);
			ChatUtils.msgSuccess(getP(), "Set the " + value + " to " + input);
			new InputMenu(getCc(), getP(), "crate key required", "null", "Does the crate require a key to open?", String.class, this);
		}
		else if(value.equalsIgnoreCase("mob type"))
		{
			try
			{
				EntityTypes et = EntityTypes.valueOf(input.toUpperCase());
				newCrate.getCs().getDcp().setType(input.toUpperCase());
				ChatUtils.msgSuccess(getP(), "Set the " + value + " to " + input);
				new InputMenu(getCc(), getP(), "crate key required", "null", "Does the crate require a key to open?", String.class, this);
			}
			catch(Exception exc)
			{
				ChatUtils.msgError(getP(), input + " is not valid in the list of mobs: " + Arrays.toString(EntityTypes.values()));
			}
		}
		else if(value.equalsIgnoreCase("crate key required"))
		{
			if(Utils.isBoolean(input))
			{
				ChatUtils.msgSuccess(getP(), "Set the " + value + " to " + input);
				newCrate.getCs().setRequireKey(Boolean.valueOf(input));
				new InputMenu(getCc(), getP(), "crate animation", "null", "Available animations: " + Arrays.toString(CrateType.values()), String.class, this);
			}
			else
			{
				ChatUtils.msgError(getP(), input + " is not true / false");
			}
		}
		else if(value.equalsIgnoreCase("crate animation"))
		{
			try
			{
				CrateType ct = CrateType.valueOf(input.toUpperCase());
				newCrate.getCs().setCt(ct);
				ChatUtils.msgSuccess(getP(), "Set the " + value + " to " + input);
				new IGCCratesMain(getCc(), getP(), this, newCrate).open();
			}
			catch(Exception exc)
			{
				ChatUtils.msgError(getP(), input + " is not valid in the list of crate animations: " + Arrays.toString(CrateType.values()));
			}
		}*/
        return false;
    }
}
