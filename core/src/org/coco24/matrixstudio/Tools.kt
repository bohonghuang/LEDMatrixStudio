package org.coco24.matrixstudio

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Queue
import com.kotcrab.vis.ui.widget.VisTable
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

object Tools
{
    abstract class Tool(val ledPannel: LEDPannel)
    {
        val toolIcon = Image()
        var name = ""
        val optionsTable = VisTable()
        abstract fun operate(sourceLed: LED?, led: LED)
    }
    class Pen(ledPannel: LEDPannel, val frontColor: Color): Tool(ledPannel)
    {
        init
        {
            name = "笔工具"
        }
        override fun operate(sourceLed: LED?, led: LED)
        {
            led.setTempColor(frontColor)
        }
    }
    class Eraser(ledPannel: LEDPannel): Tool(ledPannel)
    {
        init
        {
            name = "橡皮擦工具"
        }
        override fun operate(sourceLed: LED?, led: LED)
        {
            led.setTempColor(Color.BLACK)
        }
    }
    class Rectangle(ledPannel: LEDPannel, val frontColor: Color, val backColor: Color): Tool(ledPannel)
    {
        init
        {
            name = "矩形工具"
        }
        val leds = ledPannel.leds
        override fun operate(sourceLed: LED?, led: LED)
        {
            var tx = led.pannelX
            var ty = led.pannelY
            var fx = sourceLed?.pannelX?: tx
            var fy = sourceLed?.pannelY?: ty
            for(y in 0 until leds.size)
            {
                for(x in 0 until leds[0].size)
                {
                    if((x in fx..tx || x in tx .. fx)&& (y in fy..ty || y in ty .. fy))
                    {
                        leds[y][x].setTempColor(frontColor)
                    }
                    else
                    {
                        ledPannel.leds[y][x].setTempColor(leds[y][x].image.color)
                    }
                }
            }
        }
    }
    fun min(a: Int, b: Int) = if(a < b) a else b
    fun max(a: Int, b: Int) = if(a > b) a else b
    class Line(ledPannel: LEDPannel, val frontColor: Color): Tool(ledPannel)
    {
        init
        {
            name = "线段工具"
        }
        companion object
        {
            val pixmap: Pixmap = Pixmap(16, 16, Pixmap.Format.RGBA8888)
        }
        override fun operate(sourceLed: LED?, led: LED)
        {
            pixmap.setColor(Color.BLACK)
            pixmap.fill()
            val tx = led.pannelX
            val ty = led.pannelY
            val fx = sourceLed?.pannelX?: tx
            val fy = sourceLed?.pannelY?: ty
            pixmap.setColor(Color.WHITE)
            pixmap.drawLine(fx, fy, tx, ty)
            for(y in 0 until ledPannel.HEIGHT)
            {
                for(x in 0 until ledPannel.WIDTH)
                {
                    if(pixmap.getPixel(x, y) == Color.WHITE.toIntBits())
                    {
                        ledPannel.leds[y][x].setTempColor(frontColor)
                    }
                    else
                    {
                        ledPannel.leds[y][x].setTempColor(ledPannel.leds[y][x].image.color)
                    }
                }
            }
        }
    }
    class Bucket(ledPannel: LEDPannel, val frontColor: Color): Tool(ledPannel)
    {
        init
        {
            name = "油漆桶工具"
        }
        val fillQueue = LinkedList<LED>()
        val filledLeds = kotlin.Array(ledPannel.HEIGHT, { kotlin.BooleanArray(ledPannel.WIDTH) })
        val directions = Array.with(kotlin.arrayOf(0, -1),
                kotlin.arrayOf(0, 1),
                kotlin.arrayOf(1, 0),
                kotlin.arrayOf(-1, 0))
        override fun operate(sourceLed: LED?, led: LED)
        {
            fillQueue.clear()
            fillQueue.addLast(led)
            filledLeds.forEach {
                it.fill(false, 0, ledPannel.WIDTH)
            }
            var currentLed: LED
            do
            {
                currentLed = fillQueue.pop()
                currentLed.setTempColor(frontColor)
                filledLeds[currentLed.pannelY][currentLed.pannelX] = true
                directions.forEach {
                    val y = currentLed.pannelY + it[1]
                    val x = currentLed.pannelX + it[0]
                    if(x in 0 until ledPannel.WIDTH && y in 0 until ledPannel.HEIGHT)
                    {
                        val sideLed = ledPannel.leds[y][x]
                        if(sideLed.image.color == led.image.color && !filledLeds[sideLed.pannelY][sideLed.pannelX])
                        {
                            fillQueue.push(sideLed)
                        }
                    }
                }
            }while (fillQueue.size > 0)
            for(y in 0 until ledPannel.HEIGHT)
                for(x in 0 until ledPannel.WIDTH)
                    if(!filledLeds[y][x])
                        ledPannel.leds[y][x].setTempColor(ledPannel.leds[y][x].image.color)
        }

    }

}