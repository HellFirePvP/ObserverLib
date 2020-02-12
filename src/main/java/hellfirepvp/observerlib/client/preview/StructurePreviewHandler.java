package hellfirepvp.observerlib.client.preview;

import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.EnumSet;
import java.util.function.Consumer;

/**
 * This class is part of the ObserverLib Mod
 * The complete source code for this mod can be found on github.
 * Class: StructurePreviewHandler
 * Created by HellFirePvP
 * Date: 12.02.2020 / 18:23
 */
public class StructurePreviewHandler implements ITickHandler {

    private static final StructurePreviewHandler INSTANCE = new StructurePreviewHandler();

    private StructurePreview currentPreview = null;

    private StructurePreviewHandler() {}

    public static StructurePreviewHandler getInstance() {
        return INSTANCE;
    }

    void setStructurePreview(StructurePreview preview) {
        if (this.currentPreview != null) {
            this.currentPreview.onRemove();
        }
        this.currentPreview = preview;
    }

    public void attachEventListeners(IEventBus bus) {
        bus.addListener(EventPriority.HIGH, this::render);
    }

    public void attachTickHandlers(Consumer<ITickHandler> registrar) {
        registrar.accept(this);
    }

    private void render(RenderWorldLastEvent event) {
        World renderWorld = Minecraft.getInstance().world;
        PlayerEntity player = Minecraft.getInstance().player;
        if (renderWorld == null || player == null || this.currentPreview == null) {
            return;
        }

        if (this.currentPreview.canRender(renderWorld, player.getPosition())) {
            this.currentPreview.render(renderWorld);
        }
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        World renderWorld = Minecraft.getInstance().world;
        PlayerEntity player = Minecraft.getInstance().player;
        if (renderWorld == null || player == null) {
            this.currentPreview = null;
            return;
        }

        if (this.currentPreview != null) {
            if (!this.currentPreview.canPersist(renderWorld, player.getPosition())) {
                this.currentPreview.onRemove();
                this.currentPreview = null;
            } else {
                this.currentPreview.tick(renderWorld, player.getPosition());
            }
        }
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.CLIENT);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "ObserverLib Structure Preview";
    }
}
