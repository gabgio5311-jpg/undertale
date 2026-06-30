package com.example.undertale;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Canal de rede do mod. Teclas (keybinds) só existem no cliente, então quando o
 * jogador aperta a tecla do osso precisamos avisar o servidor — é ele quem cria a
 * entidade e aplica o dano.
 */
public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(UndertaleMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ThrowBonePacket.class,
                ThrowBonePacket::encode,
                ThrowBonePacket::decode,
                ThrowBonePacket::handle);
        CHANNEL.registerMessage(id++, LvSyncPacket.class,
                LvSyncPacket::encode,
                LvSyncPacket::decode,
                LvSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /** Manda o total de almas atual de um jogador pro cliente dele (atualiza GUI/tooltip). */
    public static void syncLv(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new LvSyncPacket(LvData.getTotalAlmas(player)));
    }

    /**
     * Pacote (cliente -> servidor) pedindo pra arremessar um osso.
     * Não carrega dados: o servidor lê a posição/rotação do próprio jogador que enviou.
     */
    public static class ThrowBonePacket {

        public ThrowBonePacket() {
        }

        public static void encode(ThrowBonePacket msg, FriendlyByteBuf buf) {
            // sem dados
        }

        public static ThrowBonePacket decode(FriendlyByteBuf buf) {
            return new ThrowBonePacket();
        }

        public static void handle(ThrowBonePacket msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context context = ctx.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;

                // Exige o set completo do Sans (igual ao bônus de voo).
                // Troque por uma checagem só do capacete se quiser permitir sem o set.
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
            context.setPacketHandled(true);
        }
    }

    /**
     * Pacote (servidor -> cliente) com o total de almas do jogador.
     * O cliente guarda em {@link ClientLvData} pra GUI/tooltip mostrarem o LV real.
     */
    public static class LvSyncPacket {

        private final int total;

        public LvSyncPacket(int total) {
            this.total = total;
        }

        public static void encode(LvSyncPacket msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.total);
        }

        public static LvSyncPacket decode(FriendlyByteBuf buf) {
            return new LvSyncPacket(buf.readVarInt());
        }

        public static void handle(LvSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context context = ctx.get();
            // Roda no cliente (PLAY_TO_CLIENT). ClientLvData é puro, então é seguro.
            context.enqueueWork(() -> ClientLvData.total = msg.total);
            context.setPacketHandled(true);
        }
    }
}
