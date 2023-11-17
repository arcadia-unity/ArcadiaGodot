# 8/26/2023

Started work on a `godot4` branch, did some trivial C# changes, then ran into `System.TypeLoadException: Could not load type 'System.ActivationContext' from assembly 'mscorlib, Version=4.0.0.0`.  Godot4 is using net 6.0, so the `Clojure.dll` etc. artifacts aren't going to work.  (I had grabbed those from one of the arcadia repos, there was some unity specific alterations involved in their build but I don't believe I would need anything special) I am hopefull that i can get net6.0 compatible artifacts from https://github.com/clojure/clojure-clr/wiki/Getting-started

ok! that seemed to work, now error is `System.ArgumentNullException: Value cannot be null. (Parameter 'path1')` and I am hoping that's not an issue with the compiled DLLs.

# 8/27/2023

I don't know, could i be loading a path incorrectly with `RT.load("clojure/core");`? I should read the CLR source code for the stack trace:

```
E 0:00:01:0835   :0 @ void System.ArgumentNullException.Throw(System.String ): System.TypeInitializationException: The type initializer for 'clojure.lang.RT' threw an exception. ---> System.ArgumentNullException: Value cannot be null. (Parameter 'path1')
  <C++ Error>    System.TypeInitializationException
  <C++ Source>   :0 @ void System.ArgumentNullException.Throw(System.String )
  <Stack Trace>  :0 @ void System.ArgumentNullException.Throw(System.String )
                 :0 @ System.String System.IO.Path.Combine(System.String , System.String )
                 :0 @ System.IO.FileInfo clojure.lang.RT.FindFile(System.String , System.String )
                 :0 @ System.IO.FileInfo clojure.lang.RT.FindFile(System.String )
                 :0 @ System.IO.FileInfo clojure.lang.RT+<>c__DisplayClass421_0.<load>b__0(System.String )
                 :0 @ U[] clojure.lang.CljCompiler.Ast.ClrExtensions.Map<T , U >(System.Collections.Generic.ICollection`1[T] , System.Func`2[T,U] )
                 :0 @ void clojure.lang.RT.load(System.String , Boolean )
                 :0 @ void clojure.lang.RT.load(System.String )
                 :0 @ clojure.lang.RT..cctor()
                 :0 @ --- End of inner exception stack trace ---()
                 :0 @ void clojure.lang.RT.load(System.String )
                 ArcadiaHook.cs:60 @ void Arcadia.Boot.Initialize()
                 ArcadiaHook.cs:219 @ void Arcadia.ArcadiaHook._EnterTree()
                 Node.cs:2057 @ Boolean Godot.Node.InvokeGodotClassMethod(Godot.NativeInterop.godot_string_name& , Godot.NativeInterop.NativeVariantPtrArgs , Godot.NativeInterop.godot_variant& )
                 Arcadia.ArcadiaHook_ScriptMethods.generated.cs:96 @ Boolean Arcadia.ArcadiaHook.InvokeGodotClassMethod(Godot.NativeInterop.godot_string_name& , Godot.NativeInterop.NativeVariantPtrArgs , Godot.NativeInterop.godot_variant& )
                 CSharpInstanceBridge.cs:24 @ Godot.NativeInterop.godot_bool Godot.Bridge.CSharpInstanceBridge.Call(IntPtr , Godot.NativeInterop.godot_string_name* , Godot.NativeInterop.godot_variant** , Int32 , Godot.NativeInterop.godot_variant_call_error* , Godot.NativeInterop.godot_variant* )
```

https://github.com/clojure/clojure-clr/blob/master/Clojure/Clojure/Lib/RT.cs

`path1` would be the first arg.

At line `https://github.com/clojure/clojure-clr/blob/master/Clojure/Clojure/Lib/RT.cs#L3399` `GetFindFilePaths` may be returning a null? I may need to recreate each line and see where the problem is.

