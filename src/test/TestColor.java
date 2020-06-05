package test;

import model.GeoTrafficLightMaster;
import model.OSMgraph.Node;

import java.awt.*;
import java.util.List;
import java.util.TreeMap;

public class TestColor {

    public static Color chooseCarColor(boolean test) {
        if (test) {
            return Color.RED;
        }
         Color c = new Color(255, 0, 0);
        if (c.equals(Color.RED)) {
            System.out.println(true);
        }
        do {
            c = new Color((float) Math.random(),
                    (float) Math.random(), (float) Math.random());
            System.out.println(c);
        } while (c.equals(Color.RED));
        return c;
    }

    public static void main(String[] args) {
        Node node = new Node(1, 1, 1);
        GeoTrafficLightMaster m = new GeoTrafficLightMaster(1, node, 1);

        TreeMap<GeoTrafficLightMaster, List<GeoTrafficLightMaster>> tree = new TreeMap<GeoTrafficLightMaster, List<GeoTrafficLightMaster>>();

        if (tree.containsKey(m)) {
            System.out.println("da");
        } else  {
            System.out.println("nu");
        }


        //System.out.println(chooseCarColor(false));
    }
}
