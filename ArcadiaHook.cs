using Godot;
using System;
using System.Collections.Generic;
using Path = System.IO.Path;
using clojure.lang;

namespace Arcadia
{

    public class Boot {
        private static bool _initialized = false;

        static void DisableSpecChecking()
        {
            System.Environment.SetEnvironmentVariable("CLOJURE_SPEC_CHECK_ASSERTS", "false");
            System.Environment.SetEnvironmentVariable("CLOJURE_SPEC_SKIP_MACROS", "true");
            System.Environment.SetEnvironmentVariable("clojure.spec.check-asserts", "false");
            System.Environment.SetEnvironmentVariable("clojure.spec.skip-macros", "true");
        }

        public static void AddSourcePaths()
        {
            var env = System.Environment.GetEnvironmentVariable("CLOJURE_LOAD_PATH");
            var SourcePaths = Util.Invoke(RT.var("arcadia.internal.config", "get-config-key"), "source-paths");
            foreach(string SourcePath in RT.toArray(SourcePaths))
            {
                env = env + Path.PathSeparator + Path.Combine(System.IO.Directory.GetCurrentDirectory(), SourcePath);
            }
            System.Environment.SetEnvironmentVariable("CLOJURE_LOAD_PATH", env);
        }

        public static void SetClojureLoadPath()
        {
            System.Environment.SetEnvironmentVariable(
                "CLOJURE_LOAD_PATH",
                Path.Combine(System.IO.Directory.GetCurrentDirectory(), "ArcadiaGodot", "Source") +
                Path.PathSeparator+
                Path.Combine(System.IO.Directory.GetCurrentDirectory(), "ArcadiaGodot", "Clojure"));
        }

        public static void SetClojureLoadPathWithDLLs()
        {
            System.Environment.SetEnvironmentVariable(
                "CLOJURE_LOAD_PATH",
                Path.Combine(System.IO.Directory.GetCurrentDirectory(), "ArcadiaGodot", "Source") +
                Path.PathSeparator +
                Path.Combine(System.IO.Directory.GetCurrentDirectory(), "ArcadiaGodot", "Clojure") +
                Path.PathSeparator +
                Path.Combine(System.IO.Directory.GetCurrentDirectory(), "dlls"));
        }

        public static void Initialize()
        {
            if (!_initialized)
            {
                _initialized = true;
                GD.Print("Starting Arcadia..");
                DisableSpecChecking();
                SetClojureLoadPathWithDLLs();
                RT.load("clojure/core");
                RT.load("arcadia/internal/namespace");
                if (OS.IsDebugBuild()) {
                    RT.load("arcadia/repl");
                    Util.Invoke(RT.var("arcadia.repl", "launch"), null);
                    RT.load("arcadia/internal/config");
                    AddSourcePaths();
                    if (RT.booleanCast(Util.Invoke(RT.var("arcadia.internal.config", "get-config-key"), "reload-on-change")))
                    {
                        if (System.Environment.OSVersion.Platform == System.PlatformID.MacOSX){
                            var watcher = new MacOSArcadiaWatcher(false);
                        } else {
                            var watcher = new CrossPlatformArcadiaWatcher();
                        }
                    }
                }
                GD.Print("Arcadia loaded!");
            }
        }
    }

	public class ArcadiaHook : Node
	{
        public Node target;
        public System.Object state;

        private Dictionary<string, IFn> _enter_tree_fns = new Dictionary<string, IFn>();
        private Dictionary<string, IFn> _exit_tree_fns = new Dictionary<string, IFn>();
        private Dictionary<string, IFn> _ready_fns = new Dictionary<string, IFn>();
        private Dictionary<string, IFn> _tree_ready_fns = new Dictionary<string, IFn>();
        private Dictionary<string, IFn> _process_fns = new Dictionary<string, IFn>();
        private Dictionary<string, IFn> _physics_process_fns = new Dictionary<string, IFn>();
        private Dictionary<string, IFn> _input_fns = new Dictionary<string, IFn>();
        private Dictionary<string, IFn> _unhandled_input_fns = new Dictionary<string, IFn>();

        [Export]
        public string ready_fn = "";
        [Export]
        public string tree_ready_fn = "";
        [Export]
        public string enter_tree_fn = "";
        [Export]
        public string exit_tree_fn = "";
        [Export]
        public string process_fn = "";
        [Export]
        public string physics_process_fn = "";
        [Export]
        public string input_fn = "";
        [Export]
        public string unhandled_input_fn = "";

        public void RemoveAll(){
            _enter_tree_fns.Clear();
            _exit_tree_fns.Clear();
            _ready_fns.Clear();
            _tree_ready_fns.Clear();
            _process_fns.Clear();
            _physics_process_fns.Clear();
            _input_fns.Clear();
            _unhandled_input_fns.Clear();
        }

