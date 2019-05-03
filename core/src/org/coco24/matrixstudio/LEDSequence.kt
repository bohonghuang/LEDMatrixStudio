package org.coco24.matrixstudio

open class LEDSequence(name: String = "Sequence", var width: Int = 0, var height: Int = 0): NamedArray<LEDPage>(name), Cloneable
{
    public override fun clone(): Any
    {
        val sequence = LEDSequence(name, width, height)
        forEach {
            sequence.add(it.clone() as LEDPage)
        }
        return sequence
    }
}