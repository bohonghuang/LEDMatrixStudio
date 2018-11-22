package org.coco24.matrixstudio

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.VisLabel
import kotlin.math.min

class LEDEntity(texture: Texture): MyTable()
{
    val label = VisLabel()
    fun centerLabel() = label.setPosition(width / 2, height / 2)
    var ledCell: LEDCell? = null
    set(value)
    {
        field = value
        image.color = value?.color?:return
        if(value is LEDCell.QueuedLEDCell)
        {
            label.setText("${value.ledsQueue.name}\n@${value.ledsQueueOffset}")
            centerLabel()
            label.isVisible = true
        }
        else
            label.isVisible = false
    }
    var tempLedCell: LEDCell? = null
    set(value)
    {
        field = value
        tempImage.color = field?.color?:return
    }
    var pannelX = -1
    var pannelY = -1
    var size = 72f
    val imageStack = Stack()
    val image = object : Image(texture)
    {
        override fun setVisible(visible: Boolean)
        {
            super.setVisible(visible)
            tempImage.isVisible = !visible;
        }
    }
    val backImage = Image(texture)
    val tempImage = Image(texture)
    init
    {
        imageStack.add(backImage)
        imageStack.add(image)
        imageStack.add(tempImage)
        imageStack.pack()
        add(imageStack).expand()
//                .maxSize(size, size)

        label.setAlignment(Align.center, Align.center)
        addActor(label)
        pack()
        backImage.color = Color.GRAY
        tempImage.color.a = 0f
        image.color.a = 0f
    }
    val hsvArray = FloatArray(3)
    override fun setColor(color: Color?)
    {
        if(color == null) return;
        color.toHsv(hsvArray)
        if(ledCell is LEDCell.ColorizedLEDCell)
        ledCell?.color?.set(color.r, color.g, color.b, min(hsvArray[2], color.a))
        else ledCell = LEDCell.ColorizedLEDCell(color)
        image.color = ledCell?.color?:return

    }
    fun setTempColor(color: Color)
    {
        color.toHsv(hsvArray)
        tempImage.setColor(color.r, color.g, color.b, min(hsvArray[2], color.a))
    }
    fun colorFromTemp()
    {
        color = tempImage.color
        image.isVisible = true
    }
    fun colorToTemp()
    {
        tempImage.color = image.color
        image.isVisible = false
    }
    fun cellFromTemp()
    {
        ledCell = tempLedCell
        image.isVisible = true
    }
    fun cellToTemp()
    {
        tempLedCell = ledCell
        image.isVisible = false
    }
    override fun getColor(): Color
    {
        return image.color
    }
}