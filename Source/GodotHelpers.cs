using Godot;
using System;

namespace Arcadia
{

    public class GodotHelpers {
        public static PackedScene LoadScene(String s)
		{
            return (PackedScene)ResourceLoader.Load(s);
        }

        public static AudioStream LoadAudio(String s)
		{
            return (AudioStream)ResourceLoader.Load(s);
        }

        public static Texture LoadTexture(String s)
		{
            return (Texture)ResourceLoader.Load(s);
        }

        public static Node Instance(PackedScene pscn) {
            return pscn.Instance();    
        }


        public static Boolean IsActionPressed(String action)
		{
            return Input.IsActionPressed(action);
        }
    }

}
