package mcjty.rftools;

import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

import java.util.ArrayList;
import java.util.List;

public class Achievements {
    public static AchievementPage page;

    public static Achievement theFirstStep;
    public static Achievement researching;
    public static Achievement scrambled;
    public static Achievement smallBits;
    public static Achievement dimletMaster;
    public static Achievement firstDimension;
    public static Achievement firstTeleport;
    public static Achievement hardPower;
    public static Achievement clearVision;
    public static Achievement specialOres;

    public static void init() {
        List<Achievement> achievements = new ArrayList<Achievement>();

        theFirstStep = new Achievement("achievement.theFirstStep", "theFirstStep", 0, 0, new ItemStack(ModItems.unknownDimlet), null).registerStat();
        achievements.add(theFirstStep);

        researching = new Achievement("achievement.researching", "researching", 2, 2, new ItemStack(ModBlocks.dimletResearcherBlock), theFirstStep).registerStat();
        achievements.add(researching);

        smallBits = new Achievement("achievement.smallBits", "smallBits", 3, 3, new ItemStack(ModItems.dimletBaseItem), researching).registerStat();
        achievements.add(smallBits);

        dimletMaster = new Achievement("achievement.dimletMaster", "dimletMaster", 4, 3, new ItemStack(ModBlocks.dimletWorkbenchBlock), smallBits).registerStat();
        achievements.add(dimletMaster);

        scrambled = new Achievement("achievement.scrambled", "scrambled", 3, 1, new ItemStack(ModBlocks.dimensionEnscriberBlock), researching).registerStat();
        achievements.add(scrambled);

        firstDimension = new Achievement("achievement.firstDimension", "firstDimension", 0, 4, new ItemStack(ModItems.realizedDimensionTab), researching).registerStat();
        achievements.add(firstDimension);

        firstTeleport = new Achievement("achievement.firstTeleport", "firstTeleport", 0, -2, new ItemStack(ModBlocks.matterTransmitterBlock), null).registerStat();
        achievements.add(firstTeleport);

        hardPower = new Achievement("achievement.hardPower", "hardPower", 2, -2, new ItemStack(ModBlocks.endergenicBlock), null).registerStat();
        achievements.add(hardPower);

        clearVision = new Achievement("achievement.clearVision", "clearVision", 4, -2, new ItemStack(ModBlocks.screenBlock), null).registerStat();
        achievements.add(clearVision);

        specialOres = new Achievement("achievement.specialOres", "specialOres", -2, 4, new ItemStack(ModItems.dimensionalShard), firstDimension).registerStat();
        achievements.add(specialOres);

        page = new AchievementPage("RfTools", achievements.toArray(new Achievement[achievements.size()]));
        AchievementPage.registerAchievementPage(page);
    }

    public static void trigger(EntityPlayer player, Achievement achievement) {
        if (achievement.parentAchievement != null) {
            trigger(player, achievement.parentAchievement);
        }
        player.triggerAchievement(achievement);
    }
}
