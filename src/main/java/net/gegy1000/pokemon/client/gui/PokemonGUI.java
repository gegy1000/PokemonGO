package net.gegy1000.pokemon.client.gui;

import POGOProtos.Enums.TeamColorOuterClass;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.ilexiconn.llibrary.client.gui.ElementGUI;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public abstract class PokemonGUI extends ElementGUI {
    public void drawTexturedModalRect(float x, float y, float minU, float minV, float maxU, float maxV, float width, float height) {
        GlStateManager.pushMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, this.zLevel).tex(minU, maxV).endVertex();
        buffer.pos(x + width, y + height, this.zLevel).tex(maxU, maxV).endVertex();
        buffer.pos(x + width, y, this.zLevel).tex(maxU, minV).endVertex();
        buffer.pos(x, y, this.zLevel).tex(minU, minV).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    public void drawTexturedModalRect(float x, float y, int textureX, int textureY, int width, int height, int texWidth, int texHeight, double scaleX, double scaleY) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scaleX, scaleY, 0.0);
        x /= scaleX;
        y /= scaleY;
        float f = 1.0F / texWidth;
        float f1 = 1.0F / texHeight;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, this.zLevel).tex(textureX * f, (textureY + height) * f1).endVertex();
        buffer.pos(x + width, y + height, this.zLevel).tex((textureX + width) * f, (textureY + height) * f1).endVertex();
        buffer.pos(x + width, y, this.zLevel).tex((textureX + width) * f, (textureY * f1)).endVertex();
        buffer.pos(x, y, this.zLevel).tex(textureX * f, textureY * f1).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    public void drawRectangle(double x, double y, double width, double height, int color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        float a = (float) (color >> 24 & 0xFF) / 255.0F;
        float r = (float) (color >> 16 & 0xFF) / 255.0F;
        float g = (float) (color >> 8 & 0xFF) / 255.0F;
        float b = (float) (color & 0xFF) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexBuffer = tessellator.getBuffer();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vertexBuffer.pos(x, y + height, 0.0).color(r, g, b, a).endVertex();
        vertexBuffer.pos(x + width, y + height, 0.0).color(r, g, b, a).endVertex();
        vertexBuffer.pos(x + width, y, 0.0).color(r, g, b, a).endVertex();
        vertexBuffer.pos(x, y, 0.0).color(r, g, b, a).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static class Team {
        private final TeamColorOuterClass.TeamColor teamColor;
        private ResourceLocation teamTexture;
        private final String teamName;

        public Team(TeamColorOuterClass.TeamColor team) {
            this.teamColor = team;
            this.teamTexture = PokemonGUIHandler.NEUTRAL_TEXTURE;
            String teamName = "neutral";
            switch (team) {
                case YELLOW:
                    this.teamTexture = PokemonGUIHandler.INSTINCT_TEXTURE;
                    teamName = "instinct";
                    break;
                case RED:
                    this.teamTexture = PokemonGUIHandler.VALOR_TEXTURE;
                    teamName = "valor";
                    break;
                case BLUE:
                    this.teamTexture = PokemonGUIHandler.MYSTIC_TEXTURE;
                    teamName = "mystic";
                    break;
            }

            this.teamName = "team." + teamName + ".name";
        }

        public ResourceLocation getTeamTexture() {
            return this.teamTexture;
        }

        public String getTeamName() {
            return I18n.translateToLocal(this.teamName);
        }

        public TeamColorOuterClass.TeamColor toTeamColor() {
            return this.teamColor;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
