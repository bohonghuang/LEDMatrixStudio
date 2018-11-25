package org.coco24.matrixstudio

import aurelienribon.tweenengine.Timeline
import aurelienribon.tweenengine.Tween
import aurelienribon.tweenengine.TweenManager
import aurelienribon.tweenengine.equations.Linear
import com.badlogic.gdx.*
import com.badlogic.gdx.files.FileHandle
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
import com.kotcrab.vis.ui.FocusManager
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.*
import com.kotcrab.vis.ui.widget.Tooltip
import com.kotcrab.vis.ui.widget.color.ColorPicker
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter
import com.kotcrab.vis.ui.widget.file.FileChooser
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter
import com.kotcrab.vis.ui.widget.file.FileChooserListener
import com.kotcrab.vis.ui.widget.file.FileTypeFilter
import org.coco24.matrixstudio.LEDCell.ColorizedLEDCell
import org.coco24.matrixstudio.MyGdxGame.R
import java.io.FileFilter
import java.lang.IllegalStateException

class MainScreen : Screen, PagesSurface, Tools.CellsFiller, Project
{
    override fun setName(name: String)
    {
        projectName = name
    }

    override fun setSequences(sequences: Array<LEDSequence>)
    {
        lightsSeqs.clear()
        lightsSeqs.addAll(sequences)
    }

    override fun setQueues(queues: Array<NamedQueue<LEDCell>>)
    {
        lightsQueues.clear()
        lightsQueues.addAll(queues)
    }

    var projectName = "未命名"
    override fun getName(): String
    {
        return projectName
    }

    override fun getSequences(): Array<LEDSequence>
    {
        return lightsSeqs
    }

