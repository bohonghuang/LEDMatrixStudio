package org.coco24.matrixstudio

import aurelienribon.tweenengine.Timeline
import aurelienribon.tweenengine.Tween
import aurelienribon.tweenengine.TweenManager
import aurelienribon.tweenengine.equations.Linear
import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.*
import com.kotcrab.vis.ui.widget.Tooltip
import com.kotcrab.vis.ui.widget.color.ColorPicker
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter
import org.coco24.matrixstudio.MyGdxGame.R
import java.lang.IllegalStateException

class MainScreen: Screen, PagesSurface
{


    lateinit var ledPannelScrollPane: VisScrollPane

    var pannelWidth = 10
    var pannelHeight = 10

    var pannelSize = 500f * R.SCALE
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
            if (mx < 0 || my < 0 || mx > ledPannel.minWidth || my > ledPannel.minHeight) (ledPannel.listeners[0] as InputListener).mouseMoved(null, vector2.x, vector2.y)
            return super.mouseMoved(screenX, screenY)
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean
        {
            vector2.x = screenX.toFloat()
            vector2.y = screenY.toFloat()
            screenToStageCoordinates(vector2)
            ledPannel.stageToLocalCoordinates(vector2)
            if (ledPannelTouchedDown) (ledPannel.listeners[0] as InputListener).touchDragged(null, vector2.x, vector2.y, pointer)
            return super.touchDragged(screenX, screenY, pointer)
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean
        {
            if (ledPannelTouchedDown) (ledPannel.listeners[0] as InputListener).touchUp(null, 0f, 0f, 0, 0)
            ledPannelTouchedDown = false
            return super.touchUp(screenX, screenY, pointer, button)
        }

        override fun keyDown(keyCode: Int): Boolean
        {
            when (keyCode)
            {
                Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT -> ctrl = true
                Input.Keys.ALT_LEFT, Input.Keys.ALT_RIGHT -> alt = true
                Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT -> shift = true
            }
            return super.keyDown(keyCode)
        }

        override fun keyUp(keyCode: Int): Boolean
        {
            when (keyCode)
            {
                Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT -> ctrl = false
                Input.Keys.ALT_LEFT, Input.Keys.ALT_RIGHT -> alt = false
                Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT -> shift = false
            }
            return super.keyUp(keyCode)
        }
    }
    val ledPannel = object : LEDPannelEntity(pannelWidth, pannelHeight)
    {
        override fun updateLed(x: Float, y: Float, touched: Boolean): LEDEntity?
        {
            ledPannelTouchedDown = touched
            return super.updateLed(x, y, touched)
        }

        override fun putDown(firstLedEntity: LEDEntity?, ledEntity: LEDEntity)
        {
            tools[lightsToolsButtonGroup.checkedIndex].operate(firstLedEntity, ledEntity)
        }
    }
    val tools = Array<Tools.Tool>()
    lateinit var lightsFrontColorImage: Image
    lateinit var lightsBackColorImage: Image
    val pages = object : Array<LEDPage>()
    {
        fun syncTotalFrames()
        {
            totalFrameLabel.setText(size.toString())
        }
        override fun add(value: LEDPage?)
        {
            super.add(value)
            syncTotalFrames()
        }

        override fun removeIndex(index: Int): LEDPage
        {
            val ret = super.removeIndex(index)
            syncTotalFrames()
            return ret
        }

        override fun insert(index: Int, value: LEDPage?)
        {
            super.insert(index, value)
            syncTotalFrames()
        }
    }

    var currentPageIndex = 0
        set(value)
        {
            if (value >= 0 && value < pages.size) field = value
        }

    lateinit var totalFrameLabel: VisLabel
    lateinit var currentFrameTextField: VisTextField

