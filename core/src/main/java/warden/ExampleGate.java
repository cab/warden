package warden;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static warden.Stdlib.*;

public class ExampleGate {


    private final static Set blacklist = set("you");


    public static boolean notGate(User user) {
        boolean f = NOT(true);
        return f;
    }

    public static boolean inGate(User user) {
        List list = list("blacklist");
        boolean inList = in(list, user);
        return inList;
    }


    public static boolean compareGateGT(User user) {
        short x = 2;
        short y = 1;
        boolean compared = x > y;
        return compared;
    }

    public static boolean compareGateLT(User user) {
        short x = 2;
        short y = 1;
        boolean compared = x < y;
        return compared;
    }

    public static boolean andGate(User user) {
        boolean x = true;
        boolean y = false;
        boolean both = x && y;
        return both;
    }

    public static boolean andGateExplicit(User user) {
        short x = 2;
        short y = 1;
        boolean a = x > y;
        boolean b = y > x;
        boolean both = false;
        if(a) {
            if(b) {
                both = true;
            }
        }
        return both;
    }


    public static boolean orGateExplicit(User user) {
        boolean a = true;
        boolean b = false;
        boolean both = a || b;
        return both;
    }





    public static boolean emptyGate(User user) {
        return false;
    }

}
