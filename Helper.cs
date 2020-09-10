using System;
using clojure.lang;
using Godot;

public class Helper
{
    public static Node instance (PackedScene scn) {
        return scn.Instance();
    }

}
