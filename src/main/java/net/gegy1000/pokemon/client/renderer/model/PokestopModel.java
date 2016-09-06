package net.gegy1000.pokemon.client.renderer.model;

import net.ilexiconn.llibrary.client.model.tools.AdvancedModelBase;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.entity.Entity;

public class PokestopModel extends AdvancedModelBase {
    private AdvancedModelRenderer base;
    private AdvancedModelRenderer base2;
    private AdvancedModelRenderer shape1;
    private AdvancedModelRenderer pole;
    private AdvancedModelRenderer baseEdge1;
    private AdvancedModelRenderer baseEdge2;
    private AdvancedModelRenderer shape1_1;
    private AdvancedModelRenderer shape1_2;
    private AdvancedModelRenderer shape1_3;
    private AdvancedModelRenderer FloatingCube;
    private AdvancedModelRenderer shape1_4;
    private AdvancedModelRenderer shape1_5;
    private AdvancedModelRenderer shape1_6;
    private AdvancedModelRenderer shape1_7;
    private AdvancedModelRenderer shape1_8;
    private AdvancedModelRenderer shape1_9;
    private AdvancedModelRenderer shape1_10;
    private AdvancedModelRenderer shape1_11;
    private AdvancedModelRenderer shape1_12;
    private AdvancedModelRenderer shape1_13;
    private AdvancedModelRenderer shape1_14;
    private AdvancedModelRenderer shape1_15;

