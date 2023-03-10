package org.coco24.matrixstudio

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Array

open class LEDPannelEntity(val WIDTH: Int, val HEIGHT: Int): Table()
{
    val leds = Array<Array<LEDEntity>>()
    var ledsSize = 72f
    var layoutLock = false
    init
    {
        val ledTexture = Texture("led.png")
        val sideLedTexture = Texture("led_side.png")
        val emptyTexture = Texture(Pixmap(0, 0, Pixmap.Format.RGB565))
        for(i in 0 until HEIGHT)
        {
            val row = Array<LEDEntity>()
            for(j in 0 until WIDTH)
            {
                val led = LEDEntity(if((i == 0 || i == HEIGHT - 1) && (j == 0 || j == WIDTH - 1)) emptyTexture else if(i == 0 || j == 0 || i == HEIGHT - 1 || j == WIDTH - 1) sideLedTexture else ledTexture)
                led.pannelX = j
                led.pannelY = i
                row.add(led)
            }
            leds.add(row)
        }
        var h = 0f
        val color = Color(1f, 1f, 1f, 1f )
        putLeds()
//        layoutLock = true
        touchable = Touchable.childrenOnly

        addListener(object: InputListener()
        {
            override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean
            {
                updateLed(x, y, false)
                return super.mouseMoved(event, x, y)
            }

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean
            {
                cellsToTemp()
                firstLedEntity = updateLed(x, y, true)
                return super.touchDown(event, x, y, pointer, button)
            }
            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int)
            {
                updateLed(x, y, true)
                super.touchDragged(event, x, y, pointer)
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int)
            {
                cellsFromTemp()
                firstLedEntity = null
                super.touchUp(event, x, y, pointer, button)
            }
        })
    }

    private fun putLeds()
    {
        clearChildren()
        leds.forEach {
            it.forEach {
                add(it)
                        .expand()
//                        .size(ledsSize)
//                        .minSize(ledsSize).maxSize(ledsSize)
                it.color = Color.BLACK
            }
            row()
        }
        pack()
    }

    override fun validate()
    {
        if(!layoutLock)
        super.validate()
    }
    open fun putDown(firstLedEntity: LEDEntity?, ledEntity: LEDEntity)
    {
    }
    val vector2 = Vector2()
    var firstLedEntity: LEDEntity? = null
    open fun updateLed(x: Float, y: Float, touched: Boolean): LEDEntity?
    {
        var returnVal: LEDEntity? = null
        vector2.x = x
        vector2.y = y
        val mx = vector2.x
        val my = vector2.y
        leds.forEach {
            it.forEach {
                if(mx > it.x && mx < it.x + it.width
                        && my > it.y && my < it.y + it.height)
                {
                    it.parentAlpha = 0.75f
                    if(touched)
                        putDown(firstLedEntity, it)
                    returnVal = it
                }
                else
                {
                    it.parentAlpha = 1f
                }
            }
        }
        return returnVal
    }
    fun colorToTemp()
    {
        leds.forEach {
            it.forEach {
                it.colorToTemp()
            }
        }
    }
    fun colorFromTemp()
    {
        leds.forEach {
            it.forEach {
                it.colorFromTemp()
            }
        }
    }
    fun cellsFromTemp()
    {
        leds.forEach {
            it.forEach {
                it.cellFromTemp()
            }
        }
    }
    fun cellsToTemp()
    {
        leds.forEach {
            it.forEach {
                it.cellToTemp()
            }
        }
    }
    fun centerLabels() = leds.forEach {
        it.forEach {
            it.centerLabel()
        }
    }
}