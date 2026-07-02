package com.example.undertale;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/**
 * Trigger de advancement customizado: dispara quando o jogador ALCANÇA um LV mínimo.
 *
 * O LV não é um conceito vanilla (vive no NBT persistente — ver {@link LvData}), então
 * não há trigger nativo que sirva. Este é disparado do servidor toda vez que o LV é
 * sincronizado ({@link ModNetwork#syncLv}), passando o LV atual; a conquista casa quando
 * esse LV é maior ou igual ao {@code lv} pedido no JSON.
 *
 * Registrado em {@link ModCriteria} no setup comum.
 */
public class ReachLvTrigger extends SimpleCriterionTrigger<ReachLvTrigger.Instance> {

    public static final ResourceLocation ID = new ResourceLocation(UndertaleMod.MOD_ID, "reach_lv");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        int lv = GsonHelper.getAsInt(json, "lv", 1);
        return new Instance(player, lv);
    }

    /** Chamado pelo servidor com o LV atual do jogador. */
    public void trigger(ServerPlayer player, int lv) {
        this.trigger(player, instance -> instance.matches(lv));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        private final int lv;

        public Instance(ContextAwarePredicate player, int lv) {
            super(ID, player);
            this.lv = lv;
        }

        /** Conquista completa quando o LV atual chegou (ou passou) o exigido. */
        public boolean matches(int currentLv) {
            return currentLv >= this.lv;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("lv", this.lv);
            return json;
        }
    }
}
