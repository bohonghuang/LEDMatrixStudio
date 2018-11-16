package org.coco24.matrixstudio

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import kotlin.math.min

class LED(texture: Texture): MyTable()
{
    var pannelX = -1
    var pannelY = -1
    var size = 72f
    val imageStack = Stack()
    val image = Image(texture)
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
        pack()
        backImage.color = Color.GRAY
        tempImage.color.a = 0f
    }
    val hsvArray = FloatArray(3)
    override fun setColor(color: Color?)
    {
        if(color == null) return;
        color.toHsv(hsvArray)
        image.setColor(color.r, color.g, color.b, min(hsvArray[2], color.a))
    }
    fun setTempColor(color: Color)
    {
        color.toHsv(hsvArray)
        tempImage.setColor(color.r, color.g, color.b, min(hsvArray[2], color.a))
    }
    fun colorFromTemp()
    {
        color = tempImage.color
        tempImage.isVisible = false
        image.isVisible = true
    }
    fun colorToTemp()
    {
        tempImage.color = image.color
        image.isVisible = false
        tempImage.isVisible = true
    }

    override fun getColor(): Color
    {
        return image.color
    }
}