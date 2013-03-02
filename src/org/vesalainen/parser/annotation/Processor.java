/*
 * Copyright (C) 2013 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vesalainen.parser.annotation;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.vesalainen.bcc.ClassFile;
import org.vesalainen.parser.GenClassCompiler;

/**
 * @author Timo Vesalainen
 */
@SupportedAnnotationTypes("org.vesalainen.parser.annotation.GenClassname")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class Processor extends AbstractProcessor
{

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        Filer filer = processingEnv.getFiler();
        Messager msg = processingEnv.getMessager();
        for (TypeElement te : annotations)
        {
            //msg.printMessage(Diagnostic.Kind.NOTE, "processing", te);
            for (Element e : roundEnv.getElementsAnnotatedWith(te))
            {
                TypeElement type = (TypeElement) e;
                try
                {
                    msg.printMessage(Diagnostic.Kind.NOTE, "processing", type);
                    String qualifiedName = type.getQualifiedName().toString();
                    Class<?> thisClass = Class.forName(qualifiedName);
                    GenClassCompiler.compile(thisClass, filer);
                }
                catch (ReflectiveOperationException | IOException ex)
                {
                    ex.printStackTrace();
                    msg.printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), e);
                }
            }
        }
        return true;
    }

}
