package me.ztowne13.customcrates.crates.types.animations.inventory;

import com.cryptomorin.xseries.XMaterial;
import me.ztowne13.customcrates.crates.Crate;
import me.ztowne13.customcrates.crates.CrateState;
import me.ztowne13.customcrates.crates.options.rewards.Reward;
import me.ztowne13.customcrates.crates.options.sounds.SoundData;
import me.ztowne13.customcrates.crates.types.animations.AnimationDataHolder;
import me.ztowne13.customcrates.crates.types.animations.CrateAnimationType;
import me.ztowne13.customcrates.interfaces.InventoryBuilder;
import me.ztowne13.customcrates.interfaces.items.ItemBuilder;
import me.ztowne13.customcrates.interfaces.logging.StatusLogger;
import me.ztowne13.customcrates.interfaces.logging.StatusLoggerEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ztowne13 on 7/7/16.
 * <p>
 * inv-name: '&8&l> &6&l%crate%'
 * tick-sound: BLOCK_STONE_HIT, 5, 5
 * click-sound: ENTITY_HORSE_GALLOP, 5, 5
 * uncover-sound: ENTITY_PLAYER_LEVELUP, 5, 5
 * minimum-rewards: 1
 * maximum-rewards: 4
 * count: true
 * random-display-duration: 50
 * uncover-block: CHEST;0
 */
public class DiscoverAnimation extends InventoryCrateAnimation {
    private final Random random = new Random();
    private SoundData clickSound;
    private SoundData uncoverSound;
    private int minRewards;
    private int maxRewards;
    private int shuffleDisplayDuration;
    private int invRows;
    private String coverBlockName = "&aReward #%numbetsorr%";
    private String coverBlockLore = "&7You have &f%remaining-clicks% rewards to chose from.";
    private String rewardBlockName = "&aReward";
    private String rewardBlockUnlockName = "&aClick me to unlock your reward.";
    private String rewardBlockWaitingName = "&aUncover all rewards to unlock";
    private ItemBuilder uncoverBlock;
    private ItemBuilder rewardBlock;
    private boolean count;

    public DiscoverAnimation(Crate crate) {
        super(crate, CrateAnimationType.INV_DISCOVER);
    }

    @Override
    public void tickInventory(InventoryAnimationDataHolder dataHolder, boolean update) {
        DiscoverAnimationDataHolder dadh = (DiscoverAnimationDataHolder) dataHolder;

        switch (dadh.getCurrentState()) {
            case PLAYING:
                drawFillers(dadh, 1);
                updateUncoverTiles(dadh);
                drawUncoverTiles(dadh);
                break;
            case WAITING:
                drawFillers(dadh, 1);
                drawUncoverTiles(dadh);
                break;
            case SHUFFLING:
                drawFillers(dadh, 1);
                updateShufflingTiles(dadh);
                drawShufflingTiles(dadh);
                break;
            case UNCOVERING:
                drawFillers(dadh, 1);
                updateWinningTiles(dadh);
                drawWinningTiles(dadh);
                break;
            case ENDING:
                drawFillers(dadh, 1);
                drawWinningTiles(dadh);
                break;
            default:
                break;
        }
    }

    @Override
    public void drawIdentifierBlocks(InventoryAnimationDataHolder inventoryAnimationDataHolder) {
        // EMPTY
    }

    @Override
    public ItemBuilder getFiller() {
        return new ItemBuilder(XMaterial.AIR);
    }

    @Override
    public boolean updateTicks(AnimationDataHolder dataHolder) {
        DiscoverAnimationDataHolder dadh = (DiscoverAnimationDataHolder) dataHolder;

        switch (dadh.getCurrentState()) {
            case WAITING:
            case ENDING:
                dadh.setWaitingTicks(dataHolder.getWaitingTicks() + 1);
                break;
            case SHUFFLING:
                dadh.setShuffleTicks(dadh.getShuffleTicks() + (int) BASE_SPEED);
                break;
            default:
                break;
        }

        return false;
    }

    @Override
    public void checkStateChange(AnimationDataHolder dataHolder, boolean update) {
        DiscoverAnimationDataHolder dadh = (DiscoverAnimationDataHolder) dataHolder;

        switch (dadh.getCurrentState()) {
            case PLAYING:
                if (dadh.getRemainingClicks() <= 0) {
                    dadh.setCurrentState(AnimationDataHolder.State.WAITING);
                }
                break;
            case WAITING:
                if (dadh.getWaitingTicks() == 20) {
                    dadh.setWaitingTicks(0);
                    dadh.setCurrentState(AnimationDataHolder.State.SHUFFLING);
                }
                break;
            case SHUFFLING:
                if (dadh.getShuffleTicks() > getShuffleDisplayDuration()) {
                    dadh.setCurrentState(AnimationDataHolder.State.UNCOVERING);
                }
                break;
            case UNCOVERING:
                if (dadh.getAlreadyDisplayedRewards().keySet().size() == dadh.getAlreadyChosenSlots().size()) {
                    dadh.setCurrentState(AnimationDataHolder.State.ENDING);
                }
                break;
            case ENDING:
                if (dadh.getWaitingTicks() == 50) {
                    dadh.setCurrentState(AnimationDataHolder.State.COMPLETED);
                }
                break;
            default:
                break;
        }
    }

