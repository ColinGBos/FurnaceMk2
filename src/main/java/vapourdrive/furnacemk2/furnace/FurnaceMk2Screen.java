package vapourdrive.furnacemk2.furnace;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.furnace.slots.AbstractFurnaceMk2Slot;
import vapourdrive.furnacemk2.furnace.slots.SlotCore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FurnaceMk2Screen extends AbstractContainerScreen<FurnaceMk2Container> {
    private final FurnaceMk2Container container;

    private final ResourceLocation GUI = new ResourceLocation(FurnaceMk2.MODID, "textures/gui/furnacemk2_gui_alt.png");

    final static int COOK_BAR_XPOS = 67;
    final static  int COOK_BAR_YPOS = 16;
    final static  int COOK_BAR_ICONX = 176;   // texture position of white arrow icon [u,v]
    final static  int COOK_BAR_ICONY = 14;
    final static  int COOK_BAR_WIDTH = 24;
    final static  int COOK_BAR_HEIGHT = 16;

    final static  int EXP_XPOS = 93;
    final static  int EXP_YPOS = 55;
    final static  int EXP_ICONX = 176;   // texture position of flame icon [u,v]
    final static  int EXP_ICONY = 31;
    final static  int EXP_HEIGHT = 12;
    final static  int EXP_WIDTH = 51;

    final static  int FUEL_XPOS = 35;
    final static  int FUEL_YPOS = 17;
    final static  int FUEL_ICONX = 177;   // texture position of flame icon [u,v]
    final static  int FUEL_ICONY = 44;
    final static  int FUEL_HEIGHT = 52;
    final static  int FUEL_WIDTH = 7;

    DecimalFormat df = new DecimalFormat("#,###");

    public FurnaceMk2Screen(FurnaceMk2Container container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
    }

    @Override
    public void render(@NotNull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull PoseStack matrixStack, int mouseX, int mouseY) {
        if(FurnaceMk2.debugMode) {
            int horStart = -100;
            drawString(matrixStack, Minecraft.getInstance().font, "Cook: " + menu.getFurnaceData(0), horStart, 30, 0xffffff);
            drawString(matrixStack, Minecraft.getInstance().font, "MaxCook: " + menu.getFurnaceData(1), horStart, 40, 0xffffff);
            drawString(matrixStack, Minecraft.getInstance().font, "Exp: " + menu.getFurnaceData(2), horStart, 50, 0xffffff);
            drawString(matrixStack, Minecraft.getInstance().font, String.format("Cooking: %.2f", menu.getCookProgress()), horStart, 60, 0xffffff);
        }
        super.renderLabels(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        int guiLeft = this.leftPos;
        int guiTop = this.topPos;

        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

        //Draw the cooking progress (arrow)
        int l = (int)(container.getCookProgress() * COOK_BAR_WIDTH);
        this.blit(matrixStack, guiLeft + COOK_BAR_XPOS, guiTop + COOK_BAR_YPOS, COOK_BAR_ICONX, COOK_BAR_ICONY, l + 1, COOK_BAR_HEIGHT);

        //Draw the currently stored experience
        int k = (int)(container.getExperiencePercentage() * EXP_WIDTH);
        this.blit(matrixStack, guiLeft + EXP_XPOS, guiTop + EXP_YPOS, EXP_ICONX, EXP_ICONY, k + 1, EXP_HEIGHT);

        int m = (int)(container.getFuelPercentage() * (FUEL_HEIGHT));
        this.blit(matrixStack, guiLeft+FUEL_XPOS, guiTop+FUEL_YPOS+FUEL_HEIGHT-m, FUEL_ICONX, FUEL_ICONY+FUEL_HEIGHT-m, FUEL_WIDTH, m);

    }

    @Override
    protected void renderTooltip(@NotNull PoseStack matrixStack, int mouseX, int mouseY) {
        //if (!this.minecraft.player.inventory.getCarried().isEmpty()) return;  // no tooltip if the player is dragging something

        assert this.minecraft != null;
        assert this.minecraft.player != null;
        boolean notCarrying = this.minecraft.player.inventoryMenu.getCarried().isEmpty();

        List<Component> hoveringText = new ArrayList<>();

        if (this.hoveredSlot != null && !this.hoveredSlot.hasItem() && this.hoveredSlot instanceof AbstractFurnaceMk2Slot) {
            AbstractFurnaceMk2Slot furnaceSlot = (AbstractFurnaceMk2Slot) this.hoveredSlot;
            String title = furnaceSlot.getTitle();
            if(title != null) {
                hoveringText.add(Component.translatable(furnaceSlot.getTitle()).withStyle(ChatFormatting.GREEN));
            }
            if (this.hoveredSlot instanceof SlotCore) {
                SlotCore slot = (SlotCore) this.hoveredSlot;
                hoveringText.add(Component.literal("").append(slot.getUpgradeItem().getDefaultInstance().getDisplayName()).withStyle(ChatFormatting.ITALIC));
            }
        }

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
            hoveringText.add(Component.literal("Exp: ").append(""+df.format(experience)+"/"+df.format(container.getMaxExp()/100)));
        }

        // If the mouse is over the experience bar, add hovering text
        if (notCarrying && isInRect(this.leftPos + FUEL_XPOS, this.topPos + FUEL_YPOS, FUEL_WIDTH, FUEL_HEIGHT, mouseX, mouseY)){
            int fuel = container.getFuelStored()/100;
            hoveringText.add(Component.literal("Fuel: ").append(""+df.format(fuel)+"/"+df.format(container.getMaxFuel()/100)));
        }

        // If hoveringText is not empty draw the hovering text.  Otherwise, use vanilla to render tooltip for the slots
        if (!hoveringText.isEmpty()){
            renderComponentTooltip(matrixStack, hoveringText, mouseX, mouseY);
        } else {
            super.renderTooltip(matrixStack, mouseX, mouseY);
        }
    }

    // Returns true if the given x,y coordinates are within the given rectangle
    public static boolean isInRect(int x, int y, int xSize, int ySize, int mouseX, int mouseY){
        return ((mouseX >= x && mouseX <= x+xSize) && (mouseY >= y && mouseY <= y+ySize));
    }

}
