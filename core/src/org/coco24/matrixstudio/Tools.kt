package org.coco24.matrixstudio

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Array
import com.kotcrab.vis.ui.widget.VisTable
import java.util.*

object Tools
{
    abstract class Tool(val ledPannelEntity: LEDPannelEntity)
    {
        var toolIcon: Drawable? = null
        var name = ""
        val optionsTable = VisTable()
        abstract fun operate(sourceLedEntity: LEDEntity?, ledEntity: LEDEntity)
    }
    interface CellsFiller
    {
        fun getFrontCell(): LEDCell;
        fun getBackCell(): LEDCell;
    }
    class Pen(ledPannelEntity: LEDPannelEntity, val cellsFiller: CellsFiller): Tool(ledPannelEntity)
    {
        val frontCell: LEDCell
        get() = cellsFiller.getFrontCell()
        init
        {
            name = "笔工具"
            toolIcon = TextureRegionDrawable(TextureRegion(Texture("pen.png")))
        }
        override fun operate(sourceLedEntity: LEDEntity?, ledEntity: LEDEntity)
        {
            ledEntity.tempLedCell = frontCell.clone() as LEDCell
        }
    }
    class Eraser(ledPannelEntity: LEDPannelEntity): Tool(ledPannelEntity)
    {
        init
        {
            name = "橡皮擦工具"
            toolIcon = TextureRegionDrawable(TextureRegion(Texture("eraser.png")))
        }
        override fun operate(sourceLedEntity: LEDEntity?, ledEntity: LEDEntity)
        {
            ledEntity.tempLedCell = LEDCell.ColorizedLEDCell()
        }
    }
    class Rectangle(ledPannelEntity: LEDPannelEntity, val cellsFiller: CellsFiller): Tool(ledPannelEntity)
    {
        val frontCell: LEDCell
        get() = cellsFiller.getFrontCell()
        val backCell: LEDCell
        get() = cellsFiller.getBackCell()
        init
        {
            name = "矩形工具"
            toolIcon = TextureRegionDrawable(TextureRegion(Texture("rect.png")))
        }
        val leds = ledPannelEntity.leds
        override fun operate(sourceLedEntity: LEDEntity?, ledEntity: LEDEntity)
        {
            var tx = ledEntity.pannelX
            var ty = ledEntity.pannelY
            var fx = sourceLedEntity?.pannelX?: tx
            var fy = sourceLedEntity?.pannelY?: ty
            for(y in 0 until leds.size)
            {
                for(x in 0 until leds[0].size)
                {
                    if((x in fx..tx || x in tx .. fx)&& (y in fy..ty || y in ty .. fy))
                    {
                        leds[y][x].tempLedCell = frontCell.clone() as LEDCell
                    }
                    else
                    {
                        ledPannelEntity.leds[y][x].tempLedCell = ledPannelEntity.leds[y][x].ledCell
                    }
                }
            }
        }
    }
    fun min(a: Int, b: Int) = if(a < b) a else b
    fun max(a: Int, b: Int) = if(a > b) a else b
    class Line(ledPannelEntity: LEDPannelEntity, val cellsFiller: CellsFiller): Tool(ledPannelEntity)
    {
        val frontCell: LEDCell
            get() = cellsFiller.getFrontCell()
        init
        {
            name = "线段工具"
            toolIcon = TextureRegionDrawable(TextureRegion(Texture("line.png")))
        }
        companion object
        {
            val pixmap: Pixmap = Pixmap(16, 16, Pixmap.Format.RGBA8888)
        }
        override fun operate(sourceLedEntity: LEDEntity?, ledEntity: LEDEntity)
        {
            pixmap.setColor(Color.BLACK)
            pixmap.fill()
            val tx = ledEntity.pannelX
            val ty = ledEntity.pannelY
            val fx = sourceLedEntity?.pannelX?: tx
            val fy = sourceLedEntity?.pannelY?: ty
            pixmap.setColor(Color.WHITE)
            pixmap.drawLine(fx, fy, tx, ty)
            for(y in 0 until ledPannelEntity.HEIGHT)
            {
                for(x in 0 until ledPannelEntity.WIDTH)
                {
                    if(pixmap.getPixel(x, y) == Color.WHITE.toIntBits())
                    {
                        ledPannelEntity.leds[y][x].tempLedCell = frontCell.clone() as LEDCell
                    }
                    else
                    {
                        ledPannelEntity.leds[y][x].tempLedCell = ledPannelEntity.leds[y][x].ledCell
                    }
                }
            }
        }
    }
    class Bucket(ledPannelEntity: LEDPannelEntity, val cellsFiller: CellsFiller): Tool(ledPannelEntity)
    {
        val frontCell: LEDCell
            get() = cellsFiller.getFrontCell()
        init
        {
            name = "油漆桶工具"
            toolIcon = TextureRegionDrawable(TextureRegion(Texture("bucket.png")))
        }
        val fillQueue = LinkedList<LEDEntity>()
        val filledLeds = kotlin.Array(ledPannelEntity.HEIGHT, { kotlin.BooleanArray(ledPannelEntity.WIDTH) })
        val directions = Array.with(kotlin.arrayOf(0, -1),
                kotlin.arrayOf(0, 1),
                kotlin.arrayOf(1, 0),
                kotlin.arrayOf(-1, 0))
        override fun operate(sourceLedEntity: LEDEntity?, ledEntity: LEDEntity)
        {
            fillQueue.clear()
            fillQueue.addLast(ledEntity)
            filledLeds.forEach {
                it.fill(false, 0, ledPannelEntity.WIDTH)
            }
            var currentLedEntity: LEDEntity
            do
            {
                currentLedEntity = fillQueue.pop()
                currentLedEntity.tempLedCell = frontCell.clone() as LEDCell
                filledLeds[currentLedEntity.pannelY][currentLedEntity.pannelX] = true
                directions.forEach {
                    val y = currentLedEntity.pannelY + it[1]
                    val x = currentLedEntity.pannelX + it[0]
                    if(x in 0 until ledPannelEntity.WIDTH && y in 0 until ledPannelEntity.HEIGHT)
                    {
                        val sideLed = ledPannelEntity.leds[y][x]
                        if(sideLed.ledCell == ledEntity.ledCell && !filledLeds[sideLed.pannelY][sideLed.pannelX])
                        {
                            fillQueue.push(sideLed)
                        }
                    }
                }
            }while (fillQueue.size > 0)
            for(y in 0 until ledPannelEntity.HEIGHT)
                for(x in 0 until ledPannelEntity.WIDTH)
                    if(!filledLeds[y][x])
                        ledPannelEntity.leds[y][x].tempLedCell = ledPannelEntity.leds[y][x].ledCell
        }

    }

}