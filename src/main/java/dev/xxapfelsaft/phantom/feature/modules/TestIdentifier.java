package dev.xxapfelsaft.phantom.feature.modules;
import net.minecraft.resources.Identifier;
import java.lang.reflect.Method;
public class TestIdentifier {
    public static void main(String[] args) {
        for (Method m : Identifier.class.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + java.util.Arrays.toString(m.getParameterTypes()));
        }
    }
}
