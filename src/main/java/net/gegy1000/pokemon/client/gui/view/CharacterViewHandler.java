package net.gegy1000.pokemon.client.gui.view;

import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CharacterViewHandler extends ViewHandler {
    public CharacterViewHandler(PokemonViewGUI gui) {
        super(gui);
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        try {
            ScaledResolution resolution = new ScaledResolution(this.mc);
            this.fontRenderer.drawString(I18n.translateToLocal("gui.avatar.name"), 10, 28, LLibrary.CONFIG.getTextColor()); //TODO make this an element
            int avatarWidth = resolution.getScaleFactor() * 30;
            int avatarHeight = resolution.getScaleFactor() * 45;
            this.drawRectangle(10, 38, avatarWidth, avatarHeight, LLibrary.CONFIG.getSecondarySubcolor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY) {

    }

    @Override
    public void initView() {

    }

    @Override
    public void cleanupView() {

    }
}
