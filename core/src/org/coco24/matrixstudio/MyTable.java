package org.coco24.matrixstudio;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class MyTable extends Table
{
    static class R
    {
        static float DEFAULT_SCALE = 1.0f;
    }
    protected float clickRange = 10f;
//            * R.PPI_SCALE;

    public float getParentAlpha()
    {
        return parentAlpha;
    }

    public void setParentAlpha(float parentAlpha)
    {
        this.parentAlpha = parentAlpha;
    }

    float offsetX = 0;

    public float getOffsetX()
    {
        return offsetX;
    }

    public void setOffsetX(float offsetX)
    {
        this.offsetX = offsetX;
    }

    public float getOffsetY()
    {
        return offsetY;
    }

    public void setOffsetY(float offsetY)
    {
        this.offsetY = offsetY;
    }

    float offsetY = 0;


    float parentAlpha = 1.0f;

    enum ClickType
    {
        TARGET_RANGE_DETECTION, SIZE_RANGE_DETECTION
    }

    ClickType clickType;

    public MyTable()
    {
        super();
        this.clickType = ClickType.TARGET_RANGE_DETECTION;
        init();
    }

    public MyTable(ClickType clickType)
    {
        super();
        this.clickType = clickType;
        init();
    }

    public boolean isDraggable()
    {
        return draggable;
    }

    public void setDraggable(boolean draggable)
    {
        this.draggable = draggable;
    }

    private boolean draggable = false;

    protected void init()
    {
        setTouchable(Touchable.enabled);
        addListener(new InputListener()
        {
            float touchDownX = 0, touchDownY = 0;

            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button)
            {
                touchDownX = x;
                touchDownY = y;
                handleTouchDown(x, y, pointer, button);
                return super.touchDown(event, x, y, pointer, button);
            }

            public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button)
            {
                handleTouchUp(x, y, pointer, button);
                if (clickType == ClickType.TARGET_RANGE_DETECTION)
                {
                    if (x >= touchDownX - clickRange && x <= touchDownX + clickRange
                            && y >= touchDownY - clickRange && y <= touchDownY + clickRange)
                    {
                        handleClick();
                    }
                } else if (clickType == ClickType.SIZE_RANGE_DETECTION)
                {
                    System.out.println("x=" + x + ";y=" + y);
                    System.out.println("getX=" + getX() + ";getY=" + getY());
                    Vector2 tablePosition = new Vector2();
                    tablePosition.x = getX();
                    tablePosition.y = getY();
                    Vector2 touchUpPosition = new Vector2();
                    touchUpPosition.x = x;
                    touchUpPosition.y = y;
                    tablePosition = MyTable.this.getParent().localToStageCoordinates(tablePosition);
                    touchUpPosition = MyTable.this.localToStageCoordinates(touchUpPosition);
                    System.out.println("positionX=" + tablePosition.x + ";positionY=" + tablePosition.y);
                    if (touchUpPosition.x >= tablePosition.x && touchUpPosition.x <= tablePosition.x + getScaledWidth() && touchUpPosition.y >= tablePosition.y && touchUpPosition.y <= tablePosition.y + getScaledHeight())
                    {
                        handleClick();
                    }
                }
            }

            public void touchDragged(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer)
            {
                if(draggable)
                handleTouchDragged(x, y, pointer);
            }

        });
    }

    @Override
    public float getX()
    {
        return super.getX() + offsetX;
    }

    @Override
    public float getY()
    {
        return super.getY() + offsetY;
    }


    public float getBackgroundScale()
    {
        if (backgroundScale != -1) return backgroundScale;
        else return R.DEFAULT_SCALE;
    }

    public void setBackgroundScale(float backgroundScale)
    {
        this.backgroundScale = backgroundScale;
    }

    float backgroundScale = 1;

    @Override
    protected void drawBackground(Batch batch, float parentAlpha, float x, float y)
    {

        if (getBackground() == null) return;
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        float scale = getBackgroundScale();
        if (getBackground() instanceof NinePatchDrawable)
        {
            NinePatchDrawable ninePatchDrawable = (NinePatchDrawable) getBackground();
//            System.out.println(ninePatchDrawable.getLeftWidth());
            ninePatchDrawable.draw(batch, x, y,
                    (ninePatchDrawable.getLeftWidth()),//自带scale和自减，本来应该是ninePatchDrawable.getLeftWidth()*(scale-1)的！！！
                    ninePatchDrawable.getBottomHeight(),
                    getScaledWidth() / scale,
                    getScaledHeight() / scale, scale, scale, 0);
            setScaleX(getScaledWidth() / getWidth());
            setScaleY(getScaledHeight() / getHeight());
        } else
        {
            getBackground().draw(batch, x, y, getScaledWidth() * scale, getScaledHeight() * scale);
            setScale(scale);
        }
    }

    public float getScaledWidth()
    {

        float scale = getBackgroundScale();
        if (getBackground() instanceof NinePatchDrawable)
        {
            NinePatchDrawable ninePatchDrawable = (NinePatchDrawable) getBackground();
            return super.getWidth() + (ninePatchDrawable.getLeftWidth() + ninePatchDrawable.getRightWidth()) * (scale - 1);
        } else return super.getWidth() * scale;
    }

    public float getScaledHeight()
    {
        float scale = getBackgroundScale();
        if (getBackground() instanceof NinePatchDrawable)
        {
            NinePatchDrawable ninePatchDrawable = (NinePatchDrawable) getBackground();
            return super.getHeight() + (ninePatchDrawable.getTopHeight() + ninePatchDrawable.getBottomHeight()) * (scale - 1);
        } else return super.getHeight() * scale;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        super.draw(batch, this.parentAlpha);
    }
    //	public float getOriginWidth()
//	{
//		return super.getWidth();
//	}
//	public float getOriginHeight()
//	{
//		return super.getHeight();
//	}


    protected void handleClick()
    {
        System.out.println("tableClick!");
    }

    protected void handleTouchUp(float x, float y, int pointer, int button)
    {
    }

    protected void handleTouchDown(float x, float y, int pointer, int button)
    {
    }
    protected void handleTouchDragged(float x, float y, int pointer)
    {
    }
}
