package com.example.undertale;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia (no SERVIDOR) o osso que flutua embaixo do jogador enquanto ele VOA
 * com o set completo do Sans (o "voar montado num osso").
 *
 * A cada tick de jogador: se ele deve ter o osso e ainda não tem, invoca um em
 * modo FOLLOW; se não deve mais ter, remove. O reposicionamento "embaixo do
 * jogador" é feito pela própria entidade ({@code repositionBelow}).
 */
@Mod.EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BoneArmorSansFollowHandler {

    // jogador -> id da entidade osso que o segue
    private static final Map<UUID, Integer> FOLLOWERS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
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
    public static void onLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        FOLLOWERS.remove(event.getEntity().getUUID());
    }
}
