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

package org.vesalainen.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.vesalainen.bcc.ClassCompiler;
import org.vesalainen.bcc.FieldInitializer;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.SubClass;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.grammar.math.MathExpressionParser;
import org.vesalainen.grammar.math.MethodExpressionHandler;
import org.vesalainen.grammar.math.MethodExpressionHandlerFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GenRegex;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.MapDef;
import org.vesalainen.parser.annotation.MathExpression;
import org.vesalainen.regex.Regex;

/**
 * @author Timo Vesalainen
 */
public class GenClassCompiler  implements ClassCompiler, ParserConstants
{
    protected TypeElement superClass;
    protected SubClass subClass;
    protected Filer filer;
    protected List<RegexWrapper> regexList;
    private MathExpressionParser mathExpressionParser;
    /**
     * Creates a parser using grammar.
     * @param superClass Super class for parser. Possible parser annotations
     * are not processed.
     * @param fullyQualifiedname  Parser class name .
     * @param grammar
     * @throws NoSuchMethodException
     * @throws IOException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException 
     */
    protected GenClassCompiler(TypeElement superClass) throws IOException, ReflectiveOperationException
    {
        this.superClass = superClass;
        GenClassname genClassname = superClass.getAnnotation(GenClassname.class);
        this.subClass = new SubClass(superClass, genClassname.value(), genClassname.modifiers());
    }
    public static GenClassCompiler compile(TypeElement superClass, Filer filer) throws IOException, ReflectiveOperationException
    {
        GenClassCompiler compiler;
        GrammarDef grammarDef = superClass.getAnnotation(GrammarDef.class);
        if (grammarDef != null)
        {
            compiler = new ParserCompiler(superClass);
        }
        else
        {
            MapDef mapDef = superClass.getAnnotation(MapDef.class);
            if (mapDef != null)
            {
                compiler = new MapCompiler(superClass);
            }
            else
            {
                compiler = new GenClassCompiler(superClass);
            }
        }
        compiler.setFiler(filer);
        compiler.compile();
        if (filer == null)
        {
            System.err.println("warning! classes directory not set");
        }
        else
        {
            compiler.saveClass();
        }
        return compiler;
    }
    /**
     * Handles initializer, constructor and other common annotation compilations.
     * <p>Common handled annotations are: @GenRegex
     * @throws IOException
     * @throws ReflectiveOperationException 
     */
    @Override
    public void compile() throws IOException, ReflectiveOperationException
    {
        compileInitializers();
        compileConstructors();
        for (ExecutableElement method : ElementFilter.methodsIn(El.getAllMembers(superClass)))
        {
            MathExpression mathExpression = method.getAnnotation(MathExpression.class);
            if (mathExpression != null)
            {
                compileMathExpression(method, mathExpression);
            }
        }
    }

    private void compileMathExpression(final ExecutableElement method, final MathExpression mathExpression) throws IOException, ReflectiveOperationException
    {
        TypeMirror returnType = method.getReturnType();
        if (!(
                Typ.isSubtype(returnType, Typ.getTypeFor(Number.class)) ||
                (Typ.isPrimitive(returnType) && returnType.getKind() != TypeKind.BOOLEAN)
                ))
        {
            throw new IllegalArgumentException(method+" return type is not number");
        }
        for (VariableElement param : method.getParameters())
        {
            if (param.asType().getKind() != TypeKind.ARRAY)
            {
                if (!Typ.isSameType(returnType, param.asType()))
                {
                    throw new IllegalArgumentException(method+" parameter type not the same as return type");
                }
            }
        }
        MethodCompiler mc = new MethodCompiler()
        {
            @Override
            protected void implement() throws IOException
            {
                try
                {
                    MethodExpressionHandler handler = MethodExpressionHandlerFactory.getInstance(method, this);
                    mathExpressionParser = (MathExpressionParser) GenClassFactory.getGenInstance(MathExpressionParser.class);
                    mathExpressionParser.parse(mathExpression, handler);
                    treturn();
                }
                catch (ReflectiveOperationException ex)
                {
                    throw new IOException(ex);
                }
            }
        };
        subClass.overrideMethod(mc, method, Modifier.PUBLIC);
    }

