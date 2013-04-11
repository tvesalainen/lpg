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
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.parser.GenClassCompiler;

/**
 * @author Timo Vesalainen
 */
@SupportedAnnotationTypes("org.vesalainen.parser.annotation.GenClassname")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class Processor extends AbstractProcessor
{

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        El.setElements(processingEnv.getElementUtils());
        Typ.setTypes(processingEnv.getTypeUtils());
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Initialized");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        Filer filer = processingEnv.getFiler();
        Messager msg = processingEnv.getMessager();
        for (TypeElement te : annotations)
        {
            for (Element e : roundEnv.getElementsAnnotatedWith(te))
            {
                TypeElement type = (TypeElement) e;
                try
                {
                    msg.printMessage(Diagnostic.Kind.NOTE, "processing", type);
                    GenClassCompiler.compile(type, filer);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    msg.printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), e);
                }
            }
        }
        return true;
    }

}
