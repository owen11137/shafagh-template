package com.misc.shafagh;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ModuleBoundaryTest {

    private static final String ROOT = "com.misc.shafagh";
    private static final List<String> DOMAINS = List.of("base", "cif", "dpst", "loan");
    private static JavaClasses classes;

    @BeforeAll
    static void importProductionClasses() {
        classes = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(ROOT);
    }

    @Test
    void commonInfrastructureDoesNotDependOnDomainsOrApplicationConfiguration() {
        noClasses().that().resideInAPackage(ROOT + ".common..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        ROOT + ".base..", ROOT + ".cif..", ROOT + ".dpst..",
                        ROOT + ".loan..", ROOT + ".orchestration..", ROOT + ".config..")
                .check(classes);
    }

    @Test
    void domainApisRemainIndependentOfPersistenceAndImplementation() {
        noClasses().that().resideInAPackage("..api..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..impl..", ROOT + ".common.persistence..",
                        "jakarta.persistence..", "org.hibernate..", "org.springframework..")
                .check(classes);
    }

    @Test
    void domainsUseOtherDomainsOnlyThroughApis() {
        for (String domain : DOMAINS) {
            String[] otherImplementations = DOMAINS.stream()
                    .filter(other -> !other.equals(domain))
                    .map(other -> ROOT + "." + other + ".impl..")
                    .toArray(String[]::new);
            noClasses().that().resideInAPackage(ROOT + "." + domain + ".impl..")
                    .should().dependOnClassesThat().resideInAnyPackage(otherImplementations)
                    // Some domains currently have no implementation classes.
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }
}
