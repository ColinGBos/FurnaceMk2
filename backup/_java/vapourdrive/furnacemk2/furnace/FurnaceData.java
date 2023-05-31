package vapourdrive.furnacemk2.furnace;

import net.minecraft.world.inventory.ContainerData;
import vapourdrive.furnacemk2.FurnaceMk2;

public class FurnaceData implements ContainerData {

    public int experience;
    public int burnProgress;
    public int currentMaxBurn;
    public int cookProgress;
    public int cookMax;

    @Override
    public int get(int index) {
//        FurnaceMk2.debugLog(String.format("BP: %s", this.burnProgress));
//        FurnaceMk2.debugLog(String.format("CMB: %s", this.currentMaxBurn));
//        FurnaceMk2.debugLog(String.format("CP: %s", this.cookProgress));
//        FurnaceMk2.debugLog(String.format("EXP: %s", this.experience));
//        FurnaceMk2.debugLog(String.format("CM: %s", this.cookMax));
        return switch (index) {
            case 0 -> this.burnProgress;
            case 1 -> this.currentMaxBurn;
            case 2 -> this.cookProgress;
            case 3 -> this.experience;
            case 4 -> this.cookMax;
            default -> 0;
        };
    }

    @Override
    public void set(int index, int value) {
        switch (index) {
            case 0 -> this.burnProgress = value;
            case 1 -> this.currentMaxBurn = value;
            case 2 -> this.cookProgress = value;
            case 3 -> this.experience = value;
            case 4 -> this.cookMax = value;
        }
    }

    @Override
    public int getCount() {
        return 5;
    }
}
