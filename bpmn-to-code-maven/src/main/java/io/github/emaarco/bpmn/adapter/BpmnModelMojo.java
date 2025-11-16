package io.github.emaarco.bpmn.adapter;

import io.github.emaarco.bpmn.adapter.inbound.CreateProcessApiFilesystemPlugin;
import io.github.emaarco.bpmn.domain.shared.OutputLanguage;
import io.github.emaarco.bpmn.domain.shared.ProcessEngine;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(
        name = "generate-bpmn-api",
        defaultPhase = LifecyclePhase.NONE,
        requiresProject = false
)
public class BpmnModelMojo extends AbstractMojo {

    @Parameter(property = "baseDir", defaultValue = ".")
    private String baseDir;

    @Parameter(property = "filePattern", defaultValue = "src/main/resources/*.bpmn")
    private String filePattern;

    @Parameter(property = "outputFolderPath", defaultValue = "src/main/kotlin")
    private String outputFolderPath;

    @Parameter(property = "packagePath", defaultValue = "de.emaarco.generated")
    private String packagePath;

    @Parameter(property = "outputLanguage", defaultValue = "KOTLIN")
    private String outputLanguage;

    @Parameter(property = "processEngine")
    private String processEngine;

    @Parameter(property = "useVersioning", defaultValue = "false")
    private Boolean useVersioning;

    @Override
    public void execute() {
        CreateProcessApiFilesystemPlugin plugin = new CreateProcessApiFilesystemPlugin();
        OutputLanguage language = OutputLanguage.valueOf(outputLanguage);
        ProcessEngine engine = ProcessEngine.valueOf(processEngine);
        plugin.execute(baseDir, filePattern, outputFolderPath, packagePath, language, engine, useVersioning);
    }

}
