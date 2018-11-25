package org.coco24.matrixstudio

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.leff.midi.MidiFile
import com.leff.midi.MidiTrack

object LightsConverter
{
    var mk2ColorPalette = intArrayOf(//LaunchpadColorPallette
            0x00000000, //0
            0x001E1E1E, //1
            0x007F7F7F, //2
            0x00FFFFFF, //3
            0x00FF4C4C, //4
            0x00FF0000, //5
            0x00590000, //6
            0x00190000, //7
            0x00FFBD6C, //8
            0x00FF5400, //9
            0x00591D00, //10
            0x00271B00, //11
            0x00FFFF4C, //12
            0x00FFFF00, //13
            0x00595900, //14
            0x00191900, //15
            0x0088FF4C, //16
            0x0054FF00, //17
            0x001D5900, //18
            0x00142B00, //19
            0x004CFF4C, //20
            0x0000FF00, //21
            0x00005900, //22
            0x00001900, //23
            0x004CFF5E, //24
            0x0000FF19, //25
            0x0000590D, //26
            0x00001902, //27
            0x004CFF88, //28
            0x0000FF55, //29
            0x0000591D, //30
            0x00001F12, //31
            0x004CFFB7, //32
            0x0000FF99, //33
            0x00005935, //34
            0x00001912, //35
            0x004CC3FF, //36
            0x0000A9FF, //37
            0x00004152, //38
            0x00001019, //39
            0x004C88FF, //40
            0x000055FF, //41
            0x00001D59, //42
            0x00000819, //43
            0x004C4CFF, //44
            0x000000FF, //45
            0x00000059, //46
            0x00000019, //47
            0x00874CFF, //48
            0x005400FF, //49
            0x00190064, //50
            0x000F0030, //51
            0x00FF4CFF, //52
            0x00FF00FF, //53
            0x00590059, //54
            0x00190019, //55
            0x00FF4C87, //56
            0x00FF0054, //57
            0x0059001D, //58
            0x00220013, //59
            0x00FF1500, //60
            0x00993500, //61
            0x00795100, //62
            0x00436400, //63
            0x00033900, //64
            0x00005735, //65
            0x0000547F, //66
            0x000000FF, //67
            0x0000454F, //68
            0x002500CC, //69
            0x007F7F7F, //70
            0x00202020, //71
            0x00FF0000, //72
            0x00BDFF2D, //73
            0x00AFED06, //74
            0x0064FF09, //75
            0x00108B00, //76
            0x0000FF87, //77
            0x0000A9FF, //78
            0x00002AFF, //79
            0x003F00FF, //80
            0x007A00FF, //81
            0x00B21A7D, //82
            0x00402100, //83
            0x00FF4A00, //84
            0x0088E106, //85
            0x0072FF15, //86
            0x0000FF00, //87
            0x003BFF26, //88
            0x0059FF71, //89
            0x0038FFCC, //90
            0x005B8AFF, //91
            0x003151C6, //92
            0x00877FE9, //93
            0x00D31DFF, //94
            0x00FF005D, //95
            0x00FF7F00, //96
            0x00B9B000, //97
            0x0090FF00, //98
            0x00835D07, //99
            0x00392B00, //100
            0x00144C10, //101
            0x000D5038, //102
            0x0015152A, //103
            0x0016205A, //104
            0x00693C1C, //105
            0x00A8000A, //106
            0x00DE513D, //107
            0x00D86A1C, //108
            0x00FFE126, //109
            0x009EE12F, //110
            0x0067B50F, //111
            0x001E1E30, //112
            0x00DCFF6B, //113
            0x0080FFBD, //114
            0x009A99FF, //115
            0x008E66FF, //116
            0x00404040, //117
            0x00757575, //118
            0x00E0FFFF, //119
            0x00A00000, //120
            0x00350000, //121
            0x001AD000, //122
            0x00074200, //123
            0x00B9B000, //124
            0x003F3100, //125
            0x00B35F00, //126
            0x004B1502  //127,
    )
    fun rgbToRgba(rgb: Int): Int = (rgb shl 8) + 0xFF
    fun nearestColor(color: Color, palette: IntArray): Int
    {
        val multipler = color.a / 1f
        val targetColor = Color(color.r * multipler, color.g * multipler, color.b * multipler, 1f)
        val tempColor = Color(Color.WHITE)
        var nearestIndex = 0
        var nearestValue = Float.MAX_VALUE
        for(i in 0 until palette.size)
        {
            tempColor.set(rgbToRgba(palette[i]))
            val tempValue = Math.abs(targetColor.r - tempColor.r) + Math.abs(targetColor.g - tempColor.g) + Math.abs(targetColor.b - tempColor.b)
            if(tempValue < nearestValue)
            {
                nearestIndex = i
                nearestValue = tempValue
            }
        }
        return nearestIndex
    }
    class MidiMessage(byteArray: ByteArray): javax.sound.midi.MidiMessage(byteArray)
    {
        override fun clone(): Any
        {
            return this;
        }

    }
    enum class CoordinatesConfig
    {
        Launchapd, LaunchpadPro, Generic, Matrix203
    }
    fun pannelMatrix2MatrixCoordinates(x: Int, y: Int, config: CoordinatesConfig): Int //按键矩阵
    {
        when(config)
        {
            CoordinatesConfig.LaunchpadPro, CoordinatesConfig.Launchapd ->
            {
                val startNote = 24
                val column = 4
                val height = 8
                val pre = (x / column) * (column * height)
                return startNote + pre + ((7 - y) * column + x % column)
            }
            CoordinatesConfig.Generic ->
            {
                val startNote = 24
                return startNote + (7 - y) * 8 + x
            }
            CoordinatesConfig.Matrix203 ->
            {
                val startNote = 24
                return startNote + y * 8 + x
            }

        }
    }
    fun pannel2MatrixCoordinates(_x: Int, _y: Int, config: CoordinatesConfig): Int
    {
        var x = _x
        var y = _y
        if(x !in 1 .. 8 || y !in 1 .. 8) return -1 //TODO 侧边灯光
        x -= 1
        y -= 1
        return pannelMatrix2MatrixCoordinates(x, y, config)
    }
    fun writeMidiFileFromSequence(seq: Array<LEDPage>, config: CoordinatesConfig, palette: IntArray, fileHandle: FileHandle)
    {
        val midiFile = MidiFile()
        val midiTrack = MidiTrack()
        for(pageIndex in 0 until seq.size)
        {
                for(y in 0 until seq[pageIndex].leds.size)
                {
                    for(x in 0 until seq[pageIndex].leds[y].size)
                    {
                        val keyNum = pannel2MatrixCoordinates(x, y, config)
                        if(keyNum == -1) continue
                        val velocity = nearestColor(seq[pageIndex].leds[y][x].color, palette)
                        if(velocity == 0) continue
                        midiTrack.insertNote( 1, keyNum, velocity, pageIndex.toLong(), 1L)
                    }
                }

        }

        midiFile.addTrack(midiTrack)
        midiFile.write(fileHandle.write(false))
    }
}