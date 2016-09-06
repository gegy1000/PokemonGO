package net.gegy1000.pokemon.client.renderer.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class DefaultPokemonModel extends ModelBase {
    private int pokemonDisplayList;
    private boolean pokemonListCompiled;

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch, float scale) {
        super.render(entity, limbSwing, limbSwingAmount, age, yaw, pitch, scale);
        if (!this.pokemonListCompiled) {
            this.pokemonDisplayList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(this.pokemonDisplayList, GL11.GL_COMPILE);
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(0.0, 1.0, -0.5).tex(0.0, 1.0).endVertex();
            buffer.pos(0.0, 1.0, 0.5).tex(1.0, 1.0).endVertex();
            buffer.pos(0.0, 0.0, 0.5).tex(1.0, 0.0).endVertex();
            buffer.pos(0.0, 0.0, -0.5).tex(0.0, 0.0).endVertex();
            buffer.pos(0.025, 1.0, -0.5).tex(0.0, 1.0).endVertex();
            buffer.pos(0.025, 1.0, 0.5).tex(1.0, 1.0).endVertex();
            buffer.pos(0.025, 0.0, 0.5).tex(1.0, 0.0).endVertex();
            buffer.pos(0.025, 0.0, -0.5).tex(0.0, 0.0).endVertex();
            float textureScale = 1.0F / 96.0F;
            for (int lineX = 0; lineX < 96; lineX++) {
                float textureX = lineX * textureScale;
                float textureX2 = (lineX + 1) * textureScale;
                buffer.pos(0.0, 0.0, textureX - 0.5).tex(textureX, 0.0).endVertex();
                buffer.pos(0.0, 1.0, textureX - 0.5).tex(textureX, 1.0).endVertex();
                buffer.pos(0.025, 1.0, textureX - 0.5).tex(textureX2, 1.0).endVertex();
                buffer.pos(0.025, 0.0, textureX - 0.5).tex(textureX2, 0.0).endVertex();
                buffer.pos(0.0, 0.0, textureX2 - 0.5).tex(textureX, 0.0).endVertex();
                buffer.pos(0.0, 1.0, textureX2 - 0.5).tex(textureX, 1.0).endVertex();
                buffer.pos(0.025, 1.0, textureX2 - 0.5).tex(textureX2, 1.0).endVertex();
                buffer.pos(0.025, 0.0, textureX2 - 0.5).tex(textureX2, 0.0).endVertex();
            }
            for (int lineY = 0; lineY < 96; lineY++) {
                float textureY = lineY * textureScale;
                float textureY2 = (lineY + 1) * textureScale;
                buffer.pos(0.0, textureY, -0.5).tex(0.0, textureY).endVertex();
                buffer.pos(0.0, textureY, 0.5).tex(1.0, textureY).endVertex();
                buffer.pos(0.025, textureY, 0.5).tex(1.0, textureY2).endVertex();
                buffer.pos(0.025, textureY, -0.5).tex(0.0, textureY2).endVertex();
                buffer.pos(0.0, textureY2, -0.5).tex(0.0, textureY).endVertex();
                buffer.pos(0.0, textureY2, 0.5).tex(1.0, textureY).endVertex();
                buffer.pos(0.025, textureY2, 0.5).tex(1.0, textureY2).endVertex();
                buffer.pos(0.025, textureY2, -0.5).tex(0.0, textureY2).endVertex();
            }
            tessellator.draw();
            GlStateManager.glEndList();
            this.pokemonListCompiled = true;
        }
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.callList(this.pokemonDisplayList);
    }
}
