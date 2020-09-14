using Godot;
using System.Collections.Generic;
using clojure.lang;

public class RuntimeHook : Godot.Object
{

    public static void AddSignal(Godot.Object o, string name){
        o.AddUserSignal(name);
    }

    public static void AddSignal(Godot.Object o, string name, Godot.Collections.Array arguments){
        o.AddUserSignal(name, arguments);
    }

    public static void Emit(Godot.Object o, string name){
        o.EmitSignal(name);
    }

    public static void Emit(Godot.Object o, string name, Godot.Collections.Array arguments){
        o.EmitSignal(name, arguments);
    }

    public Dictionary<int, IFn> functions = new Dictionary<int, IFn>();

    public void Register(int hash, IFn f){
        functions.Add(hash, f);
    }

    public void CatchMethod(int hash){
        try
        {
            functions[hash].invoke();
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(Object a, int hash){
        try
        {
            functions[hash].invoke(a);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(Object a, Object b, int hash){
        try
        {
            functions[hash].invoke(a, b);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(Object a, Object b, Object c, int hash){
        try
        {
            functions[hash].invoke(a, b, c);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(Object a, Object b, Object c, Object d, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(Object a, Object b, Object c, Object d, Object e, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(Object a, Object b, Object c, Object d, Object e, Object f, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e, f);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(Object a, Object b, Object c, Object d, Object e, Object f, Object g, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e, f, g);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e, f, g, h);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e, f, g, h, i);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e, f, g, h, i, j);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j, Object k, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e, f, g, h, i, j, k);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }
}