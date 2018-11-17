package org.coco24.matrixstudio

import aurelienribon.tweenengine.TweenAccessor

interface PagesSurface
{
    companion object
    {
        class PagesSurfaceAccessor: TweenAccessor<PagesSurface>
        {
            override fun setValues(target: PagesSurface, type: Int, newValues: FloatArray)
            {
                val page = newValues[0].toInt()
                if(page != target.getCurrentPage())
                target.changePage(page)
            }

            override fun getValues(target: PagesSurface, type: Int, values: FloatArray): Int
            {
                values[0] = target.getCurrentPage().toFloat()
                return 1
            }
            enum class Type
            {
                Loop, Loop_Turnback, Loop_Reversed
            }

        }
    }
    fun changePage(page: Int);
    fun getPagesSize(): Int;
    fun getCurrentPage(): Int;
}