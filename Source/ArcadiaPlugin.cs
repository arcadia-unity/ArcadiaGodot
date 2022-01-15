#if TOOLS
using Godot;
using Arcadia;
using Util = Arcadia.Util;
using clojure.lang;

[Tool]
public class ArcadiaPlugin : EditorPlugin {
    private string ARCADIA_EXPORT = "Arcadia - Prepare for Export";

    public override void _EnterTree() {
        this.AddToolMenuItem(ARCADIA_EXPORT, this, nameof(this.Compile));
    }

    private void InitMagic() {
        Boot.Initialize();
        
        Util.Invoke(RT.var("arcadia.internal.config", "reload-config"));
        Boot.LoadConfig();
    }

    public void Compile(object ud) {
        GD.Print("Arcadia - Prepare for Export");
        
        InitMagic();
        
        RT.load("arcadia/internal/compiler");
        Util.Invoke(RT.var("arcadia.internal.compiler", "prepare-export"));
    }

    public override void _ExitTree() {
        this.RemoveToolMenuItem(ARCADIA_EXPORT);
    }

}
#endif