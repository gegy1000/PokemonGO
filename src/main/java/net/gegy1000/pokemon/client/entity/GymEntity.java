package net.gegy1000.pokemon.client.entity;

import com.pokegoapi.api.gym.Gym;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.gui.GymGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class GymEntity extends PokemonEntity {
    private Gym gym;

    public GymEntity(World world, Gym gym) {
        super(world, PokemonGO.GENERATOR.fromLong(gym.getLongitude()), PokemonGO.GENERATOR.fromLat(gym.getLatitude()));
        this.gym = gym;
        this.setSize(3.0F, 7.0F);
    }

    public Gym getGym() {
        return this.gym;
    }

    @Override
    public void onInteract() {
        Minecraft.getMinecraft().displayGuiScreen(new GymGUI(this.gym));
    }
}
