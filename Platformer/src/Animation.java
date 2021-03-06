import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * @author Logan Karstetter
 * Date: 2018
 */
public class Animation
{
    /** The constant returned instead of the localElapsedTime whenever
     * an non-looping animation has ended. This is more efficient than
     * using watchers since multiple entities share the same animations */
    public static final int ANIMATION_ENDED = -1;
    /** The sequence of images for this animation */
    private ArrayList<BufferedImage> images;
    /** The total duration of the animation in ms */
    private long totalDurationInMs;
    /** The duration that a single image displayed in ms */
    private long imageDurationInMs;
    /** Looping animation flag */
    private boolean isLooping;

    /**
     * Create a new animation using an sequence of images, a specific duration in milliseconds, and
     * a flag specifying whether the animation should loop upon completion.
     * @param imageSequence The sequence of images.
     * @param durationInMs The duration of the sequence in ms.
     * @param loopAnimation The looping animation flag
     */
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

    /**
     * Update the state of the animation. This method takes in the loop period to calculate time change and
     * an amount of time in ms passed by the caller that stores the animation's progress.
     * @param loopPeriodInMs The loop period of the game cycle in ms.
     * @param localElapsedTimeInMs The amount of time elapsed for this animation.
     * @return The updated elapsed time in ms.
     */
    public long update(long loopPeriodInMs, long localElapsedTimeInMs)
    {
        //Update the animation if we are not on the last frame or if it is looping
        if ((localElapsedTimeInMs / imageDurationInMs) < (images.size() - 1) || isLooping)
        {
            //Compute elapsed time and reset to zero if it's greater than or equal to the total duration
            localElapsedTimeInMs = (localElapsedTimeInMs + loopPeriodInMs) % totalDurationInMs;
        }
        else //Inform the entity that the animation has ended
        {
            return ANIMATION_ENDED;
        }

        //Return updated local elapsed time
        return localElapsedTimeInMs;
    }

    /**
     * Draw the animation. The frame to draw is calculated using the localElapsedTimeInMs.
     * @param dbGraphics The graphics object that will draw the animation.
     * @param x The x position on the screen to draw the animation at.
     * @param y The y position on the screen to draw the animation at.
     * @param localElapsedTimeInMs The amount of time elapsed for this animation.
     */
    public void draw(Graphics dbGraphics, int x, int y, long localElapsedTimeInMs)
    {
        //Draw the animation
        dbGraphics.drawImage(images.get((int) (localElapsedTimeInMs / imageDurationInMs)), x, y, null);
    }

    /**
     * Get the width of the first frame of animation.
     * @return The width of the image.
     */
    public int getImageWidth()
    {
        return images.get(0).getWidth();
    }

    /**
     * Get the height of the first frame of animation.
     * @return The height of the image.
     */
    public int getImageHeight()
    {
        return images.get(0).getHeight();
    }
}