    public void drawUncoverTiles(DiscoverAnimationDataHolder dadh) {
        InventoryBuilder inventoryBuilder = dadh.getInventoryBuilder();

        ItemBuilder uncoverBlockIb = uncoverBlock.setLore("")
                .addLore(coverBlockLore.replace("%remaining-clicks%", dadh.getRemainingClicks() + ""));
        ItemBuilder alreadyUncoveredIb = rewardBlock;
        alreadyUncoveredIb.setDisplayName(rewardBlockWaitingName);

        for (int i = 0; i < inventoryBuilder.getSize(); i++) {
            if (dadh.getAlreadyChosenSlots().contains(i)) {
                inventoryBuilder.setItem(i, alreadyUncoveredIb);
            } else {
                uncoverBlockIb.setDisplayName(coverBlockName.replace("%number%", (i + 1) + ""));
                if (count) {
                    uncoverBlockIb.getStack().setAmount(i + 1);
                }
                inventoryBuilder.setItem(i, uncoverBlockIb);
            }
        }
    }

    public void updateUncoverTiles(DiscoverAnimationDataHolder dadh) {
        for (int slot : dadh.getClickedSlots()) {
            if (!dadh.getAlreadyChosenSlots().contains(slot) && dadh.getRemainingClicks() != 0) {
                dadh.getAlreadyChosenSlots().add(slot);
                dadh.setRemainingClicks(dadh.getRemainingClicks() - 1);

                if (clickSound != null)
                    clickSound.playTo(dadh.getPlayer(), dadh.getLocation());
            }
        }

        dadh.getClickedSlots().clear();
    }

    public void drawShufflingTiles(DiscoverAnimationDataHolder dadh) {
        ItemBuilder reward = rewardBlock;
        reward.setDisplayName(rewardBlockName);

        for (int i = 0; i < dadh.getShufflingTiles().size(); i++) {
            dadh.getInventoryBuilder().setItem(dadh.getShufflingTiles().get(i), reward);
        }
    }

    public void updateShufflingTiles(DiscoverAnimationDataHolder dadh) {
        dadh.getShufflingTiles().clear();

        for (int i = 0; i < dadh.getInventoryBuilder().getSize(); i++) {
            if (random.nextInt(7) == 1) {
                dadh.getShufflingTiles().add(i);
            }
        }
    }

    public void drawWinningTiles(DiscoverAnimationDataHolder dadh) {
        InventoryBuilder inventoryBuilder = dadh.getInventoryBuilder();

        ItemBuilder reward = rewardBlock;
        reward.setDisplayName(rewardBlockUnlockName);

        for (int i : dadh.getAlreadyChosenSlots()) {
            if (dadh.getAlreadyDisplayedRewards().containsKey(i)) {
                inventoryBuilder.setItem(i, dadh.getAlreadyDisplayedRewards().get(i).getDisplayBuilder());
            } else {
                inventoryBuilder.setItem(i, reward);
            }
        }
    }

    public void updateWinningTiles(DiscoverAnimationDataHolder dadh) {
        for (int slot : dadh.getClickedSlots()) {
            if (dadh.getAlreadyChosenSlots().contains(slot) && !dadh.getAlreadyDisplayedRewards().containsKey(slot)) {
                Reward newR = getCrate().getSettings().getReward().getRandomReward();

                if (uncoverSound != null)
                    uncoverSound.playTo(dadh.getPlayer(), dadh.getLocation());

                dadh.getAlreadyDisplayedRewards().put(slot, newR);
            }
        }

        dadh.getClickedSlots().clear();
    }

    @Override
    public void endAnimation(AnimationDataHolder dataHolder) {
        DiscoverAnimationDataHolder dadh = (DiscoverAnimationDataHolder) dataHolder;
        Player player = dadh.getPlayer();

        ArrayList<Reward> rewards = new ArrayList<>(dadh.getAlreadyDisplayedRewards().values());

        finishAnimation(player, rewards, null);
        getCrate().tick(dadh.getLocation(), CrateState.OPEN, player, rewards);
    }

