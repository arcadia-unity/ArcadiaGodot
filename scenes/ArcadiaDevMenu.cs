using Godot;
using Arcadia;
using Util = Arcadia.Util;
using clojure.lang;

public class ArcadiaDevMenu : Node
{
     private void InitMagic() {
        Boot.Initialize();
        
        Util.Invoke(RT.var("arcadia.internal.config", "reload-config"));
        Boot.LoadConfig();
    }
    
    public void _on_Compile_arcadia_internal_filesystem_button_pressed() {
        GD.Print("Arcadia - Recompile arcadia.internal.filesystem");
        
        InitMagic();
        
        RT.load("arcadia/internal/compiler");
        Util.Invoke(RT.var("arcadia.internal.compiler", "recompile-filesystem"));
    }
    
}