Ok great, that gives me the load paths with the problematic empty string in the middle:

```
-------
C:\dev\godot4\arcadia-dev\.godot\mono\temp\bin\Debug\
C:\dev\godot4\arcadia-dev\.godot\mono\temp\bin\Debug\bin
C:\dev\godot4\arcadia-dev

C:\dev\godot4\arcadia-dev\ArcadiaGodot\Source
C:\dev\godot4\arcadia-dev\ArcadiaGodot\Clojure
C:\dev\godot4\arcadia-dev\dlls
-------
```

https://learn.microsoft.com/en-us/dotnet/api/system.reflection.assembly.location?view=net-7.0 states that "In .NET 5 and later versions, for bundled assemblies, the value returned is an empty string.", maybe this is just a bug with CLR for net 5+

From here, I probably need to build CLR myself with a little edit to discard empty paths 

# 11/7/2023

I had written a whole journal entry here while in linux about trying to get clojure-clr to build with dotnet BUT it was very trivial with Visual Studio. 

After some help from dmiller and Daniel Gerson on the #CLR slack I am building clojure-clr for net6.0 and the filepaths error is resolved. I removed the old clojure namespace dlls and replaced the clojure source.  Seems to be close to working, I am getting an error when compiling one of the clojure .clj files:

```
E 0:00:07:0001   :0 @ System.String clojure.lang.Util.NameForType(System.Type ): System.TypeInitializationException: The type initializer for 'clojure.lang.RT' threw an exception. ---> clojure.lang.Compiler+CompilerException: Syntax error macroexpanding at (C:\dev\godot4\arcadia-dev\ArcadiaGodot\Clojure\clojure\clr\io.clj:1:1). ---> System.NullReferenceException: Object reference not set to an instance of an object.
  <C++ Error>    System.TypeInitializationException
  <C++ Source>   :0 @ System.String clojure.lang.Util.NameForType(System.Type )
  <Stack Trace>  :0 @ System.String clojure.lang.Util.NameForType(System.Type )
                 :0 @ System.Type clojure.lang.Namespace.importClass(System.Type )
                 :0 @ System.Object clojure.clr.io$eval11901loading__5860__auto____11906__11909.invoke()
                 :0 @ System.Object clojure.clr.io$eval11901__11912.invokeStatic()
                 :0 @ System.Object clojure.clr.io$eval11901__11912.invoke()
                 :0 @ System.Object clojure.lang.Compiler.eval(System.Object )
                 :0 @ System.Object clojure.lang.Compiler.eval(System.Object )
                 :0 @ System.Object clojure.lang.Compiler.load(System.IO.TextReader , System.String , System.String , System.String )
                 :0 @ --- End of inner exception stack trace ---()
```

# 11/8/2023

Looking into the io.clj issue, it's probably just a bad import of TextReader? 

Actually strangely enough i am thinking that TextReader class is being mentioned in that error because it's used by the .load mechanism. because if I remove the import of it it I still see it in the call stack. maybe I have to bisect imports until I find the one that is breaking things

importing either of these is breaking it: `(System.Net.Sockets Socket NetworkStream)`

I'm going to comment out the usage there and see if I can get clojure up and running at least.. Also having trouble with `System.Net.Webclient` classes in that ns

I wonder if Godot's C# does not support these? I can try to use them from a C# script later on.

Good news it's compiled clojure and I'm on to arcadia cljs:

Just working through small differences, several imports threw errors but weren't being used. `Godot.Object` is now `Godot.GodotObject`

# 11/9/2023

`arcadia.core` compiling, `clojure.main` ns can't find `clojure/spec/alpha`, where is clojure spec?? Pasting it in, hope it's the right version

ok now it's complaining about an import in `core.server` while compiling the repl stuff. Guessing it's the Socket etc. like it couldn't do in io.clj. It's probably time to figure out why i can't import these classes, guess my first avenue will be to try and use them in a C# class 

