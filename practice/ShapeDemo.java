import java.util.*;
public class ShapeDemo{
    public static void main(String[] args){
        List<Shape>shapes=new ArrayList<>();
        shapes.add(new Circle(2));
        shapes.add(new Rectangle(3,4));
        for(Shape s:shapes){
            System.out.println(s.describe());
        }
        ShapeService service=new ShapeServiceImpl();
        ShapeController controller=new ShapeController(service);
        controller.handle(shapes);
    }
}
interface Shape{
    public double area();
    default String describe(){
        return String.format("%s 面积=%.2f",getClass().getSimpleName(),area());
    }
}
class Circle implements Shape{
    private final double r;
    Circle(double r){this.r=r;}
    public double area(){
        return Math.PI*r*r;
    }
}
class Rectangle implements Shape{
    private final double w,h;
    Rectangle(double w,double h){this.w=w;this.h=h;}
    public double area(){
        return w*h;
    }
}
interface ShapeService{
    public double totalArea(List<Shape>shapes);
    public Shape largest(List<Shape>shapes);
}
class ShapeServiceImpl implements ShapeService{
    @Override
    public double totalArea(List<Shape>shapes){
        double sum=0;
        for(Shape s:shapes){
            sum+=s.area();
        }
        return sum;
    }

    @Override
    public Shape largest(List<Shape> shapes) {
        if(shapes.isEmpty()){
            return null;
        }
        Shape best=shapes.get(0);
        for(Shape s:shapes){
            if(s.area()>best.area()){
                best=s;
            }
        }
        return best;
    }
}
class ShapeController{
    private final ShapeService shapeService;
    ShapeController(ShapeService shapeService){
        this.shapeService=shapeService;
    }
    void handle(List<Shape>shapes){
        System.out.println("总面积="+shapeService.totalArea(shapes));
        Shape largest=shapeService.largest(shapes);
        System.out.println("最大="+(largest==null?"列表为空":largest.describe()));
    }
}