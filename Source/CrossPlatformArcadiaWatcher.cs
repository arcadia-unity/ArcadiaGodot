using System.IO;
using System.Collections.Generic;
using System.Threading;
using clojure.lang;
using Godot;

public class CrossPlatformArcadiaWatcher
{
    // Polls the FS
    private System.Threading.Timer poll_timer;

    // Reloads files
    private System.Threading.Timer drain_timer;

    private Dictionary<string, bool> changed_files = new Dictionary<string, bool>();
    private Dictionary<string, FileInfo> last_files;

    private bool verbose_logging = false;

    public CrossPlatformArcadiaWatcher(bool verbose)
    {
        this.verbose_logging = verbose;
        poll_timer = new System.Threading.Timer(CheckStatus, new AutoResetEvent(false), 100, 100);
        drain_timer = new System.Threading.Timer(DrainChanges, new AutoResetEvent(false), 100, 100);
    }

    private void CheckStatus(System.Object stateInfo)
    {
        var path = ProjectSettings.GlobalizePath("res://");
        if (verbose_logging) { GD.Print($"Polling clj files in {path}..."); }
        var di = new DirectoryInfo(path);
        var files = di.GetFiles("*.clj?", SearchOption.AllDirectories);

        // First time checking, nothing to compare against.
        var lookup = new Dictionary<string, FileInfo>();
        foreach (var fi in files)
        {
            var name = fi.FullName.Replace(path, "");
            if (verbose_logging) { GD.Print("Scanning " + name); }
            lookup.Add(name, fi);
        }

        if (last_files != null)
        {
            foreach (var kv in lookup)
            {
                var name = kv.Key;
                var fi = kv.Value;

                if (last_files.ContainsKey(name))
                {
                    var changed = fi.LastWriteTime != last_files[name].LastWriteTime;
                    if (changed)
                    {
                        OnChanged(name);
                    }
                }
            }
        }

        last_files = lookup;
    }

    private void OnChanged(string fileName)
    {
        GD.Print("Detected change in file " + fileName);
        try
        {
            lock (changed_files)
            {
                changed_files[fileName] = true;
            }
        }
        catch (System.Exception err)
        {
            GD.PrintErr(err);
        }
        finally
        {

        }
    }

    private void DrainChanges(System.Object stateInfo)
    {
        lock (changed_files)
        {
            foreach (var path in changed_files.Keys)
            {

                if (path != null)
                {
                    GD.Print("reloading ", path);
                    try
                    {
                        Arcadia.Util.Invoke(RT.var("arcadia.repl", "main-thread-load-path"), path);
                    }
                    catch (System.Exception e)
                    {
                        GD.PrintErr(e);
                    }

                }

            }
            changed_files.Clear();
        }
    }
}