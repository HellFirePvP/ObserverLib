package hellfirepvp.observerlib.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;

import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleBossBar
 * Created by HellFirePvP
 * Date: 12.02.2020 / 21:06
 */
public class SimpleBossInfo extends LerpingBossEvent {

    private SimpleBossInfo(UUID id, Component name,
                           BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay,
                           boolean darkenScreen, boolean playBossMusic, boolean worldFog) {
        super(id, name, 1F, color, overlay, darkenScreen, playBossMusic, worldFog);
    }

    public static SimpleBossInfo create(Component text, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
        return create(UUID.randomUUID(), text, color, overlay);
    }

    public static SimpleBossInfo create(Component text, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay,
                                        boolean darkenScreen, boolean playBossMusic, boolean worldFog) {
        return create(UUID.randomUUID(), text, color, overlay, darkenScreen, playBossMusic, worldFog);
    }

    public static SimpleBossInfo create(UUID id, Component text, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
        return new SimpleBossInfo(id, text, color, overlay, false, false, false);
    }

    public static SimpleBossInfo create(UUID id, Component text, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay,
                                        boolean darkenScreen, boolean playBossMusic, boolean worldFog) {
        return new SimpleBossInfo(id, text, color, overlay, darkenScreen, playBossMusic, worldFog);
    }

    public boolean displayInfo() {
        if (Minecraft.getInstance().level == null) {
            return false;
        }
        BossHealthOverlay gui = Minecraft.getInstance().gui.bossOverlay;
        if (!gui.events.containsKey(this.getId())) {
            return gui.events.put(this.getId(), this) == null;
        }
        return false;
    }

    public boolean removeInfo() {
        return Minecraft.getInstance().gui.bossOverlay.events.remove(this.getId()) != null;
    }
}
