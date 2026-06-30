package com.example.undertale;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia (no SERVIDOR) o osso que flutua embaixo do jogador enquanto ele VOA
 * com o set completo do Sans (o "voar montado num osso").
 */
@EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class BoneArmorSansFollowHandler {

    // jogador -> id da entidade osso que o segue
    private static final Map<UUID, Integer> FOLLOWERS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) return; // só servidor

        UUID id = player.getUUID();
        boolean shouldHave = SansArmorHandler.isWearingFullSet(player) && player.getAbilities().flying;

        Integer existingId = FOLLOWERS.get(id);
        BoneArmorSansEntity existing = null;
        if (existingId != null && level.getEntity(existingId) instanceof BoneArmorSansEntity g && g.isAlive()) {
            existing = g;
        }

        if (shouldHave) {
            if (existing == null) {
                BoneArmorSansEntity follower =
                        new BoneArmorSansEntity(ModEntities.BONE_ARMOR_SANS.get(), level);
                follower.setupFollow(player);
                level.addFreshEntity(follower);
                FOLLOWERS.put(id, follower.getId());
            }
        } else {
            if (existing != null) {
                existing.discard();
            }
            FOLLOWERS.remove(id);
        }
    }

    // Limpa o registro quando o jogador sai (a entidade some sozinha por não achar o dono).
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FOLLOWERS.remove(event.getEntity().getUUID());
    }
}
