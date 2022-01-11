using System;
using clojure.lang;
using System.Collections.Generic;
using Assembly = System.Reflection.Assembly;
using Godot;
using Path = System.IO.Path;

namespace Arcadia
{
	public class Util
	{

		public static ArcadiaHook GetHook(Godot.Node o){
			for (int i = 0; i < o.GetChildCount(); i++)
			{
				ArcadiaHook found = o.GetChildOrNull<ArcadiaHook>(i);
				if (found != null){
					return found;
				}
			}
			return null;
		}

		// ==================================================================
		// tuple implementation
		public struct Tuple2<T, V>
		{
			public T Item1;
			public V Item2;

            public Tuple2(T item1, V item2)
            {
                Item1 = item1;
                Item2 = item2;
            }
        }

		// ==================================================================
		// namespace and Var loading

		public static Var requireVar;

		public static void EnsureRequireVar ()
		{
			if (requireVar == null) {
				Invoke(RT.var("clojure.core", "require"),
					   Symbol.intern("arcadia.internal.namespace"));
				requireVar = RT.var("arcadia.internal.namespace", "quickquire");
			}
		}

		public static void require (string s)
		{
			EnsureRequireVar();
			Invoke(requireVar, Symbol.intern(s));
		}

		public static void require (Symbol s)
		{
			EnsureRequireVar();
			Invoke(requireVar, s);
		}

		public static void getVar (ref Var v, string ns, string name)
		{
			if (v == null)
				v = RT.var(ns, name);
		}

		// ==================================================================
		// Var invocation

		public static object Invoke (Var v)
		{
			return ((IFn)v.getRawRoot()).invoke();
		}

		public static object Invoke (Var v, object a)
		{
			return ((IFn)v.getRawRoot()).invoke(a);
		}

		public static object Invoke (Var v, object a, object b)
		{
			return ((IFn)v.getRawRoot()).invoke(a, b);
		}

		public static object Invoke (Var v, object a, object b, object c)
		{
			return ((IFn)v.getRawRoot()).invoke(a, b, c);
		}

		// TODO: get rid of this when var invocation debugged
		public static IFn AsIFn (IFn f)
		{
			Var v = f as Var;
			if (v != null) {
				return (IFn)v.getRawRoot();
			}
			return f;
		}

		// ==================================================================
		// Arrays

		// Could use linq for this stuff, but sometimes there's a virtue
		// to explicitness and nonmagic

		public static T[] ArrayAppend<T> (T[] arr, T x)
		{
			T[] arr2 = new T[arr.Length + 1];
			arr.CopyTo(arr2, 0);
			arr2[arr2.Length - 1] = x;
			return arr2;
		}

		public static T[] ArrayPrepend<T> (T[] arr, T x)
		{
			T[] arr2 = new T[arr.Length + 1];
			if (arr2.Length > 1) {
				Array.Copy(arr, 0, arr2, 1, arr.Length);
			}
			arr2[0] = x;
			return arr2;
		}

		public static T[] ArrayConcat<T> (T[] arr1, T[] arr2)
		{
			T[] arr3 = new T[arr1.Length + arr2.Length];
			Array.Copy(arr1, 0, arr3, 0, arr1.Length);
			Array.Copy(arr2, 0, arr3, arr1.Length, arr2.Length);
			return arr3;
		}

		// test this
		public static T[] ArrayRemove<T> (T[] arr, int inx)
		{
			if (inx < 0 || arr.Length < inx)
				throw new IndexOutOfRangeException();
			T[] arr2 = new T[arr.Length - 1];
			if (arr2.Length == 0)
				return arr2;
			Array.Copy(arr, 0, arr2, 0, inx);
			Array.Copy(arr, inx + 1, arr2, inx, arr.Length - inx - 1);
			return arr2;
		}

		// mutating ops

