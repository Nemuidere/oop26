public class Point {
    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "x = " + this.x + "; y = " + this.y;
    }

    public String toSvg() {
        return String.format("<svg height=\"100\" width=\"100\" xmlns=\"http://www.w3.org/2000/svg\">\n  <circle r=\"45\" cx=\"%f\" cy=\"%f\" fill=\"red\" />\n</svg>", this.x, this.y);
    }

    public void translate(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    public Point translated(double dx, double dy) {
        double x1 = this.x + dx;
        double y1 = this.y + dy;
        return new Point(x1, y1);
    }

}
