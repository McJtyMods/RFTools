package mcjty.rftools.apideps;

import mcjty.rftools.RFTools;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class WrenchChecker {

    private static List<Class> wrenchClasses=new ArrayList<Class>();

    public static void init() {
        for (String className : new String[] {
                /*
                 * Can add or remove class names here
                 * Use API interface names where possible, in case of refactoring
                 * note that many mods implement BC wrench API iff BC is installed
                 * and include no wrench API of their own - we use implementation
                 * classes here to catch these cases.
                */
                "buildcraft.api.tools.IToolWrench",             //Buildcraft
                "resonant.core.content.ItemScrewdriver",        //Resonant Induction
                "ic2.core.item.tool.ItemToolWrench",            //IC2
                "ic2.core.item.tool.ItemToolWrenchElectric",    //IC2 (more)
                "mrtjp.projectred.api.IScrewdriver",            //Project Red
                "mods.railcraft.api.core.items.IToolCrowbar",   //Railcraft
                "com.bluepowermod.items.ItemScrewdriver",       //BluePower
                "cofh.api.item.IToolHammer",                    //Thermal Expansion and compatible
                "appeng.items.tools.quartz.ToolQuartzWrench",   //Applied Energistics
                "crazypants.enderio.api.tool.ITool",            //Ender IO
                "mekanism.api.IMekWrench",                      //Mekanism
                "pneumaticCraft.common.item.ItemPneumaticWrench",
                "powercrystals.minefactoryreloaded.api.IToolHammer"

        }) {
            try {
                wrenchClasses.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                RFTools.log("Failed to load wrench class " + className + " (this is not an error)");
            }
        }
    }

    public static boolean isAWrench(Item item) {
        for (Class c : wrenchClasses) {
            if (item.getClass().isAssignableFrom(c)) {
                return true;
            }
        }
        return false;
    }

}
