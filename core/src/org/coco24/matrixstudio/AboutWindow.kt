package org.coco24.matrixstudio

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.VisTable
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.VisWindow
import org.coco24.matrixstudio.MyGdxGame.R

class AboutWindow: VisWindow("关于")
{
    val rootTable = VisTable()
    val logoTexture = Texture("logo.png")
    init
    {
        val largeLogo = Image(logoTexture)
        rootTable.add(largeLogo).expand().pad(10f * R.SCALE).size(200f * R.SCALE).row()
        rootTable.addSeparator().width(160f * R.SCALE).row()
        rootTable.add("Matrix Studio").row()
        rootTable.add("2018").row()
        rootTable.addSeparator().width(160f * R.SCALE).row()
        val confirmButton = VisTextButton("确定")
        confirmButton.addListener(object : ChangeListener()
        {
            override fun changed(event: ChangeEvent?, actor: Actor?)
            {
                close()
            }
        })
        rootTable.add(confirmButton).expand().pad(5f * R.SCALE).minWidth(250f * R.SCALE)
        rootTable.pack()
        add(rootTable)
        pack()
        addCloseButton()
    }

    override fun remove(): Boolean
    {
        val rt = super.remove()
        logoTexture.dispose()
        return  rt
    }

    override fun fadeIn(): VisWindow
    {
        setPosition(stage.width / 2, stage.height / 2, Align.center)
        return super.fadeIn()
    }
}