package me.n1ar4.jar.analyzer.analyze.spring.asm;

import me.n1ar4.jar.analyzer.analyze.spring.SpringConstant;
import me.n1ar4.jar.analyzer.analyze.spring.SpringController;
import me.n1ar4.jar.analyzer.analyze.spring.SpringMapping;
import me.n1ar4.jar.analyzer.analyze.spring.SpringParam;
import me.n1ar4.jar.analyzer.core.ClassReference;
import me.n1ar4.jar.analyzer.core.MethodReference;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SpringMethodAdapter extends MethodVisitor {
    private final Map<MethodReference.Handle, MethodReference> methodMap;
    private final List<SpringParam> requestParam = new ArrayList<>();
    private SpringMapping currentMapping;
    private final SpringController controller;
    private final String name;
    private final String owner;
    private final String desc;

    private SpringPathAnnoAdapter pathAnnoAdapter = null;

    public SpringMethodAdapter(String name, String descriptor, String owner,
                               int api, MethodVisitor methodVisitor,
                               SpringController currentController,
                               Map<MethodReference.Handle, MethodReference> methodMap) {
        super(api, methodVisitor);
        this.owner = owner;
        this.desc = descriptor;
        this.name = name;
        this.methodMap = methodMap;
        this.controller = currentController;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitAnnotation(descriptor, visible);
        if (descriptor.equals(SpringConstant.RequestMappingAnno) ||
                descriptor.equals(SpringConstant.GetMappingAnno) ||
                descriptor.equals(SpringConstant.PostMappingAnno)) {
            if (currentMapping == null) {
                currentMapping = new SpringMapping();
            }
            currentMapping.setMethodName(new MethodReference.Handle(
                    new ClassReference.Handle(this.owner), this.name, this.desc));
            currentMapping.setController(controller);
            currentMapping.setMethodReference(methodMap.get(currentMapping.getMethodName()));
            currentMapping.setParamMap(this.requestParam);
            pathAnnoAdapter = new SpringPathAnnoAdapter(Const.ASMVersion, av);
            av = pathAnnoAdapter;
        }
        if (descriptor.equals(SpringConstant.ResponseBodyAnno)) {
            if (currentMapping == null) {
                currentMapping = new SpringMapping();
            }
            currentMapping.setRest(true);
        }
        return av;
    }

    @Override
    public void visitCode() {
        if (this.currentMapping != null) {
            if (pathAnnoAdapter != null) {
                if (!pathAnnoAdapter.getResults().isEmpty()) {
                    if (!pathAnnoAdapter.getResults().get(0).startsWith("/")) {
                        pathAnnoAdapter.getResults().set(0, "/" + pathAnnoAdapter.getResults().get(0));
                    }
                    if (pathAnnoAdapter.getResults().get(0).endsWith("/")) {
                        pathAnnoAdapter.getResults().set(0, pathAnnoAdapter.getResults().get(0).substring(0, pathAnnoAdapter.getResults().get(0).length() - 1));
                    }
                    if (!StringUtil.isNull(controller.getBasePath())) {
                        if (controller.getBasePath().endsWith("/")) {
                            controller.setBasePath(controller.getBasePath().substring(0, controller.getBasePath().length() - 1));
                        }
                        currentMapping.setPath(controller.getBasePath() + pathAnnoAdapter.getResults().get(0));
                    } else {
                        currentMapping.setPath(pathAnnoAdapter.getResults().get(0));
                    }
                }
            }
        }
        Type[] argTypes = Type.getArgumentTypes(this.desc);
        for (int i = 0; i < argTypes.length; i++) {
            if (i < this.requestParam.size()) {
                this.requestParam.get(i).setParamType(argTypes[i].getClassName());
                this.requestParam.get(i).setParamIndex(i);
            }
        }
        super.visitCode();
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitParameterAnnotation(parameter, descriptor, visible);
        if (descriptor.equals(SpringConstant.RequestParamAnno)) {
            return new SpringAnnoAdapter(Const.ASMVersion, av, requestParam, parameter);
        }
        return av;
    }


    @Override
    public void visitParameter(String name, int access) {
        SpringParam param = new SpringParam();
        param.setParamName(name);
        this.requestParam.add(param);
        super.visitParameter(name, access);
    }


    @Override
    public void visitEnd() {
        if (currentMapping != null) {
            this.requestParam.forEach(param -> {
                if (param.getReqName() == null || param.getReqName().isEmpty()) {
                    param.setReqName(param.getParamName());
                }
            });
            for (SpringMapping mapping : currentMapping.getController().getMappings()) {
                if (mapping.getPath().endsWith("/")) {
                    mapping.setPath(mapping.getPath().substring(0, mapping.getPath().length() - 1));
                }
            }
            controller.addMapping(currentMapping);
        }
        super.visitEnd();
    }
}
