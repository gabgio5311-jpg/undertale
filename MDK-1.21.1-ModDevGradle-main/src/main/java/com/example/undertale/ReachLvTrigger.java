package com.example.undertale;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Trigger de advancement customizado: dispara quando o jogador ALCANÇA um LV mínimo.
 *
 * O LV não é um conceito vanilla (vive no NBT persistente — ver {@link LvData}), então
 * não há trigger nativo que sirva. Este é disparado do servidor toda vez que o LV é
 * sincronizado ({@link ModNetwork#syncLv}), passando o LV atual; a conquista casa quando
 * esse LV é maior ou igual ao {@code lv} pedido no JSON.
 *
 * <p>No NeoForge 1.21.1 os triggers viraram registrados (via {@link ModCriteria}) e usam
 * {@link Codec} para a instância — não há mais {@code getId()}/{@code serializeToJson}.
 * A condição no JSON continua {@code {"lv": N}}.
 */
public class ReachLvTrigger extends SimpleCriterionTrigger<ReachLvTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    /** Chamado pelo servidor com o LV atual do jogador. */
    public void trigger(ServerPlayer player, int lv) {
        this.trigger(player, instance -> instance.matches(lv));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, int lv)
            implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.INT.optionalFieldOf("lv", 1).forGetter(TriggerInstance::lv)
        ).apply(instance, TriggerInstance::new));

        /** Conquista completa quando o LV atual chegou (ou passou) o exigido. */
        public boolean matches(int currentLv) {
            return currentLv >= this.lv;
        }
    }
}
