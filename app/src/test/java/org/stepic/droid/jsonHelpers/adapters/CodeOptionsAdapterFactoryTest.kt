package org.stepic.droid.jsonHelpers.adapters

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import org.stepic.droid.model.Block
import org.stepik.android.model.structure.code.CodeOptions
import org.stepic.droid.testUtils.TestingGsonProvider

class CodeOptionsAdapterFactoryTest {

    private val gson: Gson = TestingGsonProvider.gson

    @Test
    fun codeOptionsEmpty_null() {
        val json = """{}"""

        val codeOptions = gson.fromJson(json, CodeOptions::class.java)

        assertNull(codeOptions)
    }

    @Test
    fun codeOptionsFull_success() {
        val codeOptionsJson = """{"limits": {"haskell": {"time": 2, "memory": 256}, "kotlin": {"time": 2, "memory": 256}, "scala": {"time": 2, "memory": 256}, "rust": {"time": 3, "memory": 256}, "asm64": {"time": 1, "memory": 256}, "javascript": {"time": 3, "memory": 256}, "java": {"time": 2, "memory": 256}, "shell": {"time": 1, "memory": 256}, "haskell 7.10": {"time": 2, "memory": 256}, "c++11": {"time": 1, "memory": 256}, "java8": {"time": 2, "memory": 256}, "asm32": {"time": 1, "memory": 256}, "python3": {"time": 3, "memory": 256}, "c++": {"time": 1, "memory": 256}, "octave": {"time": 3, "memory": 256}, "mono c#": {"time": 2, "memory": 256}, "clojure": {"time": 2, "memory": 256}, "r": {"time": 2, "memory": 256}}, "execution_memory_limit": 256, "code_templates": {"haskell": "main :: IO ()\n-- put your code here", "kotlin": "fun main(args: Array<String>) {\n    // put your code here\n}", "scala": "# put your Scala code here", "rust": "fn main() {\n    // put your Rust code here\n}", "asm64": "# put your asm64 code here", "javascript": "// put your javascript (node.js) code here", "java": "class Main {\n  public static void main(String[] args) {\n    // put your code here\n  }\n}", "shell": "# put your shell (bash) code here", "haskell 7.10": "main :: IO ()\n-- put your code here", "c++11": "#include <cassert>\n#include <iostream>\n\nclass Fibonacci final {\n public:\n  static int get(int n) {\n    assert(n >= 0);\n    // put your code here\n    return n;\n  }\n};\n\nint main(void) {\n  int n;\n  std::cin >> n;\n  std::cout << Fibonacci::get(n) << std::endl;\n  return 0;\n}", "java8": "class Main {\n  public static void main(String[] args) {\n    // put your code here\n  }\n}", "asm32": "# put your asm32 code here", "python3": "def fib(n):\n    # put your code here\n\ndef main():\n    n = int(input())\n    print(fib(n))\n\n\nif __name__ == \"__main__\":\n    main()", "c++": "#include <cassert>\n#include <iostream>\n\nclass Fibonacci {\n public:\n  static int get(int n) {\n    assert(n >= 0);\n    // put your code here\n    return n;\n  }\n};\n\nint main(void) {\n  int n;\n  std::cin >> n;\n  std::cout << Fibonacci::get(n) << std::endl;\n  return 0;\n}", "octave": "# put your octave code here", "mono c#": "using System;\n\npublic class MainClass\n{\n    public static void Main()\n    {\n        // put your c# code here\n    }\n}", "clojure": ";; put your clojure code here", "r": "# put your R code here"}, "execution_time_limit": 1, "samples": [["3", "2"]]}"""

        val codeOptions = gson.fromJson(codeOptionsJson, CodeOptions::class.java)

        assertNotNull(codeOptions)
        assertNotNull(codeOptions.limits)
        assertNotNull(codeOptions.codeTemplates)
        assertNotNull(codeOptions.samples)
        assertEquals(1, codeOptions.executionTimeLimit)
        assertEquals(256, codeOptions.executionMemoryLimit)
    }

    @Test
    fun blockWithEmptyOptions_nullOptions() {
        val jsonBlock = """{"name": "text", "text": "<p>Hi, Kirill Makarov,<br>We've created this lesson for you. Now you can improve and extend it!<br> To add more steps, press the blue \"Plus\" button.<br> To save your changes, press the \"Save changes\" button at the bottom of the page.<br><br>\nGood Luck!<br>Stepik Team.</p>", "video": null, "animation": null, "options": {}, "subtitle_files": []}"""

        val block = gson.fromJson(jsonBlock, Block::class.java)

        assertNotNull(block)
        assertNotNull(block.name)
        assertNull(block.options)
    }

    @Test
    fun emptyObject_emptyFields() {
        val jsonBlock = """{}"""

        val block = gson.fromJson(jsonBlock, Block::class.java)

        assertNotNull(block)
        assertNull(block.name)
    }

    @Test
    fun nullCodeOption_null() {
        val jsonBlock = """null"""

        val codeOptions = gson.fromJson(jsonBlock, CodeOptions::class.java)

        assertNull(codeOptions)
    }
}