```
(ns ^{:doc "Socket server support"
      :author "Alex Miller"}
  clojure.core.server
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.main :as m])
  (:import
   [clojure.lang LineNumberingTextReader]                                                    ;;; LineNumberingPushbackReader
   [System.Net.Sockets Socket SocketException TcpListener TcpClient]                         ;;; [java.net InetAddress Socket ServerSocket SocketException]
   [System.IO StreamReader StreamWriter TextReader  IOException]                              ;;; [java.io Reader Writer PrintWriter BufferedWriter BufferedReader InputStreamReader OutputStreamWriter]
   [System.Net Dns IPAddress]))  
```

Hmm, i can absolutely use these in C# like `public static Socket sock = new Socket(SocketType.Stream, ProtocolType.Tcp);`, what is going on?

Looking at the error I am now suspecting that `clojure.lang.Util.NameForType` is failing in some situations, preventing these imports.

```
E 0:00:17:0466   :0 @ System.String clojure.lang.Util.NameForType(System.Type ): clojure.lang.Compiler+CompilerException: Syntax error macroexpanding at (C:\dev\godot4\arcadia-dev\ArcadiaGodot\Source\arcadia\core.clj:1:1). ---> System.NullReferenceException: Object reference not set to an instance of an object.
  <C++ Error>    clojure.lang.Compiler+CompilerException
  <C++ Source>   :0 @ System.String clojure.lang.Util.NameForType(System.Type )
  <Stack Trace>  :0 @ System.String clojure.lang.Util.NameForType(System.Type )
                 :0 @ System.Type clojure.lang.Namespace.importClass(System.Type )
                 :0 @ System.Object arcadia.core$eval27655loading__20258__auto____27660__27663.invoke()
```

I'll guess next rebuild Clojure.dll with some debug prints and see what the null is from, looks like the Type.FullName property can return null

# 11/12/2023

Actually no, the type passed in is null..

Having a hard time debugging this, would be great if I could print to some kind of console. My current assumption is that something broke in higher NET versions with resolving a type from a string name.  Currently looking at `RT.classForName` which is used in `ImportExpr.cs`'s `Eval`

# 11/13/2023

In Visual Studio I am building the Clojure.Compile solution with a targetframework of net6.0 in hopes that it will trigger the import issue and be easier to debug. It's running into a similar thing:

```
TypeNotFoundException: Unable to find type: System.Runtime.InteropServices.RuntimeInformation
---
This exception was originally thrown at this call stack:
    clojure.lang.RT.classForNameE(string) in RT.cs
    clojure.lang.CljCompiler.Ast.HostExpr.MaybeType(object, bool) in HostExpr.cs
    clojure.lang.Compiler.MacroexpandSeq1(clojure.lang.ISeq) in Compiler.cs
    clojure.lang.Compiler.AnalyzeSeq(clojure.lang.CljCompiler.Ast.ParserContext
```

# 11/13/2023

So something interesting, when printing a class that couldn't resolve with GetType the error goes away, so there must be some sort of tree shaking going on that isn't including some classes.  

```
Type t = Type.GetType(p, false);
if (t == null)
{
    Console.WriteLine(String.Format("cant GetType {0}", p));
    Console.WriteLine(String.Format(".. {0}", System.Runtime.InteropServices.RuntimeInformation.FrameworkDescription));
}
```

I could potentially get everything to compile by just including code that references the missing classes. Would be better to figure out how to preserve a list of namespaces or something in the project.

adding this to `RT.cs`

```
        static void NoTrim()
        {
            var a = System.Runtime.InteropServices.RuntimeInformation.FrameworkDescription;
            var b = System.Net.Sockets.Socket.OSSupportsIPv4;
            var c = System.Net.Sockets.NetworkStream.Null;

        }
```

I think this will all work but I'm having a hell of a time getting it to build with target `net6.0` again, going to try tomorrow