using Godot;
using System.Collections.Generic;
using clojure.lang;

public partial class AdhocSignals : Godot.Node
{

	public static void AddSignal(Godot.GodotObject o, string name){
		o.AddUserSignal(name);
	}

	public static void AddSignal(Godot.GodotObject o, string name, string[] names, int[] types){
		Godot.Collections.Array arguments = new Godot.Collections.Array();
		for (int i = 0; i < names.Length; i++)
		{
			arguments.Add(new Godot.Collections.Dictionary {{ "name", names[i] }, { "type", types[i] }});
		}
		o.AddUserSignal(name, arguments);
	}

	public static void Emit(Godot.GodotObject o, string name)
	{
		o.EmitSignal(name);
	}

    public static void Emit(Godot.GodotObject o, string name, Godot.Variant a)
    {
        o.EmitSignal(name, a);
    }

    public Dictionary<string, IFn> functions = new Dictionary<string, IFn>();

	public void Register(string hash, IFn f){
		try
		{
			functions.Add(hash, f);
		}
		catch (System.ArgumentException)
		{
		}
	}

	public void CatchMethod(string hash){
		try
		{
			functions[hash].invoke();
		}
		catch (System.Exception err)
		{
			GD.PrintErr(err);
		}
	}

	public void CatchMethod(System.Object a, string hash){
		try
		{
			functions[hash].invoke(a);
		}
		catch (System.Exception err)
		{
			GD.PrintErr(err);
		}
	}

	public void CatchMethod(System.Object a, System.Object b, string hash){
		try
		{
			functions[hash].invoke(a, b);
		}
		catch (System.Exception err)
		{
			GD.PrintErr(err);
		}
	}

	public void CatchMethod(System.Object a, System.Object b, System.Object c, string hash){
		try
		{
			functions[hash].invoke(a, b, c);
		}
		catch (System.Exception err)
		{
			GD.PrintErr(err);
		}
	}

	public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, string hash){
		try
		{
			functions[hash].invoke(a, b, c, d);
		}
		catch (System.Exception err)
		{
			GD.PrintErr(err);
		}
	}

	public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, string hash){
		try
		{
			functions[hash].invoke(a, b, c, d, e);
		}
		catch (System.Exception err)
		{
			GD.PrintErr(err);
		}
	}

	public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, string hash){
		try
		{
			functions[hash].invoke(a, b, c, d, e, f);
		}
		catch (System.Exception err)
		{
			GD.PrintErr(err);
		}
	}

	public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, System.Object g, string hash){
		try
		{
			functions[hash].invoke(a, b, c, d, e, f, g);
		}
		catch (System.Exception err)
		{
			GD.PrintErr(err);
		}
	}

	public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, System.Object g, System.Object h, string hash){
		try
		{
			functions[hash].invoke(a, b, c, d, e, f, g, h);
		}
		catch (System.Exception err)
		{
			GD.PrintErr(err);
		}
	}

	public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, System.Object g, System.Object h, System.Object i, string hash){
		try
		{
			functions[hash].invoke(a, b, c, d, e, f, g, h, i);
		}
		catch (System.Exception err)
		{
			GD.PrintErr(err);
		}
	}

	public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, System.Object g, System.Object h, System.Object i, System.Object j, string hash){
		try
		{
			functions[hash].invoke(a, b, c, d, e, f, g, h, i, j);
		}
		catch (System.Exception err)
		{
			GD.PrintErr(err);
		}
	}

	public void CatchMethod(System.Object a, System.Object b, System.Object c, System.Object d, System.Object e, System.Object f, System.Object g, System.Object h, System.Object i, System.Object j, System.Object k, string hash){
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
