package MyError;


public class MyError implements Comparable<MyError> {
    private int lineno;
    private MyErrorType type;
    
    public MyError(int lineno, MyErrorType type) {
        this.lineno = lineno;
        this.type = type;
    }
    
    public String toString() {
        return lineno + " " + type.toString();
    }
    
    public int compareTo(MyError o) {
        return this.lineno - o.lineno;
    }
}
