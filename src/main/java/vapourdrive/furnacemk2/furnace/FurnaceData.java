package vapourdrive.furnacemk2.furnace;

import net.minecraft.world.inventory.ContainerData;

public class FurnaceData implements ContainerData {
    public enum Data {
        FUEL,
        COOK_PROGRESS,
        COOK_MAX,
        EXPERIENCE
    }

    private final int[] data = {0, 0, 0, 0};

    public int get(Data data) {
        return this.get(data.ordinal());
    }

    @Override
    public int get(int index) {
        if (index >= data.length || index < 0) {
            return 0;
        } else {
            return data[index];
        }
    }

    public void set(Data data, int value) {
        this.set(data.ordinal(), value);
    }

    @Override
    public void set(int index, int value) {
        if (index < data.length && index >= 0) {
            data[index] = value;
        }
    }

    @Override
    public int getCount() {
        return data.length;
    }
}
