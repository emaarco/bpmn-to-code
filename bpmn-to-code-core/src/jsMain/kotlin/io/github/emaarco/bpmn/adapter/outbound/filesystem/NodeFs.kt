package io.github.emaarco.bpmn.adapter.outbound.filesystem

/** Kotlin/JS facades over the Node.js `fs` and `path` modules. */
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

// useEsModules() means no `require` global at runtime; createRequire gives a working one.
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
