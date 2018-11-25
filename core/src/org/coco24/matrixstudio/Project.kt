package org.coco24.matrixstudio

import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.Array

interface Project
{
    fun setName(name: String)
    fun getName(): String
    fun setSequences(sequences: Array<LEDSequence>)
    fun getSequences(): Array<LEDSequence>
    fun setQueues(queues: Array<NamedQueue<LEDCell>>)
    fun getQueues(): Array<NamedQueue<LEDCell>>
}