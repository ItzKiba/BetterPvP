package me.mykindos.betterpvp.core.listener.loader;

import me.mykindos.betterpvp.core.framework.BPvPPlugin;
import me.mykindos.betterpvp.core.framework.Loader;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class ListenerLoader extends Loader {

    public ListenerLoader(BPvPPlugin plugin) {
        super(plugin);
    }

    public void register(Listener listener) {
        plugin.getListeners().add(listener);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        count++;
    }

    public void load(Listener listener) {
        plugin.getInjector().injectMembers(listener);
        register(listener);
    }

    @Override
    public void load(Class<?> clazz) {
        try {
            Listener listener = (Listener) plugin.getInjector().getInstance(clazz);
            load(listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
