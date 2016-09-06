package net.gegy1000.pokemon.client.gui.view;

import POGOProtos.Enums.TeamColorOuterClass;
import POGOProtos.Networking.Requests.Messages.SetPlayerTeamMessageOuterClass;
import POGOProtos.Networking.Requests.RequestTypeOuterClass;
import POGOProtos.Networking.Responses.SetPlayerTeamResponseOuterClass;
import com.google.protobuf.ByteString;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.main.AsyncServerRequest;
import com.pokegoapi.util.AsyncHelper;
import net.gegy1000.pokemon.client.gui.LoginGUI;
import net.gegy1000.pokemon.client.gui.PokemonGUI;
import net.gegy1000.pokemon.client.gui.element.TeamElement;
import net.gegy1000.pokemon.client.gui.view.inventory.InventoryViewHandler;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.ilexiconn.llibrary.client.gui.element.ElementHandler;
import net.ilexiconn.llibrary.client.gui.element.LabelElement;
import net.ilexiconn.llibrary.client.gui.element.WindowElement;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class PokemonViewGUI extends PokemonGUI {
    private ButtonElement<PokemonViewGUI> character;
    private ButtonElement<PokemonViewGUI> nearby;
    private ButtonElement<PokemonViewGUI> inventory;
    private ButtonElement<PokemonViewGUI> statistics;

    private ViewMode viewMode = ViewMode.CHARACTER;

    private Map<ViewMode, ViewHandler> viewHandlers = new HashMap<>();

    public PokemonViewGUI() {
        for (ViewMode mode : ViewMode.values()) {
            try {
                this.viewHandlers.put(mode, mode.getViewHandler().getDeclaredConstructor(PokemonViewGUI.class).newInstance(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initElements() {
        if (PokemonHandler.API == null) {
            WindowElement<PokemonViewGUI> window = new WindowElement<>(this, I18n.translateToLocal("gui.failure.name"), 172, 50, false);
            new LabelElement<>(this, I18n.translateToLocal("gui.not_logged_in.name"), 2.0F, 18.0F).withParent(window);
            new ButtonElement<>(this, I18n.translateToLocal("gui.login.name"), 1.0F, 30.0F, 84, 19, (button) -> {
                this.mc.displayGuiScreen(new LoginGUI());
                return true;
            }).withParent(window).withColorScheme(THEME_WINDOW);
            new ButtonElement<>(this, I18n.translateToLocal("gui.cancel.name"), 87.0F, 30.0F, 84, 19, (button) -> {
                this.mc.displayGuiScreen(null);
                return true;
            }).withParent(window).withColorScheme(THEME_WINDOW);
            ElementHandler.INSTANCE.addElement(this, window);
        }
        ElementHandler.INSTANCE.addElement(this, this.character = new ButtonElement<>(this, I18n.translateToLocal("view.character.name"), this.width - 180.0F, 0.0F, 60, 18, (button) -> {
            this.setViewMode(ViewMode.CHARACTER);
            return true;
        }));
        ElementHandler.INSTANCE.addElement(this, this.nearby = (ButtonElement<PokemonViewGUI>) new ButtonElement<>(this, I18n.translateToLocal("view.nearby.name"), this.width - 240.0F, 0.0F, 60, 18, (button) -> {
            this.setViewMode(ViewMode.NEARBY);
            return true;
        }).withColorScheme(THEME_TAB_ACTIVE));
        ElementHandler.INSTANCE.addElement(this, this.inventory = new ButtonElement<>(this, I18n.translateToLocal("view.inventory.name"), this.width - 120.0F, 0.0F, 60, 18, (button) -> {
            this.setViewMode(ViewMode.INVENTORY);
            return true;
        }));
        ElementHandler.INSTANCE.addElement(this, this.statistics = new ButtonElement<>(this, I18n.translateToLocal("view.statistics.name"), this.width - 60.0F, 0.0F, 60, 18, (button) -> {
            this.setViewMode(ViewMode.STATISTICS);
            return true;
        }));
        this.setViewMode(ViewMode.NEARBY);
    }

    @Override
    public void drawScreen(float mouseX, float mouseY, float partialTicks) {
        this.drawRectangle(0, this.height - 34.0F, this.width, 34.0F, LLibrary.CONFIG.getPrimaryColor());
        this.drawRectangle(0, 0, this.width, 18.0F, LLibrary.CONFIG.getPrimaryColor());
        this.fontRendererObj.drawString(I18n.translateToLocal("gui.pokemon_view.name"), 5, 5, LLibrary.CONFIG.getTextColor());
        if (PokemonHandler.API != null) {
            try {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                Team team = new Team(PokemonHandler.API.getPlayerProfile().getPlayerData().getTeam());
                this.mc.getTextureManager().bindTexture(team.getTeamTexture());
                this.drawTexturedModalRect(0, this.height - 32, 0, 0, 32, 32, 32, 32, 1.0, 1.0);

                this.fontRendererObj.drawString(team.getTeamName(), 35, this.height - 20, LLibrary.CONFIG.getTextColor());
                this.fontRendererObj.drawString(PokemonHandler.getUsername(), this.width - this.fontRendererObj.getStringWidth(PokemonHandler.getUsername()) - 9, this.height - 20, LLibrary.CONFIG.getTextColor());

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                int stardust = 0;
                try {
                    stardust = PokemonHandler.API.getPlayerProfile().getCurrency(PlayerProfile.Currency.STARDUST);
                } catch (Exception e) {
                }

                int pokecoins = 0;
                try {
                    pokecoins = PokemonHandler.API.getPlayerProfile().getCurrency(PlayerProfile.Currency.POKECOIN);
                } catch (Exception e) {
                }

                this.mc.getTextureManager().bindTexture(STARDUST_TEXTURE);
                this.drawTexturedModalRect(this.width / 3, this.height - 24, 0.0F, 0.0F, 1.0F, 1.0F, 16, 16);

                this.mc.getTextureManager().bindTexture(POKECOIN_TEXTURE);
                this.drawTexturedModalRect(this.width - this.width / 3 - 16, this.height - 24, 0.0F, 0.0F, 1.0F, 1.0F, 16, 16);

                this.fontRendererObj.drawString(String.valueOf(stardust), this.width / 3 + 16, this.height - 19, LLibrary.CONFIG.getTextColor());
                this.fontRendererObj.drawString(String.valueOf(pokecoins), this.width - this.width / 3 + 3, this.height - 19, LLibrary.CONFIG.getTextColor());

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                this.viewHandlers.get(this.viewMode).render(mouseX, mouseY, partialTicks);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        try {
            if (PokemonHandler.API != null) {
                PlayerProfile playerProfile = PokemonHandler.API.getPlayerProfile();
                if (playerProfile != null) {
                    if (playerProfile.getPlayerData().getTeam() == TeamColorOuterClass.TeamColor.NEUTRAL) {
                        if (mouseX >= 0 && mouseX <= 32 && mouseY >= this.height - 32 && mouseY <= this.height) {
                            WindowElement<PokemonViewGUI> window = new WindowElement<>(this, I18n.translateToLocal("gui.select_team.name"), 210, 84, true);
                            new TeamElement<>(this, 0.0F, 14.0F, 70, 70, new Team(TeamColorOuterClass.TeamColor.YELLOW), (team) -> {
                                this.setTeam(team, window);
                                return null;
                            }).withParent(window);
                            new TeamElement<>(this, 70.0F, 14.0F, 70, 70, new Team(TeamColorOuterClass.TeamColor.BLUE), (team) -> {
                                this.setTeam(team, window);
                                return null;
                            }).withParent(window);
                            new TeamElement<>(this, 140.0F, 14.0F, 70, 70, new Team(TeamColorOuterClass.TeamColor.RED), (team) -> {
                                this.setTeam(team, window);
                                return null;
                            }).withParent(window);
                            ElementHandler.INSTANCE.addElement(this, window);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTeam(Team team, WindowElement<PokemonViewGUI> window) {
        ElementHandler.INSTANCE.removeElement(this, window);
        WindowElement<PokemonViewGUI> confirmWindow = new WindowElement<>(this, I18n.translateToLocal("gui.confirm.name"), 235, 47, false);
        new LabelElement<>(this, I18n.translateToLocalFormatted("gui.team_confirm.name", team.getTeamName()), 2.0F, 16.0F).withParent(confirmWindow);
        new ButtonElement<>(this, I18n.translateToLocal("gui.cancel.name"), 1.0F, 31.0F, 116, 15, (button) -> {
            ElementHandler.INSTANCE.removeElement(this, confirmWindow);
            ElementHandler.INSTANCE.addElement(this, window);
            return true;
        }).withParent(confirmWindow).withColorScheme(THEME_WINDOW);
        new ButtonElement<>(this, I18n.translateToLocal("gui.okay.name"), 118.0F, 31.0F, 116, 15, (button) -> {
            ElementHandler.INSTANCE.removeElement(this, confirmWindow);
            new Thread(() -> {
                try {
                    SetPlayerTeamMessageOuterClass.SetPlayerTeamMessage message = SetPlayerTeamMessageOuterClass.SetPlayerTeamMessage.newBuilder().setTeam(team.toTeamColor()).build();
                    AsyncServerRequest request = new AsyncServerRequest(RequestTypeOuterClass.RequestType.SET_PLAYER_TEAM, message);
                    ByteString byteString = AsyncHelper.toBlocking(PokemonHandler.API.getRequestHandler().sendAsyncServerRequests(request));
                    SetPlayerTeamResponseOuterClass.SetPlayerTeamResponse response = SetPlayerTeamResponseOuterClass.SetPlayerTeamResponse.parseFrom(byteString);
                    if (response.getStatus() == SetPlayerTeamResponseOuterClass.SetPlayerTeamResponse.Status.SUCCESS) {
                        PokemonHandler.API.getPlayerProfile().updateProfile();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            return true;
        }).withParent(confirmWindow).withColorScheme(THEME_WINDOW);
        ElementHandler.INSTANCE.addElement(this, confirmWindow);
    }

    public void setViewMode(ViewMode viewMode) {
        ViewMode prevViewMode = this.viewMode;

        if (prevViewMode != null) {
            prevViewMode.getButton(this).withColorScheme(ButtonElement.DEFAULT);
        }

        this.viewMode = viewMode;
        this.viewMode.getButton(this).withColorScheme(THEME_TAB_ACTIVE);

        if (prevViewMode != null) {
            this.viewHandlers.get(prevViewMode).cleanupView();
        }

        this.viewHandlers.get(this.viewMode).initView();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.viewHandlers.get(this.viewMode).cleanupView();
    }

    public enum ViewMode {
        CHARACTER((gui) -> gui.character, CharacterViewHandler.class),
        NEARBY((gui) -> gui.nearby, NearbyViewHandler.class),
        INVENTORY((gui) -> gui.inventory, InventoryViewHandler.class),
        STATISTICS((gui) -> gui.statistics, StatisticsViewHandler.class);

        private Function<PokemonViewGUI, ButtonElement<PokemonViewGUI>> button;
        private Class<? extends ViewHandler> viewHandler;

        ViewMode(Function<PokemonViewGUI, ButtonElement<PokemonViewGUI>> button, Class<? extends ViewHandler> viewHandler) {
            this.button = button;
            this.viewHandler = viewHandler;
        }

        public ButtonElement<PokemonViewGUI> getButton(PokemonViewGUI gui) {
            return this.button.apply(gui);
        }

        public Class<? extends ViewHandler> getViewHandler() {
            return this.viewHandler;
        }
    }
}
