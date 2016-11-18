package net.gegy1000.pokemon.client.entity;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class PokemonEntity {
    private World world;
    private double x;
    private double y;
    private double z;

    private float width = 1.5F;
    private float height = 2.0F;

    private AxisAlignedBB bounds;

    public PokemonEntity(World world, double x, double z) {
        this.world = world;
        this.x = x;
        this.z = z;
        this.update();
        this.updateBounds();
    }

    public void update() {
        this.y = this.world.getHeight(new BlockPos((int) this.x, 0, (int) this.z)).getY();
    }

    public void updateBounds() {
        float halfWidth = this.width / 2;
        this.bounds = new AxisAlignedBB(this.x - halfWidth, this.y, this.z - halfWidth, this.x + halfWidth, this.y + this.height, this.z + halfWidth);
    }

    protected void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        this.updateBounds();
    }

    public AxisAlignedBB getBounds() {
        return this.bounds;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public abstract void onInteract();
}
