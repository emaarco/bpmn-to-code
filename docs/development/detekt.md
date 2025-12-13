# Detekt Static Code Analysis

This project uses [Detekt](https://detekt.dev/) for static code analysis of Kotlin code to maintain code quality and consistency.

## Running Detekt

### Analyze All Modules

```bash
./gradlew detektAll
```

This runs Detekt on all subprojects and displays violations in the console.

### Analyze Specific Module

```bash
./gradlew :bpmn-to-code-core:detekt
./gradlew :bpmn-to-code-gradle:detekt
./gradlew :bpmn-to-code-maven:detekt
./gradlew :bpmn-to-code-web:detekt
```

### Generate Reports Even on Failure

By default, if detekt finds violations, the build fails. To ensure all modules are analyzed and reports are generated:

```bash
./gradlew detektAll --continue
```

The `--continue` flag tells Gradle to continue executing tasks even if some fail, ensuring all HTML reports are generated.

## Viewing Reports

### Console Output

Violations are displayed directly in the terminal when running Detekt. Example:

```
/path/to/File.kt:10:1: io.ktor.http.* is a wildcard import. Replace it with fully qualified imports. [WildcardImport]
```

### HTML Reports

For detailed analysis, open the HTML reports in your browser:

```bash
# macOS
open bpmn-to-code-core/build/reports/detekt/detekt.html
open bpmn-to-code-web/build/reports/detekt/detekt.html

# Linux
xdg-open bpmn-to-code-core/build/reports/detekt/detekt.html
```

Each module generates its own report at:
```
<module-name>/build/reports/detekt/detekt.html
```

## Auto-Fixing Issues

Some violations can be automatically corrected:

```bash
./gradlew detekt --auto-correct
```

**Note:** Not all rules support autocorrection. Review changes before committing.

## Common Violations

- **WildcardImport**: Replace wildcard imports (e.g., `import io.ktor.http.*`) with explicit imports
- **NewLineAtEndOfFile**: Add a newline at the end of the file
- **MaxLineLength**: Lines exceeding 120 characters (default limit)
- **UnusedPrivateMember**: Remove unused private functions, properties, or classes

## Configuration

Detekt is configured in the root `build.gradle.kts` with default rules. 
No custom configuration file is used to keep setup simple.

Key settings:
- Uses Detekt's default rule set
- Runs checks in parallel for better performance
- Generates HTML and console reports
- JVM target: 21

## IDE Integration

### IntelliJ IDEA

1. Install the Detekt plugin: **Preferences → Plugins → Search "Detekt"**
2. The plugin will automatically detect the project configuration
3. Issues will be highlighted in the editor

### VS Code

1. Install the Kotlin extension
2. Install the Detekt extension
3. Issues will be highlighted in the editor

## Troubleshooting

### Build Fails Due to Violations

If Detekt finds violations, the build will fail. You have two options:

1. **Fix the violations** (recommended):
   - Review the console or HTML report
   - Fix issues manually or use `--auto-correct`
   - Run Detekt again to verify

2. **Skip Detekt temporarily** (for testing only):
   ```bash
   ./gradlew build -x detekt
   ```

### No Console Output

Ensure you're running Gradle 7.0+ and the console reporter is enabled in `build.gradle.kts`:

```kotlin
reports {
    txt.required.set(true)  // Console output
}
```

### HTML Reports Not Generated

If you don't see HTML reports after running `detektAll`:

1. **Build failed early**: Use `--continue` to analyze all modules
   ```bash
   ./gradlew detektAll --continue
   ```

2. **Check build directories**: Reports are at `<module>/build/reports/detekt/detekt.html`
   ```bash
   # List all generated reports
   find . -name "detekt.html" -path "*/build/reports/detekt/*"
   ```

3. **Run on specific module**: To see reports for a single module
   ```bash
   ./gradlew :bpmn-to-code-core:detekt
   open bpmn-to-code-core/build/reports/detekt/detekt.html
   ```

4. **Clean and rebuild**: Sometimes cached results can cause issues
   ```bash
   ./gradlew clean detektAll --continue
   ```

## Resources

- [Detekt Documentation](https://detekt.dev/)
- [Detekt Rules](https://detekt.dev/docs/rules/complexity)
- [Default Configuration](https://github.com/detekt/detekt/blob/main/detekt-core/src/main/resources/default-detekt-config.yml)
