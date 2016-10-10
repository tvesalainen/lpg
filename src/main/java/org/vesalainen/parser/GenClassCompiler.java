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
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.vesalainen.bcc.ClassCompiler;
import org.vesalainen.bcc.FieldInitializer;
import org.vesalainen.bcc.FragmentCompiler;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.SubClass;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;
import org.vesalainen.grammar.math.MathExpressionParserFactory;
import org.vesalainen.grammar.math.MathExpressionParserIntf;
import org.vesalainen.grammar.math.MethodExpressionHandler;
import org.vesalainen.grammar.math.MethodExpressionHandlerFactory;
import org.vesalainen.parser.annotation.DFAMap;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GenRegex;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.MathExpression;
import org.vesalainen.regex.Regex;

/**
 * @author Timo Vesalainen
 */
public class GenClassCompiler  implements ClassCompiler
{
    protected TypeElement superClass;
    protected SubClass subClass;
    protected ProcessingEnvironment env;
    /**
     * Creates a parser using grammar.
     * @param superClass Super class for parser. Possible parser annotations
     * are not processed.
     * @throws IOException 
     */
    protected GenClassCompiler(TypeElement superClass) throws IOException
    {
        this.superClass = superClass;
        GenClassname genClassname = superClass.getAnnotation(GenClassname.class);
        if (genClassname == null)
        {
            throw new UnsupportedOperationException(superClass.getQualifiedName()+" using @GrammarDef without @GenClassname is not supported (yet)");
        }
        this.subClass = new SubClass(superClass, genClassname.value(), genClassname.modifiers());
    }

    /**
     * Compiles a subclass for annotated superClass
     * @param superClass Annotated class
     * @param env ProcessingEnvironment
     * @return
     * @throws IOException
     */
    public static GenClassCompiler compile(TypeElement superClass, ProcessingEnvironment env) throws IOException
    {
        GenClassCompiler compiler;
        GrammarDef grammarDef = superClass.getAnnotation(GrammarDef.class);
        if (grammarDef != null)
        {
            compiler = new ParserCompiler(superClass);
        }
        else
        {
            DFAMap mapDef = superClass.getAnnotation(DFAMap.class);
            if (mapDef != null)
            {
                compiler = new MapCompiler(superClass);
            }
            else
            {
                compiler = new GenClassCompiler(superClass);
            }
        }
        compiler.setProcessingEnvironment(env);
        compiler.compile();
        if (env == null)
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
     */
    @Override
    public void compile() throws IOException
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

    private void compileMathExpression(final ExecutableElement method, final MathExpression mathExpression) throws IOException
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
                MethodExpressionHandler handler = MethodExpressionHandlerFactory.getInstance(method, this);
                MathExpressionParserIntf<TypeMirror,ExecutableElement,VariableElement> mathExpressionParser = (MathExpressionParserIntf) MathExpressionParserFactory.getInstance();
                mathExpressionParser.parse(mathExpression, handler);
                treturn();
            }
        };
        subClass.overrideMethod(mc, method, Modifier.PUBLIC);
    }

    public void compileInitializers() throws IOException
    {
        List<FieldInitializer> initializers = resolvStaticInitializers();
        subClass.codeStaticInitializer(initializers.toArray(new FieldInitializer[initializers.size()]));
    }
    public void compileConstructors() throws IOException
    {
        compileConstructors(null);
    }
    public void compileConstructors(FragmentCompiler fc) throws IOException
    {
        List<FieldInitializer> initializers = resolvInitializers();
        subClass.codeDefaultConstructor(fc, initializers.toArray(new FieldInitializer[initializers.size()]));
    }

    /**
     *
     * @param env
     */
    @Override
    public void setProcessingEnvironment(ProcessingEnvironment env)
    {
        this.env = env;
    }

    public ProcessingEnvironment getProcessingEnvironment()
    {
        return env;
    }

    /**
     * Saves Parser class 
     * @throws IOException
     */
    @Override
    public void saveClass() throws IOException
    {
        subClass.createSourceFile(env);
        subClass.save(env);
    }

    /**
     * Compile the generated class dynamically. Nice method for experimenting and testing.
     * @return
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
    protected List<FieldInitializer> resolvInitializers() throws IOException
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
        return list;
    }
    protected List<FieldInitializer> resolvStaticInitializers() throws IOException
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
        return list;
    }
    private static int regexCount;
    protected FieldInitializer createRegex(VariableElement field) throws IOException
    {
        if (!Typ.isAssignable(field.asType(), Typ.getTypeFor(Regex.class)))
        {
            throw new IllegalArgumentException(field+" cannot be initialized with Regex subclass");
        }
        GenRegex ra = field.getAnnotation(GenRegex.class);
        String className = subClass.getQualifiedName()+"Regex"+regexCount;
        regexCount++;
        SubClass sc = Regex.createSubClass(ra.value(), className, ra.options());
        sc.createSourceFile(env);
        sc.save(env);
        return FieldInitializer.getObjectInstance(field, sc);
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
