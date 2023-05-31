package vapourdrive.furnacemk2.items;

import net.minecraft.world.item.ItemStack;

public interface IExperienceStorage {
    int getMaxExperienceStored(ItemStack stack);
    int getCurrentExperienceStored(ItemStack stack);
    int extractExperience(ItemStack stack, int maxExtract, boolean simulate);
    int receiveExperience(ItemStack stack, int maxReceive, boolean simulate);
}
