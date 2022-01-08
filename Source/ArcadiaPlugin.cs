#if TOOLS
using Godot;
using Arcadia;
using Util = Arcadia.Util;
using clojure.lang;

[Tool]
public class ArcadiaPlugin : EditorPlugin {
    private string TOOL_NAME = "Arcadia -> Prepare for Export";

    public override void _EnterTree() {
        this.AddToolMenuItem(TOOL_NAME, this, nameof(this.Compile));
    }

    public void Compile(object ud) {
        GD.Print("Arcadia - Prepare for Export");
        
        Boot.Initialize();
        
        Util.Invoke(RT.var("arcadia.internal.config", "reload-config"));
        Boot.LoadConfig();
        
        RT.load("arcadia/internal/compiler");
        Util.Invoke(RT.var("arcadia.internal.compiler", "prepare-export"), "dlls");
    }
    
    public override void _ExitTree() {
        this.RemoveToolMenuItem(TOOL_NAME);
    }

}
#endif