		public static void WindowShift<T> (T[] arr, int windowStart, int windowEnd, int shiftTo)
		{
			Array.Copy(arr, windowStart, arr, shiftTo, windowEnd - windowStart);
		}

		// move an item to another place and shift everything else to fill in
		public static void Reposition<T> (T[] arr, int sourceInx, int targInx)
		{
			var x = arr[sourceInx];
			if (sourceInx == targInx) {
				return;
			} else if (sourceInx < targInx) {
				WindowShift(arr, sourceInx + 1, targInx, sourceInx);
			} else {
				WindowShift(arr, targInx, sourceInx, targInx + 1);
			}
			arr[targInx] = x;
		}

		// ==================================================================
		// qualified names of keywords, symbols, vars

		public static string QualifiedName (Symbol s)
		{
			if (s.Namespace != null)
				return s.Namespace + "/" + s.Name;
			return s.Name;
		}

		public static String QualifiedName (Keyword kw)
		{
			if (kw.Namespace != null) {
				return kw.Namespace + "/" + kw.Name;
			}
			return kw.Name;
		}

		public static String QualifiedName (Var v)
		{
			return QualifiedName(v.sym);
		}

		public static Tuple2<string, string> SplitQualifiedName (string qualifiedName)
		{
			int i = qualifiedName.IndexOf('/');
			if (i == -1) {
				return new Tuple2<string, string>(null, qualifiedName);
			}
			return new Tuple2<string, string>(qualifiedName.Substring(0, i), qualifiedName.Substring(i));
		}

		// loads var namespace as it goes
		public static Var DeserializeVar (string name)
		{
			var ss = SplitQualifiedName(name);
			if (ss.Item1 != null) {
				require(ss.Item1);
				return RT.var(ss.Item1, ss.Item2);
			}
			throw new ArgumentException("Can only deserialize qualified Var names. Var name: " + name);
		}

		// ==================================================================
		// Persistent maps

		public static IPersistentMap DictionaryToMap<T1, T2> (Dictionary<T1, T2> dict)
		{
			ITransientMap bldg = (ITransientMap)PersistentHashMap.EMPTY.asTransient();
			foreach (var kv in dict) {
				bldg = bldg.assoc(kv.Key, kv.Value);
			}
			return bldg.persistent();
		}

		// TODO replace when better interface for fast PersistentMap iteration
		class MapToDictionaryRFn<T1, T2> : AFn
		{
			Dictionary<T1, T2> dict;

			public MapToDictionaryRFn (Dictionary<T1, T2> dict_)
			{
				dict = dict_;
			}

			public override object invoke (object arg, object k, object v)
			{
				dict.Add((T1)k, (T2)v);
				return arg;
			}
		}

		public static Dictionary<T1, T2> MapToDictionary<T1, T2> (IPersistentMap m)
		{
			return MapToDictionary<T1, T2>(m, new Dictionary<T1, T2>());
		}

		public static Dictionary<T1, T2> MapToDictionary<T1, T2> (IPersistentMap m, Dictionary<T1, T2> dict)
		{
			clojure.lang.IKVReduce m2 = m as clojure.lang.IKVReduce;
			if (m2 != null) {
				m2.kvreduce(new MapToDictionaryRFn<T1, T2>(dict), null);
			} else {
				foreach (var e in m) {
					dict.Add((T1)e.key(), (T2)e.val());
				}
			}
			return dict;
		}

		// ------------------------------------------------------------------
		// serialization thereof
		// as usual, change this when we have 0-alloc persistent map iteration

		class SerializeKeyVarMapRFn : AFn
		{
			string[] keysAr;
			string[] valsAr;
			int i = 0;

			public SerializeKeyVarMapRFn (string[] keysAr, string[] valsAr)
			{
				this.keysAr = keysAr;
				this.valsAr = valsAr;
			}