    @Override
    public void loadDataValues(StatusLogger statusLogger) {
        FileConfiguration fc = getFileHandler().get();

        invName = fc.getString(prefix + "inv-name");

        invRows = fileHandler.getFileDataLoader()
                .loadInt(prefix + "inventory-rows", 3, statusLogger, StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_INVROWS_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_INVROWS_INVALID);

        minRewards = fileHandler.getFileDataLoader()
                .loadInt(prefix + "minimum-rewards", 1, statusLogger, StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_MINREWARDS_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_MINREWARDS_INVALID);

        maxRewards = fileHandler.getFileDataLoader()
                .loadInt(prefix + "maximum-rewards", 1, statusLogger, StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_MAXREWARDS_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_MAXREWARDS_INVALID);

        shuffleDisplayDuration = fileHandler.getFileDataLoader()
                .loadInt(prefix + "random-display-duration", 1, statusLogger, StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_RANDDISPLAYLOCATION_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_RANDDISPLAYLOCATION_INVALID);

        uncoverBlock =
                fileHandler.getFileDataLoader().loadItem(prefix + "cover-block", new ItemBuilder(XMaterial.CHEST), statusLogger,
                        StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_COVERBLOCK_INVALID
                );

        count = fileHandler.getFileDataLoader().loadBoolean(prefix + "count", true, statusLogger, StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                StatusLoggerEvent.ANIMATION_DISCOVER_COUNT_SUCCESS,
                StatusLoggerEvent.ANIMATION_DISCOVER_COUNT_INVALID);

        tickSound =
                fileHandler.getFileDataLoader().loadSound(prefix + "tick-sound", statusLogger, StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_TICKSOUND_SOUND_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_TICKSOUND_SOUND_FAILURE,
                        StatusLoggerEvent.ANIMATION_DISCOVER_TICKSOUND_VOLUME_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_TICKSOUND_VOLUME_INVALID,
                        StatusLoggerEvent.ANIMATION_DISCOVER_TICKSOUND_PITCHVOL_INVALID,
                        StatusLoggerEvent.ANIMATION_DISCOVER_TICKSOUND_PITCH_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_TICKSOUND_PITCH_INVALID);

        clickSound =
                fileHandler.getFileDataLoader().loadSound(prefix + "click-sound", statusLogger, StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_CLICKSOUND_SOUND_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_CLICKSOUND_SOUND_FAILURE,
                        StatusLoggerEvent.ANIMATION_DISCOVER_CLICKSOUND_VOLUME_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_CLICKSOUND_VOLUME_INVALID,
                        StatusLoggerEvent.ANIMATION_DISCOVER_CLICKSOUND_PITCHVOL_INVALID,
                        StatusLoggerEvent.ANIMATION_DISCOVER_CLICKSOUND_PITCH_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_CLICKSOUND_PITCH_INVALID);

        uncoverSound =
                fileHandler.getFileDataLoader().loadSound(prefix + "uncover-sound", statusLogger, StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_UNCOVERSOUND_SOUND_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_UNCOVERSOUND_SOUND_FAILURE,
                        StatusLoggerEvent.ANIMATION_DISCOVER_UNCOVERSOUND_VOLUME_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_UNCOVERSOUND_VOLUME_INVALID,
                        StatusLoggerEvent.ANIMATION_DISCOVER_UNCOVERSOUND_PITCHVOL_INVALID,
                        StatusLoggerEvent.ANIMATION_DISCOVER_UNCOVERSOUND_PITCH_SUCCESS,
                        StatusLoggerEvent.ANIMATION_DISCOVER_UNCOVERSOUND_PITCH_INVALID);

        rewardBlock = fileHandler.getFileDataLoader()
                .loadItem(prefix + "reward-block", new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE), statusLogger,
                        StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_REWARDBLOCK_INVALID
                );
        rewardBlock.setDisplayName("");

        coverBlockName = fileHandler.getFileDataLoader()
                .loadString(prefix + "cover-block-name", getStatusLogger(), StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_COVER_BLOCK_NAME_SUCCESS);

        coverBlockLore = fileHandler.getFileDataLoader()
                .loadString(prefix + "cover-block-lore", getStatusLogger(), StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_COVER_BLOCK_LORE_SUCCESS);

        rewardBlockName = fileHandler.getFileDataLoader()
                .loadString(prefix + "reward-block-name", getStatusLogger(), StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                        StatusLoggerEvent.ANIMATION_DISCOVER_REWARD_BLOCK_NAME_SUCCESS);

        rewardBlockUnlockName = fileHandler.getFileDataLoader().loadString(prefix + "reward-block-unlock-name", getStatusLogger(),
                StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                StatusLoggerEvent.ANIMATION_DISCOVER_REWARD_BLOCK_UNBLOCK_NAME_SUCCESS);

        rewardBlockWaitingName = fileHandler.getFileDataLoader().loadString(prefix + "reward-block-waiting-name", getStatusLogger(),
                StatusLoggerEvent.ANIMATION_VALUE_NONEXISTENT,
                StatusLoggerEvent.ANIMATION_DISCOVER_REWARD_BLOCK_WAITING_NAME_SUCCESS);
    }

    @Override
    public String getInvName() {
        return invName;
    }

    @Override
    public void setInvName(String invName) {
        this.invName = invName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public SoundData getTickSound() {
        return tickSound;
    }

    @Override
    public void setTickSound(SoundData tickSound) {
        this.tickSound = tickSound;
    }


    public int getMinRewards() {
        return minRewards;
    }

    public int getMaxRewards() {
        return maxRewards;
    }

    public int getShuffleDisplayDuration() {
        return shuffleDisplayDuration;
    }

    public boolean isCount() {
        return count;
    }

    public void setCount(boolean count) {
        this.count = count;
    }

    public int getInvRows() {
        return invRows;
    }
}
