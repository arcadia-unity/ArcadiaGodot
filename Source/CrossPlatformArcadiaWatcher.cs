using System;
using System.IO;
using System.Collections.Generic;
using System.Threading;
using clojure.lang;
using Godot;

public class CrossPlatformArcadiaWatcher
{
    private static readonly object reload_lock = new object();
    private Dictionary<string, bool> file_busy = new Dictionary<string, bool>();
    private FileSystemWatcher watcher;

    public CrossPlatformArcadiaWatcher()
    {
        var path = ProjectSettings.GlobalizePath("res://");
        var watcher = new FileSystemWatcher(path);

        this.watcher = watcher;

        watcher.NotifyFilter = NotifyFilters.CreationTime
            | NotifyFilters.DirectoryName
            | NotifyFilters.FileName
            | NotifyFilters.LastWrite
            | NotifyFilters.Size;

        watcher.Changed += OnChanged;
        watcher.Created += OnCreated;

        watcher.Filter = "*.clj";
        watcher.IncludeSubdirectories = true;
        watcher.EnableRaisingEvents = true;
    }

    static string ToRelativePath(FileSystemEventArgs ea){
        var path = ProjectSettings.GlobalizePath("res://");
        return ea.FullPath.Replace(path, "");
    }

    private void ReadFileTimeout(string RelativePath){
        // We use `file_busy[RelativePath]` variable to deterime if a file is
        // being reloaded. We add a short delay setting it back to `false` in
        // order to prevent any duplicate reloads, since
        // `Arcadia.Util.Invoke(ReplVar, RelativePath)` seems to trigger
        // `Changed` events.
        System.Threading.Timer timer = null;
        timer = new System.Threading.Timer((obj) =>
        {
            file_busy[RelativePath] = false;
            timer.Dispose();
        },
            null, 50, System.Threading.Timeout.Infinite);
    }

    private void reload(string RelativePath){
        GD.Print("reloading ", RelativePath);
        file_busy[RelativePath] = true;
        var ReplVar = RT.var("arcadia.repl", "main-thread-load-path");
        Arcadia.Util.Invoke(ReplVar, RelativePath);
        this.ReadFileTimeout(RelativePath);
    }

    private void OnChanged(object sender, FileSystemEventArgs ea)
    {
        try
        {
            lock (reload_lock)
            {
                var RelativePath = ToRelativePath(ea);

                // FileSystemWatcher considers all files initially found as "Changed".
                // That's why we skip them if they aren't in `file_busy`.
                if (!file_busy.ContainsKey(RelativePath)){
                    file_busy[RelativePath] = false;
                    return;
                }

                if(file_busy[RelativePath]){
                    return;
                }

                this.reload(RelativePath);
            }

        }
        catch (System.Exception e)
        {
            GD.PrintErr(e);
        }
    }

    private void OnCreated(object sender, FileSystemEventArgs ea)
    {

        try
        {
            var RelativePath = ToRelativePath(ea);
            file_busy[RelativePath] = true;
            this.reload(RelativePath);
        }
        catch (System.Exception e)
        {
            GD.PrintErr(e);
        }
    }
}
