package hellfirepvp.observerlib.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ClientBossInfo;
import net.minecraft.client.gui.overlay.BossOverlayGui;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;

import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleBossBar
 * Created by HellFirePvP
 * Date: 12.02.2020 / 21:06
 */
public class SimpleBossInfo extends ClientBossInfo {

    private SimpleBossInfo(SUpdateBossInfoPacket pkt) {
        super(pkt);
    }

    public static Builder newBuilder(ITextComponent text, BossInfo.Color color, BossInfo.Overlay overlay) {
        return newBuilder(UUID.randomUUID(), text, color, overlay);
    }

    public static Builder newBuilder(UUID id, ITextComponent text, BossInfo.Color color, BossInfo.Overlay overlay) {
        return new Builder(id, text, color, overlay);
    }

    public boolean displayInfo() {
        if (Minecraft.getInstance().world == null) {
            return false;
        }
        BossOverlayGui gui = Minecraft.getInstance().ingameGUI.overlayBoss;
        if (!gui.mapBossInfos.containsKey(this.getUniqueId())) {
            return gui.mapBossInfos.put(this.getUniqueId(), this) == null;
        }
        return false;
    }

    public boolean removeInfo() {
        return Minecraft.getInstance().ingameGUI.overlayBoss.mapBossInfos.remove(this.getUniqueId()) != null;
    }

    public static class Builder extends BossInfo {

        private Builder(UUID barUUID, ITextComponent text, Color color, Overlay overlay) {
            super(barUUID, text, color, overlay);
        }

        public SimpleBossInfo build() {
            return new SimpleBossInfo(new SUpdateBossInfoPacket(SUpdateBossInfoPacket.Operation.ADD, this));
        }
    }
}
