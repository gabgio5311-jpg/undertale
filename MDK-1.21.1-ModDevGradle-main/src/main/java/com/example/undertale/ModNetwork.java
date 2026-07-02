package com.example.undertale;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Rede do mod (sistema de PAYLOAD do NeoForge 1.21.1, que substitui o SimpleChannel do Forge).
 *
 * Teclas (keybinds) só existem no cliente, então quando o jogador aperta a tecla do
 * osso precisamos avisar o servidor — é ele quem cria a entidade e aplica o dano.
 */
@EventBusSubscriber(modid = UndertaleMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ThrowBonePayload.TYPE,
                ThrowBonePayload.STREAM_CODEC,
                ThrowBonePayload::handle);
        registrar.playToClient(
                LvSyncPayload.TYPE,
                LvSyncPayload.STREAM_CODEC,
                LvSyncPayload::handle);
    }

    /** Manda o total de almas atual de um jogador pro cliente dele (atualiza GUI/tooltip). */
    public static void syncLv(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new LvSyncPayload(LvData.getTotalAlmas(player)));
        // Checa as conquistas de LV (único ponto que roda em kill/login/respawn/dimensão).
        ModCriteria.REACH_LV.get().trigger(player, LvData.getInfo(player).lv());
    }

    /**
     * Pacote (cliente -> servidor) pedindo pra arremessar um osso.
     * Não carrega dados: o servidor lê a posição/rotação do próprio jogador que enviou.
     */
    public record ThrowBonePayload() implements CustomPacketPayload {

        public static final Type<ThrowBonePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(UndertaleMod.MOD_ID, "throw_bone"));

        // Pacote vazio: StreamCodec.unit sempre devolve a mesma instância.
        public static final StreamCodec<RegistryFriendlyByteBuf, ThrowBonePayload> STREAM_CODEC =
                StreamCodec.unit(new ThrowBonePayload());

        @Override
        public Type<ThrowBonePayload> type() {
            return TYPE;
        }

        public static void handle(ThrowBonePayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)) return;

                // Exige o set completo do Sans (igual ao bônus de voo).
                if (!SansArmorHandler.isWearingFullSet(player)) return;

                ServerLevel level = player.serverLevel();
                BoneArmorSansEntity bone =
                        new BoneArmorSansEntity(ModEntities.BONE_ARMOR_SANS.get(), level);

                // Nasce na frente do rosto e voa na direção em que o jogador olha.
                Vec3 look = player.getLookAngle();
                Vec3 spawn = player.getEyePosition().add(look.scale(1.0));
                bone.setPos(spawn.x, spawn.y, spawn.z);
                bone.setupThrow(player);   // modo THROWN: projétil que voa e some ao acertar

                level.addFreshEntity(bone);
            });
        }
    }

    /**
     * Pacote (servidor -> cliente) com o total de almas do jogador.
     * O cliente guarda em {@link ClientLvData} pra GUI/tooltip mostrarem o LV real.
     */
    public record LvSyncPayload(int total) implements CustomPacketPayload {

        public static final Type<LvSyncPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(UndertaleMod.MOD_ID, "lv_sync"));

        public static final StreamCodec<RegistryFriendlyByteBuf, LvSyncPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, LvSyncPayload::total,
                        LvSyncPayload::new);

        @Override
        public Type<LvSyncPayload> type() {
            return TYPE;
        }

        public static void handle(LvSyncPayload payload, IPayloadContext context) {
            // Roda no cliente (playToClient). ClientLvData é puro, então é seguro.
            context.enqueueWork(() -> ClientLvData.total = payload.total());
        }
    }
}
