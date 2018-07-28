import java.awt.*;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Logan Karstetter
 * Date: 07/01/2018
 */
public class BlockManager
{
    /** The default width of the grid in blocks */
    private static final int DEFAULT_WIDTH = 10;
    /** The default height of the grid in blocks*/
    private static final int DEFAULT_HEIGHT = 10;
    /** The width of each block */
    private static final int BLOCK_WIDTH = 30;
    /** The height of each block */
    private static final int BLOCK_HEIGHT = 30;

    /** The grid of block ids that create the map */
    private int[][] blocksGrid;
    /** The width of the grid of blocks (in blocks) */
    private int gridWidth = DEFAULT_WIDTH;
    /** The height of the grid of blocks (in blocks) */
    private int gridHeight = DEFAULT_HEIGHT;
    /** The width of the entire map in pixels */
    private int mapWidth;
    /** The height of the entire map in pixels */
    private int mapHeight;

    /** The path to the file to load levels from */
    private String filePath = "Levels/LevelsConfig.txt";

    /**
     * Create a BlockManager for centralizing all the interactions with the
     * blocks. Upon creation a BlockManager initializes all the blocks used
     * in the game. An ImageLoader is passed to load the images for each of
     * the blocks.
     * @param imageLoader The ImageLoader used to load the block's images.
     */
    public BlockManager(ImageLoader imageLoader)
    {
        //Create the blocksGrid
        Block.initializeBlocks(imageLoader, BLOCK_WIDTH, BLOCK_HEIGHT);

        //Load the layout of the blocks from the level config(s)
        loadLevels();

        //Calculate the map dimensions
        mapWidth = gridWidth * BLOCK_WIDTH;
        mapHeight = gridHeight * BLOCK_HEIGHT;
    }

    /**
     * Draw the blocks onto the screen using the specified offset.
     * If a block cannot be seen on screen then it will not be drawn.
     * @param dbGraphics The graphics object used to draw the blocks.
     * @param offsetX The pixel offset in the x direction.
     * @param offsetY The pixel offset in the y direction.
     */
    public void draw(Graphics dbGraphics, int offsetX, int offsetY)
    {
        //Draw each block onto the screen in row order
        for (int x = 0; x < gridWidth; x++)
        {
            for (int y = 0; y < gridHeight; y++)
            {
                //Only draw the blocks that can be seen on screen
                if ((x * BLOCK_WIDTH + BLOCK_WIDTH + offsetX > 0)
                        && (x * BLOCK_WIDTH + offsetX < PlatformerPanel.WIDTH))
                {
                    Block.blocks.get(blocksGrid[x][y]).draw(dbGraphics,
                            x * BLOCK_WIDTH + offsetX, y * BLOCK_HEIGHT + offsetY);
                }
            }
        }
    }

    /**
     * Determine whether the sprite will collide with a block if it attempts
     * to move by the given xStep value. If not, then the original xStep value
     * is returned. Otherwise, the method will calculate the xStep needed to
     * place the sprite up against the block without intersecting it.
     * @param xPos The x coordinate position of the sprite with respect to the map.
     * @param yPos The y coordinate position of the sprite with respect to the map.
     * @param spriteWidth The width of the sprite in pixels.
     * @param xStep The number of pixels to the left or right the sprite is trying to move.
     * @return The original or an adjusted xStep value.
     */
    public int checkHorizontalCollisions(int xPos, int yPos, int spriteWidth, int xStep)
    {
        //Declare two variables for holding grid coordinates
        int gridX, gridY;

        //Determine whether the sprite is moving left or right
        if (xStep > 0) //The sprite is moving right
        {
            //Calculate the grid coordinates, account for sprite width
            gridX = (xPos + xStep + spriteWidth) / BLOCK_WIDTH;
            gridY = yPos / BLOCK_HEIGHT;

            //Check special conditions that could trigger exceptions
            if (gridX >= gridWidth)  //If the gridX is at or outside the bounds of the grid
            {
                //We've reached the right edge of the map, move the sprite up against the edge
                return mapWidth - (xPos + spriteWidth);
            }
            else if (gridY < 0) //If the gridY is above the map, the sprite may be jumping
            {
                return xStep;
            }

            //If the movement will result in collision with a block
            if (Block.blocks.get(blocksGrid[gridX][gridY]).isActive())
            {
                return (gridX * BLOCK_WIDTH) - (xPos + spriteWidth);
            }
            else //Return the original xStep
            {
                return xStep;
            }
        }
        else //The sprite is moving left
        {
            //We do not need to account for sprite width
            gridX = (xPos + xStep) / BLOCK_WIDTH;
            gridY = yPos / BLOCK_HEIGHT;

            //Check special conditions that could trigger exceptions
            if (gridX < 0) //If the gridX is less than zero
            {
                //We're headed off the left side of the map
                return 0 - xPos;
            }
            else if (gridY < 0) //If the gridY is above the map, the sprite may be jumping
            {
                return xStep;
            }

            //If the movement will result in collision with a block
            if (Block.blocks.get(blocksGrid[gridX][gridY]).isActive())
            {
                //Account for the width of the block
                return ((gridX * BLOCK_WIDTH) + BLOCK_WIDTH) - xPos;
            }
            else //Return the original xStep
            {
                return xStep;
            }
        }
    }

