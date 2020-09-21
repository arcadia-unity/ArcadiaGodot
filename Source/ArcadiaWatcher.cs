using System;
using System.IO;
using System.Collections.Generic;
using System.Threading;
using System.Security.Permissions;
using System.Text.RegularExpressions;
using clojure.lang;
using Godot;

public class ArcadiaWatcher
{
    private FileSystemWatcher watcher;
    private Regex clj_file_regex = new Regex(@"\.clj[c]?$",
          RegexOptions.Compiled | RegexOptions.IgnoreCase);
    
    private Dictionary<string, bool> changed_files = new Dictionary<string, bool>();

    [PermissionSet(SecurityAction.Demand, Name = "FullTrust")]
    public ArcadiaWatcher()
    {
        watcher = new FileSystemWatcher();
        watcher.Path = ProjectSettings.GlobalizePath("res://");
        watcher.IncludeSubdirectories = true;
        watcher.NotifyFilter =  NotifyFilters.FileName | NotifyFilters.Size;

        //watcher.Filter = "*.clj?";
        watcher.Changed += OnChanged;
        watcher.Created += OnChanged;
        watcher.Renamed += OnChanged;
        watcher.EnableRaisingEvents = true;

        System.Threading.Timer timer = new System.Threading.Timer(this.DrainChanges, new AutoResetEvent(false), 100, 100);

        GD.Print("watching .clj files in ", watcher.Path);
    }

    private void OnChanged(object source, FileSystemEventArgs e) {
        
        if (clj_file_regex.Matches(e.Name).Count > 0)
        {
            try
            {
                lock (changed_files){
                    changed_files[e.Name] = true;
                }
            }
            catch (System.Exception err)
            {
                GD.PrintErr(err);
            }
            finally{
                
            }
        }
    }

    private void DrainChanges(System.Object stateInfo){
        lock (changed_files)
        {
            foreach (var path in changed_files.Keys){
                
                if (path != null){
                    GD.Print("reloading ", path);
                    try
                    {
                        Arcadia.Util.Invoke(RT.var("clojure.core", "load-file"), path);
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