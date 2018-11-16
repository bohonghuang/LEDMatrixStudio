package org.coco24.matrixstudio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.*
import com.kotcrab.vis.ui.widget.Tooltip
import com.kotcrab.vis.ui.widget.color.ColorPicker
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter
import kotlin.math.min

class MainScreen: Screen
{
    object R
    {
        var SCALE = 1F
    }
    lateinit var ledPannelScrollPane: VisScrollPane
    var pannelSize = 400f * R.SCALE
    set(value)
    {
        field = value
        val ledPannelCell = (ledPannelScrollPane.actor as Table).cells[0]
        ledPannelCell.size(field)
        (ledPannelScrollPane.actor as Table).invalidate()
        (ledPannelScrollPane.actor as Table).pack()
        ledPannelScrollPane.actor = ledPannelScrollPane.actor
    }
    var ledPannelTouchedDown = false
    lateinit var lightsToolsButtonGroup: ButtonGroup<VisImageButton>
    val ledPannel = object : LEDPannel()
    {
        override fun updateLed(x: Float, y: Float, touched: Boolean): LED?
        {
            ledPannelTouchedDown = touched
            return super.updateLed(x, y, touched)
        }
        override fun putDown(firstLed: LED?, led: LED)
        {
            tools[lightsToolsButtonGroup.checkedIndex].operate(firstLed, led)
        }
    }
    val tools = Array<Tools.Tool>()
    lateinit var lightsFrontColorImage: Image
    lateinit var lightsBackColorImage: Image
    val frontColor = object : Color()
    {
        override fun set(color: Color?): Color
        {
            lightsFrontColorImage.color = color
            return super.set(color)
        }
    }
    val backColor = object : Color()
    {
        override fun set(color: Color?): Color
        {
            lightsBackColorImage.color = color
            return super.set(color)
        }
    }
    init
    {
        tools.add(Tools.Pen(ledPannel, frontColor))
        tools.add(Tools.Eraser(ledPannel))
        tools.add(Tools.Line(ledPannel, frontColor))
        tools.add(Tools.Rectangle(ledPannel, frontColor, backColor))
        tools.add(Tools.Bucket(ledPannel, frontColor))

        R.SCALE = MyGdxGame.R.PPI / Gdx.graphics.ppiY
    }

    var ctrl = false
    var alt = false
    var shift = false

    val stage = object : Stage(ScreenViewport())
    {
        val vector2 = Vector2()
        override fun mouseMoved(screenX: Int, screenY: Int): Boolean
        {
            vector2.x = screenX.toFloat()
            vector2.y = screenY.toFloat()
            screenToStageCoordinates(vector2)
            ledPannel.stageToLocalCoordinates(vector2)
            val mx = vector2.x
            val my = vector2.y
            if(mx < 0 || my < 0 || mx > ledPannel.minWidth || my > ledPannel.minHeight)
                (ledPannel.listeners[0] as InputListener).mouseMoved(null, vector2.x, vector2.y)
            return super.mouseMoved(screenX, screenY)
        }
        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean
        {
            vector2.x = screenX.toFloat()
            vector2.y = screenY.toFloat()
            screenToStageCoordinates(vector2)
            ledPannel.stageToLocalCoordinates(vector2)
            if(ledPannelTouchedDown)
                (ledPannel.listeners[0] as InputListener).touchDragged(null, vector2.x, vector2.y, pointer)
            return super.touchDragged(screenX, screenY, pointer)
        }
        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean
        {
            if(ledPannelTouchedDown)
            (ledPannel.listeners[0] as InputListener).touchUp(null, 0f, 0f, 0, 0)
            ledPannelTouchedDown = false
            return super.touchUp(screenX, screenY, pointer, button)
        }

        override fun keyDown(keyCode: Int): Boolean
        {
            when(keyCode)
            {
                Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT -> ctrl = true
                Input.Keys.ALT_LEFT, Input.Keys.ALT_RIGHT -> alt = true
                Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT -> shift = true
            }
            return super.keyDown(keyCode)
        }

        override fun keyUp(keyCode: Int): Boolean
        {
            when(keyCode)
            {
                Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT -> ctrl = false
                Input.Keys.ALT_LEFT, Input.Keys.ALT_RIGHT -> alt = false
                Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT -> shift = false
            }
            return super.keyUp(keyCode)
        }
    }

    val rootTable = Table()
    val mainTable = Table()

    override fun hide()
    {

    }

