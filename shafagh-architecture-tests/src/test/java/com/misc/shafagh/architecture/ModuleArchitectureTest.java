package com.misc.shafagh.architecture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.tngtech.archunit.lang.SimpleConditionEvent.satisfied;
import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.Repository;

/**
 * قوانین معماری پروژه. نقض هر قانون = شکست بیلد.
 *
 * ترتیب وابستگی ماژول‌ها: base  <-  cif  <-  dpst  <-  loan   (فقط از طریق api)
 * ماژول orchestration فقط به api ها وابسته است.
 */
class ModuleArchitectureTest {

    private static final String ROOT = "com.misc.shafagh";
    private static final List<String> MODULES = List.of("base", "cif", "dpst", "loan");

    private static com.tngtech.archunit.core.domain.JavaClasses imported;

    @BeforeAll
    static void load() {
        imported = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(ROOT);
    }

    private static String pkg(String module) {
        return ROOT + "." + module + "..";
    }

    @Test
    void apiPackagesDoNotDependOnImplPackages() {
        noClasses().that().resideInAPackage("..api..")
                .should().dependOnClassesThat().resideInAPackage("..impl..")
                .check(imported);
    }

    @Test
    void apiPackagesAreFrameworkFree() {
        noClasses().that().resideInAPackage("..api..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework..")
                .check(imported);
    }

    @Test
    void implOfOneModuleNeverUsesImplOfAnotherModule() {
        for (String module : MODULES) {
            String[] otherImpls = MODULES.stream()
                    .filter(m -> !m.equals(module))
                    .map(m -> ROOT + "." + m + ".impl..")
                    .toArray(String[]::new);
            noClasses().that().resideInAPackage(ROOT + "." + module + ".impl..")
                    .should().dependOnClassesThat().resideInAnyPackage(otherImpls)
                    .because("modules must talk to each other only through exposed API services")
                    .check(imported);
        }
    }

    @Test
    void orchestrationUsesOnlyApis() {
        noClasses().that().resideInAPackage(ROOT + ".orchestration..")
                .should().dependOnClassesThat().resideInAPackage("..impl..")
                .allowEmptyShould(true)
                .check(imported);
    }

    @Test
    void moduleDependencyDirectionIsRespected() {
        noClasses().that().resideInAPackage(pkg("base"))
                .should().dependOnClassesThat().resideInAnyPackage(pkg("cif"), pkg("dpst"), pkg("loan"))
                .check(imported);
        noClasses().that().resideInAPackage(pkg("cif"))
                .should().dependOnClassesThat().resideInAnyPackage(pkg("dpst"), pkg("loan"))
                .check(imported);
        noClasses().that().resideInAPackage(pkg("dpst"))
                .should().dependOnClassesThat().resideInAPackage(pkg("loan"))
                .check(imported);
    }

    @Test
    void modulesAreFreeOfCycles() {
        slices().matching(ROOT + ".(*)..").should().beFreeOfCycles().check(imported);
    }

    @Test
    void entitiesAndRepositoriesLiveInImplPackages() {
        classes().that().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..impl..")
                .check(imported);
        classes().that().areAssignableTo(Repository.class)
                .should().resideInAPackage("..impl..")
                .check(imported);
    }

    /** هر ماژول در impl خودش یک کلاس <Module>Schema با ثابت NAME دارد و نام اسکیماها تکراری نیست. */
    @Test
    void everyModuleDeclaresItsOwnDistinctSchema() {
        Set<String> seen = new HashSet<>();
        for (String module : MODULES) {
            String schema = declaredSchema(module);
            assertFalse(schema == null || schema.isBlank(), "Schema of module " + module + " is empty");
            assertTrue(seen.add(schema.toUpperCase()), "Schema " + schema + " is used by more than one module");
        }
    }

    /** هر Entity فقط می‌تواند به جدولی در اسکیمای ماژول خودش نگاشت شود (@Table(schema = <Module>Schema.NAME)). */
    @Test
    void entitiesMapOnlyToTheSchemaOfTheirOwnModule() {
        classes().that().areAnnotatedWith(Entity.class)
                .should(useOwnModuleSchema())
                .check(imported);
    }

    private static String declaredSchema(String module) {
        String className = ROOT + "." + module + ".impl."
                + Character.toUpperCase(module.charAt(0)) + module.substring(1) + "Schema";
        try {
            return (String) Class.forName(className).getField("NAME").get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Module '" + module + "' must declare " + className
                    + " with: public static final String NAME", e);
        }
    }

    private static ArchCondition<JavaClass> useOwnModuleSchema() {
        return new ArchCondition<>("be mapped to the Oracle schema of its own module") {
            @Override
            public void check(JavaClass entity, ConditionEvents events) {
                String module = entity.getPackageName().split("\\.")[3];
                String expected = declaredSchema(module);
                Optional<Table> table = entity.tryGetAnnotationOfType(Table.class);
                if (table.isPresent() && expected.equalsIgnoreCase(table.get().schema())) {
                    events.add(satisfied(entity, entity.getName() + " uses " + table.get().schema()));
                } else {
                    events.add(violated(entity, entity.getName()
                            + " must declare @Table(schema = <Module>Schema.NAME), expected schema " + expected));
                }
            }
        };
    }
}
