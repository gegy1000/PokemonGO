package net.gegy1000.pokemon.client.gui.view;

import POGOProtos.Networking.Responses.ClaimCodenameResponseOuterClass;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonRequestHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.ilexiconn.llibrary.client.gui.element.InputElement;
import net.ilexiconn.llibrary.client.gui.element.LabelElement;
import net.ilexiconn.llibrary.client.gui.element.WindowElement;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

@SideOnly(Side.CLIENT)
public class CharacterViewHandler extends ViewHandler {
    private InputElement<PokemonViewGUI> name;

    public CharacterViewHandler(PokemonViewGUI gui) {
        super(gui);
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        try {
            ScaledResolution resolution = new ScaledResolution(this.mc);
            this.fontRenderer.drawString(I18n.translateToLocal("gui.avatar.name"), 10, 38, LLibrary.CONFIG.getTextColor()); //TODO make this an element
            int avatarWidth = resolution.getScaleFactor() * 30;
            int avatarHeight = resolution.getScaleFactor() * 45;
            this.drawRectangle(10, 48, avatarWidth, avatarHeight, LLibrary.CONFIG.getSecondarySubcolor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY) {

    }

    @Override
    public void initView() {
        this.getGUI().addElement(this.name = new InputElement<>(this.getGUI(), 4.0F, 22.0F, 100, PokemonHandler.getUsername(), (input) -> {
            if (!Objects.equals(PokemonHandler.getUsername(), this.name.getText())) {
                PokemonHandler.addTask(() -> {
                    try {
                        ClaimCodenameResponseOuterClass.ClaimCodenameResponse.Status status = PokemonRequestHandler.setUsername(this.name.getText());
                        String statusWindowTitle = null;
                        String statusWindowMessage = null;
                        switch (status) {
                            case CODENAME_CHANGE_NOT_ALLOWED:
                                statusWindowTitle = I18n.translateToLocal("gui.failure.name");
                                statusWindowMessage = I18n.translateToLocal("gui.not_allowed.name");
                                break;
                            case CODENAME_NOT_AVAILABLE:
                                statusWindowTitle = I18n.translateToLocal("gui.failure.name");
                                statusWindowMessage = I18n.translateToLocal("gui.not_available.name");
                                break;
                            case CODENAME_NOT_VALID:
                                statusWindowTitle = I18n.translateToLocal("gui.failure.name");
                                statusWindowMessage = I18n.translateToLocal("gui.invalid_name.name");
                                break;
                            case CURRENT_OWNER:
                                statusWindowTitle = I18n.translateToLocal("gui.failure.name");
                                statusWindowMessage = I18n.translateToLocal("gui.current_owner.name");
                                break;
                            case SUCCESS:
                                statusWindowTitle = I18n.translateToLocal("gui.success.name");
                                statusWindowMessage = I18n.translateToLocal("gui.name_change.name");
                                break;
                        }
                        if (statusWindowMessage != null) {
                            int windowWidth = this.fontRenderer.getStringWidth(statusWindowMessage) + 4;
                            WindowElement<PokemonViewGUI> window = new WindowElement<>(this.getGUI(), statusWindowTitle, windowWidth, 45, false);
                            new LabelElement<>(this.getGUI(), statusWindowMessage, 2, 18).withParent(window);
                            new ButtonElement<>(this.getGUI(), I18n.translateToLocal("gui.okay.name"), 1, 29, windowWidth - 2, 15, (button) -> {
                                this.getGUI().removeElement(window);
                                return true;
                            }).withParent(window).withColorScheme(PokemonGUIHandler.THEME_WINDOW);
                            this.getGUI().addElement(window);
                        }
                    } catch (Exception e) {
                    }
                    return null;
                });
            }
        }));
    }

    @Override
    public void cleanupView() {
        if (this.name != null) {
            this.getGUI().removeElement(this.name);
        }
    }
}