    override fun getQueues(): Array<NamedQueue<LEDCell>>
    {
        return lightsQueues
    }


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
            ledPannel.centerLabels()
        }
    lateinit var lightsToolsButtonGroup: ButtonGroup<VisImageButton>
    val draggableWidgets = Array<Actor>()
    val touchedHashMap = HashMap<Any, Boolean>()
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
            draggableWidgets.forEach {
                if(touchedHashMap.get(it) == true)
                {
                    vector2.x = screenX.toFloat()
                    vector2.y = screenY.toFloat()
                    screenToStageCoordinates(vector2)
                    it.stageToLocalCoordinates(vector2)
                    it.listeners.forEach {
                        if(it is InputListener)
                            it.touchDragged(null, vector2.x, vector2.y, pointer)
                    }
                }
            }
            return super.touchDragged(screenX, screenY, pointer)
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean
        {
            draggableWidgets.forEach {
                if(touchedHashMap.get(it) == true)
                {
                    it.listeners.forEach {
                        if(it is InputListener)
                        {
                            it.touchUp(null, 0f, 0f, pointer, button)
                        }
                    }
                }
                touchedHashMap.put(it, false)
            }
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
            touchedHashMap.put(this, touched)
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
    fun newSeq(name: String): LEDSequence //TODO: 修复
    {
        val array = LEDSequence(name, ledPannel.WIDTH, ledPannel.HEIGHT)
        array.add(LEDPage(pannelWidth, pannelHeight))
        return array
    }
    fun syncTotalFrames()
    {
        totalFrameLabel.setText(currentSeq.size.toString())
    }
    var currentSeq: LEDSequence
    set(value)
    {
        currentSeqIndex = lightsSeqs.indexOf(value)
    }
    get()
    {
        return lightsSeqs[currentSeqIndex]
    }
    var currentPageIndex = 0
        set(value)
        {
            if (value >= 0 && value < currentSeq.size) field = value
        }
    fun refreshLightsSeqsList()
    {
        lightsSeqList.clearItems()
        val list = Array<String>()
        lightsSeqs.forEach {
            list.add(it.name)
        }
        lightsSeqList.setItems(list)
    }
    val lightsSeqs = object : Array<LEDSequence>()
    {
        override fun addAll(vararg array: LEDSequence?)
        {
            super.addAll(*array)
            refreshLightsSeqsList()
        }
        override fun add(value: LEDSequence?)
        {
            super.add(value)
            refreshLightsSeqsList()
        }

        override fun removeIndex(index: Int): LEDSequence
        {
            val ret = super.removeIndex(index)
            refreshLightsSeqsList()
            return ret
        }

        override fun insert(index: Int, value: LEDSequence?)
        {
            super.insert(index, value)
            refreshLightsSeqsList()
        }
    }
    var currentSeqIndex: Int = 0
    set(value)
    {
        if(value >= 0 && value < lightsSeqs.size)
            field = value
//        lightsSeqList.selectedIndex = value
    }
//    get()
//    {
//        return lightsSeqList.selectedIndex
//    }

    lateinit var totalFrameLabel: VisLabel
    lateinit var currentFrameTextField: VisTextField
    lateinit var lightsSeqList: VisList<String>

    var ctrl = false
    var alt = false
    var shift = false
    val frontColorizedLEDCell = LEDCell.PointedColorizedLEDCell(Color())
    val backColorizedLEDCell = LEDCell.PointedColorizedLEDCell(Color())
    val frontQueuedLEDCell = LEDCell.QueuedLEDCell()
    val backQueuedLEDCell = LEDCell.QueuedLEDCell()
    private var frontCell: LEDCell = frontColorizedLEDCell
    set(value)
    {
        field = value
        lightsFrontColorImage.color = value.color
    }
    private var backCell: LEDCell = backColorizedLEDCell
    set(value)
    {
        field = value
        lightsBackColorImage.color = value.color
    }
    override fun getFrontCell(): LEDCell = frontCell
    override fun getBackCell(): LEDCell = backCell
    init
    {
        tools.add(Tools.Pen(ledPannel, this))
        tools.add(Tools.Eraser(ledPannel))
        tools.add(Tools.Line(ledPannel, this))
        tools.add(Tools.Rectangle(ledPannel, this))
        tools.add(Tools.Bucket(ledPannel, this))
    }

    var currentPage: LEDPage
        get()
        {
            return currentSeq[currentPageIndex]
        }
        set(value)
        {
            currentPageIndex = currentSeq.indexOf(value)
        }

    fun changePage(page: Int)
    {
        pannelToPage()
        currentPageIndex = page
        pageToPannel()

        currentFrameTextField.text = (currentPageIndex + 1).toString()
        currentFrameTextField.isInputValid = true
    }
    fun changeSeq(seq: Int)
    {
        if(!(seq in 0 until lightsSeqs.size))
            return
        pannelToPage()
        currentPageIndex = 0
        currentFrameTextField.text = (currentPageIndex + 1).toString()
        currentSeqIndex = seq
        totalFrameLabel.setText(currentSeq.size.toString())
        pageToPannel()
    }
    override fun showPage(page: Int)
    {
        currentPageIndex = page
        pageToPannel()
        currentFrameTextField.text = (currentPageIndex + 1).toString()
    }

    override fun getPagesSize(): Int
    {
        return currentSeq.size
    }

    override fun getCurrentPage(): Int
    {
        return currentPageIndex
    }
    fun pageToPannel() = sync(true)
    fun pannelToPage() = if(currentSeqIndex in 0 until lightsSeqs.size) sync(false) else currentSeqIndex = currentSeqIndex - 1
    fun sync(pageToPannel: Boolean)
    {
        val currentPage = currentPage
        if (currentPage.getWidth() == ledPannel.WIDTH && currentPage.getHeight() == ledPannel.HEIGHT)
        {
            for (i in 0 until currentPage.getHeight())
            {
                for (j in 0 until currentPage.getWidth())
                {
                    if(pageToPannel)
                    ledPannel.leds[i][j].ledCell = currentPage.leds[i][j]
                    else currentPage.leds[i][j] = ledPannel.leds[i][j].ledCell
                }
            }
        } else
        {
            throw IllegalStateException("ERROR")
        }
    }
    enum class PlayerStatus
    {
        Playing, Paused, Stopped
    }
    enum class PlayMode(val _name: String = "PlayMode")
    {
        Ordered("正序播放"), OrderedReversed("倒序播放"), Loop("循环播放"), LoopReversed("辗转播放")
    }

    val playerTweenManager = TweenManager()
    var playerStatus = PlayerStatus.Stopped
    var playMode = PlayMode.Ordered
    var playerSpeed = 200
    val widgetsOriginalTouchable = HashMap<Actor, Touchable>()
    val disabledWidgetsWhenPlaying = Array<Actor>()
    lateinit var lightsQueueList: VisList<String>
    fun refreshLightsQueue()
    {
        lightsQueueList.clearItems()
        val nameArray = Array<String>()
        lightsQueues.forEach {
            nameArray.add(it.name)
        }
        lightsQueueList.setItems(nameArray)
    }
    val lightsQueues = object : Array<NamedQueue<LEDCell>>()
    {
        override fun addAll(array: Array<out NamedQueue<LEDCell>>?)
        {
            super.addAll(array)
            refreshLightsQueue()
        }
        override fun add(value: NamedQueue<LEDCell>?)
        {
            super.add(value)
            refreshLightsQueue()
        }
        override fun removeIndex(index: Int): NamedQueue<LEDCell>?
        {
            if(!(index in 0 until size))
                return null
            val ret = super.removeIndex(index)
            refreshLightsQueue()
            return ret
        }

        override fun insert(index: Int, value: NamedQueue<LEDCell>?)
        {
            super.insert(index, value)
            refreshLightsQueue()
        }
    }
    var currentLightsQueuesIndex: Int
    set(value)
    {
        lightsQueueList.selectedIndex = value
    }
    get()
    {
        return lightsQueueList.selectedIndex
    }
    var currentLightsQueue: NamedQueue<LEDCell>?
    set(value)
    {
        currentLightsQueuesIndex = lightsQueues.indexOf(value)
    }
    get()
    {
        if(currentLightsQueuesIndex in 0 until lightsQueues.size)
        return lightsQueues[currentLightsQueuesIndex]
        else return null
    }
    fun colorCell()
    {
        frontCell = frontColorizedLEDCell
        backCell = backColorizedLEDCell
    }
    override fun show()
    {
        val tableBackground = (VisUI.getSkin().get(VisImageButton.VisImageButtonStyle::class.java).up as NinePatchDrawable).tint(if (R.PPI < 128) Color.WHITE else Color.LIGHT_GRAY)

        val rootTable = Table()
        run {
            val menuBar = MenuBar();
            run {
                val fileMenu = Menu("文件");
                run {
                    val newFileMenuItem = MenuItem("新建工程")
                    newFileMenuItem.addListener(object : ChangeListener()
                    {
                        override fun changed(event: ChangeEvent?, actor: Actor?)
                        {
                            currentPageIndex = 0
                            initProject()
                            pageToPannel()
                            syncTotalFrames()
                            colorCell()
                        }
                    })
                    val openFileMenuItem = MenuItem("打开工程...");
                    openFileMenuItem.addListener(object : ChangeListener()
                    {
                        override fun changed(event: ChangeEvent?, actor: Actor?)
                        {
                            val fileChooser = FileChooser("打开工程", FileChooser.Mode.OPEN)
                            val fileFilter = FileTypeFilter(true)
                            fileFilter.addRule("Matrix Studio 工程文件 (*.msp)", "msp")
                            fileChooser.setFileTypeFilter(fileFilter)
                            fileChooser.setListener(object : FileChooserListener
                            {
                                override fun selected(files: Array<FileHandle>?)
                                {
                                    if(files == null) return
                                    if(files.size > 0)
                                    {
                                        ProjectXMLUtils.loadFromXML(this@MainScreen, files[0])
                                        pageToPannel()
                                        syncTotalFrames()
                                        colorCell()
                                    }
                                    fileChooser.fadeOut()
                                }
                                override fun canceled() = fileChooser.fadeOut()
                            })
                            fileChooser.width = 900f
                            stage.addActor(fileChooser)
                            fileChooser.fadeIn()
                        }
                    })
                    val saveFileMenuItem = MenuItem("保存工程...")
                    saveFileMenuItem.addListener(object : ChangeListener()
                    {
                        override fun changed(event: ChangeEvent?, actor: Actor?)
                        {
                            val fileChooser = FileChooser("保存工程", FileChooser.Mode.SAVE)
                            val fileFilter = FileTypeFilter(true)
                            fileFilter.addRule("Matrix Studio 工程文件 (*.msp)", "msp")
                            fileChooser.setFileTypeFilter(fileFilter)
                            fileChooser.setListener(object : FileChooserListener
                            {
                                override fun selected(files: Array<FileHandle>?)
                                {
                                    if(files == null) return
                                    if(files.size > 0)
                                    {
                                        pannelToPage()
                                        projectName = files[0].nameWithoutExtension() //TODO: 暂时
                                        ProjectXMLUtils.writeXML(this@MainScreen, files[0])
                                    }
                                    fileChooser.fadeOut()
                                }
                                override fun canceled() = fileChooser.fadeOut()
                            })
                            fileChooser.setDefaultFileName("$projectName.msp")
                            fileChooser.width = 900f
                            stage.addActor(fileChooser)
                            fileChooser.fadeIn()
                        }
                    })
                    val closeFileMenuItem = MenuItem("关闭工程");
                    val exitFileMenuItem = MenuItem("退出");
                    exitFileMenuItem.addListener(object : ChangeListener()
                    {
                        override fun changed(event: ChangeEvent?, actor: Actor?)
                        {
                            Gdx.app.exit()
                        }
                    })
                    val saveCurrentSeqMenuItem = MenuItem("导出片段...")
                    saveCurrentSeqMenuItem.addListener(object : ChangeListener()
                    {
                        override fun changed(event: ChangeEvent?, actor: Actor?)
                        {
                            val fileChooser = FileChooser("导出灯光片段", FileChooser.Mode.SAVE)
                            val fileFilter = FileTypeFilter(true)
                            fileFilter.addRule("MIDI文件 (*.mid)", "mid")
                            fileChooser.setFileTypeFilter(fileFilter)
                            fileChooser.setListener(object : FileChooserListener
                            {
                                override fun selected(files: Array<FileHandle>?)
                                {
                                    if(files == null) return
                                    if(files.size > 0)
                                    {
                                        saveAsMidiFile(files[0])
                                    }
                                    fileChooser.fadeOut()
                                }
                                override fun canceled() = fileChooser.fadeOut()
                            })
                            fileChooser.setDefaultFileName("${currentSeq.name}.mid")
                            fileChooser.width = 900f
                            stage.addActor(fileChooser)
                            fileChooser.fadeIn()
                        }
                    })
                    fileMenu.addItem(newFileMenuItem)
                    fileMenu.addItem(openFileMenuItem);
                    fileMenu.addItem(saveFileMenuItem)
//                    fileMenu.addItem(closeFileMenuItem);
                    fileMenu.addSeparator()
                    fileMenu.addItem(saveCurrentSeqMenuItem)
                    fileMenu.addSeparator()
                    fileMenu.addItem(exitFileMenuItem);
                }
                val editMenu = Menu("编辑");
                run {
                    fun offsetCurrentPageQueuedIndex(offset: Int)
                    {
                        currentPage.leds.forEach {
                            it.forEach {
                                if(it is LEDCell.QueuedLEDCell)
                                {
                                    it.ledsQueueOffset += offset
                                }
                            }
                        }
                    }
                    val subQueueOffsetMenuItem = MenuItem("前移页序列编号")
                    subQueueOffsetMenuItem.addListener(object : ChangeListener()
                    {
                        override fun changed(event: ChangeEvent?, actor: Actor?)
                        {
                            pannelToPage()
                            offsetCurrentPageQueuedIndex(-1)
                            pageToPannel()
                        }
                    })

                    val addQueueOffsetMenuItem = MenuItem("后移页序列编号")
                    addQueueOffsetMenuItem.addListener(object : ChangeListener()
                    {
                        override fun changed(event: ChangeEvent?, actor: Actor?)
                        {
                            pannelToPage()
                            offsetCurrentPageQueuedIndex(1)
                            pageToPannel()
                        }
                    })

                    val convertAllQueuedLedCell = MenuItem("移除片段序列编号")
                    convertAllQueuedLedCell.addListener(object : ChangeListener()
                    {
                        override fun changed(event: ChangeEvent?, actor: Actor?)
                        {
                            currentSeq.forEach {
                                it.leds.forEach {
                                    for(i in 0 until it.size)
                                    {
                                        val ledCell = it[i]
                                        if(ledCell is LEDCell.QueuedLEDCell)
                                        {
                                            it[i] = ledCell.toColorizedLEDCell()
                                        }
                                    }
                                }
                            }
                            pageToPannel()
                        }
                    })
                    editMenu.addItem(subQueueOffsetMenuItem)
                    editMenu.addItem(addQueueOffsetMenuItem)
                    editMenu.addSeparator()
                    editMenu.addItem(convertAllQueuedLedCell)
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
//                    viewMenu.addSeparator()
//                    viewMenu.addItem(viewPannelStyle)
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
//                menuBar.addMenu(effectMenu);
//                menuBar.addMenu(deviceMenu);
                menuBar.addMenu(helpMenu);
            }

            val subMenuBar = VisTable()
            run {
                val subMenuBarLeftTable = VisTable()
                subMenuBarLeftTable.add("选择设备：").expandX()
                val deviceSelectBox = VisSelectBox<String>()
                deviceSelectBox.setItems("Virtual Matrix")
                subMenuBarLeftTable.add(deviceSelectBox).minWidth(200f * R.SCALE)
                val playerControllerSpeedTextField = VisTextField(playerSpeed.toString())
                playerControllerSpeedTextField.textFieldFilter = object : VisTextField.TextFieldFilter.DigitsOnlyFilter()
                {
                    override fun acceptChar(textField: VisTextField, c: Char): Boolean
                    {
                        if (super.acceptChar(textField, c))
                        {
                            return true
                        } else
                        {
                            playerSpeed = textField.text.toIntOrNull() ?: 0
                            FocusManager.resetFocus(stage)
                            return false
                        }
                    }
                }

//                subMenuBarLeftTable.addSeparator(true)
                subMenuBarLeftTable.add("播放速度：")
                subMenuBarLeftTable.add(playerControllerSpeedTextField).width(64f * R.SCALE)
                val playerControllerPlayButton = VisImageButton("default")
                playerControllerPlayButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        playerControllerSpeedTextField.textFieldFilter.acceptChar(playerControllerSpeedTextField, '\n')
                        play()
                    }
                })
                playerControllerPlayButton.add(Image(Texture("play.png"))).maxSize(24 * R.SCALE)
                playerControllerPlayButton.pack()
                val playerControllerStopButton = VisTextButton("■")
                playerControllerStopButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        stop()
                    }
                })
                subMenuBarLeftTable.add(playerControllerPlayButton)
                subMenuBarLeftTable.add(playerControllerStopButton)
                val playModeSelectBox = VisSelectBox<String>()
                val playModeNames = Array<String>()
                PlayMode.values().forEach {
                    playModeNames.add(it._name)
                }
                playModeSelectBox.items = playModeNames
                playModeSelectBox.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        playMode = PlayMode.values()[playModeSelectBox.selectedIndex]
                        if(playerStatus == PlayerStatus.Playing)
                            play()
                    }
                })
                subMenuBarLeftTable.add(playModeSelectBox)
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
                            lightsSeqTable.add("灯光片段").left()
                            val buttonsTable = VisTable()
                            val addLightsSeqButton = VisTextButton("+")
                            addLightsSeqButton.addListener(object : ChangeListener()
                            {
                                override fun changed(event: ChangeEvent?, actor: Actor?)
                                {
                                    val nameWindow = Windows.TextRequiredWindow()
                                    nameWindow.callback = object : Windows.BooleanCallback
                                    {
                                        override fun callback(result: Boolean)
                                        {
                                            if(result)
                                            {
                                                lightsSeqs.add(newSeq(nameWindow.text))
                                            }
                                        }
                                    }
                                    stage.addActor(nameWindow)
                                    nameWindow.fadeIn()
                                }
                            })
                            val editLightsSeqButton = VisTextButton("E")
                            editLightsSeqButton.addListener(object : ChangeListener()
                            {
                                override fun changed(event: ChangeEvent?, actor: Actor?)
                                {
                                    val nameWindow = Windows.TextRequiredWindow("编辑名称", currentSeq.name)
                                    nameWindow.callback = object : Windows.BooleanCallback
                                    {
                                        override fun callback(result: Boolean)
                                        {
                                            if(result)
                                            {
                                                currentSeq.name = nameWindow.text
                                                refreshLightsSeqsList()
                                            }
                                        }
                                    }
                                    stage.addActor(nameWindow)
                                    nameWindow.fadeIn()
                                }
                            })
                            val deleteLightsSeqButton = VisTextButton("×")
                            deleteLightsSeqButton.addListener(object : ChangeListener()
                            {
                                override fun changed(event: ChangeEvent?, actor: Actor?)
                                {
                                    if(lightsSeqs.size > 1)
                                    {
                                        lightsSeqs.removeIndex(currentSeqIndex)
                                    }
                                }
                            })
                            buttonsTable.add(addLightsSeqButton)
                            buttonsTable.add(editLightsSeqButton)
                            buttonsTable.add(deleteLightsSeqButton)
                            buttonsTable.pack()
                            lightsSeqTable.add(buttonsTable).row()
                            lightsSeqList = VisList<String>()
                            lightsSeqList.addListener(object : ChangeListener()
                            {
                                override fun changed(event: ChangeEvent?, actor: Actor?)
                                {
                                    changeSeq(lightsSeqList.selectedIndex)
                                }
                            })
                            lightsSeqTable.add(lightsSeqList).colspan(2).expand().top().fillX().row()
                            lightsSeqTable.cells.forEach {
                                it.pad(5f * R.SCALE)
                            }
                            lightsSeqTable.pack()
                        }
                        val lightsQueueTable = VisTable()
                        run {
                            lightsQueueList = VisList<String>()
                            lightsQueueList.addListener(object : ChangeListener()
                            {
                                override fun changed(event: ChangeEvent?, actor: Actor?)
                                {
                                    if(lightsQueueList.selectedIndex >= 0)
                                    frontQueuedLEDCell.ledsQueue = lightsQueues[lightsQueueList.selectedIndex]
                                }
                            })
                            lightsQueueTable.add("灯光序列").left()
                            val buttonsTable = VisTable()

                            val addLightsQueueButton = VisTextButton("+")
                            addLightsQueueButton.addListener(object : ChangeListener()
                            {
                                override fun changed(event: ChangeEvent?, actor: Actor?)
                                {
                                    val queueEditor = LEDQueueEditor()
                                    queueEditor.callback = object : Windows.BooleanCallback
                                    {
                                        override fun callback(result: Boolean)
                                        {
                                            if(result)
                                            {
                                                lightsQueues.add(queueEditor.ledQueue)
                                            }
                                        }
                                    }
                                    stage.addActor(queueEditor)
                                    queueEditor.fadeIn()
                                }
                            })
                            val editLightsQueueButton = VisTextButton("E")
                            editLightsQueueButton.addListener(object : ChangeListener()
                            {
                                override fun changed(event: ChangeEvent?, actor: Actor?)
                                {
                                    val queueEditor = LEDQueueEditor(Utils.copyNamedQueue(currentLightsQueue?:return))
                                    queueEditor.callback = object : Windows.BooleanCallback
                                    {
                                        override fun callback(result: Boolean)
                                        {
                                            if(result)
                                            {
                                                Utils.copyNamedQueue(queueEditor.ledQueue, currentLightsQueue?:return)
                                                refreshLightsQueue()
                                                pageToPannel()
                                            }
                                        }
                                    }
                                    stage.addActor(queueEditor)
                                    queueEditor.fadeIn()
                                }
                            })
                            val deleteLightsQueueButton = VisTextButton("×")
                            deleteLightsQueueButton.addListener(object : ChangeListener()
                            {
                                override fun changed(event: ChangeEvent?, actor: Actor?)
                                {
                                    if(frontCell == frontQueuedLEDCell && frontQueuedLEDCell.ledsQueue == currentLightsQueue)
                                        frontCell = frontColorizedLEDCell
                                    if(backCell == backQueuedLEDCell && backQueuedLEDCell.ledsQueue == currentLightsQueue)
                                        backCell = backColorizedLEDCell
                                    lightsQueues.removeIndex(currentLightsQueuesIndex)
                                }
                            })
                            buttonsTable.add(addLightsQueueButton)
                            buttonsTable.add(editLightsQueueButton)
                            buttonsTable.add(deleteLightsQueueButton)
                            lightsQueueTable.add(buttonsTable).row()

                            lightsQueueTable.add(lightsQueueList).colspan(2).expand().top().fillX().row()
                            lightsQueueTable.cells.forEach {
                                it.pad(5f * R.SCALE)
                            }
                            lightsQueueTable.pack()
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
                            frontColorizedLEDCell.setColor(Color.RED)
                            frontCell = frontColorizedLEDCell
                            lightsFrontColor.add(lightsFrontColorImage)
                            lightsFrontColor.pack()
                            lightsFrontColor.addListener(object : ChangeListener()
                            {
                                override fun changed(event: ChangeEvent?, actor: Actor?)
                                {
                                    changeCell(frontCell)
                                }
                            })
                            lightsFrontColor.background = VisUI.getSkin().get(VisTextButton.VisTextButtonStyle::class.java).focusBorder
                            val lightsBackColor = VisImageButton("default")
                            lightsBackColorImage = Image(colorDrawable)

                            backColorizedLEDCell.setColor(Color.WHITE)
                            backCell = backColorizedLEDCell
                            lightsBackColor.add(lightsBackColorImage)
                            lightsBackColor.pack()
                            lightsBackColor.addListener(object : ChangeListener()
                            {
                                override fun changed(event: ChangeEvent?, actor: Actor?)
                                {
                                    changeCell(backCell)
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
                        mainLeftSplitPane.setWidgets(lightsToolsTable, lightsColorTable, lightsQueueTable, lightsSeqTable)
                        mainLeftSplitPane.setSplit(0, 0.125f * R.splitScale)
                        mainLeftSplitPane.setSplit(1, 0.25f * R.splitScale)
                        mainLeftSplitPane.pack()

                        setBackground(mainLeftSplitPane, tableBackground)
                    }
                    disabledWidgetsWhenPlaying.add(mainLeftSplitPane)
                    val ledPannelTable = VisTable()
                    run {
                        ledPannelTable.add(ledPannel).maxSize(pannelSize).pad(100f).expand().center()
                        ledPannelTable.pack()
                        draggableWidgets.add(ledPannel)
                        ledPannelScrollPane = VisScrollPane(ledPannelTable)
                        ledPannelScrollPane.listeners.removeIndex(1)
                        ledPannelScrollPane.addListener(object : InputListener()
                        {
                            override fun scrolled(event: InputEvent?, x: Float, y: Float, amount: Int): Boolean
                            {
                                if (ctrl)
                                {
                                    pannelSize -= amount * 20f
                                } else
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
                    mainMultiSplitPane.setSplit(0, 0.2f * R.splitScale)
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
                    if (c.isDigit())
                        true
                    else
                    {
                        val page = textField.text.toIntOrNull() ?: 0
                        changePage(page - 1)
                        textField.isInputValid = !(page < 0 || page >= currentSeq.size)
                        FocusManager.resetFocus(stage)
                        false
                    }

                }
                val deleteFrameButton = VisTextButton("×")
                deleteFrameButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        if (currentSeq.size > 1)
                        {
                            currentSeq.removeIndex(currentPageIndex)
                            syncTotalFrames()
                            currentPageIndex--
                            currentFrameTextField.text = (currentPageIndex + 1).toString()
                            pageToPannel()
                        } else
                        {
                            for(i in 0 until currentPage.leds.size)
                            {
                                for(j in 0 until currentPage.leds[0].size)
                                {
                                    val it = currentPage.leds[i][j]
                                    if(it is ColorizedLEDCell)
                                        it.setColor(Color.BLACK)
                                    else currentPage.leds[i][j] = ColorizedLEDCell()
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
                        duplicatePage()
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
                        currentSeq.insert(currentPageIndex + 1, LEDPage(pannelWidth, pannelHeight))
                        syncTotalFrames()
                        changePage(currentPageIndex + 1)
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

        initProject()

        Gdx.input.inputProcessor = stage;

        (stage.viewport as ScreenViewport).unitsPerPixel = R.unitsPerPixel
        changePage(0)

    }

    private fun initProject()
    {
        lightsSeqs.clear()
        lightsQueues.clear()

        val rainbowQueue = NamedQueue<LEDCell>()
        rainbowQueue.name = "彩虹渐变"
        //TODO：背景色新建对象
        for (h in 0 until 360 step 30)
        {
            val color = Color(Color.WHITE)
            rainbowQueue.addLast(ColorizedLEDCell(color.fromHsv(h.toFloat(), 1f, 1f)))
        }
        lightsQueues.add(rainbowQueue)
        lightsSeqs.add(newSeq("新建灯光片段"))
    }

    private fun saveAsMidiFile(file: FileHandle)
    {
        val exporter = MidiFileExporter()
        exporter.callback = object : Windows.BooleanCallback
        {
            override fun callback(result: Boolean)
            {
                if(result)
                {
                    pannelToPage()
                    LightsConverter.writeMidiFileFromSequence(currentSeq, exporter.layoutConfig, LightsConverter.mk2ColorPalette, file)
                }
            }
        }
        stage.addActor(exporter)
        exporter.fadeIn()

    }

    private fun stop()
    {
        playerStatus = PlayerStatus.Stopped
        Gdx.graphics.isContinuousRendering = false
        playerTweenManager.killAll()
        disabledWidgetsWhenPlaying.forEach {
            setTouchable(it, Touchable.enabled)
        }
    }

    private fun play()
    {
        if(currentSeq.size <= 1) return
        pannelToPage()
        playerStatus = PlayerStatus.Playing
        Gdx.graphics.isContinuousRendering = true
        playerTweenManager.killAll()
        val reverseNeededTime = 1f / currentSeq.size
        val initialTween = Tween.set(this@MainScreen, 0).target(0f)
        val playTween = Tween.to(this@MainScreen, 0, 1f - reverseNeededTime).target(currentSeq.size - 1f).ease(Linear.INOUT)
        val initialReversedTween = Tween.set(this@MainScreen, 0).target(currentSeq.size - 1f)
        val playReversedTween = Tween.to(this@MainScreen, 0, 1f - reverseNeededTime * 2) .target(0.99999f).ease(Linear.INOUT).delay(reverseNeededTime)
        when(playMode)
        {
            PlayMode.Ordered -> Timeline.createSequence().push(initialTween).push(playTween.delay(reverseNeededTime)).setCallback { i, baseTween -> stop() }
            PlayMode.OrderedReversed -> Timeline.createSequence().push(initialReversedTween).push(playReversedTween).setCallback { i, baseTween -> stop() }
            PlayMode.Loop -> Timeline.createSequence().push(initialTween).push(playTween).repeat(Tween.INFINITY, reverseNeededTime).start(playerTweenManager)
            PlayMode.LoopReversed -> Timeline.createSequence().push(initialTween).push(playTween).push(initialReversedTween).push(playReversedTween).repeat(Tween.INFINITY, 0f).start(playerTweenManager)
        }.start(playerTweenManager)

        disabledWidgetsWhenPlaying.forEach {
            setTouchable(it, Touchable.disabled)
        }
    }

    private fun duplicatePage()
    {
        pannelToPage()
        val page = LEDPage(pannelWidth, pannelHeight)
        val currentPage = currentPage
        for (i in 0 until pannelHeight)
        {
            for (j in 0 until pannelHeight)
            {
                page.leds[i][j] = currentPage.leds[i][j].clone() as LEDCell
                val led = page.leds[i][j]
                if (led is LEDCell.QueuedLEDCell)
                    led.ledsQueueOffset++
                //TODO: 移除
                //TODO: offset设置
                //TODO: SmartInsert
                //TODO: 池
            }
        }
        currentSeq.insert(currentPageIndex + 1, page)
        syncTotalFrames()
        changePage(currentPageIndex + 1)
    }

    fun setBackground(actor: Actor, background: Drawable)
    {
        if (actor is Table)
            actor.setBackground(background)
        else if (actor is WidgetGroup)
            actor.children.forEach { setBackground(it, background) }

    }

    fun setTouchable(actor: Actor, touchable: Touchable)
    {
        widgetsOriginalTouchable.put(actor, touchable)
        actor.touchable =
                if (touchable == Touchable.enabled)
//                    if (actor is WidgetGroup) Touchable.childrenOnly
//                    else
                        Touchable.enabled
                else touchable
        if (actor is Button)
        {
            actor.isDisabled = touchable == Touchable.disabled
        } else if (actor is WidgetGroup)
            actor.children.forEach { setTouchable(it, touchable) }
    }

    fun changeCell(mLedCell: LEDCell)
    {
        val front = mLedCell.hashCode() == frontCell.hashCode()
        val cellTypePopupMenu = PopupMenu()
        val cellTypeColorizedMenuItem = MenuItem("颜色")
        val cellTypeQueuedMenuItem = MenuItem("序列")
        cellTypePopupMenu.addItem(cellTypeColorizedMenuItem)
        cellTypePopupMenu.addItem(cellTypeQueuedMenuItem)
        cellTypePopupMenu.showMenu(stage, if(front) lightsFrontColorImage else lightsBackColorImage)
        cellTypeColorizedMenuItem.addListener(object: ChangeListener()
        {
            override fun changed(event: ChangeEvent?, actor: Actor?)
            {
                val ledCell = if(front) frontColorizedLEDCell  else backColorizedLEDCell
                var _colorPicker: ColorPicker? = null
                val colorPicker = ColorPicker(if (front) "选择前景色" else "选择背景色", object : ColorPickerAdapter()
                {
                    override fun finished(newColor: Color?)
                    {
                        if (newColor == null) return
                        ledCell.setColor(newColor)
                        _colorPicker?.remove()
                        if (front)
                            frontCell = ledCell
                        else backCell = ledCell
                    }
                })
                var loopCount = 0
                (colorPicker.cells[colorPicker.cells.size - 1].actor as Table).cells.forEach {
                    val button = it.actor
                    if (button is VisTextButton)
                    {
                        button.setText(when ("${button.text}")
                        {
                            "OK" -> "确定"
                            "Cancel" -> "取消"
                            "Restore" -> "重置"
                            else -> ""
                        })
                    }
                    loopCount++
                }
                _colorPicker = colorPicker
                colorPicker.color = ledCell.color
                stage.addActor(colorPicker)
            }
        })
        cellTypeQueuedMenuItem.addListener(object : ChangeListener()
        {
            override fun changed(event: ChangeEvent?, actor: Actor?)
            {
                if(lightsQueues.size == 0) return
                val offsetInputWindow = Windows.NumberRequiredWindow("请输入序列起始编号", (if(front) frontQueuedLEDCell else backQueuedLEDCell).ledsQueueOffset , true, true)
                offsetInputWindow.callback = object : Windows.BooleanCallback
                {
                    override fun callback(result: Boolean)
                    {
                        if(result)
                        {
                            if(front)
                            {
                                frontQueuedLEDCell.ledsQueue = currentLightsQueue
                                frontQueuedLEDCell.ledsQueueOffset = offsetInputWindow.number
                                frontCell = frontQueuedLEDCell
                            }
                            else{
                                backQueuedLEDCell.ledsQueue = currentLightsQueue
                                backQueuedLEDCell.ledsQueueOffset = offsetInputWindow.number
                                backCell = backQueuedLEDCell
                            }
                        }
                    }
                }
                stage.addActor(offsetInputWindow)
                offsetInputWindow.fadeIn()
            }
        })

    }

    override fun render(delta: Float)
    {
        stage.act();
        playerTweenManager.update(Gdx.graphics.deltaTime * playerSpeed / 8 / currentSeq.size)
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

    open inner class LEDQueueEditor(val ledQueue: NamedQueue<LEDCell> = NamedQueue()) : VisWindow("编辑灯光序列")
    {
        var callback: Windows.BooleanCallback? = null
        init
        {
            val ledTexture = Texture("led.png")
            val rootTable = VisTable()
            run {
                val addButton = VisTextButton("+")
                val addFrontButton = VisTextButton("+")
                val removeFrontButton = VisTextButton("×")
                val removeButton = VisTextButton("×")
                val buttonsTable = VisTable()

                val ledsTable = VisTable()
                val ledsScrollPane = VisScrollPane(ledsTable)
                ledsScrollPane.setFadeScrollBars(false)
                ledsScrollPane.setFlickScroll(false)
                fun updateTable()
                {
                    ledsTable.pack()
                    ledsScrollPane.invalidate()
                    ledsScrollPane.pack()
                    ledsScrollPane.actor = ledsScrollPane.actor
                    rootTable.pack()
                }
                fun addLedEntity(ledCell: LEDCell)
                {
                    val led = LEDEntity(ledTexture)
                    ledsTable.add(led).size(32f * R.SCALE)
                    led.ledCell = ledCell
                    led.addListener(object : InputListener()
                    {
                        override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean
                        {
                            led.color = frontColorizedLEDCell.color
                            return true
                        }
                    })
                }
                ledQueue.forEach {
                    addLedEntity(it)
                }
                addFrontButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        val ledCell = ColorizedLEDCell()
                        ledQueue.addFirst(ledCell)
                        ledsTable.clearChildren()
                        ledQueue.forEach {
                            addLedEntity(it)
                        }
                        updateTable()
                    }
                })
                addButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        val ledCell = ColorizedLEDCell()
                        ledQueue.addLast(ledCell)
                        addLedEntity(ledCell)
                        updateTable()
                    }
                })
                removeFrontButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        if(ledQueue.size == 0) return
                        ledQueue.removeFirst()
                        ledsTable.clearChildren()
                        ledQueue.forEach {
                            addLedEntity(it)
                        }
                        updateTable()
                    }
                })
                removeButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        if(ledQueue.size == 0) return
                        ledQueue.removeLast()
                        ledsTable.clearChildren()
                        ledQueue.forEach {
                            addLedEntity(it)
                        }
                        updateTable()
                    }
                })
                val duplicateAllButton = VisTextButton("C")
                duplicateAllButton.addListener(object : ChangeListener()
                {
                    override fun changed(event: ChangeEvent?, actor: Actor?)
                    {
                        val leds = Array<LEDCell>()
                        ledQueue.forEach {
                            val cell = ColorizedLEDCell(it.color)
                            addLedEntity(cell)
                            leds.add(cell)
                        }
                        leds.forEach {
                            ledQueue.addLast(it)
                        }
                    }
                })
                buttonsTable.add(addFrontButton)
                buttonsTable.add(removeFrontButton)
                buttonsTable.add(duplicateAllButton)
                buttonsTable.add(removeButton)
                buttonsTable.add(addButton)
                buttonsTable.pack()
                rootTable.add(buttonsTable).row()
                rootTable.add(ledsScrollPane).size(500f * R.SCALE, 64f * R.SCALE).row()
                val ynButtonsTable = Windows.createBooleanButtonsTable(object : Windows.BooleanCallback
                {
                    override fun callback(result: Boolean)
                    {
                        if(ledQueue.size == 0)
                        {
                            callback?.callback(false)
                            fadeOut()
                            return
                        }
                        if (result)
                        {
                            val nameWindow = Windows.TextRequiredWindow(text = ledQueue.name)
                            nameWindow.callback = object : Windows.BooleanCallback
                            {
                                override fun callback(confirmed: Boolean)
                                {
                                    if (confirmed)
                                    {
                                        ledQueue.name = nameWindow.text
                                        callback?.callback(true)
                                        fadeOut()
                                    }
                                }
                            }
                            stage.addActor(nameWindow)
                            nameWindow.fadeIn()
                        } else
                        {
                            callback?.callback(false)
                            fadeOut()
                        }
                    }
                })
                rootTable.add(ynButtonsTable).expand().fillX()
                rootTable.pack()

                add(rootTable)
                pack()
            }
            addCloseButton()
            centerWindow ()
        }

        override fun fadeIn(time: Float): VisWindow
        {
            //TODO: 替换私有颜色选择器
            frontCell = frontColorizedLEDCell
            return super.fadeIn(time)
        }
        open fun finish()
        {

        }
    }
    class MidiFileExporter(): VisWindow("MIDI灯光片段选项")
    {
        var callback: Windows.BooleanCallback? = null
        var layoutConfig = LightsConverter.CoordinatesConfig.LaunchpadPro
        init
        {
            val layoutConfigSelectBox = VisSelectBox<String>()
            val layoutNames = Array<String>()
            LightsConverter.CoordinatesConfig.values().forEach {
                layoutNames.add(it.name)
            }
            layoutConfigSelectBox.setItems(layoutNames)
            val paletteConfigSelectBox = VisSelectBox<String>()
            paletteConfigSelectBox.setItems("Launchpad MKII/Pro")
            val buttonTable = Windows.createBooleanButtonsTable(object : Windows.BooleanCallback
            {
                override fun callback(result: Boolean)
                {
                    layoutConfig = LightsConverter.CoordinatesConfig.values()[layoutConfigSelectBox.selectedIndex]
                    callback?.callback(result)
                    fadeOut()
                }
            })
            add("布局配置：")
            add(layoutConfigSelectBox).row()
            add("色板配置：")
            add(paletteConfigSelectBox).row()
            add(buttonTable).colspan(2)
            pack()
            isModal = true
            centerWindow()
        }
    }
}