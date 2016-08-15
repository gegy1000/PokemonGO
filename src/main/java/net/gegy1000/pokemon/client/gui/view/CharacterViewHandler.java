package net.gegy1000.pokemon.client.gui.view;

import com.pokegoapi.api.player.PlayerProfile;
import net.gegy1000.pokemon.pokemon.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CharacterViewHandler extends ViewHandler {
    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        try {
            ScaledResolution resolution = new ScaledResolution(this.mc);
            this.fontRendererObj.drawString("Avatar:", 10, 28, LLibrary.CONFIG.getTextColor()); //TODO make this an element
            int avatarWidth = resolution.getScaleFactor() * 30;
            int avatarHeight = resolution.getScaleFactor() * 45;
            this.drawRectangle(10, 38, avatarWidth, avatarHeight, LLibrary.CONFIG.getSecondarySubcolor());
            PlayerProfile profile = PokemonHandler.GO.getPlayerProfile();
            this.fontRendererObj.drawString("Pokecoins: " + profile.getCurrency(PlayerProfile.Currency.POKECOIN), 10, avatarHeight + 42, LLibrary.CONFIG.getTextColor());
            this.fontRendererObj.drawString("Stardust: " + profile.getCurrency(PlayerProfile.Currency.STARDUST), 10, avatarHeight + 52, LLibrary.CONFIG.getTextColor());
        } catch (Exception e) {
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