    /**
     * Determine whether the sprite will collide with a block if it attempts
     * to jump or fall by the given yStep value. If so, then the method will
     * calculate an adjusted yStep value needed to hit the bottom of or land
     * on top of the block. Otherwise, the original yStep will be returned.
     * @param xPos The x coordinate position of the sprite with respect to the map.
     * @param yPos The y coordinate position of the sprite with respect to the map.
     * @param spriteWidth The width of the sprite in pixels.
     * @param spriteHeight The height of the sprite in pixels
     * @param yStep The number of pixels up or down the sprite is trying to move.
     * @return The original or an adjusted yStep value.
     */
    public int checkVerticalCollisions(int xPos, int yPos, int spriteWidth, int spriteHeight, int yStep)
    {
        //Declare three variables for the sprite's grid coordinates
        int gridXLeft, gridXRight, gridY; //The two x grid coordinates check the left and right sides of the sprite

        //Determine whether the sprite is moving up or down
        if (yStep < 0) //The sprite is moving upwards
        {
            //Calculate the grid coordinates
            gridXLeft = xPos / BLOCK_WIDTH;
            gridXRight = (xPos + spriteWidth) / BLOCK_WIDTH;
            gridY = (yPos + yStep) / BLOCK_HEIGHT;

            //Check if the sprite is jumping or falling out of the map
            if (gridY < 0 || gridY >= gridHeight)
            {
                return yStep; //Let them fall
            }
            else if (gridXRight >= gridWidth) //If the sprite is near the right border of the map
            {
                gridXRight = gridXLeft; //Nothing exists off the map so there cannot be collision, ignore gridXRight here
            }

            //If the jumping will result in collision with any overhead block
            if (Block.blocks.get(blocksGrid[gridXLeft][gridY]).isActive()
                    || Block.blocks.get(blocksGrid[gridXRight][gridY]).isActive())
            {
                return ((gridY * BLOCK_HEIGHT) + BLOCK_HEIGHT) - yPos; //Account for block height
            }
            else //Return original yStep
            {
                return yStep;
            }
        }
        else //The sprite is moving downwards
        {
            //Account for sprite height when falling
            gridXLeft = xPos / BLOCK_WIDTH;
            gridXRight = (xPos + spriteWidth) / BLOCK_WIDTH;
            gridY = (yPos + yStep + spriteHeight) / BLOCK_HEIGHT;

            //Check special conditions that could cause exceptions
            if (gridY < 0 || gridY >= gridHeight) //If the sprite is jumping or falling out of the map
            {
                return yStep; //Let them fall
            }
            else if (gridXRight >= gridWidth) //If the sprite is near the right border of the map
            {
                gridXRight = gridXLeft; //Nothing exists off the map so there cannot be collision, ignore gridXRight here
            }

            //If the falling will result in collision with any block beneath the sprite
            if (Block.blocks.get(blocksGrid[gridXLeft][gridY]).isActive()
                    || Block.blocks.get(blocksGrid[gridXRight][gridY]).isActive())
            {
                return (gridY * BLOCK_HEIGHT) - (yPos + spriteHeight);
            }
            else //Return original yStep
            {
                return yStep;
            }
        }
    }

