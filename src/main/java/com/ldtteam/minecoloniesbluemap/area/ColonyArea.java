package com.ldtteam.minecoloniesbluemap.area;

import com.flowpowered.math.vector.Vector2d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class containing the X and Z coordinates for a colony it's generated area for Dynmap.
 */
public class ColonyArea
{
    private final List<Vector2d> points;

    private Vector2d secondLast;
    private Vector2d last;

    /**
     * Default constructor.
     */
    public ColonyArea()
    {
        this.points = new ArrayList<>();
    }

    /**
     * Get the list of points.
     *
     * @return
     */
    public Collection<Vector2d> getPoints()
    {
        return this.points;
    }

    /**
     * Adds a new X Z coord onto this area.
     * This method automatically ensures that the added point is not in the same line as a previous point,
     * so that we not make an unnecessary amount of waypoints.
     *
     * @param x The X coord.
     * @param z The Z coord.
     */
    public void addPoint(double x, double z)
    {
        Vector2d newPoint = new Vector2d(x, z);

        // If the current X or Z values match at least 2 items back, we remove the last (middle of the comparison) item
        // from the deque for simplification.
        if ((last != null && secondLast != null) && ((last.getX() == x && secondLast.getX() == x) || (last.getY() == z && secondLast.getY() == z)))
        {
            this.points.remove(this.points.size() - 1);
        }

        this.points.add(newPoint);

        if (last != null)
        {
            secondLast = last.clone();
        }
        last = newPoint;
    }

    /**
     * Add a hole into the area, a hole is another {@link ColonyArea} containing the borders of a set of points which is completely encompassed by the current
     * area.
     * This works by selecting 2 points from the current area, as well as the hole, which have the short distance towards one another.
     * Afterward it combines the hole it's X Z coords into the current area, creating a line to link up the 2 areas.
     *
     * @param hole The area instance to add as a hole to the current area.
     */
    public void addHole(@Nonnull final ColonyArea hole)
    {
        // Find the closest distance between any point of the current area and the hole.
        double minimumDistance = Double.MAX_VALUE;
        int selectedAreaPointIndex = -1;
        int selectedHolePointIndex = -1;

        int areaPointIndex = 0;
        int holePointIndex = 0;
        for (Vector2d point : points)
        {
            holePointIndex = 0;
            for (Vector2d holePoint : hole.points)
            {
                double distance = distanceSq(point, holePoint);
                if (distance < minimumDistance)
                {
                    minimumDistance = distance;
                    selectedAreaPointIndex = areaPointIndex;
                    selectedHolePointIndex = holePointIndex;
                }
                holePointIndex++;
            }
            areaPointIndex++;
        }

        generateHole(hole, selectedAreaPointIndex, selectedHolePointIndex);
    }

    /**
     * Obtain the square of the distance between two points.
     *
     * @param first  the first point
     * @param second the second point.
     * @return the square of the distance between the two points.
     */
    private double distanceSq(Vector2d first, Vector2d second)
    {
        double px = second.getX() - first.getX();
        double py = second.getY() - first.getY();
        return px * px + py * py;
    }

    private void generateHole(@Nonnull final ColonyArea hole, int selectedAreaPointIndex, int selectedHolePointIndex)
    {
        if (selectedAreaPointIndex >= 0 && selectedHolePointIndex >= 0)
        {
            ArrayList<Vector2d> newPoints = new ArrayList<>();

            // We need to intersect the area with the hole at the selected points.
            int currentPosition = selectedHolePointIndex;
            boolean round = false;
            while (!round)
            {
                Vector2d point = hole.points.get(currentPosition);
                newPoints.add(point.clone());

                currentPosition++;
                if (currentPosition >= hole.points.size())
                {
                    currentPosition = 0;
                }

                if (currentPosition == selectedHolePointIndex)
                {
                    Vector2d initialPoint = hole.points.get(currentPosition);
                    newPoints.add(initialPoint.clone());
                    round = true;
                }
            }

            // Add the selected area point at the end of the list again in order to return the line back to the original area
            Vector2d areaPoint = this.points.get(selectedAreaPointIndex);
            newPoints.add(areaPoint.clone());

            this.points.addAll(selectedAreaPointIndex + 1, newPoints);
        }
    }

    /**
     * Closes off the area by adding a last point which links up the last point back to the first one.
     */
    public void close()
    {
        this.points.add(new Vector2d(this.points.get(0).getX(), this.points.get(0).getY()));
    }
}
