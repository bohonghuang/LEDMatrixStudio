package org.coco24.matrixstudio

import com.badlogic.gdx.utils.Array

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
                array.add(LEDCell.ColorizedLEDCell())
            }
            leds.add(array)
        }
    }

    fun getWidth(): Int = width
    fun getHeight(): Int = height
}