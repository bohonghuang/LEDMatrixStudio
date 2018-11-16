package org.coco24.matrixstudio;

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

public class MyGdxGame extends Game
{
    public static class R
    {
        static float PPI = 108;
    }
	SpriteBatch batch;
	Texture img;

	@Override
	public void create () {

	    System.out.println("PPI: " + Gdx.graphics.getPpiY()) ;
	    float ppi = (Gdx.graphics.getPpiX() + Gdx.graphics.getPpiY()) / 2;
	    ppi = R.PPI;
	    if(ppi <= 128)
//		    VisUI.load(VisUI.SkinScale.X1);
                VisUI.load("skins/neutralizer/skin/neutralizer-ui.json");
		else VisUI.load(VisUI.SkinScale.X2);
//        VisUI.load("skins/tinted/x2/tinted.json");

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/default.ttf"));
		LazyBitmapFont lazyBitmapFont = new LazyBitmapFont(generator, (int)(ppi * 0.15f));
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
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		setScreen(new MainScreen());
        Gdx.graphics.setContinuousRendering(false);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();

//		Gdx.gl.glClearColor(1, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//		batch.begin();
//		batch.draw(img, 0, 0);
//		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
