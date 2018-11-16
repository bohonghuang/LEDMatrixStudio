package org.coco24.matrixstudio.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import org.coco24.matrixstudio.MyGdxGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		config.title = "Matrix Studio";
		config.addIcon("logo_m.png", Files.FileType.Internal);
		new LwjglApplication(new MyGdxGame(), config);
	}
}
