package vapourdrive.furnacemk2.items;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import vapourdrive.furnacemk2.config.ConfigSettings;
import vapourdrive.furnacemk2.setup.ModSetup;
import vapourdrive.furnacemk2.utils.ExperienceUtils;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;

public class ItemCrystal extends Item implements IExperienceStorage{
    public static final String TAG_EXPERIENCE = "FurnaceMK2.Crystal.Experience";

    public ItemCrystal() {
        super(new Item.Properties().stacksTo(1).tab(ModSetup.ITEM_GROUP));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int storedXP = getCurrentExperienceStored(stack);

        if(player.isShiftKeyDown() && storedXP < getMaxExperienceStored(stack)) {
            int xpToStore = 0;
            int xpForCurrentLevel = ExperienceUtils.getExperienceForLevel(player.experienceLevel);
            xpToStore = ExperienceUtils.getPlayerXP(player) - xpForCurrentLevel;

            //player has exactly x > 0 levels (xp bar looks empty)
            if(xpToStore == 0 && player.experienceLevel > 0) {
                xpToStore = xpForCurrentLevel - ExperienceUtils.getExperienceForLevel(player.experienceLevel - 1);
            }

            if(xpToStore == 0) {
                return new InteractionResultHolder<>(InteractionResult.PASS, stack);
            }

            int actuallyStored = receiveExperience(stack, xpToStore, false); //store as much XP as possible

            //negative value removes xp
            if(actuallyStored > 0) {
                ExperienceUtils.addPlayerXP(player, -actuallyStored);
            }

            if(world.isClientSide()) {
                world.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.05F, getPitch(world.getRandom(), stack));
            }

            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        else if(!player.isShiftKeyDown() && storedXP > 0) {
            int xpForPlayer = ExperienceUtils.getExperienceForLevel(player.experienceLevel + 1) - ExperienceUtils.getPlayerXP(player);
            //if retrievalPercentage is 75%, these 75% should be given to the player, but an extra 25% needs to be removed from the tome
            //using floor to be generous towards the player, removing slightly less xp than should be removed (can't be 100% accurate, because XP is saved as an int)
            int xpToRetrieve = (int)Math.floor(xpForPlayer);
            int actuallyRemoved = extractExperience(stack, xpToRetrieve, false);

            //if the tome had less xp than the player should get, apply the XP loss to that value as well
            if(actuallyRemoved < xpForPlayer) {
                xpForPlayer = (int) Math.floor(actuallyRemoved);
            }

            ExperienceUtils.addPlayerXP(player, xpForPlayer);

            //picking up XP orbs creates a sound already, so only play a sound when XP is retrieved directly
            if(world.isClientSide()) {
                world.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.05F, getPitch(world.getRandom(), stack));
            }
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    public float getPitch (RandomSource rand, ItemStack stack) {
        return 0.4F + (rand.nextFloat() - rand.nextFloat()) * 0.1F + ((float)this.getCurrentExperienceStored(stack)/(float)this.getMaxExperienceStored(stack)) * 0.5F;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getMaxExperienceStored(ItemStack stack) {
        return ConfigSettings.CRYSTAL_EXPERIENCE_STORAGE.get();
    }

    @Override
    public int getCurrentExperienceStored(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_EXPERIENCE);
    }

    @Override
    public int extractExperience(ItemStack stack, int maxExtract, boolean simulate) {
        int experience = getCurrentExperienceStored(stack);
        int experienceExtracted = Math.min(experience, maxExtract);
        if (!simulate) {
            experience -= experienceExtracted;
            stack.getOrCreateTag().putInt(TAG_EXPERIENCE, experience);
        }
        return experienceExtracted;
    }

    @Override
    public int receiveExperience(ItemStack stack, int maxReceive, boolean simulate) {
        int experience = getCurrentExperienceStored(stack);
        int experienceReceived = Math.min(getMaxExperienceStored(stack) - experience, maxReceive);
        if (!simulate) {
            experience += experienceReceived;
            stack.getOrCreateTag().putInt(TAG_EXPERIENCE, experience);
        }
        return experienceReceived;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.round(((float)getCurrentExperienceStored(stack) / (float)getMaxExperienceStored(stack))*13);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        float f = Math.max(0.0f, ((float)getMaxExperienceStored(stack) - (float)getCurrentExperienceStored(stack)) / (float)getMaxExperienceStored(stack));
        return Mth.hsvToRgb(0.4f, 1.0f-f, 1.0f-f/2.0f);
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return getCurrentExperienceStored(stack) > 0;
    }


    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        return false;
    }

    @Override
    public boolean isRepairable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, Level world, List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("message.crystal_gem_item.1").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("message.crystal_gem_item.2").withStyle(ChatFormatting.BLUE));
        DecimalFormat df = new DecimalFormat("#,###");
        tooltip.add(Component.translatable("message.crystal_gem_item.3").append(df.format(this.getCurrentExperienceStored(stack)) + "/" + df.format(this.getMaxExperienceStored(stack))).withStyle(ChatFormatting.GREEN));
    }
}
