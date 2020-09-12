using Godot;
using System;
using System.IO;
using Path = System.IO.Path;
using clojure.lang;
using Arcadia;

public class ArcadiaHook : Node
{

    static Var RequireVar;
    private static bool _initialized = false;

    // workaround for spec issues
    static void DisableSpecChecking()
    {
        System.Environment.SetEnvironmentVariable("CLOJURE_SPEC_CHECK_ASSERTS", "false");
        System.Environment.SetEnvironmentVariable("CLOJURE_SPEC_SKIP_MACROS", "true");
        System.Environment.SetEnvironmentVariable("clojure.spec.check-asserts", "false");
        System.Environment.SetEnvironmentVariable("clojure.spec.skip-macros", "true");
    }

    public static void SetClojureLoadPath()
    {
        System.Environment.SetEnvironmentVariable("CLOJURE_LOAD_PATH", 
            System.IO.Directory.GetCurrentDirectory()+Path.DirectorySeparatorChar+"ArcadiaGodot"+Path.DirectorySeparatorChar+"Source"+
            Path.PathSeparator+
            System.IO.Directory.GetCurrentDirectory());        
    }

    public static void SetClojureLoadPathWithDLLs()
    {
        RuntimeBootstrapFlag._startDefaultServer = false;
        RuntimeBootstrapFlag.SkipSpecChecks = true;
        RuntimeBootstrapFlag.CodeLoadOrder = new[] {
                    RuntimeBootstrapFlag.CodeSource.InitType,
                    RuntimeBootstrapFlag.CodeSource.FileSystem,
                    RuntimeBootstrapFlag.CodeSource.EmbeddedResource };

        System.Environment.SetEnvironmentVariable("CLOJURE_LOAD_PATH", 
            System.IO.Directory.GetCurrentDirectory()+Path.DirectorySeparatorChar+"ArcadiaGodot"+Path.DirectorySeparatorChar+"Source"+
            Path.PathSeparator+
            System.IO.Directory.GetCurrentDirectory()+Path.DirectorySeparatorChar+"ArcadiaGodot"+Path.DirectorySeparatorChar+"Infrastructure"+
            Path.PathSeparator+
            System.IO.Directory.GetCurrentDirectory()+Path.DirectorySeparatorChar+"ArcadiaGodot"+Path.DirectorySeparatorChar+"Infrastructure"+Path.DirectorySeparatorChar+"Desktop"+
            Path.PathSeparator+
            System.IO.Directory.GetCurrentDirectory()+Path.DirectorySeparatorChar+"dlls"+
            Path.PathSeparator+
            System.IO.Directory.GetCurrentDirectory());        
    }

    static ArcadiaHook()
    {
    }

    public ArcadiaHook()
    {

    }

    public static void Initialize()
    {
        GD.Print("Starting Arcadia..");
        //DisableSpecChecking();
        SetClojureLoadPathWithDLLs();
        RT.load("clojure/core");
        if (OS.IsDebugBuild()) {
            GD.Print("Starting clojure REPL..");
            RT.load("arcadia/repl");
            Invoke(RT.var("clojure.core", "require"), Symbol.intern("arcadia.repl"));
            //NRepl.StartServer();
        }
		GD.Print("Arcadia loaded!");
        
    }

    public static object Invoke (Var v, object a)
    {
        return ((IFn)v.getRawRoot()).invoke(a);
    }

    public static object Invoke (Var v, object a, object b)
    {
        return ((IFn)v.getRawRoot()).invoke(a, b);
    }

    public static object Invoke (Var v, object a, object b, object c)
    {
        return ((IFn)v.getRawRoot()).invoke(a, b, c);
    }



    [Export]
    public string ready_fns = "";
    private IFn[] _ready_fns;
    public override void _Ready()
    {
        for (int i = 0; i < _ready_fns.Length; i++)
        {
            try
            {
               _ready_fns[i].invoke(this);
            }
            catch (Exception err)
            {
               GD.Print(err);
            }
        }
    }



    [Export]
    public string enter_tree_fns = "";
    private IFn[] _enter_tree_fns;
    public override void _EnterTree()
    {
        if (!_initialized)
        {
            _initialized = true;
            Initialize();
        }
        Invoke(RT.var("clojure.core", "require"), Symbol.intern("arcadia.core"));
        _ready_fns = (IFn[])Invoke(RT.var("arcadia.core", "ifn-arr"), ready_fns);
        _enter_tree_fns = (IFn[])Invoke(RT.var("arcadia.core", "ifn-arr"), enter_tree_fns);
        _exit_tree_fns = (IFn[])Invoke(RT.var("arcadia.core", "ifn-arr"), exit_tree_fns);
        _process_fns = (IFn[])Invoke(RT.var("arcadia.core", "ifn-arr"), process_fns);
        _fixed_process_fns = (IFn[])Invoke(RT.var("arcadia.core", "ifn-arr"), fixed_process_fns);
        _input_fns = (IFn[])Invoke(RT.var("arcadia.core", "ifn-arr"), input_fns);
        _unhandled_input_fns = (IFn[])Invoke(RT.var("arcadia.core", "ifn-arr"), unhandled_input_fns);

        for (int i = 0; i < _enter_tree_fns.Length; i++)
        {
            try
            {
               _enter_tree_fns[i].invoke(this);
            }
            catch (Exception err)
            {
               GD.Print(err);
            }
        }
    }

    [Export]
    public string exit_tree_fns = "";
    private IFn[] _exit_tree_fns;
    public override void _ExitTree()
    {
        for (int i = 0; i < _exit_tree_fns.Length; i++)
        {
            try
            {
               _exit_tree_fns[i].invoke(this);
            }
            catch (Exception err)
            {
               GD.Print(err);
            }
        }
    }

    [Export]
    public string process_fns = "";
    private IFn[] _process_fns;
    public override void _Process(float delta)
    {
        for (int i = 0; i < _process_fns.Length; i++)
        {
            try
            {
               _process_fns[i].invoke(this, delta);
            }
            catch (Exception err)
            {
               GD.Print(err);
            }     
        }
    }

    [Export]
    public string fixed_process_fns = "";
    private IFn[] _fixed_process_fns;
    public override void _PhysicsProcess(float delta)
    {
        for (int i = 0; i < _fixed_process_fns.Length; i++)
        {
            try
            {
               _fixed_process_fns[i].invoke(this, delta);
            }
            catch (Exception err)
            {
               GD.Print(err);
            }
        }
        
    }

    [Export]
    public string input_fns = "";
    private IFn[] _input_fns;
    public override void _Input(InputEvent e)
    {
        for (int i = 0; i < _input_fns.Length; i++)
        {
            try
            {
               _input_fns[i].invoke(this, e);
            }
            catch (Exception err)
            {
               GD.Print(err);
            }           
        }
    }

    [Export]
    public string unhandled_input_fns = "";
    private IFn[] _unhandled_input_fns;
    public override void _UnhandledInput(InputEvent e)
    {
        for (int i = 0; i < _unhandled_input_fns.Length; i++)
        {
            try
            {
               _unhandled_input_fns[i].invoke(this, e);
            }
            catch (Exception err)
            {
               GD.Print(err);
            }                  
        }
    }
    
    public void _junk()
	{

        
        
	}

}
