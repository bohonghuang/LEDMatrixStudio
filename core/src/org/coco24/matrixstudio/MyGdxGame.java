package org.coco24.matrixstudio;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerStyle;
import com.rpsg.lazyFont.LazyBitmapFont;

import aurelienribon.tweenengine.Tween;

public class MyGdxGame extends Game
{
    public static class R
    {
        static float PPI = 100;
        static float SCALE = 1f;
        static float unitsPerPixel = 1f;
        static int fontSize = 20;
    }
	@Override
	public void create () {

	    System.out.println("PPI: " + Gdx.graphics.getPpiY()) ;

		Tween.registerAccessor(MainScreen.class, new PagesSurface.Companion.PagesSurfaceAccessor());

		R.PPI = (Gdx.graphics.getPpiX() + Gdx.graphics.getPpiY()) / 2;
		if(Gdx.app.getType() == Application.ApplicationType.Desktop)
		{
			R.SCALE = R.PPI / 100;
			R.fontSize *= 0.8f;
			if(R.PPI < 128)
//				VisUI.load(VisUI.SkinScale.X1);
				VisUI.load("skins/neutralizerui/neutralizer-ui.json");
			else
			{
				VisUI.load(VisUI.SkinScale.X2);
				R.SCALE /= 2;
			}

		}
		else
		{
			VisUI.load(VisUI.SkinScale.X2);
			R.SCALE = 1.2f;
			R.unitsPerPixel =  600f / Gdx.graphics.getHeight();
			R.fontSize *= 0.9f;
		}
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/default.ttf"));
		LazyBitmapFont lazyBitmapFont = new LazyBitmapFont(generator, (int)(R.fontSize * R.SCALE));
		VisUI.getSkin().get(Label.LabelStyle.class).font = lazyBitmapFont;
		VisUI.getSkin().get(VisLabel.LabelStyle.class).font = lazyBitmapFont;
		VisUI.getSkin().get(VisTextButton.VisTextButtonStyle.class).font = lazyBitmapFont;
		VisUI.getSkin().get(TextButton.TextButtonStyle.class).font = lazyBitmapFont;
		VisUI.getSkin().get(MenuItem.MenuItemStyle.class).font = lazyBitmapFont;
		VisUI.getSkin().get(Menu.MenuStyle.class).openButtonStyle.font = lazyBitmapFont;
		VisUI.getSkin().get(VisList.ListStyle.class).font = lazyBitmapFont;
		VisUI.getSkin().get(VisSelectBox.SelectBoxStyle.class).font = lazyBitmapFont;
		VisUI.getSkin().get(SelectBox.SelectBoxStyle.class).font = lazyBitmapFont;
		VisUI.getSkin().get(VisSelectBox.SelectBoxStyle.class).listStyle.font = lazyBitmapFont;
		VisUI.getSkin().get(VisWindow.WindowStyle.class).titleFont = lazyBitmapFont;
		VisUI.getSkin().get(VisTextField.VisTextFieldStyle.class).font = lazyBitmapFont;
		VisUI.getSkin().get(ColorPickerStyle.class).titleFont = lazyBitmapFont;
		VisUI.getSkin().get(VisCheckBox.VisCheckBoxStyle.class).font = lazyBitmapFont;
		setScreen(new MainScreen());
        Gdx.graphics.setContinuousRendering(false);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();
	}
	
	@Override
	public void dispose () {
		VisUI.dispose();

	}
}
