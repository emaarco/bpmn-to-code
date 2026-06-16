package io.github.emaarco.bpmn.adapter;

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessJsonFilesystemPlugin;
import io.github.emaarco.bpmn.domain.shared.ProcessEngine;
import io.github.emaarco.bpmn.domain.validation.model.ValidationConfig;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven Mojo for generating JSON representations of BPMN process models
 */
@Mojo(
		name = "generate-bpmn-json",
		defaultPhase = LifecyclePhase.NONE,
		requiresProject = false
)
public class BpmnJsonMojo extends AbstractMojo {

	@Parameter(property = "baseDir", defaultValue = ".")
	private String baseDir;

	@Parameter(property = "filePattern", defaultValue = "src/main/resources/*.bpmn")
	private String filePattern;

	@Parameter(property = "outputFolderPath", defaultValue = "src/main/resources/bpmn-json")
	private String outputFolderPath;

	@Parameter(property = "processEngine")
	private String processEngine;

	@SuppressWarnings("unused")
	public BpmnJsonMojo() {
	}

	@Override
	public void execute() {
		getLog().warn(
				"[bpmn-to-code] bpmn-to-code will be moved to the io.miragon namespace. The 'io.github.emaarco' " +
						"coordinates and the 'io.github.emaarco:bpmn-to-code-maven' plugin are DEPRECATED and will not " +
						"receive further updates. Migrate to the io.miragon:bpmn-to-code-maven plugin / " +
						"'io.miragon:bpmn-to-code-*' — see https://github.com/miragon/bpmn-to-code"
		);
		CreateProcessJsonFilesystemPlugin plugin = new CreateProcessJsonFilesystemPlugin();
		ProcessEngine engine = ProcessEngine.valueOf(processEngine);
		plugin.execute(baseDir, filePattern, outputFolderPath, engine, new ValidationConfig());
		getLog().info("BPMN JSON files generated successfully");
	}

}