    public void compileInitializers() throws IOException
    {
        subClass.codeStaticInitializer(resolvStaticInitializers());
    }
    public void compileConstructors() throws IOException
    {
        subClass.codeDefaultConstructor(resolvInitializers());
    }
    @Override
    public void setFiler(Filer filer)
    {
        this.filer = filer;
    }

    public Filer getFiler()
    {
        return filer;
    }

    /**
     * Saves Parser class in java classfile format in dir. File path is defined by
     * dir and classname. Example dir = c:\temp class is foo.bar.Main file path is
     * c:\temp\foo\bar\Main.class
     * if srcDir is not null, creates a byte code source file to dir. File content is similar to the
     * output of javap utility. Your IDE might be able to use this file for debugging
     * the actual byte code. (NetBeans can if this file located like java source
     * files)
     *
     * Example dir = c:\src class is foo.bar.Main Source file path is
     * c:\src\foo\bar\Main.jasm
     * @throws IOException
     */
    @Override
    public void saveClass() throws IOException
    {
        subClass.createSourceFile(filer);
        subClass.save(filer);
        if (regexList != null)
        {
            for (RegexWrapper rw : regexList)
            {
                try
                {
                    Regex.saveAs(rw.getExpression(), filer, rw.getClassname(), rw.getOptions());
                }
                catch (Exception ex)
                {
                    throw new IOException(ex);
                }
            }
        }
    }

    /**
     * Compile the generated class dynamically. Nice method for experimenting and testing.
     * @return
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Class<?> loadDynamic()
    {
        try
        {
            return subClass.load();
        }
        catch (IOException ex)
        {
            throw new ParserException(ex);
        }
    }

    public Object newInstance()
    {
        try
        {
            Class<?> c = subClass.load();
            return c.newInstance();
        }
        catch (IOException | InstantiationException | IllegalAccessException ex)
        {
            throw new ParserException(ex);
        }
    }
    protected FieldInitializer[] resolvInitializers()
    {
        List<FieldInitializer> list = new ArrayList<>();
        for (VariableElement field : ElementFilter.fieldsIn(El.getAllMembers(superClass)))
        {
            if (!field.getModifiers().contains(Modifier.STATIC))
            {
                GenRegex annotation = field.getAnnotation(GenRegex.class);
                if (annotation != null)
                {
                    list.add(createRegex(field));
                }
            }
        }
        return list.toArray(new FieldInitializer[list.size()]);
    }
    protected FieldInitializer[] resolvStaticInitializers()
    {
        List<FieldInitializer> list = new ArrayList<>();
        for (VariableElement field : ElementFilter.fieldsIn(El.getAllMembers(superClass)))
        {
            if (field.getModifiers().contains(Modifier.STATIC))
            {
                GenRegex annotation = field.getAnnotation(GenRegex.class);
                if (annotation != null)
                {
                    list.add(createRegex(field));
                }
            }
        }
        return list.toArray(new FieldInitializer[list.size()]);
    }
    protected FieldInitializer createRegex(VariableElement f)
    {
        if (!Typ.isAssignable(f.asType(), Typ.getTypeFor(Regex.class)))
        {
            throw new IllegalArgumentException(f+" cannot be initialized with Regex subclass");
        }
        if (regexList == null)
        {
            regexList = new ArrayList<>();
        }
        GenRegex ra = f.getAnnotation(GenRegex.class);
        String cn = subClass.getQualifiedName()+"Regex"+regexList.size();
        regexList.add(new RegexWrapper(ra.value(), cn, ra.options()));
        return FieldInitializer.getObjectInstance(f, cn);
    }

    protected static class RegexWrapper
    {
        private String expression;
        private String classname;
        private Regex.Option[] options;

        public RegexWrapper(String expression, String classname, Regex.Option... options)
        {
            this.expression = expression;
            this.classname = classname;
            this.options = options;
        }

        public String getClassname()
        {
            return classname;
        }

        public String getExpression()
        {
            return expression;
        }

        public Regex.Option[] getOptions()
        {
            return options;
        }

    }

    public TypeElement getSuperClass()
    {
        return superClass;
    }

    public SubClass getSubClass()
    {
        return subClass;
    }
}
