package mcjty.rftools.dimension;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;

public class DamageSourcePowerLow extends DamageSource {
    public DamageSourcePowerLow(String damageType) {
        super(damageType);
        setDamageBypassesArmor();
        setDamageIsAbsolute();
    }

    @Override
    public IChatComponent func_151519_b(EntityLivingBase livingBase) {
        String s = "death.dimension.powerfailure";
        return new ChatComponentTranslation(s, livingBase.func_145748_c_());
    }
}