        public void Add(string type, string k, IFn v){
            switch (type)
            {
                case "_enter_tree":
                    _enter_tree_fns[k] = v;
                    break;
                case "_exit_tree":
                    _exit_tree_fns[k] = v;
                    break;
                case "_ready":
                    _ready_fns[k] = v;
                    break;
                case "_tree_ready":
                    _tree_ready_fns[k] = v;
                    break;
                case "_process":
                    _process_fns[k] = v;
                    break;
                case "_physics_process":
                    _physics_process_fns[k] = v;
                    break;
                case "_input":
                    _input_fns[k] = v;
                    break;
                case "_unhandled_input":
                    _unhandled_input_fns[k] = v;
                    break;
                default:
                    throw new System.ArgumentException("unknown hook type: "+type, "type");
            }
        }

        public void Remove(string type, string k){
            switch (type)
            {
                case "_enter_tree_fns":
                    if (_enter_tree_fns.ContainsKey(k))
                    {
                        _enter_tree_fns.Remove(k);
                    }
                    break;
                case "_exit_tree_fns":
                    if (_exit_tree_fns.ContainsKey(k))
                    {
                        _exit_tree_fns.Remove(k);
                    }
                    break;
                case "_ready_fns":
                    if (_ready_fns.ContainsKey(k))
                    {
                        _ready_fns.Remove(k);
                    }
                    break;
                case "_tree_ready_fns":
                    if (_tree_ready_fns.ContainsKey(k))
                    {
                        _tree_ready_fns.Remove(k);
                    }
                    break;
                case "_process":
                    if (_process_fns.ContainsKey(k))
                    {
                        _process_fns.Remove(k);
                    }
                    break;
                case "_physics_process_fns":
                    if (_physics_process_fns.ContainsKey(k))
                    {
                        _physics_process_fns.Remove(k);
                    }
                    break;
                case "_input_fns":
                    if (_input_fns.ContainsKey(k))
                    {
                        _input_fns.Remove(k);
                    }
                    break;
                case "_unhandled_input_fns":
                    if (_unhandled_input_fns.ContainsKey(k))
                    {
                        _unhandled_input_fns.Remove(k);
                    }
                    break;
                default:
                    throw new System.ArgumentException("unknown hook type", "type");
            }
        }

        public void AddEditorHook(string type, string field){
            IFn fn = (IFn)Util.Invoke(RT.var("arcadia.internal.namespace", "eval-ifn"), field);
            if (fn != null)
            {
                Add(type, "editor", fn);
            }
        }

        // overridable methods

        public override void _EnterTree()
        {
            Boot.Initialize();
            target = this.GetParent();
            AddEditorHook("_enter_tree", enter_tree_fn);
            AddEditorHook("_exit_tree", exit_tree_fn);
            AddEditorHook("_ready", ready_fn);
            AddEditorHook("_tree_ready", tree_ready_fn);
            AddEditorHook("_process", process_fn);
            AddEditorHook("_physics_process", physics_process_fn);
            AddEditorHook("_input", input_fn);
            AddEditorHook("_unhandled_input", unhandled_input_fn);
            foreach (var item in _enter_tree_fns)
            {
                try
                {
                    item.Value.invoke(target, Keyword.intern(item.Key));
                }
                catch (Exception err)
                {
                    GD.PrintErr(err);
                }
            }
        }

        public override void _ExitTree()
        {
            foreach (var item in _exit_tree_fns)
            {
                try
                {
                    item.Value.invoke(target, Keyword.intern(item.Key));
                }
                catch (Exception err)
                {
                    GD.PrintErr(err);
                }     
            }
        }

        public override void _Ready()
        {
            foreach (var item in _ready_fns)
            {
                try
                {
                    item.Value.invoke(target, Keyword.intern(item.Key));
                }
                catch (Exception err)
                {
                    GD.PrintErr(err);
                }     
            }
            CallDeferred("_TreeReady");
        }

        public void _TreeReady()
        {
            foreach (var item in _tree_ready_fns)
            {
                try
                {
                    item.Value.invoke(target, Keyword.intern(item.Key));
                }
                catch (Exception err)
                {
                    GD.PrintErr(err);
                }     
            }
        }

        public override void _Process(float delta)
        {
            foreach (var item in _process_fns)
            {
                try
                {
                    item.Value.invoke(target, Keyword.intern(item.Key), delta);
                }
                catch (Exception err)
                {
                    GD.PrintErr(err);
                }     
            }
        }

        public override void _PhysicsProcess(float delta)
        {
            foreach (var item in _physics_process_fns)
            {
                try
                {
                    item.Value.invoke(target, Keyword.intern(item.Key), delta);
                }
                catch (Exception err)
                {
                    GD.PrintErr(err);
                }     
            }
        }

        public override void _Input(InputEvent e)
        {
            foreach (var item in _input_fns)
            {
                try
                {
                    item.Value.invoke(target, Keyword.intern(item.Key), e);
                }
                catch (Exception err)
                {
                    GD.PrintErr(err);
                }     
            }
        }

        public override void _UnhandledInput(InputEvent e)
        {
            foreach (var item in _unhandled_input_fns)
            {
                try
                {
                    item.Value.invoke(target, Keyword.intern(item.Key), e);
                }
                catch (Exception err)
                {
                    GD.PrintErr(err);
                }     
            }
        }

    }
}
