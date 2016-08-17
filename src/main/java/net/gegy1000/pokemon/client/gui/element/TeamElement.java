package net.gegy1000.pokemon.client.gui.element;

import POGOProtos.Enums.TeamColorOuterClass;
import net.gegy1000.pokemon.client.gui.PokemonGUI;
import net.ilexiconn.llibrary.client.gui.element.Element;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class TeamElement<T extends GuiScreen> extends Element<T> {
    private PokemonGUI.Team team;
    private Function<PokemonGUI.Team, Void> function;

    public TeamElement(T gui, float posX, float posY, int width, int height, PokemonGUI.Team team, Function<PokemonGUI.Team, Void> function) {
        super(gui, posX, posY, width, height);
        this.team = team;
        this.function = function;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        this.drawRectangle(this.getPosX(), this.getPosY(), this.getWidth(), this.getHeight(), this.isSelected(mouseX, mouseY) ? this.getColorScheme().getSecondaryColor() : this.getColorScheme().getPrimaryColor());
        GlStateManager.enableTexture2D();
        this.getGUI().mc.getTextureManager().bindTexture(this.team.getTeamTexture());
        this.drawTexturedRectangle(this.getPosX(), this.getPosY(), this.getWidth(), this.getHeight(), 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            this.function.apply(this.team);
            return true;
        }
        return false;
    }
}
