public class Segment {
    public Point p1;
    public Point p2;
    public Segment(Point p1, Point p2){
        this.p1 = p1;
        this.p2 = p2;
    }

    public double length(){
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.hypot(dx, dy);
    }

    public static Segment longestSegment(Segment[] segments) {
        Segment longest = segments[0];

        for (int i = 1; i < segments.length; i++) {
            if (segments[i].length() > longest.length()) {
                longest = segments[i];
            }
        }

        return longest;
    }
}
