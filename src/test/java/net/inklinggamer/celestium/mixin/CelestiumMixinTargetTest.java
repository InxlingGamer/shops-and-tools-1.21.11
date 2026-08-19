package net.inklinggamer.celestium.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CelestiumMixinTargetTest {
    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final String SHADOW = "Lorg/spongepowered/asm/mixin/Shadow;";
    private static final String ACCESSOR = "Lorg/spongepowered/asm/mixin/gen/Accessor;";
    private static final String INVOKER = "Lorg/spongepowered/asm/mixin/gen/Invoker;";
    private static final Set<String> INJECTORS = Set.of(
            "Lorg/spongepowered/asm/mixin/injection/Inject;",
            "Lorg/spongepowered/asm/mixin/injection/Redirect;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyArg;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyArgs;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;"
    );
    private static final String MODIFY_CONSTANT = "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;";
    private static final Map<String, ClassNode> CLASS_CACHE = new HashMap<>();
    private static final List<String> FAILURES = new ArrayList<>();

    private CelestiumMixinTargetTest() {
    }

    public static void main(String[] args) throws Exception {
        int mixinCount = 0;
        mixinCount += auditConfig(Path.of("src", "main", "resources", "celestium.mixins.json"), "mixins");
        mixinCount += auditConfig(Path.of("src", "main", "resources", "celestium.client.mixins.json"), "client");

        check(mixinCount == 24, "Expected 24 configured Mixins, found " + mixinCount);
        auditAnvilRegression();
        auditSourceSelectors();

        if (!FAILURES.isEmpty()) {
            throw new AssertionError("Mixin target audit failed:\n - " + String.join("\n - ", FAILURES));
        }

        System.out.println("Validated all " + mixinCount + " configured Mixins against Minecraft 26.2 bytecode.");
    }

    private static int auditConfig(Path configPath, String listName) throws IOException {
        JsonObject config = JsonParser.parseString(Files.readString(configPath)).getAsJsonObject();
        String packageName = config.get("package").getAsString();
        JsonArray mixins = config.getAsJsonArray(listName);
        check(mixins != null, configPath + " must contain a " + listName + " array");
        if (mixins == null) {
            return 0;
        }

        for (var element : mixins) {
            auditMixin(packageName + "." + element.getAsString());
        }
        return mixins.size();
    }

    private static void auditMixin(String mixinClassName) throws IOException {
        String mixinInternalName = mixinClassName.replace('.', '/');
        ClassNode mixinClass = readClass(mixinInternalName);
        if (mixinClass == null) {
            check(false, "Configured Mixin class is missing: " + mixinClassName);
            return;
        }

        AnnotationNode mixinAnnotation = annotation(mixinClass, MIXIN);
        check(mixinAnnotation != null, mixinClassName + " is missing @Mixin");
        if (mixinAnnotation == null) {
            return;
        }

        List<String> targets = mixinTargets(mixinAnnotation);
        check(targets.size() == 1, mixinClassName + " must declare exactly one target, found " + targets);
        if (targets.size() != 1) {
            return;
        }

        String targetName = targets.getFirst();
        ClassNode targetClass = readClass(targetName);
        check(targetClass != null, mixinClassName + " target class is missing: " + targetName.replace('/', '.'));
        if (targetClass == null) {
            return;
        }

        for (FieldNode field : mixinClass.fields) {
            if (annotation(field, SHADOW) != null) {
                check(findField(targetClass, field.name, field.desc), mixinClassName + " shadows missing field " + field.name + ":" + field.desc);
            }
        }

        for (MethodNode method : mixinClass.methods) {
            if (annotation(method, SHADOW) != null) {
                check(findMethod(targetClass, method.name, method.desc), mixinClassName + " shadows missing method " + method.name + method.desc);
            }

            AnnotationNode accessor = annotation(method, ACCESSOR);
            if (accessor != null) {
                String fieldName = stringValue(accessor, "value", inferAccessorName(method.name));
                String fieldDescriptor = Type.getArgumentCount(method.desc) == 0
                        ? Type.getReturnType(method.desc).getDescriptor()
                        : Type.getArgumentTypes(method.desc)[0].getDescriptor();
                check(findField(targetClass, fieldName, fieldDescriptor), mixinClassName + " accesses missing field " + fieldName + ":" + fieldDescriptor);
            }

            AnnotationNode invoker = annotation(method, INVOKER);
            if (invoker != null) {
                String targetMethod = stringValue(invoker, "value", inferInvokerName(method.name));
                check(findMethod(targetClass, targetMethod, method.desc), mixinClassName + " invokes missing method " + targetMethod + method.desc);
            }

            for (AnnotationNode injector : injectorAnnotations(method)) {
                List<MethodNode> selectedMethods = selectTargetMethods(targetClass, injector, mixinClassName, method.name);
                auditAtTargets(injector, mixinClassName, method.name);
                if (MODIFY_CONSTANT.equals(injector.desc)) {
                    auditConstants(injector, selectedMethods, mixinClassName, method.name);
                }
            }
        }
    }

    private static List<MethodNode> selectTargetMethods(ClassNode targetClass, AnnotationNode injector, String mixinClassName, String handlerName) {
        Object selectorsValue = values(injector).get("method");
        List<String> selectors = asStrings(selectorsValue);
        check(!selectors.isEmpty(), mixinClassName + "." + handlerName + " has no target method selector");
        List<MethodNode> selected = new ArrayList<>();
        for (String selector : selectors) {
            int descriptorStart = selector.indexOf('(');
            String methodName = descriptorStart < 0 ? selector : selector.substring(0, descriptorStart);
            String descriptor = descriptorStart < 0 ? null : selector.substring(descriptorStart);
            List<MethodNode> matches = targetClass.methods.stream()
                    .filter(candidate -> candidate.name.equals(methodName) && (descriptor == null || candidate.desc.equals(descriptor)))
                    .toList();
            check(!matches.isEmpty(), mixinClassName + "." + handlerName + " targets missing method " + targetClass.name + "." + selector);
            if (descriptor == null) {
                check(matches.size() == 1, mixinClassName + "." + handlerName + " uses ambiguous descriptorless selector " + targetClass.name + "." + selector + " (" + matches.size() + " overloads)");
            }
            selected.addAll(matches);
        }
        return selected;
    }

    private static void auditAtTargets(AnnotationNode injector, String mixinClassName, String handlerName) throws IOException {
        for (AnnotationNode at : nestedAnnotations(values(injector).get("at"))) {
            String target = stringValue(at, "target", "");
            if (target.isEmpty()) {
                continue;
            }
            if (!target.startsWith("L") || !target.contains(";")) {
                check(false, mixinClassName + "." + handlerName + " has unsupported @At target " + target);
                continue;
            }

            int ownerEnd = target.indexOf(';');
            String ownerName = target.substring(1, ownerEnd);
            String member = target.substring(ownerEnd + 1);
            ClassNode owner = readClass(ownerName);
            check(owner != null, mixinClassName + "." + handlerName + " references missing @At owner " + ownerName);
            if (owner == null) {
                continue;
            }

            int methodDescriptor = member.indexOf('(');
            int fieldDescriptor = member.indexOf(':');
            if (methodDescriptor >= 0) {
                String name = member.substring(0, methodDescriptor);
                String descriptor = member.substring(methodDescriptor);
                check(findMethod(owner, name, descriptor), mixinClassName + "." + handlerName + " references missing @At method " + ownerName + "." + member);
            } else if (fieldDescriptor >= 0) {
                String name = member.substring(0, fieldDescriptor);
                String descriptor = member.substring(fieldDescriptor + 1);
                check(findField(owner, name, descriptor), mixinClassName + "." + handlerName + " references missing @At field " + ownerName + "." + member);
            } else {
                check(false, mixinClassName + "." + handlerName + " has malformed @At target " + target);
            }
        }
    }

    private static void auditConstants(AnnotationNode injector, List<MethodNode> targetMethods, String mixinClassName, String handlerName) {
        List<AnnotationNode> constants = nestedAnnotations(values(injector).get("constant"));
        check(!constants.isEmpty(), mixinClassName + "." + handlerName + " has no @Constant selector");
        for (AnnotationNode constant : constants) {
            Map<String, Object> constantValues = values(constant);
            if (!constantValues.containsKey("intValue")) {
                check(false, mixinClassName + "." + handlerName + " uses an unsupported non-integer constant selector");
                continue;
            }
            int expected = (Integer) constantValues.get("intValue");
            int ordinal = (Integer) constantValues.getOrDefault("ordinal", -1);
            for (MethodNode targetMethod : targetMethods) {
                int matches = countIntegerConstants(targetMethod, expected);
                check(matches > 0, mixinClassName + "." + handlerName + " cannot find integer constant " + expected + " in " + targetMethod.name + targetMethod.desc);
                if (ordinal >= 0) {
                    check(matches > ordinal, mixinClassName + "." + handlerName + " requests constant ordinal " + ordinal + " but only " + matches + " matches exist");
                }
            }
        }
    }

    private static void auditAnvilRegression() throws IOException {
        ClassNode mixin = readClass("net/inklinggamer/celestium/mixin/client/AnvilScreenMixin");
        MethodNode handler = mixin.methods.stream()
                .filter(method -> method.name.equals("celestium$removeTooExpensiveTextLimit"))
                .findFirst()
                .orElse(null);
        check(handler != null, "Anvil cost display handler is missing");
        if (handler == null) {
            return;
        }
        AnnotationNode injector = annotation(handler, MODIFY_CONSTANT);
        check(injector != null, "Anvil cost display handler is missing @ModifyConstant");
        if (injector == null) {
            return;
        }
        Map<String, Object> injectorValues = values(injector);
        List<String> selectors = asStrings(injectorValues.get("method"));
        String expectedSelector = "extractLabels(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V";
        check(selectors.equals(List.of(expectedSelector)), "Anvil Mixin must use the exact Minecraft 26.2 extractLabels descriptor");
        check(Integer.valueOf(1).equals(injectorValues.get("require")), "Anvil Mixin must require exactly one injection");
        check(Integer.valueOf(1).equals(injectorValues.get("expect")), "Anvil Mixin must expect exactly one injection");

        ClassNode anvilScreen = readClass("net/minecraft/client/gui/screens/inventory/AnvilScreen");
        List<MethodNode> methods = anvilScreen.methods.stream()
                .filter(method -> method.name.equals("extractLabels") && method.desc.equals("(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"))
                .toList();
        check(methods.size() == 1, "Minecraft 26.2 AnvilScreen must expose exactly one expected extractLabels method");
        if (methods.size() == 1) {
            check(countIntegerConstants(methods.getFirst(), 40) == 1, "Minecraft 26.2 AnvilScreen.extractLabels must contain exactly one cost limit constant of 40");
        }
    }

    private static void auditSourceSelectors() throws IOException {
        for (Path root : List.of(Path.of("src", "main", "java"), Path.of("src", "client", "java"))) {
            try (var files = Files.walk(root)) {
                files.filter(path -> path.toString().endsWith(".java"))
                        .forEach(path -> {
                            try {
                                check(!Files.readString(path).matches("(?s).*method_[0-9]+.*"), path + " contains an obsolete intermediary method selector");
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }
                        });
            }
        }
    }

    private static int countIntegerConstants(MethodNode method, int expected) {
        int count = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            Integer value = integerConstant(instruction);
            if (value != null && value == expected) {
                count++;
            }
        }
        return count;
    }

    private static Integer integerConstant(AbstractInsnNode instruction) {
        int opcode = instruction.getOpcode();
        if (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5) {
            return opcode - Opcodes.ICONST_0;
        }
        if (instruction instanceof IntInsnNode intInstruction) {
            return intInstruction.operand;
        }
        if (instruction instanceof LdcInsnNode ldc && ldc.cst instanceof Integer value) {
            return value;
        }
        return null;
    }

    private static boolean findField(ClassNode owner, String name, String descriptor) throws IOException {
        return hierarchy(owner).stream().flatMap(type -> type.fields.stream())
                .anyMatch(field -> field.name.equals(name) && field.desc.equals(descriptor));
    }

    private static boolean findMethod(ClassNode owner, String name, String descriptor) throws IOException {
        return hierarchy(owner).stream().flatMap(type -> type.methods.stream())
                .anyMatch(method -> method.name.equals(name) && method.desc.equals(descriptor));
    }

    private static List<ClassNode> hierarchy(ClassNode root) throws IOException {
        List<ClassNode> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        collectHierarchy(root, result, visited);
        return result;
    }

    private static void collectHierarchy(ClassNode type, List<ClassNode> result, Set<String> visited) throws IOException {
        if (type == null || !visited.add(type.name)) {
            return;
        }
        result.add(type);
        if (type.superName != null) {
            collectHierarchy(readClass(type.superName), result, visited);
        }
        for (String interfaceName : type.interfaces) {
            collectHierarchy(readClass(interfaceName), result, visited);
        }
    }

    private static ClassNode readClass(String internalName) throws IOException {
        if (CLASS_CACHE.containsKey(internalName)) {
            return CLASS_CACHE.get(internalName);
        }
        String resourceName = internalName + ".class";
        try (InputStream input = CelestiumMixinTargetTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) {
                CLASS_CACHE.put(internalName, null);
                return null;
            }
            ClassNode node = new ClassNode();
            new ClassReader(input).accept(node, 0);
            CLASS_CACHE.put(internalName, node);
            return node;
        }
    }

    private static List<String> mixinTargets(AnnotationNode mixin) {
        List<String> targets = new ArrayList<>();
        Object classTargets = values(mixin).get("value");
        if (classTargets instanceof List<?> list) {
            for (Object value : list) {
                if (value instanceof Type type) {
                    targets.add(type.getInternalName());
                }
            }
        }
        Object stringTargets = values(mixin).get("targets");
        for (String target : asStrings(stringTargets)) {
            targets.add(target.replace('.', '/'));
        }
        return targets;
    }

    private static List<AnnotationNode> injectorAnnotations(MethodNode method) {
        List<AnnotationNode> result = new ArrayList<>();
        for (List<AnnotationNode> annotations : List.of(nullToEmpty(method.visibleAnnotations), nullToEmpty(method.invisibleAnnotations))) {
            for (AnnotationNode annotation : annotations) {
                if (INJECTORS.contains(annotation.desc)) {
                    result.add(annotation);
                }
            }
        }
        return result;
    }

    private static AnnotationNode annotation(List<AnnotationNode> annotations, String descriptor) {
        if (annotations == null) {
            return null;
        }
        return annotations.stream().filter(annotation -> annotation.desc.equals(descriptor)).findFirst().orElse(null);
    }

    private static AnnotationNode annotation(ClassNode type, String descriptor) {
        AnnotationNode visible = annotation(type.visibleAnnotations, descriptor);
        return visible != null ? visible : annotation(type.invisibleAnnotations, descriptor);
    }

    private static AnnotationNode annotation(FieldNode field, String descriptor) {
        AnnotationNode visible = annotation(field.visibleAnnotations, descriptor);
        return visible != null ? visible : annotation(field.invisibleAnnotations, descriptor);
    }

    private static AnnotationNode annotation(MethodNode method, String descriptor) {
        AnnotationNode visible = annotation(method.visibleAnnotations, descriptor);
        return visible != null ? visible : annotation(method.invisibleAnnotations, descriptor);
    }

    private static Map<String, Object> values(AnnotationNode annotation) {
        Map<String, Object> result = new HashMap<>();
        if (annotation.values == null) {
            return result;
        }
        for (int index = 0; index < annotation.values.size(); index += 2) {
            result.put((String) annotation.values.get(index), annotation.values.get(index + 1));
        }
        return result;
    }

    private static List<AnnotationNode> nestedAnnotations(Object value) {
        if (value instanceof AnnotationNode annotation) {
            return List.of(annotation);
        }
        if (value instanceof List<?> list) {
            return list.stream().filter(AnnotationNode.class::isInstance).map(AnnotationNode.class::cast).toList();
        }
        return List.of();
    }

    private static List<String> asStrings(Object value) {
        if (value instanceof String string) {
            return List.of(string);
        }
        if (value instanceof List<?> list) {
            return list.stream().filter(String.class::isInstance).map(String.class::cast).toList();
        }
        return List.of();
    }

    private static String stringValue(AnnotationNode annotation, String name, String fallback) {
        Object value = values(annotation).get(name);
        return value instanceof String string && !string.isEmpty() ? string : fallback;
    }

    private static String inferAccessorName(String methodName) {
        String base = methodName.replaceFirst("^.*\\$", "");
        if (base.startsWith("get") || base.startsWith("set") || base.startsWith("is")) {
            int prefixLength = base.startsWith("is") ? 2 : 3;
            return Character.toLowerCase(base.charAt(prefixLength)) + base.substring(prefixLength + 1);
        }
        return base;
    }

    private static String inferInvokerName(String methodName) {
        String base = methodName.replaceFirst("^.*\\$", "");
        if (base.startsWith("invoke") && base.length() > 6) {
            return Character.toLowerCase(base.charAt(6)) + base.substring(7);
        }
        return base;
    }

    private static <T> List<T> nullToEmpty(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            FAILURES.add(message);
        }
    }
}
