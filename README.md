ArcadiaGodot
=======
The integration of the Clojure Programming Language with the Godot game engine. This is an adaptation of [Arcadia](https://github.com/arcadia-unity/Arcadia/).

Status
------
ArcadiaGodot is alpha software, the API and installation methods will be subject to change.  

Community
---------
- [Gittr/IRC](https://gitter.im/arcadia-unity/Arcadia)


Setup
----------
You'll need a version of [Godot with Mono](https://godotengine.org/download)

This repository should be cloned into to a folder named `ArcadiaGodot` in the project folder.

Edit the `{project}.csproj` file to include the following itemgroup.  You may have to run an initial `Build` in the Editor for this file to be generated.

```xml
  <ItemGroup>
    <Compile Include="ArcadiaGodot\ArcadiaHook.cs" />
    <Reference Include="Clojure">
      <HintPath>ArcadiaGodot/Infrastructure/Clojure.dll</HintPath>
    </Reference>
    <Reference Include="Microsoft.Dynamic">
      <HintPath>ArcadiaGodot/Infrastructure/Microsoft.Dynamic.dll</HintPath>
    </Reference>
    <Reference Include="Microsoft.Scripting">
      <HintPath>ArcadiaGodot/Infrastructure/Microsoft.Scripting.dll</HintPath>
    </Reference>
    <Reference Include="Microsoft.Scripting.Core">
      <HintPath>ArcadiaGodot/Infrastructure/Microsoft.Scripting.Core.dll</HintPath>
    </Reference>
    <Reference Include="Microsoft.Scripting.Metadata">
      <HintPath>ArcadiaGodot/Infrastructure/Microsoft.Scripting.Metadata.dll</HintPath>
    </Reference>
  </ItemGroup>
```

Finally, you'll need at least one `ArcadiaHook.cs` script in your main scene.  You'll then be able to build (play button) and connect to one of the repls.

Usage
-----
To run clojure code, you'll need to associate signal methods with clojure `var` functions. In the inspector, `ArcadiaHook.cs` scripts will have string fields for each signal. Enter one or more vars separated by spaces in these fields.  If the namespace is loaded correctly, these fns will be called with the `ArcadiaHook` instance as the first argument (see Godot docs for additional arguments).

Only the project root directory is added to the clojure load path.

![godot-hooks](https://user-images.githubusercontent.com/2467644/32961551-f5a26e12-cb96-11e7-88cb-6805067b3ec0.png)

### REPL

ArcadiaGodot has both a socket repl (port 5571) and an UDP repl (port 11211). See [Arcadia](https://github.com/arcadia-unity/Arcadia/) for editor setup options.  Repls can be disabled or given other ports via a `{project}/configuration.edn` file.

For quick access try `telnet localhost 5571`.

## Differences from Arcadia Unity

This is a bare boned Arcadia setup, and is missing several notable features from core Arcadia:

* reader literals
* reactive file watching/configuration
* package manager
* Editor side clojure environment & repl
* ArcadiaState & state serialization
* scenegraph API
* runtime hook attachment
* AOT compilation - clojure code will be loaded from source at runtime
* full `arcadia.linear` namespace with optimizations

These will be slowly added in the future, especially once the Godot mono embedding is stabilized.

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

