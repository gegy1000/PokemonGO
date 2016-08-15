package net.gegy1000.pokemon.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class PasswordFontRenderer extends FontRenderer {
    public PasswordFontRenderer() {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().renderEngine, false);
    }

    @Override
    public int getCharWidth(char character) {
        return 8;
    }

    @Override
    protected float renderDefaultChar(int character, boolean italic) {
        int italicOffset = italic ? 1 : 0;
        GlStateManager.disableTexture2D();
        GlStateManager.glBegin(GL11.GL_TRIANGLE_STRIP);
        GlStateManager.glVertex3f(this.posX - 1.0F + italicOffset, this.posY, 0.0F);
        GlStateManager.glVertex3f(this.posX - 1.0F - italicOffset, this.posY + 6.99F, 0.0F);
        GlStateManager.glVertex3f(this.posX + 5.5F + italicOffset, this.posY, 0.0F);
        GlStateManager.glVertex3f(this.posX + 5.5F - italicOffset, this.posY + 6.99F, 0.0F);
        GlStateManager.glEnd();
        GlStateManager.enableTexture2D();
        return 8.0F;
    }
}