    var ctrl = false
    var alt = false
    var shift = false
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
    }



    class LEDCell()
    {
        val color = Color(Color.BLACK)
        fun setColor(newColor: Color) = color.set(newColor)
    }

    class LEDPage(private var width: Int, private var height: Int)
    {
        val leds = Array<Array<LEDCell>>()

        init
        {
            for (i in 0 until height)
            {
                val array = Array<LEDCell>()
                for (j in 0 until width)
                {
                    array.add(LEDCell())
                }
                leds.add(array)
            }
        }

        fun getWidth(): Int = width
        fun getHeight(): Int = height
    }



    var currentPage: LEDPage
        get()
        {
            return pages[currentPageIndex]
        }
        set(value)
        {
            currentPageIndex = pages.indexOf(value)
        }

    override fun changePage(page: Int)
    {

        pannelToPage()
        currentPageIndex = page
        pageToPannel()

        currentFrameTextField.text = (currentPageIndex + 1).toString()
        currentFrameTextField.isInputValid = true
    }
    override fun getPagesSize(): Int
    {
        return pages.size
    }

    override fun getCurrentPage(): Int
    {
        return currentPageIndex
    }
    fun pageToPannel() = syncPannelAndPage(true)
    fun pannelToPage() = syncPannelAndPage(false)
    private fun syncPannelAndPage(pageToPannel: Boolean)
    {
        val currentPage = currentPage
        if (currentPage.getWidth() == ledPannel.WIDTH && currentPage.getHeight() == ledPannel.HEIGHT)
        {
            for (i in 0 until currentPage.getHeight())
            {
                for (j in 0 until currentPage.getWidth())
                {
                    if (pageToPannel) ledPannel.leds[i][j].color = currentPage.leds[i][j].color
                    else currentPage.leds[i][j].setColor(ledPannel.leds[i][j].color)
                }
            }
        }
        else
        {
            throw IllegalStateException("ERROR")
        }
    }

    enum class PlayerStatus
    {
        Playing, Paused, Stopped
    }
    val playerTweenManager = TweenManager()
    var playerStatus = PlayerStatus.Stopped
    var playerSpeed = 96
    val disabledWidgetsWhenPlaying = Array<Actor>()
    override fun show()
    {
        val tableBackground = (VisUI.getSkin().get(VisImageButton.VisImageButtonStyle::class.java).up as NinePatchDrawable).tint(if(R.PPI < 128) Color.WHITE else Color.LIGHT_GRAY)

        val rootTable = Table()
        run {
            val menuBar = MenuBar();
            run {
                val fileMenu = Menu("文件");
                run {
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
                }
                val editMenu = Menu("编辑");
                run {

                }
                val viewMenu = Menu("视图")
                run {
                    val viewZoomInMenuItem = MenuItem("放大")
                    viewZoomInMenuItem.addListener(object : ChangeListener()
                    {
                        override fun changed(event: ChangeEvent?, actor: Actor?)
                        {
                            pannelSize += 60 * R.SCALE
                        }
                    })
                    val viewZoomOutMenuItem = MenuItem("缩小")
                    viewZoomOutMenuItem.addListener(object : ChangeListener()
                    {
                        override fun changed(event: ChangeEvent?, actor: Actor?)
                        {
                            pannelSize -= 60 * R.SCALE
                        }
                    })
                    val viewPannelStyle = MenuItem("矩阵面板样式")
                    viewMenu.addItem(viewZoomInMenuItem)
                    viewMenu.addItem(viewZoomOutMenuItem)
                    viewMenu.addSeparator()
                    viewMenu.addItem(viewPannelStyle)
                }
                val effectMenu = Menu("效果");
                run {

                }
                val deviceMenu = Menu("设备")
                run {
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
                }
                val helpMenu = Menu("帮助");
                run {
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
                }

                menuBar.addMenu(fileMenu);
                menuBar.addMenu(editMenu);
                menuBar.addMenu(viewMenu);
                menuBar.addMenu(effectMenu);
                menuBar.addMenu(deviceMenu);
                menuBar.addMenu(helpMenu);
            }
            val subMenuBar = VisTable()
            run {
                val subMenuBarLeftTable = VisTable()
                subMenuBarLeftTable.add("选择设备：").expandX()
                val deviceSelectBox = VisSelectBox<String>()
                deviceSelectBox.setItems("Launchpad MKII", "设备2", "设备3")
                subMenuBarLeftTable.add(deviceSelectBox).minWidth(200f * R.SCALE)
                val playerControllerSpeedTextField = VisTextField("96")
                playerControllerSpeedTextField.textFieldFilter = object : VisTextField.TextFieldFilter.DigitsOnlyFilter()
                {
                    override fun acceptChar(textField: VisTextField, c: Char): Boolean
                    {
                        if(super.acceptChar(textField, c))
                        {
                            return true
                        }else
                        {
                            playerSpeed = textField.text.toIntOrNull()?:0
                            return false
                        }
                    }
                }

//                subMenuBarLeftTable.addSeparator(true)
                subMenuBarLeftTable.add("速度:")
                subMenuBarLeftTable.add(playerControllerSpeedTextField).width(64f * R.SCALE)
                val playerControllerPlayButton = VisImageButton("default")
                playerControllerPlayButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        playerStatus = PlayerStatus.Playing
                        Gdx.graphics.isContinuousRendering = true
                        playerControllerSpeedTextField.textFieldFilter.acceptChar(playerControllerSpeedTextField, '\n')
                        playerTweenManager.killAll()
                        Timeline.createSequence()
                                .push(Tween.set(this@MainScreen, 0).target(0f))
                                .push(Tween.to(this@MainScreen, 0, 1f).target(pages.size - 0.00001f).ease(Linear.INOUT))
                                .repeat(Tween.INFINITY, 0f).start(playerTweenManager)
                        disabledWidgetsWhenPlaying.forEach {
                            setTouchable(it, Touchable.disabled)
                        }
                    }
                })
                playerControllerPlayButton.add(Image(Texture("play.png"))).maxSize(24 * R.SCALE)
                playerControllerPlayButton.pack()
                val playerControllerStopButton = VisTextButton("■")
                playerControllerStopButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        playerStatus = PlayerStatus.Stopped
                        Gdx.graphics.isContinuousRendering = false
                        playerTweenManager.killAll()
                        disabledWidgetsWhenPlaying.forEach {
                            setTouchable(it, Touchable.childrenOnly)
                        }
                    }
                })
                subMenuBarLeftTable.add(playerControllerPlayButton)
                subMenuBarLeftTable.add(playerControllerStopButton)
                subMenuBarLeftTable.cells.forEach {
                    it.left().pad(5f * R.SCALE)
                }
                subMenuBarLeftTable.pack()
                subMenuBar.add(subMenuBarLeftTable).expandX().left()
                subMenuBar.pack()
            }


            val mainTable = Table()
            run {
                val mainMultiSplitPane = MultiSplitPane(false)
                run {
                    val mainLeftSplitPane = MultiSplitPane(true)
                    run {
                        val lightsSeqTable = VisTable()
                        run {
                            lightsSeqTable.add("灯光片段").left().row()
                            val lightsSeqList = VisList<String>()
                            lightsSeqList.setItems("灯光1", "灯光2", "灯光3")
                            lightsSeqTable.add(lightsSeqList).expand().top().fillX().row()
                            lightsSeqTable.cells.forEach {
                                it.pad(5f * R.SCALE)
                            }
                            lightsSeqTable.pack()
                        }
                        val lightsPatternTable = VisTable()
                        run {
                            lightsPatternTable.add("颜色配置").left().row()
                            val lightsPatternList = VisList<String>()
                            lightsPatternList.setItems("Launchpad MKII/Pro", "Launchpad S")
                            lightsPatternTable.add(lightsPatternList).expand().top().fillX().row()
                            lightsPatternTable.cells.forEach {
                                it.pad(5f * R.SCALE)
                            }
                            lightsPatternTable.pack()
                        }
                        val lightsToolsTable = VisTable()
                        run {
                            lightsToolsTable.add("工具").left().row()
                            lightsToolsButtonGroup = ButtonGroup<VisImageButton>()
                            val checkableImageButtonStyle = VisImageButton.VisImageButtonStyle(VisUI.getSkin().get(VisImageButton.VisImageButtonStyle::class.java))
                            checkableImageButtonStyle.checked = checkableImageButtonStyle.down

                            for (i in 0 until tools.size)
                            {
                                val toolIcon = tools[i].toolIcon
                                toolIcon?.minHeight = 16f * R.SCALE
                                toolIcon?.minWidth = 16f * R.SCALE
                                val lightsToolButton = VisImageButton(checkableImageButtonStyle)
                                if (toolIcon != null) lightsToolButton.add(Image(toolIcon))
                                lightsToolButton.pack()
                                lightsToolsButtonGroup.add(lightsToolButton)
                                Tooltip.Builder(tools[i].name).target(lightsToolButton).build()
                                val cell = lightsToolsTable.add(lightsToolButton).expand()
                                val a = Actor()
                                if ((i + 1) % 8 == 0) cell.row()
                            }
                            lightsToolsTable.pack()
                        }
                        val lightsColorTable = VisTable()
                        run {
                            val colorPixmap = Pixmap(1, 1, Pixmap.Format.RGB888)
                            colorPixmap.setColor(Color.WHITE)
                            colorPixmap.drawPixel(0, 0)
                            val colorDrawable = TextureRegionDrawable(TextureRegion(Texture(colorPixmap)))
                            colorDrawable.minHeight = 20f * R.SCALE
                            colorDrawable.minWidth = 25f * R.SCALE
                            lightsColorTable.add("颜色").left().row()
                            val lightsColorStack = object : WidgetGroup()
                            {
                                override fun getPrefHeight(): Float
                                {
                                    return 30f * R.SCALE
                                }

                                override fun getPrefWidth(): Float
                                {
                                    return 35f * R.SCALE
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
                            lightsFrontColor.setPosition(0f, 10f * R.SCALE)
                            lightsBackColor.setPosition(10f * R.SCALE, 0f)
                            lightsColorStack.pack()
                            lightsColorTable.add(lightsColorStack).expand().top()
                            lightsColorTable.pack()
                        }
                        mainLeftSplitPane.setWidgets(lightsToolsTable, lightsColorTable, lightsPatternTable, lightsSeqTable)
                        mainLeftSplitPane.setSplit(0, 0.125f)
                        mainLeftSplitPane.setSplit(1, 0.25f)
                        mainLeftSplitPane.pack()

                        setBackground(mainLeftSplitPane, tableBackground)
                    }

                    val ledPannelTable = VisTable()
                    run {
                        ledPannelTable.add(ledPannel).maxSize(pannelSize).pad(100f).expand().center()
                        ledPannelTable.pack()
                        ledPannelScrollPane = VisScrollPane(ledPannelTable)
                        ledPannelScrollPane.listeners.removeIndex(1)
                        ledPannelScrollPane.addListener(object : InputListener()
                        {
                            override fun scrolled(event: InputEvent?, x: Float, y: Float, amount: Int): Boolean
                            {
                                if (ctrl)
                                {
                                    pannelSize -= amount * 20f
                                }
                                else
                                {
                                    if (ledPannelScrollPane.isScrollY) ledPannelScrollPane.scrollY += 80 * amount
                                    else if (ledPannelScrollPane.isScrollX) //
                                        ledPannelScrollPane.scrollX += 80 * amount
                                }
                                return true
                            }
                        })
                        ledPannelScrollPane.setFadeScrollBars(false)
                        ledPannelScrollPane.setFlickScroll(false)
                    }
                    mainMultiSplitPane.setWidgets(mainLeftSplitPane, ledPannelScrollPane)
                    mainMultiSplitPane.setSplit(0, 0.2f)
                }
                mainTable.add(mainMultiSplitPane).grow()
                mainTable.cells.forEach {
                    it.pad(5f * R.SCALE)
                }
                mainTable.pack()
            }
            val bottomMenuBar = VisTable()
            run {
                val timeLineControllerTable = VisTable()
                val previousFrameButton = VisTextButton("<")
                val nextFrameButton = VisTextButton(">")
                totalFrameLabel = VisLabel("0")
                totalFrameLabel.setAlignment(Align.center, Align.center)
                currentFrameTextField = VisTextField("0")
                currentFrameTextField.setAlignment(Align.center)
                val addFrameButton = VisTextButton("+")
                currentFrameTextField.textFieldFilter = VisTextField.TextFieldFilter { textField, c ->
                    if (c.isWhitespace())
                    {
                        val page = textField.text.toIntOrNull() ?: -1-1
                        changePage(page)
                        textField.isInputValid = !(page < 0 || page >= pages.size)
                    }
                    c.isDigit()
                }
                val deleteFrameButton = VisTextButton("×")
                deleteFrameButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        if(pages.size > 1)
                        {
                            pages.removeIndex(currentPageIndex)
                            currentPageIndex --
                            currentFrameTextField.text = (currentPageIndex + 1).toString()
                            pageToPannel()
                        }
                        else
                        {
                            currentPage.leds.forEach {
                                it.forEach {
                                    it.setColor(Color.BLACK)
                                }
                            }
                            pageToPannel()
                        }
                    }
                })
                val duplicateFrameButton = VisTextButton("▼")
                duplicateFrameButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        pannelToPage()
                        val page = LEDPage(pannelWidth, pannelHeight)
                        val currentPage = currentPage
                        for(i in 0 until pannelHeight)
                        {
                            for(j in 0 until pannelHeight)
                            {
                                page.leds[i][j].setColor(currentPage.leds[i][j].color)
                            }
                        }
                        pages.insert(currentPageIndex, page)
                        currentFrameTextField.text = (++ currentPageIndex + 1).toString()
                        pageToPannel()
                    }
                })
                previousFrameButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        changePage(currentPageIndex - 1)
                    }
                })
                nextFrameButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        changePage(currentPageIndex + 1)
                    }
                })
                addFrameButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        val index = pages.size
                        pages.add(LEDPage(pannelWidth, pannelHeight))
                        changePage(index)
                    }
                })
                timeLineControllerTable.add(previousFrameButton)
                timeLineControllerTable.add(currentFrameTextField).width(50f * R.SCALE)
                timeLineControllerTable.add("/")
                timeLineControllerTable.add(totalFrameLabel).width(50f * R.SCALE)
                timeLineControllerTable.add(nextFrameButton)
                timeLineControllerTable.add(addFrameButton).padLeft(5 * R.SCALE)
                timeLineControllerTable.add(duplicateFrameButton)
                timeLineControllerTable.add(deleteFrameButton)
                timeLineControllerTable.pack()
                bottomMenuBar.add(timeLineControllerTable).row()
                bottomMenuBar.pack()
                disabledWidgetsWhenPlaying.add(bottomMenuBar)
            }

            rootTable.setFillParent(true)
            rootTable.add(menuBar.table).fillX().row()
            rootTable.add(subMenuBar).fillX().row()
            rootTable.add(mainTable).grow().row()
            rootTable.add(bottomMenuBar).fillX().row()
            rootTable.pack()
            stage.addActor(rootTable);
            subMenuBar.background = tableBackground
            bottomMenuBar.background = tableBackground
            menuBar.table.background = tableBackground
        }

        Gdx.input.inputProcessor = stage;

        (stage.viewport as ScreenViewport).unitsPerPixel = R.unitsPerPixel
        pages.add(LEDPage(pannelWidth, pannelHeight))
        changePage(0)
    }

    fun setBackground(actor: Actor, background: Drawable)
    {
        if(actor is Table)
            actor.setBackground(background)
        else if(actor is WidgetGroup)
            actor.children.forEach { setBackground(it, background) }

    }
    fun setTouchable(actor: Actor,touchable: Touchable)
    {
        actor.touchable = touchable
        if(actor is Button)
        {
            actor.isDisabled = touchable == Touchable.disabled
        }
        else if(actor is WidgetGroup)
            actor.children.forEach { setTouchable(it, touchable) }
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
        (colorPicker.cells[colorPicker.cells.size - 1].actor as Table).cells.forEach {
            val button = it.actor
            if(button is VisTextButton)
            {
                button.setText(when("${button.text}")
                {
                    "OK" -> "确定"
                    "Cancel" -> "取消"
                    "Restore" -> "重置"
                    else -> ""
                })
            }
            loopCount ++
        }
        _colorPicker = colorPicker
        colorPicker.color = color
        stage.addActor(colorPicker)



    }
    override fun render(delta: Float)
    {
        stage.act();
        playerTweenManager.update(Gdx.graphics.deltaTime * playerSpeed / 8 / pages.size)
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
    override fun hide()
    {
    }
}