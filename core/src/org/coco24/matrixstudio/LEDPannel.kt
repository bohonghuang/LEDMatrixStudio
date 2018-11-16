package org.coco24.matrixstudio

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Array

open class LEDPannel: Table()
{
    val WIDTH = 8
    val HEIGHT = 8
    val leds = Array<Array<LED>>()
    var ledsSize = 72f
    var layoutLock = false
    init
    {
        val ledTexture = Texture("led.png")

        for(i in 0 until HEIGHT)
        {
            val row = Array<LED>()
            for(j in 0 until WIDTH)
            {
                val led = LED(ledTexture)
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
                colorToTemp()
                firstLed = updateLed(x, y, true)
                return super.touchDown(event, x, y, pointer, button)
            }
            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int)
            {
                updateLed(x, y, true)
                super.touchDragged(event, x, y, pointer)
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int)
            {
                colorFromTemp()
                firstLed = null
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
    open fun putDown(firstLed: LED?, led: LED)
    {
    }
    val vector2 = Vector2()
    var firstLed: LED? = null
    open fun updateLed(x: Float, y: Float, touched: Boolean): LED?
    {
        var returnVal: LED? = null
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
                        putDown(firstLed, it)
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
}