    override fun show()
    {
        val menuBar = MenuBar();
        val subMenuBar = VisTable()
        val fileMenu = Menu("文件");
        val editMenu = Menu("编辑");
        val effectMenu = Menu("效果");
        val deviceMenu = Menu("设备")
        val helpMenu = Menu("帮助");
        menuBar.addMenu(fileMenu);
        menuBar.addMenu(editMenu);
        menuBar.addMenu(effectMenu);
        menuBar.addMenu(deviceMenu);
        menuBar.addMenu(helpMenu);

        subMenuBar.background = VisUI.getSkin().get(MenuBar.MenuBarStyle::class.java).background
        val subMenuBarLeftTable = VisTable()
        subMenuBarLeftTable.add("选择设备：").expandX()
        val deviceSelectBox = VisSelectBox<String>()
        deviceSelectBox.setItems("Launchpad MKII", "设备2", "设备3")
        subMenuBarLeftTable.add(deviceSelectBox).minWidth(200f * R.SCALE)
        subMenuBarLeftTable.cells.forEach {
            it.left().pad(5f * R.SCALE)
        }
        subMenuBarLeftTable.pack()
        subMenuBar.add(subMenuBarLeftTable).expandX().left()
        subMenuBar.pack()

        val openFileMenuItem = MenuItem("打开工程...");
        val closeFileMenuItem = MenuItem("关闭工程");
        val exitFileMenuItem = MenuItem("退出");
        exitFileMenuItem.addListener(object : ChangeListener()
        {
            override fun changed(event: ChangeEvent?, actor: Actor?)
            {
                Gdx.app.exit()
            }
        })
        fileMenu.addItem(openFileMenuItem);
        fileMenu.addItem(closeFileMenuItem);
        fileMenu.addSeparator()
        fileMenu.addItem(exitFileMenuItem);

        val deviceSelectMenuItem = MenuItem("选择设备")
        val devicePreferencesMenuItem = MenuItem("设备配置...")
        val deviceKeysRemapMenuItem = MenuItem("键位映射...")
        val devicePowerMenuItem = MenuItem("电源");
        val deviceSleepMenuSubItem = MenuItem("待机")
        val deviceWakeMenuSubItem = MenuItem("唤醒")
        val deviceResetMenuSubItem = MenuItem("复位")
        devicePowerMenuItem.subMenu = PopupMenu()
        devicePowerMenuItem.subMenu.addItem(deviceSleepMenuSubItem)
        devicePowerMenuItem.subMenu.addItem(deviceWakeMenuSubItem)
        devicePowerMenuItem.subMenu.addItem(deviceResetMenuSubItem)

        deviceMenu.addItem(deviceSelectMenuItem)
        deviceMenu.addItem(deviceKeysRemapMenuItem)
        deviceMenu.addItem(devicePreferencesMenuItem);
        deviceMenu.addItem(devicePowerMenuItem);

        val aboutMenuItem = MenuItem("关于...")
        aboutMenuItem.setShortcut(Input.Keys.F1)
        aboutMenuItem.addListener(object : ChangeListener()
        {
            override fun changed(event: ChangeEvent?, actor: Actor?)
            {
                val aboutWindow = AboutWindow()
                stage.addActor(aboutWindow)
                aboutWindow.fadeIn()
            }
        })
        helpMenu.addItem(aboutMenuItem)

        val mainMultiSplitPane = MultiSplitPane(false)

        val mainLeftSplitPane = MultiSplitPane(true)

        val lightsSeqTable = VisTable()
        lightsSeqTable.add("灯光片段").left().row()
        val lightsSeqList = VisList<String>()
        lightsSeqList.setItems("灯光1", "灯光2", "灯光3")
        lightsSeqTable.add(lightsSeqList).expand().top().fillX().row()
        lightsSeqTable.cells.forEach {
            it.pad(5f * R.SCALE)
        }
        lightsSeqTable.pack()

        val lightsPatternTable = VisTable()
        lightsPatternTable.add("颜色配置").left().row()
        val lightsPatternList = VisList<String>()
        lightsPatternList.setItems("Launchpad MKII/Pro", "Launchpad S")
        lightsPatternTable.add(lightsPatternList).expand().top().fillX().row()
        lightsPatternTable.cells.forEach {
            it.pad(5f * R.SCALE)
        }
        val colorPixmap = Pixmap(1, 1, Pixmap.Format.RGB888)
        colorPixmap.setColor(Color.WHITE)
        colorPixmap.drawPixel(0, 0)
        val colorDrawable = TextureRegionDrawable(TextureRegion(Texture(colorPixmap)))
        colorDrawable.minHeight = 40f * R.SCALE
        colorDrawable.minWidth = 50f * R.SCALE

        val lightsToolsTable = VisTable()
        lightsToolsTable.add("工具").left().row()
        lightsToolsButtonGroup = ButtonGroup<VisImageButton>()
        val checkableImageButtonStyle = VisImageButton.VisImageButtonStyle(VisUI.getSkin().get(VisImageButton.VisImageButtonStyle::class.java))
        checkableImageButtonStyle.checked = checkableImageButtonStyle.down

        for(i in 0 until tools.size)
        {
            val toolIcon = Image(Texture("logo_m.png"))
            toolIcon.drawable.minHeight = 32f * R.SCALE
            toolIcon.drawable.minWidth = 32f * R.SCALE
            val lightsToolButton = VisImageButton(checkableImageButtonStyle)
            lightsToolButton.add(toolIcon)
            lightsToolButton.pack()
            lightsToolsButtonGroup.add(lightsToolButton)
            Tooltip.Builder(tools[i].name).target(lightsToolButton).build()
            val cell = lightsToolsTable.add(lightsToolButton).expand()
            if((i + 1) % 4 == 0)
                cell.row()
        }

        lightsToolsTable.pack()

        val lightsColorTable = VisTable()
        lightsColorTable.add("颜色").left().row()
        val lightsColorStack = object : WidgetGroup()
        {
            override fun getPrefHeight(): Float
            {
                return 60f * R.SCALE
            }

            override fun getPrefWidth(): Float
            {
                return 70f * R.SCALE
            }
        }
        val lightsFrontColor = VisImageButton("default")
        lightsFrontColorImage = Image(colorDrawable)
        frontColor.set(Color.RED)
        lightsFrontColorImage.color = frontColor
        lightsFrontColor.add(lightsFrontColorImage)
        lightsFrontColor.pack()
        lightsFrontColor.addListener(object : ChangeListener()
        {
            override fun changed(event: ChangeEvent?, actor: Actor?)
            {
                changeColor(frontColor)
            }
        })
        lightsFrontColor.background = VisUI.getSkin().get(VisTextButton.VisTextButtonStyle::class.java).focusBorder
        val lightsBackColor = VisImageButton("default")
        lightsBackColorImage = Image(colorDrawable)

        backColor.set(Color.WHITE)
        lightsBackColorImage.color = backColor
        lightsBackColor.add(lightsBackColorImage)
        lightsBackColor.pack()
        lightsBackColor.addListener(object : ChangeListener()
        {
            override fun changed(event: ChangeEvent?, actor: Actor?)
            {
                changeColor(backColor)
            }
        })
        lightsColorStack.addActor(lightsBackColor)
        lightsColorStack.addActor(lightsFrontColor)
        lightsFrontColor.setPosition(0f, 20f)
        lightsBackColor.setPosition(20f, 0f)
        lightsColorStack.pack()
        lightsColorTable.add(lightsColorStack).expand().top()

        mainLeftSplitPane.setWidgets(lightsToolsTable, lightsColorTable, lightsPatternTable, lightsSeqTable)
        mainLeftSplitPane.pack()

        val ledPannelTable = VisTable()
        ledPannelTable.add(ledPannel).maxSize(pannelSize).pad(100f).expand().center()
        ledPannelTable.pack()
        ledPannelScrollPane = VisScrollPane(ledPannelTable)
        ledPannelScrollPane.listeners.removeIndex(1)
        ledPannelScrollPane.addListener(object : InputListener()
        {
            override fun scrolled(event: InputEvent?, x: Float, y: Float, amount: Int): Boolean
            {
                if(ctrl)
                {
                    pannelSize -= amount * 20f
                }
                else
                {
                    if (ledPannelScrollPane.isScrollY)
                        ledPannelScrollPane.scrollY += 80 * amount
                    else if (ledPannelScrollPane.isScrollX) //
                        ledPannelScrollPane.scrollX += 80 * amount
                }
                return true
            }
        })
        ledPannelScrollPane.setFadeScrollBars(false)
        ledPannelScrollPane.setFlickScroll(false)
//        ledPannelScrollPane.setSmoothScrolling(false)

        mainMultiSplitPane.setWidgets(mainLeftSplitPane, ledPannelScrollPane)
        mainMultiSplitPane.setSplit(0, 0.25f)
        mainTable.add(mainMultiSplitPane).grow()

        mainTable.cells.forEach {
            it.pad(5f * R.SCALE)
        }

        rootTable.setFillParent(true)
        rootTable.add(menuBar.table).fillX().row()
        rootTable.add(subMenuBar).fillX().row()
        rootTable.add(mainTable).grow()
        rootTable.pack()
        stage.addActor(rootTable);

        Gdx.input.inputProcessor = stage;
        (stage.viewport as ScreenViewport).unitsPerPixel = R.SCALE
    }
    fun changeColor(color: Color)
    {
        var _colorPicker: ColorPicker? = null
        val colorPicker = ColorPicker(if(color == frontColor) "选择前景色" else "选择背景色", object : ColorPickerAdapter()
        {
            override fun finished(newColor: Color?)
            {
                if(newColor == null) return
                color.set(newColor)
                _colorPicker?.remove()
            }
        })
        var loopCount = 0
        val buttonTexts = Array.with("重置", "确定", "取消")
        (colorPicker.cells[colorPicker.cells.size - 1].actor as Table).cells.forEach {
            (it.actor as VisTextButton).setText(buttonTexts[loopCount])
            loopCount ++
        }
        _colorPicker = colorPicker
        colorPicker.color = color
        stage.addActor(colorPicker)



    }
    override fun render(delta: Float)
    {
        stage.act();
        stage.draw();
    }

    override fun pause()
    {

    }

    override fun resume()
    {
    }

    override fun resize(width: Int, height: Int)
    {
        if (width == 0 && height == 0) return  //see https://github.com/libgdx/libgdx/issues/3673#issuecomment-177606278
        stage.viewport.update(width, height, true)
    }

    override fun dispose()
    {
    }

}