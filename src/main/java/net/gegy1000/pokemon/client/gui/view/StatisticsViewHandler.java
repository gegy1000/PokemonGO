package net.gegy1000.pokemon.client.gui.view;

import com.pokegoapi.api.inventory.Stats;
import net.gegy1000.pokemon.pokemon.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class StatisticsViewHandler extends ViewHandler {
    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(',');
            DecimalFormat shortDecimalFormat = new DecimalFormat("#.##", symbols);
            Stats statistics = PokemonHandler.GO.getPlayerProfile().getStats();
            List<String> statStrings = new LinkedList<>();
            statStrings.add("Level: " + TextFormatting.DARK_GREEN + statistics.getLevel());
            statStrings.add("Experience: " + TextFormatting.DARK_GREEN + statistics.getExperience() + "XP (" + (int) ((statistics.getExperience() - statistics.getPrevLevelXp()) / (double) statistics.getNextLevelXp() * 100.0) + "%)");
            statStrings.add("Pokéballs Thrown: " + TextFormatting.BLUE + statistics.getPokeballsThrown());
            statStrings.add("Pokémon Deployed: " + TextFormatting.BLUE + statistics.getPokemonDeployed());
            statStrings.add("Pokémon Captured: " + TextFormatting.BLUE + statistics.getPokemonsCaptured());
            statStrings.add("Pokémon Encountered: " + TextFormatting.BLUE + statistics.getPokemonsEncountered());
            statStrings.add("Pokéstop Visits: " + TextFormatting.BLUE + statistics.getPokeStopVisits());
            statStrings.add("Small Rattata Caught: " + TextFormatting.BLUE + statistics.getSmallRattataCaught());
            statStrings.add("Unique Pokémon: " + TextFormatting.GOLD + statistics.getUniquePokedexEntries());
            statStrings.add("Evolutions: " + TextFormatting.GOLD + statistics.getEvolutions());
            statStrings.add("Eggs Hatched: " + TextFormatting.GOLD + statistics.getEggsHatched());
            statStrings.add("Distance Walked: " + TextFormatting.GOLD + shortDecimalFormat.format(statistics.getKmWalked()) + "km");
            statStrings.add("Big Magikarp Caught: " + TextFormatting.GOLD + statistics.getBigMagikarpCaught());
            statStrings.add("Total Battle Attacks: " + TextFormatting.RED + statistics.getBattleAttackTotal());
            statStrings.add("Won Battle Attacks: " + TextFormatting.RED + statistics.getBattleAttackWon());
            statStrings.add("Won Defenses: " + TextFormatting.RED + statistics.getBattleDefendedWon());
            statStrings.add("Total Training Battles: " + TextFormatting.RED + statistics.getBattleTrainingTotal());
            statStrings.add("Won Training Battles: " + TextFormatting.RED + statistics.getBattleTrainingWon());
            int x = 3;
            int y = 21;
            for (String stat : statStrings) {
                this.fontRendererObj.drawString("- " + stat, x, y, LLibrary.CONFIG.getTextColor());
                y += 14;
                if (y > this.getGUI().height - 40) {
                    y = 21;
                    x += 170;
                }
            }
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