    public PokestopModel() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.shape1_1 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_1.setRotationPoint(0.0F, 0.0F, -3.3F);
        this.shape1_1.addBox(-2.0F, 0.0F, -1.0F, 4, 1, 1, 0.0F);
        this.shape1_12 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_12.setRotationPoint(7.75F, 1.0F, 7.75F);
        this.shape1_12.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.shape1_11 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_11.setRotationPoint(-7.75F, 1.0F, 7.75F);
        this.shape1_11.addBox(-1.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.shape1_14 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_14.setRotationPoint(7.75F, 1.0F, -7.75F);
        this.shape1_14.addBox(0.0F, 0.0F, -1.0F, 1, 1, 1, 0.0F);
        this.FloatingCube = new AdvancedModelRenderer(this, 0, 0);
        this.FloatingCube.setRotationPoint(-0.5F, -25.0F, -0.5F);
        this.FloatingCube.addBox(-5.0F, -5.0F, -5.0F, 10, 10, 10, 0.0F);
        this.setRotateAngle(this.FloatingCube, 0.0F, 0.7853981633974483F, 2.2165681500327987F);
        this.shape1_4 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_4.setRotationPoint(0.0F, 0.0F, 7.0F);
        this.shape1_4.addBox(-7.0F, 0.0F, 0.0F, 14, 1, 2, 0.0F);
        this.setRotateAngle(this.shape1_4, -0.5277875658030852F, 0.0F, 0.0F);
        this.shape1_3 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_3.setRotationPoint(2.7755575615628914E-17F, -1.0F, 2.7755575615628914E-17F);
        this.shape1_3.addBox(-7.0F, 0.0F, -7.0F, 14, 1, 14, 0.0F);
        this.shape1_6 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_6.setRotationPoint(0.0F, 0.0F, -7.0F);
        this.shape1_6.addBox(-7.0F, 0.0F, -2.0F, 14, 1, 2, 0.0F);
        this.setRotateAngle(this.shape1_6, 0.5277875658030851F, 0.0F, 0.0F);
        this.shape1_8 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_8.setRotationPoint(-7.75F, 1.0F, 0.0F);
        this.shape1_8.addBox(-1.0F, 0.0F, -8.0F, 2, 1, 16, 0.0F);
        this.base = new AdvancedModelRenderer(this, 0, 0);
        this.base.setRotationPoint(0.0F, 23.0F, 0.0F);
        this.base.addBox(-3.5F, 0.0F, -3.5F, 7, 1, 7, 0.0F);
        this.baseEdge2 = new AdvancedModelRenderer(this, 0, 0);
        this.baseEdge2.setRotationPoint(-3.3F, 0.0F, 0.0F);
        this.baseEdge2.addBox(-1.0F, 0.0F, -1.5F, 1, 1, 3, 0.0F);
        this.shape1_7 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_7.setRotationPoint(-7.0F, 0.0F, 0.0F);
        this.shape1_7.addBox(-2.0F, 0.0F, -7.0F, 2, 1, 14, 0.0F);
        this.setRotateAngle(this.shape1_7, 0.0F, 0.0F, -0.5277875658030852F);
        this.shape1_5 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_5.setRotationPoint(7.0F, 0.0F, 0.0F);
        this.shape1_5.addBox(0.0F, 0.0F, -7.0F, 2, 1, 14, 0.0F);
        this.setRotateAngle(this.shape1_5, 0.0F, 0.0F, 0.5277875658030853F);
        this.shape1_13 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_13.setRotationPoint(-7.75F, 1.0F, -7.75F);
        this.shape1_13.addBox(-1.0F, 0.0F, -1.0F, 1, 1, 1, 0.0F);
        this.shape1 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.shape1.addBox(-3.0F, 0.0F, -4.0F, 6, 1, 8, 0.0F);
        this.baseEdge1 = new AdvancedModelRenderer(this, 0, 0);
        this.baseEdge1.setRotationPoint(3.3F, 0.0F, 0.0F);
        this.baseEdge1.addBox(0.0F, 0.0F, -1.5F, 1, 1, 3, 0.0F);
        this.base2 = new AdvancedModelRenderer(this, 0, 0);
        this.base2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.base2.addBox(-4.0F, 0.0F, -3.0F, 8, 1, 6, 0.0F);
        this.shape1_9 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_9.setRotationPoint(7.75F, 1.0F, 0.0F);
        this.shape1_9.addBox(-1.0F, 0.0F, -8.0F, 2, 1, 16, 0.0F);
        this.shape1_15 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_15.setRotationPoint(7.75F, 0.0F, -15.5F);
        this.shape1_15.addBox(-8.0F, 0.0F, -1.0F, 16, 1, 1, 0.0F);
        this.shape1_2 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_2.setRotationPoint(0.0F, 0.0F, 3.3F);
        this.shape1_2.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 1, 0.0F);
        this.pole = new AdvancedModelRenderer(this, 0, 0);
        this.pole.setRotationPoint(0.0F, -28.0F, 0.0F);
        this.pole.addBox(-0.5F, 0.0F, -0.5F, 1, 28, 1, 0.0F);
        this.shape1_10 = new AdvancedModelRenderer(this, 0, 0);
        this.shape1_10.setRotationPoint(0.0F, 1.0F, 7.75F);
        this.shape1_10.addBox(-8.0F, 0.0F, 0.0F, 16, 1, 1, 0.0F);
        this.shape1.addChild(this.shape1_1);
        this.shape1_3.addChild(this.shape1_12);
        this.shape1_3.addChild(this.shape1_11);
        this.shape1_3.addChild(this.shape1_14);
        this.pole.addChild(this.FloatingCube);
        this.shape1_3.addChild(this.shape1_4);
        this.pole.addChild(this.shape1_3);
        this.shape1_3.addChild(this.shape1_6);
        this.shape1_3.addChild(this.shape1_8);
        this.base2.addChild(this.baseEdge2);
        this.shape1_3.addChild(this.shape1_7);
        this.shape1_3.addChild(this.shape1_5);
        this.shape1_3.addChild(this.shape1_13);
        this.base.addChild(this.shape1);
        this.base2.addChild(this.baseEdge1);
        this.base.addChild(this.base2);
        this.shape1_3.addChild(this.shape1_9);
        this.shape1_11.addChild(this.shape1_15);
        this.shape1.addChild(this.shape1_2);
        this.base.addChild(this.pole);
        this.shape1_3.addChild(this.shape1_10);
        this.updateDefaultPose();
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ticks, float yaw, float pitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ticks, yaw, pitch, scale, entity);
        this.base.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ticks, float yaw, float pitch, float scaleFactor, Entity entity) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ticks, yaw, pitch, scaleFactor, entity);
        this.resetToDefaultPose();
        this.pole.rotateAngleY += ticks * 0.05F;
        this.FloatingCube.rotateAngleZ += ticks * 0.1F;
        this.bob(this.FloatingCube, 0.25F, 5.0F, false, ticks, 1.0F);
    }

    private void setRotateAngle(AdvancedModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
