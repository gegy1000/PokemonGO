package net.gegy1000.pokemon.client.gui;

import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LoggingInGUI extends PokemonGUI {
    @Override
    public void initElements() {
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!PokemonHandler.loggingIn && !PokemonHandler.loginFailed) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(float mouseX, float mouseY, float partialTicks) {
        String displayText = PokemonHandler.loginFailed ? TextFormatting.RED + I18n.translateToLocalFormatted("gui.login_failed.name", PokemonHandler.username) : I18n.translateToLocalFormatted("gui.logging_in.name", PokemonHandler.username);
        this.fontRendererObj.drawString(displayText, this.width / 2 - this.fontRendererObj.getStringWidth(displayText) / 2, this.height / 2, LLibrary.CONFIG.getTextColor(), false);
    }
}