			public override object invoke (object arg, object kIn, object vIn)
			{
				Keyword k = kIn as Keyword;
				if (k != null) {
					keysAr[i] = QualifiedName(k);
				} else {
					throw new InvalidOperationException("Keys must be Keywords, instead got instance of " + kIn.GetType());
				}

				Var v = vIn as Var;
				if (v != null) {
					valsAr[i] = QualifiedName(v);
				} else {
					throw new InvalidOperationException("Vals must be Vars, instead got instance of " + vIn.GetType());
				}
				i++;
				return null;
			}
		}

		public static Tuple2<string[], string[]> SerializeKeyVarMap (IKVReduce m, string[] keysArIn, string[] valsArIn)
		{
			int len = ((clojure.lang.Counted)m).count();
			string[] keysAr = (keysArIn != null && keysArIn.Length == len) ? keysArIn : new string[len];
			string[] valsAr = (valsArIn != null && valsArIn.Length == len) ? valsArIn : new string[len];

			m.kvreduce(new SerializeKeyVarMapRFn(keysAr, valsAr), null);

			return new Tuple2<string[], string[]>(keysAr, valsAr);
		}


		// ------------------------------------------------------------------
		// deserialization thereof

		public static IPersistentMap DeserializeKeyVarMap (string[] ks, string[] vs)
		{
			ITransientMap m = (ITransientMap)PersistentHashMap.EMPTY.asTransient();
			for (int i = 0; i < ks.Length; i++) {
				m.assoc(Keyword.intern(ks[i]), DeserializeVar(vs[i]));
			}
			return m.persistent();
		}

		// ==================================================================
		// String

		public static string TypeNameToNamespaceName (string typeName)
		{
			var inx = typeName.LastIndexOf('.');
			if (inx != -1) {
				return typeName.Substring(0, inx).Replace('_', '-');
			}
			throw new ArgumentException("No namespace string found for typeName " + typeName);
		}


		// ==================================================================
		// Timing

		public static double NTiming (IFn f, int n)
		{
			var sw = new System.Diagnostics.Stopwatch();
			sw.Start();
			for (int i = 0; i < n; i++) {
				f.invoke();
			}
			sw.Stop();
			return sw.Elapsed.TotalMilliseconds / n;
		}

		public static byte[] LoadAsBuffer(string filepath) {
			File file = new File();
			file.Open(filepath, Godot.File.ModeFlags.Read);
			var buffer = file.GetBuffer((long)file.GetLen());
			file.Close();
			
			return buffer;
		}

		public static Assembly LoadAssembly(String filepath) {
			byte[] dllBuffer = LoadAsBuffer(filepath);
			return Assembly.Load(dllBuffer);
		}

		public static void LoadAssemblyWithPdb(String filepath) {
			byte[] dllBuffer = LoadAsBuffer(filepath + ".dll");
			byte[] pdbBuffer = LoadAsBuffer(filepath + ".pdb");
			Assembly.Load(dllBuffer, pdbBuffer);
		}

		public static string LoadText(String filepath) {
			File file = new File();
			if (!file.FileExists(filepath)) return "";
            
			file.Open(filepath, Godot.File.ModeFlags.Read);
			String s = file.GetAsText();
			file.Close();
			return s;
		}

		public static void RemoveFile(String fileResPath) {
			Directory arcadiaDllsDir = new Directory();
			
			if (arcadiaDllsDir.FileExists(fileResPath)) {
				arcadiaDllsDir.Remove(fileResPath);
			}
		}

		public static void LoadAssembliesFromDirectory(String dirPath) {
				Directory arcadiaDllsDir = new Directory();
				arcadiaDllsDir.Open(dirPath);
				arcadiaDllsDir.ListDirBegin();
				String filename;
				while ((filename = arcadiaDllsDir.GetNext()) != "") {
						if (filename.EndsWith(".clj.dll")) {
								String filepath = Path.Combine(dirPath, filename);
								LoadAssembly(filepath);
						}
				}
		}

	}
}