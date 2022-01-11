using Godot;
using Path = System.IO.Path;
using Environment = System.Environment;
using clojure.lang;

namespace Arcadia {
    public class Boot {
        private static bool _initialized = false;

        // workaround for spec issues
        static void DisableSpecChecking()
        {
            Environment.SetEnvironmentVariable("CLOJURE_SPEC_CHECK_ASSERTS", "false");
            Environment.SetEnvironmentVariable("CLOJURE_SPEC_SKIP_MACROS", "true");
            Environment.SetEnvironmentVariable("clojure.spec.check-asserts", "false");
            Environment.SetEnvironmentVariable("clojure.spec.skip-macros", "true");
        }
        
        public static void SetClojureLoadPath() {
            RT.var("nostrand.core", "load-path").invoke(
                Path.Combine(System.IO.Directory.GetCurrentDirectory(), Paths.ArcadiaSource));
            RT.var("nostrand.core", "load-path").invoke(
                Path.Combine(System.IO.Directory.GetCurrentDirectory(), Paths.Infrastructure));
        }

        public static void LoadConfig() {
            var config = RT.var("arcadia.internal.config", "get-config").invoke();
            RT.var("nostrand.core", "establish-environment").invoke(config);
        }

        static void BootClojure()
		{
            DisableSpecChecking();
            if (OS.HasFeature("standalone")) { 
                // this fix export for macos m1, and iphone, need to check other platforms
                Util.LoadAssemblyWithPdb("res://addons/ArcadiaGodot/Infrastructure/Magic.Runtime");
            } 
            Util.LoadAssembliesFromDirectory("res://addons/ArcadiaGodot/Infrastructure");
			
            RT.Initialize(doRuntimePostBoostrap: false);
            RT.TryLoadInitType("clojure/core");
            RT.TryLoadInitType("magic/api");
            
            if (OS.HasFeature("standalone")) { 
                RT.TryLoadInitType("arcadia/internal/filesystem");
                RT.var("clojure.core", "*load-fn*").bindRoot(RT.var("arcadia.internal.filesystem", "-load"));
            } else {
                RT.var("clojure.core", "*load-fn*").bindRoot(RT.var("clojure.core", "-load"));
            }
            RT.var("clojure.core", "*eval-form-fn*").bindRoot(RT.var("magic.api", "eval"));
            RT.var("clojure.core", "*load-file-fn*").bindRoot(RT.var("magic.api", "runtime-load-file"));
            RT.var("clojure.core", "*compile-file-fn*").bindRoot(RT.var("magic.api", "runtime-compile-file"));
            RT.var("clojure.core", "*macroexpand-1-fn*").bindRoot(RT.var("magic.api", "runtime-macroexpand-1"));

            RT.load("nostrand/core");
            SetClojureLoadPath();

            RT.load("arcadia/internal/config");
            LoadConfig();

            RT.PostBootstrapInit();
		}

        public static void Initialize()
        {
            if (_initialized) return;
            
            _initialized = true;

            bool isGame = !Engine.EditorHint;

            if (isGame) {
                GD.Print("Starting Arcadia..");
            }

            BootClojure();
            
            RT.load("arcadia/internal/namespace");
            if (isGame && 
                OS.IsDebugBuild() && 
                OS.GetName() != "iOS" ) // IOS do not support eval
            {
                GD.Print("load repl..");
                RT.load("arcadia/repl");
                Util.Invoke(RT.var("arcadia.repl", "launch"), null);
                if (RT.booleanCast(Util.Invoke(RT.var("arcadia.internal.config", "get-config-key"), "reload-on-change"))
                    && !OS.HasFeature("standalone"))
                {
                    var watcher = new CrossPlatformArcadiaWatcher(false);
                }
            }
            
            if (isGame) GD.Print("Arcadia loaded!");
        }
    }

}
