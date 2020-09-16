using Godot;
using System.Collections.Generic;
using clojure.lang;

public class AdhocSignals : Godot.Object
{

    public static void AddSignal(Godot.Object o, string name){
        o.AddUserSignal(name);
    }

    public static void AddSignal(Godot.Object o, string name, string[] names, int[] types){
        Godot.Collections.Array arguments = new Godot.Collections.Array();
        for (int i = 0; i < names.Length; i++)
        {
            arguments.Add(new Godot.Collections.Dictionary {{ "name", names[i] }, { "type", types[i] }});
        }
        o.AddUserSignal(name, arguments);
    }

    public Dictionary<int, IFn> functions = new Dictionary<int, IFn>();

    public void Register(int hash, IFn f){
        try
        {
            functions.Add(hash, f);
        }
        catch (System.ArgumentException)
        {
        }
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

    public void CatchMethod(System.Object a, int hash){
        try
        {
            functions[hash].invoke(a);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(System.Object a, System.Object b, int hash){
        try
        {
            functions[hash].invoke(a, b);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(System.Object a, System.Object b, System.Object c, int hash){
        try
        {
            functions[hash].invoke(a, b, c);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e, f);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, System.Object g, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e, f, g);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, System.Object g, System.Object h, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e, f, g, h);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, System.Object g, System.Object h, System.Object i, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e, f, g, h, i);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, System.Object g, System.Object h, System.Object i, System.Object j, int hash){
        try
        {
            functions[hash].invoke(a, b, c, d, e, f, g, h, i, j);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }

    public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, System.Object g, System.Object h, System.Object i, System.Object j, System.Object k, int hash){
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