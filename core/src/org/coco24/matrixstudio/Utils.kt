package org.coco24.matrixstudio

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Queue

object Utils
{
    fun <T> copyQueue(source: Queue<T>, target: Queue<T> = Queue<T>()): Queue<T>
    {
        target.clear()
        source.forEach {
            target.addLast(it)
        }
        return target
    }
    fun <T> copyNamedQueue(source: NamedQueue<T>, target: NamedQueue<T> = NamedQueue()): NamedQueue<T>
    {
        target.name = source.name
        return copyQueue(source, target) as NamedQueue<T>
    }
    interface ValueGetter<T, V>
    {
        fun get(t: T): V
    }
    fun <T, V> iterableToGdxArray(iterable: Iterable<T>, valueGetter: ValueGetter<T, V>): Array<V>
    {
        val array = Array<V>()
        iterable.forEach {
            array.add(valueGetter.get(it))
        }
        return array
    }
}