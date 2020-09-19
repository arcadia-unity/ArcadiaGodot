using System.IO;
using System.Security.Permissions;
using clojure.lang;
using Godot;

public class ArcadiaWatcher
{
    public FileSystemWatcher watcher;

    [PermissionSet(SecurityAction.Demand, Name = "FullTrust")]
    public ArcadiaWatcher()
    {
        watcher = new FileSystemWatcher();
        watcher.Path = ProjectSettings.GlobalizePath("res://");
        watcher.IncludeSubdirectories = true;
        watcher.NotifyFilter = NotifyFilters.LastAccess
                                | NotifyFilters.LastWrite
                                | NotifyFilters.FileName
                                | NotifyFilters.DirectoryName;
        watcher.Filter = "*.clj?";
        watcher.Changed += OnChanged;
        watcher.Created += OnChanged;
        watcher.Renamed += OnChanged;
        watcher.EnableRaisingEvents = true;
        GD.Print("watching .clj files in ", watcher.Path);
    }

    private static void OnChanged(object source, FileSystemEventArgs e) {
        try
        {
            RT.load("clojure.core");
            Arcadia.Util.Invoke(RT.var("clojure.core", "load-file"), e.Name);
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
    }
}