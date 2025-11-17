package com.maistech.buildup;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Validates the modular architecture of the application using Spring Modulith.
 * 
 * This test ensures that:
 * - All modules are properly defined
 * - Dependencies between modules are respected
 * - No circular dependencies exist
 * - Module boundaries are not violated
 */
class ModulithArchitectureTest {

    private final ApplicationModules modules = ApplicationModules.of(BuildupApplication.class);

    /**
     * Verifies that all module boundaries are respected.
     * This test will fail if:
     * - A module accesses another module's internal classes
     * - Dependencies violate the allowed dependencies defined in package-info.java
     * - Circular dependencies are detected
     */
    @Test
    void modulesShouldRespectBoundaries() {
        modules.verify();
    }

    /**
     * Generates documentation for the modular structure.
     * Creates:
     * - Module diagrams (PlantUML)
     * - Module dependency canvas
     * - Application module structure documentation
     * 
     * Output location: target/spring-modulith-docs
     */
    @Test
    void generateModuleDocumentation() {
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
            .writeModuleCanvases();
    }

    /**
     * Displays the module structure in the console.
     * Useful for understanding the current module organization.
     */
    @Test
    void printModuleStructure() {
        modules.forEach(module -> {
            System.out.println("Module: " + module.getName());
            System.out.println("  Display Name: " + module.getDisplayName());
            System.out.println("  Base Package: " + module.getBasePackage());
            System.out.println("  Dependencies: " + module.getDependencies(modules));
            System.out.println();
        });
    }
}
