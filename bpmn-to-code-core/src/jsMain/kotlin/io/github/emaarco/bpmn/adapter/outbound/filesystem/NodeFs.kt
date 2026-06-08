package io.github.emaarco.bpmn.adapter.outbound.filesystem

/**
 * Thin Kotlin/JS facades over the Node.js built-in `fs` and `path` modules.
 *
 * bpmn-moddle is CommonJS, so we keep using `require` here too (the JS target is configured with
 * `useEsModules()`, but `require` of Node built-ins works under the Node runner).
 */
internal external interface NodeFs {
  fun writeFileSync(path: String, data: String)
  fun readFileSync(path: String, encoding: String): String
  fun mkdirSync(path: String, options: dynamic)
  fun existsSync(path: String): Boolean
  fun readdirSync(path: String, options: dynamic): Array<dynamic>
}

internal external interface NodePath {
  fun join(vararg parts: String): String
}

/**
 * Returns a CommonJS `require` function that works under both CommonJS and ES-module output.
 *
 * The core's JS target is compiled with `useEsModules()`, so the `require` global is not available
 * at runtime. `module.createRequire(...)` (Node 12+) gives us a working `require` regardless, which
 * we use to load Node built-ins (`fs`, `path`) and the CommonJS `bpmn-moddle` packages.
 */
internal fun nodeRequire(): (String) -> dynamic {
  val req = js(
    "(typeof require !== 'undefined') ? require : " +
      "process.getBuiltinModule('module').createRequire(process.cwd() + '/')"
  )
  return { id: String -> req(id) }
}

internal fun nodeFs(): NodeFs = nodeRequire()("fs").unsafeCast<NodeFs>()

internal fun nodePath(): NodePath = nodeRequire()("path").unsafeCast<NodePath>()

internal fun mkdirsRecursive(fs: NodeFs, path: String) {
  fs.mkdirSync(path, js("({ recursive: true })"))
}
