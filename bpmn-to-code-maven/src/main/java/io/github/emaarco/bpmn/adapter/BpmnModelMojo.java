package io.github.emaarco.bpmn.adapter;

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiFilesystemPlugin;
import io.github.emaarco.bpmn.domain.shared.OutputLanguage;
import io.github.emaarco.bpmn.domain.shared.ProcessEngine;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven Mojo for generating type-safe API definitions from BPMN process models
 */
@Mojo(
		name = "generate-bpmn-api",
		defaultPhase = LifecyclePhase.NONE,
		requiresProject = false
)
public class BpmnModelMojo extends AbstractMojo {
	
	/**
	 * The base directory for the plugin execution.
	 * Defaults to the current directory.
	 */
	@Parameter(property = "baseDir", defaultValue = ".")
	private String baseDir;
	
	/**
	 * Pattern for locating BPMN files to process.
	 * Defaults to "src/main/resources/*.bpmn".
	 */
	@Parameter(property = "filePattern", defaultValue = "src/main/resources/*.bpmn")
	private String filePattern;
	
	/**
	 * Output folder path where the generated API code will be written.
	 * Defaults to "src/main/kotlin".
	 */
	@Parameter(property = "outputFolderPath", defaultValue = "src/main/kotlin")
	private String outputFolderPath;
	
	/**
	 * Package path for the generated API code.
	 * Defaults to "de.emaarco.generated".
	 */
	@Parameter(property = "packagePath", defaultValue = "de.emaarco.generated")
	private String packagePath;
	
	/**
	 * Output language for code generation.
	 * Valid values: KOTLIN, JAVA. Defaults to KOTLIN.
	 */
	@Parameter(property = "outputLanguage", defaultValue = "KOTLIN")
	private String outputLanguage;
	
	/**
	 * Target process-engine for the generated API.
	 * Valid values: CAMUNDA_7, ZEEBE, OPERATON.
	 */
	@Parameter(property = "processEngine")
	private String processEngine;
	
	/**
	 * Enable API versioning for the generated code.
	 * Defaults to false.
	 */
	@Parameter(property = "useVersioning", defaultValue = "false")
	private Boolean useVersioning;
	
	/**
	 * Default constructor for maven purposes
	 */
	@SuppressWarnings("unused")
	public BpmnModelMojo() {
	}
	
	/**
	 * Executes the BPMN API generation process
	 */
	@Override
	public void execute() {
		CreateProcessApiFilesystemPlugin plugin = new CreateProcessApiFilesystemPlugin();
		OutputLanguage language = OutputLanguage.valueOf(outputLanguage);
		ProcessEngine engine = ProcessEngine.valueOf(processEngine);
		plugin.execute(baseDir, filePattern, outputFolderPath, packagePath, language, engine, useVersioning);
	}
	
}
