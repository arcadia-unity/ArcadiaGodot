ArcadiaGodot
=======
The integration of the Clojure Programming Language with the Godot game engine. This is an adaptation of [Arcadia](https://github.com/arcadia-unity/Arcadia/).

Status
------
ArcadiaGodot is at a 1.0 release and the existing API should not change.

Community
---------
- [Gittr/IRC](https://gitter.im/arcadia-unity/Arcadia)


Setup
----------
You'll need a version of [Godot with Mono](https://godotengine.org/download) and `MSBuild` (see the Requirements section on the download page).

Clone this repository into your project folder.

In the Godot Editor, run the `Project > Tools > Mono > Create C# Solution` menu command.

Edit the `{project}.csproj` file to include the following itemgroup.

```xml
  <ItemGroup>
    <Reference Include="ArcadiaGodot/Infrastructure/**/*.dll"></Reference>
  </ItemGroup>
```

Finally, you'll need at least one `ArcadiaHook.cs` script in your main scene.  You'll then be able to build (play button) and connect to one of the repls or reload `.clj` files that have changed.

Usage
-----

### ArcadiaHook.cs

While working in the editor, the `ArcadiaHook` script lets you connect Godot's [callbacks](https://docs.godotengine.org/en/stable/getting_started/step_by_step/scripting_continued.html#overridable-functions) to clojure functions.

![godot-hooks](https://user-images.githubusercontent.com/2467644/32961551-f5a26e12-cb96-11e7-88cb-6805067b3ec0.png)

Note that these fns will be called with the *parent* of the `ArcadiaHook` instance as the first argument (the `ArcadiaHook` script inherits `Node` and would prevent manipulating any non `Node` properties on it's own node).  For example if you are manipulating a camera you would attach the hook as a child like so:

![Screenshot 2020-09-19 000059](https://user-images.githubusercontent.com/2467644/93654101-d5ed0d00-f9e9-11ea-8c67-53df86244af1.jpg)

### Hooks

You can hook functions to Godot nodes in clojure with `hook+` and `hook-`.  This has the same usage as `Arcadia`'s hooks. Godot's hook types are `:enter-tree :exit-tree :ready :process :physics-process :input :unhandled-input`.

```clj
;add a anonymous hook function to the player's _Input method
(hook+ (find-node "player") :input :bar-key (fn [o k e] (log "input event:" e)))

;remove a hook fn on the menu node
(hook- (find-node "menu") :ready :my-key)
```

### Signals

[Signals](https://docs.godotengine.org/en/stable/getting_started/step_by_step/signals.html) are Godot's system for events. `arcadia.core` contains functions for working with signals: `connect`, `disconnect`, `connected?`, `add-signal`, and `emit`.

`(connect (find-node "Button") "pressed" (fn [] (log "button was pressed!")))`

Most built in nodes emit a range of useful signals (like buttons being pressed, physics collisions). If you connect a var (like `#'game.core/handle-newgame`) you will be able to redefine the function in the repl.

### REPL

ArcadiaGodot has an nRepl (port 3722), a socket repl (port 5571) and an UDP repl (port 11211). See [Arcadia](https://github.com/arcadia-unity/Arcadia/) for editor setup options.  Repls can be disabled or given other ports via a `{project}/configuration.edn` file.

For quick access try `telnet localhost 5571`.

### Exports

After building your project you'll need to manually compile your clojure namespaces into the build directory:

```
(require 'arcadia.internal.compiler)
(arcadia.internal.compiler/aot "export/dlls" ['selfsame.core])
```

The entire dependency tree for the given namespaces will be compiled as well.

Note: the path must allready exist. You can place dlls in the root build directory or a folder named "dlls".


## Differences from Arcadia Unity

Notable Arcadia features missing from ArcadiaGodot:

* package manager 
* Editor side clojure environment
* reader literals and object serialization

Contributing
------------
If you're thinking of submitting code to Arcadia – thanks! We're excited to have your help. First, all contributors must read and agree to our [contributor license agreement](./CONTRIBUTOR-LICENSE-AGREEMENT.md). It is based on [GitHub's CLA](https://cla.github.com/) and ensures that the code you submit remains useable by Arcadia and its community without issue. It confirms that

1. Anyone can use your contributions anywhere, for free, forever.
2. Your contributions do not infringe on anyone else's rights.
3. You retain ownership of your contribution.

Once you have read and agree to it, submit a [Pull Request](https://github.com/arcadia-unity/Arcadia/pull/new) adding your name and GitHub id to [CONTRIBUTORS.md](./CONTRIBUTORS.md) with the following commit message:

I have read and agree to the terms of the Arcadia Contributor License Agreement.

You only need to do this once. After that, we can review and merge any contributions you send us!


Legal
-----
Copyright © 2014-2017 Tims Gardner, Ramsey Nasser, and [contributors](./CONTRIBUTORS.md).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

