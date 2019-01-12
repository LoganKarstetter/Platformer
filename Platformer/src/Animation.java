import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Animation
{
    private ArrayList<BufferedImage> images;
    private long totalDurationInMs;
    private long imageDurationInMs;
    private boolean isLooping;
    private AnimationWatcher watcher;

    public Animation(ArrayList<BufferedImage> imageSequence, long durationInMs, boolean loopAnimation)
    {
        //Verify valid duration
        if (durationInMs <= 0)
        {
            durationInMs = 1000;
        }

        //Store animation data
        images = imageSequence;
        totalDurationInMs = durationInMs;
        imageDurationInMs = totalDurationInMs / images.size();
        isLooping = loopAnimation;
    }

    public long update(long loopPeriodInMs, long localElapsedTimeInMs)
    {
        //Verify looping flag after the first complete loop
        if ((localElapsedTimeInMs / imageDurationInMs) < (images.size() - 1) || isLooping)
        {
            //Compute elapsed time and reset to zero if it's greater than or equal to the total duration
            localElapsedTimeInMs = (localElapsedTimeInMs + loopPeriodInMs) % totalDurationInMs;
        }
        else //Inform the watcher that the animation ended
        {
            if (watcher != null) {
                watcher.animationHasEnded();
            }
        }

        //Return updated local elapsed time
        return localElapsedTimeInMs;
    }

    public void draw(Graphics dbGraphics, int x, int y, long localElapsedTimeInMs)
    {
        //Draw the animation
        dbGraphics.drawImage(images.get((int) (localElapsedTimeInMs / imageDurationInMs)), x, y, null);
    }

    public void setWatcher(AnimationWatcher animationWatcher)
    {
        watcher = animationWatcher;
    }

    public int getImageWidth()
    {
        return images.get(0).getWidth();
    }

    public int getImageHeight()
    {
        return images.get(0).getHeight();
    }
}
