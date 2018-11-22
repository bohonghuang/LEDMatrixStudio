package org.coco24.matrixstudio;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Queue;

public abstract class LEDCell implements Cloneable
{
    private static Color nullColor = new Color(0f, 0f, 0f, 0f);
    public abstract Color getColor();

    @Override
    public abstract Object clone();

    public static class PointedColorizedLEDCell extends ColorizedLEDCell
    {
        private Color color;
        public PointedColorizedLEDCell(Color color)
        {
            this.color = color;
        }
        @Override
        public Color getColor()
        {
            return color;
        }
    }
    public static class ColorizedLEDCell extends LEDCell
    {
        private static float[] hsvArray = new float[3];
        private final Color color = new Color();
        public ColorizedLEDCell()
        {
            color.set(Color.BLACK);
            color.a = 0f;
        }
        public ColorizedLEDCell(Color color)
        {
            setColor(color);
        }
        @Override
        public Color getColor()
        {
            return color;
        }
        public void setColor(Color color)
        {
            color.toHsv(hsvArray);
            float alpha = Math.min(color.a, hsvArray[2]);
            hsvArray[2] = 1f;
            getColor().fromHsv(hsvArray).a = alpha;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj instanceof ColorizedLEDCell)
            {
                return ((ColorizedLEDCell)obj).color.equals(getColor());
            }
            else return false;
        }

        @Override
        public Object clone()
        {
            ColorizedLEDCell ledCell = new ColorizedLEDCell(getColor());
            return ledCell;
        }
    }
    public static class QueuedLEDCell extends LEDCell
    {
        NamedQueue<LEDCell> ledsQueue;
        int ledsQueueOffset = 0;

        public void setLedsQueue(NamedQueue<LEDCell> ledsQueue)
        {
            this.ledsQueue = ledsQueue;
        }

        public NamedQueue<LEDCell> getLedsQueue()
        {
            return ledsQueue;
        }
        public QueuedLEDCell()
        {

        }
        public QueuedLEDCell(NamedQueue<LEDCell> ledsQueue, int offset)
        {
            this.ledsQueue = ledsQueue;
            this.ledsQueueOffset = offset;
        }
        public ColorizedLEDCell toColorizedLEDCell()
        {
            return new ColorizedLEDCell(getColor());
        }

        @Override
        public Object clone()
        {
            QueuedLEDCell ledCell = new QueuedLEDCell(ledsQueue, ledsQueueOffset);
            return ledCell;
        }

        @Override
        public Color getColor()
        {
            if(ledsQueueOffset >= 0 && ledsQueueOffset < ledsQueue.size)
            return ledsQueue.get(ledsQueueOffset).getColor();
            else return nullColor;
        }
    }
}
