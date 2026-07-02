package com.example.undertale;

import net.minecraft.client.Minecraft;

/**
 * Ponte client-only do Indicador de LV. Fica numa classe separada e só é chamada
 * a partir do ramo {@code level.isClientSide()} de {@link IndicadorLvItem#use}, de modo
 * que nada de cliente (a classe {@link Minecraft}) seja carregado num servidor dedicado.
 */
public class IndicadorLvClient {

    public static void open() {
        Minecraft.getInstance().setScreen(new IndicadorLvScreen());
    }
}