    /**
     * Load levels described within the file located at the given filePath. For a single level,
     * the format of the file is as follows. The first line should be the dimensions of the level
     * enclosed in brackets. For example, the first line of a 10x10 grid level would be [10, 10].
     * The remaining lines should be a series of integers ranging from 0 to 9 separated by commas.
     * The number of integers on each line should match the given width dimension. Similarly the
     * number of lines of numbers should match the height dimension. The number zero should be
     * reserved for "empty space" blocks.
     */
    private void loadLevels()
    {
        //Inform the user of the file reading
        System.out.println("Reading file: " + filePath);
        try
        {
            //Create an InputStream and BufferedReader to read the file
            InputStream inputStream = this.getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            //Loop until the end of the file is reached
            String line;
            int lineCount = 0; //A counter to determine which row the read id's should be stored
            while ((line = br.readLine()) != null)
            {
                //Determine what action to take based off the line read
                if (line.startsWith("//") || (line.length() == 0)) //This line is a comment or blank line
                {
                    continue;
                }
                else if (line.startsWith("[")) //This line specifies the dimensions of the grid
                {
                    //Read the dimensions of the blocksGrid
                    gridWidth = Integer.parseInt(line.substring(line.indexOf('[') + 1, line.indexOf(',')).trim());
                    gridHeight = Integer.parseInt(line.substring(line.indexOf(',') + 1, line.indexOf(']')).trim());
                    blocksGrid = new int[gridWidth][gridHeight];
                }
                else //The line is one or more numbers representing a block id
                {
                    //Split the line by commas and fill the grid with ids
                    String lineIds[] = line.split(",");
                    for (int i = 0; i < gridWidth; i++)
                    {
                        blocksGrid[i][lineCount] = Integer.parseInt(lineIds[i].trim());
                    }

                    //Increment the line counter
                    lineCount++;
                }
            }

            //Close the BufferedReader
            br.close();

            //Check that the number of lines read matches the dimensions specified in the file
            if (lineCount != gridHeight)
            {
                System.out.println("WARNING: The number of lines read does not match the given level dimensions.");
            }

            //Inform the user the ImageLoader is done reading
            System.out.println("Finished reading file: " + filePath);
        }
        catch (IOException e)
        {
            System.out.println("Error reading file: " + filePath + " " + e);
            e.printStackTrace();
        }
        catch (NumberFormatException e)
        {
            System.out.println("Format error reading file: " + filePath + " " + e);
            e.printStackTrace();
        }
        catch (IndexOutOfBoundsException e)
        {
            System.out.println("Index error reading file: " + filePath + " " + e);
            e.printStackTrace();
        }
    }

    /**
     * Get the coordinates of spawn point for the platformerSprite.
     * In the event that the spawn point cannot be located, a point
     * at (0, 0) will be returned.
     * @return A new point containing the spawn point coordinates.
     */
    public Point getSpawnPoint()
    {
        //Search the blocksGrid for a block with id == 1
        for (int i = 0; i < gridWidth; i++)
        {
            for (int j = 0; j < gridHeight; j++)
            {
                //If the id stored in the index is 1, return the coordinates
                if (blocksGrid[i][j] == 1)
                {
                    return new Point(i * BLOCK_WIDTH, j * BLOCK_HEIGHT);
                }
            }
        }

        //Return a point at (0, 0) if there are not blocks with id == 1
        System.out.println("Error locating spawn coordinates.");
        return new Point(0, 0);
    }

    /**
     * Get the dimensions of the game map.
     * @return A new point containing the map dimensions.
     */
    public Point getMapDimensions()
    {
        return new Point(mapWidth, mapHeight);
    }
}
