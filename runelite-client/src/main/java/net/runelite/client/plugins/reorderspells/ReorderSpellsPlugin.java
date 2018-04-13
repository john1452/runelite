/*
 * Copyright (c) 2017, Marius <https://github.com/96NekoRS>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.reorderspells;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.runelite.api.widgets.WidgetConfig.DRAG;
import static net.runelite.api.widgets.WidgetConfig.DRAG_ON;

@Slf4j
@PluginDescriptor(name = "Reorder Spells")
public class ReorderSpellsPlugin extends Plugin
{

    static final String CONFIG_GROUP_KEY = "reorderspells";

    static final String CONFIG_UNLOCK_REORDERING_KEY = "unlockSpellReordering";

    static final String CONFIG_SPELL_ORDER_KEY = "spellOrder";

    private static final int SPELL_WIDTH = 24;

    private static final int SPELL_HEIGHT = 24;

    private static final int SPELL_X_OFFSET = 24;

    private static final int SPELL_Y_OFFSET = 24;

    private static final int SPELL_X_OFFSET_INITIAL = 12;

    private static final int SPELL_Y_OFFSET__INITIAL = 15;

    private static final int SPELL_COLUMN_COUNT = 7;

    private static final int SPELL_COUNT = 15;

    private static final List<WidgetInfo> NORMAL_WIDGET_INFO_LIST = ImmutableList.of(
            WidgetInfo.SPELL_HOME_TELEPORT,
            WidgetInfo.SPELL_WIND_STRIKE,
            WidgetInfo.SPELL_CONFUSE,
            WidgetInfo.SPELL_ENCHANT_BOLT,
            WidgetInfo.SPELL_WATER_STRIKE,
            WidgetInfo.SPELL_LVL1_ENCHANT,
            WidgetInfo.SPELL_EARTH_STRIKE,
            WidgetInfo.SPELL_WEAKEN,
            WidgetInfo.SPELL_FIRE_STRIKE,
            WidgetInfo.SPELL_BONES_BANANAS,
            WidgetInfo.SPELL_WIND_BOLT,
            WidgetInfo.SPELL_CURSE,
            WidgetInfo.SPELL_BIND,
            WidgetInfo.SPELL_LOW_ALCHEMY
    );

    private static final List<WidgetInfo> ANCIENT_WIDGET_INFO_LIST = ImmutableList.of(
            WidgetInfo.SPELL_HOME_TELEPORT,
            WidgetInfo.SPELL_WIND_STRIKE,
            WidgetInfo.SPELL_CONFUSE,
            WidgetInfo.SPELL_ENCHANT_BOLT,
            WidgetInfo.SPELL_WATER_STRIKE,
            WidgetInfo.SPELL_LVL1_ENCHANT,
            WidgetInfo.SPELL_EARTH_STRIKE,
            WidgetInfo.SPELL_WEAKEN,
            WidgetInfo.SPELL_FIRE_STRIKE,
            WidgetInfo.SPELL_BONES_BANANAS,
            WidgetInfo.SPELL_WIND_BOLT,
            WidgetInfo.SPELL_CURSE,
            WidgetInfo.SPELL_BIND,
            WidgetInfo.SPELL_LOW_ALCHEMY
    );

    private static final List<WidgetInfo> LUNAR_WIDGET_INFO_LIST = ImmutableList.of(
            WidgetInfo.SPELL_HOME_TELEPORT,
            WidgetInfo.SPELL_WIND_STRIKE,
            WidgetInfo.SPELL_CONFUSE,
            WidgetInfo.SPELL_ENCHANT_BOLT,
            WidgetInfo.SPELL_WATER_STRIKE,
            WidgetInfo.SPELL_LVL1_ENCHANT,
            WidgetInfo.SPELL_EARTH_STRIKE,
            WidgetInfo.SPELL_WEAKEN,
            WidgetInfo.SPELL_FIRE_STRIKE,
            WidgetInfo.SPELL_BONES_BANANAS,
            WidgetInfo.SPELL_WIND_BOLT,
            WidgetInfo.SPELL_CURSE,
            WidgetInfo.SPELL_BIND,
            WidgetInfo.SPELL_LOW_ALCHEMY
    );

    private static final String LOCK = "Lock";

    private static final String UNLOCK = "Unlock";

    private static final String MENU_TARGET = "Reordering";

    private static final WidgetMenuOption FIXED_MAGIC_TAB_LOCK = new WidgetMenuOption(LOCK,
            MENU_TARGET, WidgetInfo.FIXED_VIEWPORT_MAGIC_TAB);

    private static final WidgetMenuOption FIXED_MAGIC_TAB_UNLOCK = new WidgetMenuOption(UNLOCK,
            MENU_TARGET, WidgetInfo.FIXED_VIEWPORT_MAGIC_TAB);

    private static final WidgetMenuOption RESIZABLE_MAGIC_TAB_LOCK = new WidgetMenuOption(LOCK,
            MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_MAGIC_TAB);

    private static final WidgetMenuOption RESIZABLE_MAGIC_TAB_UNLOCK = new WidgetMenuOption(UNLOCK,
            MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_MAGIC_TAB);

    private static final WidgetMenuOption RESIZABLE_BOTTOM_LINE_MAGIC_TAB_LOCK = new WidgetMenuOption(LOCK,
            MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_MAGIC_TAB);

    private static final WidgetMenuOption RESIZABLE_BOTTOM_LINE_MAGIC_TAB_UNLOCK = new WidgetMenuOption(UNLOCK,
            MENU_TARGET, WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_MAGIC_TAB);

    @Inject
    private Client client;

    @Inject
    private ReorderSpellsConfig config;

    @Inject
    private MenuManager menuManager;

    private Magic.Normal[] normalSpellOrder;
    private Magic.Ancient[] ancientSpellOrder;
    private Magic.Lunar[] lunarSpellOrder;

    static String normalSpellOrderToString(Magic.Normal[] spellOrder)
    {
        return Arrays.stream(spellOrder)
                .map(Magic.Normal::name)
                .collect(Collectors.joining(","));
    }

    static String ancientSpellOrderToString(Magic.Ancient[] spellOrder)
    {
        return Arrays.stream(spellOrder)
                .map(Magic.Ancient::name)
                .collect(Collectors.joining(","));
    }

    static String lunarSpellOrderToString(Magic.Lunar[] spellOrder)
    {
        return Arrays.stream(spellOrder)
                .map(Magic.Lunar::name)
                .collect(Collectors.joining(","));
    }

    private static Magic.Normal[] stringToNormalSpellOrder(String string)
    {
        return Arrays.stream(string.split(","))
                .map(Magic.Normal::valueOf)
                .toArray(Magic.Normal[]::new);
    }

    private static Magic.Ancient[] stringToAncientSpellOrder(String string)
    {
        return Arrays.stream(string.split(","))
                .map(Magic.Ancient::valueOf)
                .toArray(Magic.Ancient[]::new);
    }

    private static Magic.Lunar[] stringToLunarSpellOrder(String string)
    {
        return Arrays.stream(string.split(","))
                .map(Magic.Lunar::valueOf)
                .toArray(Magic.Lunar[]::new);
    }

    private static int getSpellIndex(Widget widget)
    {
        int x = (widget.getOriginalX() - SPELL_X_OFFSET_INITIAL) / SPELL_X_OFFSET;
        int y = (widget.getOriginalY() - SPELL_Y_OFFSET__INITIAL) / SPELL_Y_OFFSET;
        return x + y * SPELL_COLUMN_COUNT;
    }

    private static void setWidgetPosition(Widget widget, int x, int y)
    {
        widget.setRelativeX(x);
        widget.setRelativeY(y);
        widget.setOriginalX(x);
        widget.setOriginalY(y);
    }

    @Provides
    ReorderSpellsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ReorderSpellsConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        refreshMagicTabOption();
        normalSpellOrder = stringToNormalSpellOrder(config.normalSpellOrder());
        ancientSpellOrder = stringToAncientSpellOrder(config.ancientSpellOrder());
        lunarSpellOrder = stringToLunarSpellOrder(config.lunarSpellOrder());
        reorderSpells();
    }

    @Override
    protected void shutDown() throws Exception
    {
        clearMagicTabMenus();
        normalSpellOrder = Magic.Normal.values();
        ancientSpellOrder = Magic.Ancient.values();
        lunarSpellOrder = Magic.Lunar.values();
        reorderSpells();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            reorderSpells();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (event.getGroup().equals(CONFIG_GROUP_KEY))
        {
            if (event.getKey().equals(CONFIG_SPELL_ORDER_KEY))
            {
                normalSpellOrder = stringToNormalSpellOrder(config.normalSpellOrder());
                ancientSpellOrder = stringToAncientSpellOrder(config.ancientSpellOrder());
                lunarSpellOrder = stringToLunarSpellOrder(config.lunarSpellOrder());
            }
            else if (event.getKey().equals(CONFIG_UNLOCK_REORDERING_KEY))
            {
                refreshMagicTabOption();
            }
            reorderSpells();
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        if (event.getGroupId() == WidgetID.MAGIC_NORMAL_GROUP_ID)
        {
            reorderSpells();
        }
    }

    @Subscribe
    public void onDraggingWidgetChanged(DraggingWidgetChanged event)
    {
        // is dragging widget and mouse button released
        if (event.isDraggingWidget() && client.getMouseCurrentButton() == 0)
        {
            Widget draggedWidget = client.getDraggedWidget();
            Widget draggedOnWidget = client.getDraggedOnWidget();
            if (draggedWidget != null && draggedOnWidget != null)
            {
                int draggedGroupId = WidgetInfo.TO_GROUP(draggedWidget.getId());
                int draggedOnGroupId = WidgetInfo.TO_GROUP(draggedOnWidget.getId());
                if (draggedGroupId != WidgetID.MAGIC_NORMAL_GROUP_ID || draggedOnGroupId != WidgetID.MAGIC_NORMAL_GROUP_ID
                        || draggedOnWidget.getWidth() != SPELL_WIDTH || draggedOnWidget.getHeight() != SPELL_HEIGHT)
                {
                    return;
                }
                // reset dragged on widget to prevent sending a drag widget packet to the server
                client.setDraggedOnWidget(null);

                int fromSpellIndex = getSpellIndex(draggedWidget);
                int toSpellIndex = getSpellIndex(draggedOnWidget);

                MagicTabState magicTabState = getMagicTabState();
                if (magicTabState == MagicTabState.NORMAL)
                {
                    Magic.Normal normal = normalSpellOrder[toSpellIndex];
                    normalSpellOrder[toSpellIndex] = normalSpellOrder[fromSpellIndex];
                    normalSpellOrder[fromSpellIndex] = normal;
                }
                else if (magicTabState == MagicTabState.ANCIENT)
                {
                    Magic.Ancient ancient = ancientSpellOrder[toSpellIndex];
                    ancientSpellOrder[toSpellIndex] = ancientSpellOrder[fromSpellIndex];
                    ancientSpellOrder[fromSpellIndex] = ancient;
                }
                else if (magicTabState == MagicTabState.LUNAR)
                {
                    Magic.Lunar lunar = lunarSpellOrder[toSpellIndex];
                    lunarSpellOrder[toSpellIndex] = lunarSpellOrder[fromSpellIndex];
                    lunarSpellOrder[fromSpellIndex] = lunar;
                }

                save();
            }
        }
    }

    @Subscribe
    public void onWidgetMenuOptionClicked(WidgetMenuOptionClicked event)
    {
        if (event.getWidget() == WidgetInfo.FIXED_VIEWPORT_MAGIC_TAB
                || event.getWidget() == WidgetInfo.RESIZABLE_VIEWPORT_MAGIC_TAB
                || event.getWidget() == WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_MAGIC_TAB)
        {
            config.unlockSpellReordering(event.getMenuOption().equals(UNLOCK));
        }
    }

    private void clearMagicTabMenus()
    {
        menuManager.removeManagedCustomMenu(FIXED_MAGIC_TAB_LOCK);
        menuManager.removeManagedCustomMenu(RESIZABLE_MAGIC_TAB_LOCK);
        menuManager.removeManagedCustomMenu(RESIZABLE_BOTTOM_LINE_MAGIC_TAB_LOCK);
        menuManager.removeManagedCustomMenu(FIXED_MAGIC_TAB_UNLOCK);
        menuManager.removeManagedCustomMenu(RESIZABLE_MAGIC_TAB_UNLOCK);
        menuManager.removeManagedCustomMenu(RESIZABLE_BOTTOM_LINE_MAGIC_TAB_UNLOCK);
    }

    private void refreshMagicTabOption()
    {
        clearMagicTabMenus();
        if (config.unlockSpellReordering())
        {
            menuManager.addManagedCustomMenu(FIXED_MAGIC_TAB_LOCK);
            menuManager.addManagedCustomMenu(RESIZABLE_MAGIC_TAB_LOCK);
            menuManager.addManagedCustomMenu(RESIZABLE_BOTTOM_LINE_MAGIC_TAB_LOCK);
        }
        else
        {
            menuManager.addManagedCustomMenu(FIXED_MAGIC_TAB_UNLOCK);
            menuManager.addManagedCustomMenu(RESIZABLE_MAGIC_TAB_UNLOCK);
            menuManager.addManagedCustomMenu(RESIZABLE_BOTTOM_LINE_MAGIC_TAB_UNLOCK);
        }
    }

    private MagicTabState getMagicTabState()
    {
        HashTable componentTable = client.getComponentTable();
        for (Node node : componentTable.getNodes())
        {
            WidgetNode widgetNode = (WidgetNode) node;
            if (widgetNode.getId() == WidgetID.MAGIC_NORMAL_GROUP_ID)
            {
                return MagicTabState.NORMAL;
            }
            else if (false)
            {
                return MagicTabState.ANCIENT;
            }
            else if (false)
            {
                return MagicTabState.LUNAR;
            }
        }
        return MagicTabState.NONE;
    }

    private void save()
    {
        config.normalSpellOrder(normalSpellOrderToString(normalSpellOrder));
        config.ancientSpellOrder(ancientSpellOrderToString(ancientSpellOrder));
        config.lunarSpellOrder(lunarSpellOrderToString(lunarSpellOrder));
    }

    private void reorderSpells()
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        MagicTabState magicTabState = getMagicTabState();

        if (magicTabState == MagicTabState.NORMAL)
        {
            List<Widget> spellWidgets = NORMAL_WIDGET_INFO_LIST.stream()
                    .map(client::getWidget)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (spellWidgets.size() != NORMAL_WIDGET_INFO_LIST.size())
            {
                return;
            }

            for (int index = 0; index < normalSpellOrder.length; index++)
            {
                Magic.Normal spell = normalSpellOrder[index];
                Widget magicWidget = spellWidgets.get(spell.ordinal());

                int widgetConfig = magicWidget.getClickMask();
                if (config.unlockSpellReordering())
                {
                    // allow dragging of this widget
                    widgetConfig |= DRAG;
                    // allow this widget to be dragged on
                    widgetConfig |= DRAG_ON;
                }
                else
                {
                    // remove drag flag
                    widgetConfig &= ~DRAG;
                    // remove drag on flag
                    widgetConfig &= ~DRAG_ON;
                }
                magicWidget.setClickMask(widgetConfig);

                int x = index % SPELL_COLUMN_COUNT;
                int y = index / SPELL_COLUMN_COUNT;
                int widgetX = (x * SPELL_X_OFFSET) + SPELL_X_OFFSET_INITIAL;
                int widgetY = (y * SPELL_Y_OFFSET) + SPELL_Y_OFFSET__INITIAL;
                setWidgetPosition(magicWidget, widgetX, widgetY);
            }
        }
        else if (magicTabState == MagicTabState.ANCIENT)
        {
            List<Widget> spellWidgets = ANCIENT_WIDGET_INFO_LIST.stream()
                    .map(client::getWidget)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (spellWidgets.size() != ANCIENT_WIDGET_INFO_LIST.size())
            {
                return;
            }

            for (int index = 0; index < ancientSpellOrder.length; index++)
            {
                Magic.Ancient spell = ancientSpellOrder[index];
                Widget magicWidget = spellWidgets.get(spell.ordinal());

                int widgetConfig = magicWidget.getClickMask();
                if (config.unlockSpellReordering())
                {
                    // allow dragging of this widget
                    widgetConfig |= DRAG;
                    // allow this widget to be dragged on
                    widgetConfig |= DRAG_ON;
                }
                else
                {
                    // remove drag flag
                    widgetConfig &= ~DRAG;
                    // remove drag on flag
                    widgetConfig &= ~DRAG_ON;
                }
                magicWidget.setClickMask(widgetConfig);

                int x = index % SPELL_COLUMN_COUNT;
                int y = index / SPELL_COLUMN_COUNT;
                int widgetX = (x * SPELL_X_OFFSET) + SPELL_X_OFFSET_INITIAL;
                int widgetY = (y * SPELL_Y_OFFSET) + SPELL_Y_OFFSET__INITIAL;
                setWidgetPosition(magicWidget, widgetX, widgetY);
            }
        }
        else if (magicTabState == MagicTabState.LUNAR)
        {
            List<Widget> spellWidgets = LUNAR_WIDGET_INFO_LIST.stream()
                    .map(client::getWidget)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (spellWidgets.size() != LUNAR_WIDGET_INFO_LIST.size())
            {
                return;
            }

            for (int index = 0; index < lunarSpellOrder.length; index++)
            {
                Magic.Lunar spell = lunarSpellOrder[index];
                Widget magicWidget = spellWidgets.get(spell.ordinal());

                int widgetConfig = magicWidget.getClickMask();
                if (config.unlockSpellReordering())
                {
                    // allow dragging of this widget
                    widgetConfig |= DRAG;
                    // allow this widget to be dragged on
                    widgetConfig |= DRAG_ON;
                }
                else
                {
                    // remove drag flag
                    widgetConfig &= ~DRAG;
                    // remove drag on flag
                    widgetConfig &= ~DRAG_ON;
                }
                magicWidget.setClickMask(widgetConfig);

                int x = index % SPELL_COLUMN_COUNT;
                int y = index / SPELL_COLUMN_COUNT;
                int widgetX = (x * SPELL_X_OFFSET) + SPELL_X_OFFSET_INITIAL;
                int widgetY = (y * SPELL_Y_OFFSET) + SPELL_Y_OFFSET__INITIAL;
                setWidgetPosition(magicWidget, widgetX, widgetY);
            }
        }
    }
}
