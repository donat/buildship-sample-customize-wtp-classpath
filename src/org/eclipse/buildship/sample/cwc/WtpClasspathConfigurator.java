package org.eclipse.buildship.sample.cwc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.buildship.core.InitializationContext;
import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.buildship.core.ProjectContext;


public class WtpClasspathConfigurator implements ProjectConfigurator {


    @Override
    public void init(InitializationContext context, IProgressMonitor monitor) {
    }

    @Override
    public void configure(ProjectContext context, IProgressMonitor monitor) {
        IProject project = context.getProject();
        if (hasJavaNature(project)) {
            IJavaProject javaProject = JavaCore.create(project);
            try {
                IClasspathEntry[] classpath = javaProject.getRawClasspath();
                boolean found = false;
                for(int i = 0; i < classpath.length; ++i) {
                    IClasspathEntry entry = classpath[i];
                    if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && entry.getPath().toString().startsWith("org.eclipse.buildship.core.gradleclasspathcontainer")) {
                        List<IClasspathAttribute> attributes = new ArrayList<>(Arrays.asList(entry.getExtraAttributes()));
                        Iterator<IClasspathAttribute> it = attributes.iterator();
                        while(it.hasNext()) {
                            IClasspathAttribute attr = it.next();
                            if (Arrays.asList("org.eclipse.jst.component.dependency", "org.eclipse.jst.component.nondependency").contains(attr.getName())) {
                                it.remove();
                                found = true;
                            }
                        }
                        classpath[i] = JavaCore.newContainerEntry(entry.getPath(), entry.getAccessRules(), attributes.toArray(new IClasspathAttribute[0]), entry.isExported());
                    }
                }
                if (found) {
                    javaProject.setRawClasspath(classpath, monitor);
                }
            } catch (JavaModelException e) {
            }
        }
    }

    private boolean hasJavaNature(IProject project) {
        try {
            return project.hasNature(JavaCore.NATURE_ID);
        } catch (CoreException e) {
            return false;
        }
    }

    @Override
    public void unconfigure(ProjectContext context, IProgressMonitor monitor) {
    }

}
