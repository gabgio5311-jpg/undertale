package com.example.undertale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Tela de status do Indicador de LV (estilo caixa do Undertale).
 *
 * Mostra: LV atual, HP do jogador (lido em tempo real), almas necessárias para o
 * próximo LV, uma barra de progresso de almas e a frase de "determinação".
 *
 * É uma {@link Screen} simples (sem slots/container), então NÃO precisa registrar
 * nada — é só dar {@code Minecraft.setScreen(new IndicadorLvScreen())}
 * (ver {@link IndicadorLvClient}). Fecha no ESC.
 *
 * Os valores de LV/almas vêm de {@link ClientLvData}, sincronizado pelo servidor.
 */
public class IndicadorLvScreen extends Screen {

    private static final int PANEL_W = 200;
    private static final int PANEL_H = 130;

    // Cores ARGB.
    private static final int COR_BORDA = 0xFFFFFFFF;
    private static final int COR_FUNDO = 0xFF000000;
    private static final int COR_TITULO = 0xFFFFFF00; // amarelo "LV" do Undertale
    private static final int COR_TEXTO = 0xFFFFFFFF;
    private static final int COR_FRASE = 0xFFAAAAAA;
    private static final int COR_BARRA_FUNDO = 0xFF555555;
    private static final int COR_BARRA = 0xFFFFFF00;

    public IndicadorLvScreen() {
        super(Component.literal("Indicador de LV"));
    }

    // Não pausa o jogo: assim o HP mostrado atualiza em tempo real.
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int left = (this.width - PANEL_W) / 2;
        int top = (this.height - PANEL_H) / 2;
        int right = left + PANEL_W;
        int bottom = top + PANEL_H;
        int cx = this.width / 2;

        // Caixa: borda branca + fundo preto.
        g.fill(left - 2, top - 2, right + 2, bottom + 2, COR_BORDA);
        g.fill(left, top, right, bottom, COR_FUNDO);

        // LV/almas reais, vindos do cache sincronizado pelo servidor.
        LvData.LvInfo info = ClientLvData.info();

        int y = top + 12;

        // Título "* LV X *".
        g.drawCenteredString(this.font, Component.literal("* LV " + info.lv() + " *"),
                cx, y, COR_TITULO);
        y += 24;

        // HP (lido do jogador local).
        Player player = Minecraft.getInstance().player;
        int hp = player != null ? (int) Math.ceil(player.getHealth()) : 0;
        int maxHp = player != null ? (int) Math.ceil(player.getMaxHealth()) : 0;
        g.drawString(this.font, "HP   " + hp + " / " + maxHp, left + 16, y, COR_TEXTO);
        y += 16;

        // Almas necessárias (ou "MÁXIMO" no LV cap).
        String linhaAlmas = info.isMax()
                ? "LV máximo atingido"
                : "Almas necessárias: " + info.almasNecessarias();
        g.drawString(this.font, linhaAlmas, left + 16, y, COR_TEXTO);
        y += 18;

        // Barra de progresso de almas.
        int barLeft = left + 16;
        int barRight = right - 16;
        int barBottom = y + 8;
        g.fill(barLeft, y, barRight, barBottom, COR_BARRA_FUNDO);
        float frac = info.isMax()
                ? 1.0F
                : (info.almasNecessarias() > 0 ? (float) info.almasAtuais() / info.almasNecessarias() : 0.0F);
        frac = Math.max(0.0F, Math.min(1.0F, frac));
        int fillW = (int) ((barRight - barLeft) * frac);
        g.fill(barLeft, y, barLeft + fillW, barBottom, COR_BARRA);
        String textoBarra = info.isMax() ? "MAX" : info.almasAtuais() + "/" + info.almasNecessarias();
        g.drawCenteredString(this.font, Component.literal(textoBarra), cx, barBottom + 4, COR_TEXTO);
        y = barBottom + 22;

        // Frase de "determinação".
        g.drawCenteredString(this.font, Component.literal("Você está cheio de"), cx, y, COR_FRASE);
        y += 12;
        g.drawCenteredString(this.font, Component.literal("DETERMINAÇÃO."), cx, y, COR_FRASE);

        super.render(g, mouseX, mouseY, partialTick);
    }
}
