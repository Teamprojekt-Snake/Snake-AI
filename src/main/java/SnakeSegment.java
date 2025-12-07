import java.awt.*;

@SuppressWarnings("SpellCheckingInspection")
public class SnakeSegment {
    //wo ist der part vom Body wirklich?
    private Point gridPosition;

    //derzeitige interpolierte Position;
    private float currentX;
    private float currentY;

    //snaphot für die interpolation
    private float snapshotX;
    private float snapshotY;

    public SnakeSegment(int x, int y) {
        this.gridPosition = new Point(x, y);
        currentX = x;
        currentY = y;
        snapshotX = x;
        snapshotY = y;
        this.snapshotX = x;
        this.snapshotY = y;
    }

    public void interpolate(float progress, int targetX, int targetY) {
        this.currentX = snapshotX + (targetX - snapshotX) * progress;
        this.currentY = snapshotY + (targetY - snapshotY) * progress;

        if (targetX != gridPosition.x || targetY != gridPosition.y) {
            System.out.println("SEGMENT MISMATCH! Grid: (" + gridPosition.x + "," + gridPosition.y + ") Target: (" + targetX + "," + targetY + ")");
        }
    }

    public void setGridPosition(int x, int y) {
        this.gridPosition.x = x;
        this.gridPosition.y = y;
    }

    public void updateSnapshot() {
        this.snapshotX = this.currentX;
        this.snapshotY = this.currentY;
        //System.out.println("updateSnapshot called: current(" + currentX + "," + currentY + ") -> snapshot");
    }

    public void setSnapshot(float x, float y) {
        //System.out.println("setSnapshot called: (" + x + "," + y + ")");
        this.snapshotX = x;
        this.snapshotY = y;
    }

    public void setCurrent(float x, float y) {
        this.currentX = x;
        this.currentY = y;
    }

    public float getCurrentX() { return currentX; }
    public float getCurrentY() { return currentY; }
    public Point getGridPosition() { return gridPosition; }


}
