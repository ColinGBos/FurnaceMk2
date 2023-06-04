package vapourdrive.furnacemk2.furnace;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.vapourware.shared.base.AbstractBaseMachineScreen;
import vapourdrive.vapourware.shared.utils.DeferredComponent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FurnaceMk2Screen extends AbstractBaseMachineScreen<FurnaceMk2Container> {
    private final FurnaceMk2Container container;
    final static int COOK_BAR_XPOS = 67;
    final static  int COOK_BAR_YPOS = 21;
    final static  int COOK_BAR_ICONX = 184;   // texture position of white arrow icon [u,v]
    final static  int COOK_BAR_ICONY = 24;
    final static  int COOK_BAR_WIDTH = 24;
    final static  int COOK_BAR_HEIGHT = 16;

    final static  int EXP_XPOS = 94;
    final static  int EXP_YPOS = 56;
    final static  int EXP_ICONX = 184;   // texture position of flame icon [u,v]
    final static  int EXP_ICONY = 41;
    final static  int EXP_HEIGHT = 10;
    final static  int EXP_WIDTH = 49;

    DecimalFormat exp_f = new DecimalFormat("#,###.##");

    public FurnaceMk2Screen(FurnaceMk2Container container, Inventory inv, Component name) {
        super(container, inv, name, new DeferredComponent(FurnaceMk2.MODID, "furnacemk2"), 33, 17, 52, 158, 6, 1, true);
        this.container = container;
    }

    @Override
    protected void renderBg(@NotNull PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        //Draw the cooking progress (arrow)
        int l = (int)(container.getCookProgress() * COOK_BAR_WIDTH);
        this.blit(matrixStack, this.leftPos + COOK_BAR_XPOS, this.topPos + COOK_BAR_YPOS, COOK_BAR_ICONX, COOK_BAR_ICONY, l + 1, COOK_BAR_HEIGHT);

        //Draw the currently stored experience
        int k = (int)(container.getExperiencePercentage() * EXP_WIDTH);
        this.blit(matrixStack, this.leftPos + EXP_XPOS, this.topPos + EXP_YPOS, EXP_ICONX, EXP_ICONY, k, EXP_HEIGHT);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderTooltip(matrixStack, mouseX, mouseY);
        boolean notCarrying = this.menu.getCarried().isEmpty();

        List<Component> hoveringText = new ArrayList<>();

        // If the mouse is over the progress bar add the progress bar hovering text
        if (notCarrying && isInRect(this.leftPos + COOK_BAR_XPOS, this.topPos + COOK_BAR_YPOS, COOK_BAR_WIDTH, COOK_BAR_HEIGHT, mouseX, mouseY)){
            if(container.getCookProgress() > 0) {
                int cookPercentage = (int) (container.getCookProgress() * 100);
                hoveringText.add(Component.literal("Progress: ").append(cookPercentage + "%"));
            }
        }

        // If the mouse is over the experience bar, add hovering text
        if (notCarrying && isInRect(this.leftPos + EXP_XPOS, this.topPos + EXP_YPOS, EXP_WIDTH, EXP_HEIGHT, mouseX, mouseY)){
            float experience = (float)container.getExperienceStored()/100.0f;
            hoveringText.add(Component.literal("Exp: ").append(exp_f.format(experience)+"/"+exp_f.format(container.getMaxExp()/100)));
        }

        // If hoveringText is not empty draw the hovering text.  Otherwise, use vanilla to render tooltip for the slots
        if (!hoveringText.isEmpty()){
            renderComponentTooltip(matrixStack, hoveringText, mouseX, mouseY);
        }
    }

    @Override
    protected void getAdditionalInfoHover(List<Component> hoveringText) {
        super.getAdditionalInfoHover(hoveringText);
        hoveringText.add(Component.translatable(comp.getMod()+comp.getTail()+".wrench").withStyle(ChatFormatting.GOLD));
    }

}
