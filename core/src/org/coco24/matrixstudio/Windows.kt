package org.coco24.matrixstudio

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.kotcrab.vis.ui.widget.*
import com.kotcrab.vis.ui.widget.spinner.ArraySpinnerModel
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner
import org.coco24.matrixstudio.MyGdxGame.R

object Windows
{
    interface BooleanCallback
    {
        fun callback(result: Boolean)
    }
    fun createButtonsTable(buttonBar: ButtonBar, size: Int): VisTable
    {
        val buttonTable = buttonBar.createTable()
        var loopCount = 0
        buttonTable.cells.forEach {
            if(loopCount < size)
                it.minWidth(200f).pad(5f * R.SCALE)
            else buttonTable.cells.removeValue(it, false)
            loopCount ++
        }
        buttonTable.center()
        buttonTable.pack()
        return buttonTable
    }
    fun createBooleanButtonsTable(callback: BooleanCallback? = null): VisTable
    {
        val buttonBar = ButtonBar()
        buttonBar.setButton(ButtonBar.ButtonType.OK, "确定", object : ChangeListener()
        {
            override fun changed(event: ChangeEvent?, actor: Actor?)
            {
                callback?.callback(true)
            }
        })
        buttonBar.setButton(ButtonBar.ButtonType.CANCEL, "取消", object : ChangeListener()
        {
            override fun changed(event: ChangeEvent?, actor: Actor?)
            {
                callback?.callback(false)
            }
        })
        return createButtonsTable(buttonBar, 2)
    }
    open class TextRequiredWindow(title: String = "请输入名称", var text: String = "") : VisWindow(title)
    {
        var callback: BooleanCallback? = null
        init
        {
            val nameTextField =  VisTextField()
            nameTextField.text = text
            add(nameTextField).expand().width(300f * R.SCALE).row()
            val buttonsTable = createBooleanButtonsTable(object : BooleanCallback
            {
                override fun callback(result: Boolean)
                {
                    if(result)
                    {
                        if(nameTextField.text.isBlank() || nameTextField.text.isEmpty())
                        {
                            nameTextField.isInputValid = false
                            return
                        }
                        else {
                            nameTextField.isInputValid = true
                        }
                        text = nameTextField.text
                        callback?.callback(true)
                        fadeOut()
                    }
                    else
                    {
                        callback?.callback(false)
                        fadeOut()
                    }

                }
            })
            add(buttonsTable).expand().fillX()
            pack()
            addCloseButton()
            isModal = true
            centerWindow ()

        }
    }
    open class NumberRequiredWindow(title: String = "请输入数字", var number: Int = 0, val negativeAllowed: Boolean = false, val spinnerUsed: Boolean = false): VisWindow(title)
    {
        var callback: BooleanCallback? = null
        init
        {

            val numberTextField =
                    if(spinnerUsed){
                        val spinner = Spinner("偏移", IntSpinnerModel(number, Int.MIN_VALUE, Int.MAX_VALUE))
                        spinner.textField
                    }else
                    {
                        VisTextField(number.toString())
                    }
            numberTextField.textFieldFilter = object : VisTextField.TextFieldFilter.DigitsOnlyFilter()
            {
                override fun acceptChar(textField: VisTextField?, c: Char): Boolean
                {
                    return (negativeAllowed && c == '-') || super.acceptChar(textField, c)
                }
            }
            add(if(spinnerUsed) numberTextField.parent else numberTextField).expand().width(300f * R.SCALE).row()
            val buttonsTable = createBooleanButtonsTable(object : BooleanCallback
            {
                override fun callback(result: Boolean)
                {
                    if(result)
                    {
                        if(numberTextField.text.isBlank() || numberTextField.text.isEmpty())
                        {
                            numberTextField.isInputValid = false
                            return
                        }
                        else {
                            numberTextField.isInputValid = true
                        }
                        number = numberTextField.text.toIntOrNull()?:0
                        callback?.callback(true)
                        fadeOut()
                    }
                    else
                    {
                        callback?.callback(false)
                        fadeOut()
                    }

                }
            })
            add(buttonsTable).expand().fillX()
            pack()
            addCloseButton()
            isModal = true
            centerWindow ()

        }
    }
}