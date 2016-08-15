package net.gegy1000.pokemon.client.gui;

import net.gegy1000.pokemon.client.gui.element.PasswordInputElement;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.ilexiconn.llibrary.client.gui.element.ElementHandler;
import net.ilexiconn.llibrary.client.gui.element.InputElement;
import net.ilexiconn.llibrary.client.gui.element.LabelElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LoginGUI extends PokemonGUI {
    private InputElement<LoginGUI> username;
    private PasswordInputElement<LoginGUI> password;

    @Override
    public void initElements() {
        float centerX = this.width / 2.0F;
        float centerY = this.height / 2.0F;
        ElementHandler.INSTANCE.addElement(this, new LabelElement<>(this, "Username:", centerX - 60.0F, centerY - 30.0F));
        ElementHandler.INSTANCE.addElement(this, this.username = new InputElement<>(this, "", centerX - 60.0F, centerY - 20.0F, 120, (input) -> {
        }));
        ElementHandler.INSTANCE.addElement(this, new LabelElement<>(this, "Password:", centerX - 60.0F, centerY + 10.0F));
        ElementHandler.INSTANCE.addElement(this, this.password = new PasswordInputElement<>(this, "", centerX - 60.0F, centerY + 20.0F, 120, (input) -> {
            PokemonHandler.authenticate(this.username.getText(), this.password.getText());
            this.mc.displayGuiScreen(new LoggingInGUI());
        }));
        ElementHandler.INSTANCE.addElement(this, new ButtonElement<>(this, "Login", this.width - 60.0F, this.height - 20.0F, 60, 20, (button) -> {
            PokemonHandler.authenticate(this.username.getText(), this.password.getText());
            this.mc.displayGuiScreen(new LoggingInGUI());
            return true;
        }));
        ElementHandler.INSTANCE.addElement(this, new ButtonElement<>(this, "Cancel", 0.0F, this.height - 20.0F, 60, 20, (button) -> {
            this.mc.displayGuiScreen(null);
            return true;
        }));
    }

    @Override
    public void drawScreen(float mouseX, float mouseY, float partialTicks) {
        this.drawRectangle(0, this.height - 20.0F, this.width, 20.0F, LLibrary.CONFIG.getPrimarySubcolor());
        String titleString = "PTC Login";
        this.fontRendererObj.drawString(titleString, this.width / 2 - this.fontRendererObj.getStringWidth(titleString) / 2, this.height / 2 - 60, LLibrary.CONFIG.getTextColor(), false);
        if (PokemonHandler.GO != null) {
            String warningString = "WARNING: Already logged in as \'" + PokemonHandler.username + "\'";
            this.fontRendererObj.drawString(warningString, this.width / 2 - this.fontRendererObj.getStringWidth(warningString) / 2, this.height / 8, 0xFF0000, false);
        }
    }
}
