package com.frieren.magic;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class FocusRadialScreen extends Screen {
    private final List<FocusItem> availableFoci = new ArrayList<>();
    private int selectedIndex = -1; // -2 = центр (снять), >= 0 = индекс фокуса
    private static final double RADIUS = 62.0;

    public FocusRadialScreen() {
        super(Text.literal("Выбор набалдашника"));
    }

    @Override
    protected void init() {
        availableFoci.clear();
        if (client != null && client.player != null) {
            PlayerInventory inv = client.player.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.getItem() instanceof FocusItem focus && !availableFoci.contains(focus)) {
                    availableFoci.add(focus);
                }
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Намеренно НЕ вызываем renderBackground, чтобы мир оставался ярким и живым
        int cx = width / 2;
        int cy = height / 2;

        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double dist = Math.hypot(dx, dy);

        // Расчет выбора курсором
        if (dist <= 18) {
            selectedIndex = -2;
        } else if (!availableFoci.isEmpty()) {
            double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90.0;
            if (angle < 0) angle += 360.0;
            double slice = 360.0 / availableFoci.size();
            selectedIndex = (int) (angle / slice) % availableFoci.size();
        } else {
            selectedIndex = -1;
        }

        // Рисуем тонкие магические рунические кольца (прозрачное золото)
        drawHollowCircle(context, cx, cy, (int) RADIUS, 0x33E5C158);
        drawHollowCircle(context, cx, cy, (int) RADIUS + 18, 0x1AE5C158);
        drawHollowCircle(context, cx, cy, 22, 0x44FFFFFF);

        // 1. Центральный глиф (Снятие набалдашника)
        boolean centerHovered = (selectedIndex == -2);
        int centerBg = centerHovered ? 0x66E74C3C : 0x22111111;
        int centerRing = centerHovered ? 0xFFE74C3C : 0x44AAAAAA;
        drawHollowCircle(context, cx, cy, 14, centerRing);
        context.fill(cx - 10, cy - 10, cx + 10, cy + 10, centerBg);
        context.drawCenteredTextWithShadow(textRenderer, "✦", cx, cy - 4, centerHovered ? 0xFFE74C3C : 0x88EEEEEE);

        // 2. Слоты набалдашников по окружности
        int total = availableFoci.size();
        for (int i = 0; i < total; i++) {
            double angle = Math.toRadians((360.0 / total) * i - 90.0);
            int slotX = (int) (cx + Math.cos(angle) * RADIUS);
            int slotY = (int) (cy + Math.sin(angle) * RADIUS);

            boolean isHovered = (selectedIndex == i);

            // Мягкий полупрозрачный ореол
            int bg = isHovered ? 0x55FFD700 : 0x22000000;
            int ringColor = isHovered ? 0xFFFFD700 : 0x44E5C158;

            context.fill(slotX - 12, slotY - 12, slotX + 12, slotY + 12, bg);
            drawHollowCircle(context, slotX, slotY, 13, ringColor);

            FocusItem focus = availableFoci.get(i);
            context.drawItem(new ItemStack(focus), slotX - 8, slotY - 8);
        }

        // 3. Аккуратная подсказка под кольцом
        if (selectedIndex == -2) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Извлечь набалдашник").formatted(Formatting.RED), cx, cy + (int) RADIUS + 26, 0xFFFFFF);
        } else if (selectedIndex >= 0 && selectedIndex < availableFoci.size()) {
            context.drawCenteredTextWithShadow(textRenderer, availableFoci.get(selectedIndex).getDisplayName(), cx, cy + (int) RADIUS + 26, 0xFFFFFF);
        } else if (availableFoci.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("В инвентаре нет набалдашников").formatted(Formatting.DARK_GRAY), cx, cy + (int) RADIUS + 26, 0xFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    // Вспомогательный метод отрисовки тонкого геометрического кольца
    private void drawHollowCircle(DrawContext context, int cx, int cy, int radius, int argb) {
        int steps = 40;
        double prevX = cx + radius;
        double prevY = cy;
        for (int i = 1; i <= steps; i++) {
            double a = (2 * Math.PI / steps) * i;
            double curX = cx + Math.cos(a) * radius;
            double curY = cy + Math.sin(a) * radius;
            // Рисуем точку/пиксель по окружности
            context.fill((int) curX, (int) curY, (int) curX + 1, (int) curY + 1, argb);
            prevX = curX;
            prevY = curY;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // ЛКМ
            applySelection();
            return true;
        } else if (button == 1) { // ПКМ закрывает меню без изменений
            close();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void applySelection() {
        if (selectedIndex == -2) {
            FocusNetwork.sendSelectPacket("none");
            close();
        } else if (selectedIndex >= 0 && selectedIndex < availableFoci.size()) {
            FocusItem selected = availableFoci.get(selectedIndex);
            FocusNetwork.sendSelectPacket(Registries.ITEM.getId(selected).toString());
            close();
        }
    }

    @Override
    public boolean shouldPause() {
        return false; // Реальное время боя
    }
}