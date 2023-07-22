package io.github.mortuusars.mpfui.component;

@SuppressWarnings("unused")
public record Rectangle(int x, int y, int width, int height) {
    public int left() {
        return x;
    }

    public int top() {
        return y;
    }

    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }

    public int centerX() {
        return left() + width / 2;
    }

    public int centerY() {
        return top() + height / 2;
    }

    public Rectangle intersect(Rectangle other) {
        int right = right();
        int bottom = bottom();
        int otherRight = other.right();
        int otherBottom = other.bottom();

        int x = Math.max(this.x, other.x);
        int y = Math.max(this.y, other.y);
        int width = Math.max(0, Math.min(right, otherRight) - x);
        int height = Math.max(0, Math.min(bottom, otherBottom) - y);

        return new Rectangle(x, y, width, height);
    }

    public Rectangle shrink(int left, int top, int right, int bottom) {
        return new Rectangle(this.left() + left, this.top() + top,
                Math.max(0, width - left - right), Math.max(0, height - top - bottom));
    }

    public Rectangle shift(int x, int y) {
        return new Rectangle(this.left() + x, this.top() + y, width, height);
    }

    public boolean contains(int x, int y) {
        return x >= left() && x <= right() && y >= top() && y <= bottom();
    }

    @Override
    public String toString() {
        return "Rectangle{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
