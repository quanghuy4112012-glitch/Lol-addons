package lol.addon;

import lol.addon.modules.LolChunkFinder;
import lol.addon.modules.TuffChunkFinder;
import lol.addon.modules.SusChunkFinder;
import lol.addon.modules.SpawnerFinder;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class LolAddon extends MeteorAddon {
    public static final Category CATEGORY = new Category("LOL");

    @Override
    public void onInitialize() {
        Modules.get().add(new LolChunkFinder());
        Modules.get().add(new TuffChunkFinder());
        Modules.get().add(new SusChunkFinder());
        Modules.get().add(new SpawnerFinder());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "lol.addon";
    }
}
