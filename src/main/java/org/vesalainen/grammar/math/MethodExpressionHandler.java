/*
 * Copyright (C) 2012 Timo Vesalainen
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

package org.vesalainen.grammar.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;

/**
 * @author Timo Vesalainen
 */
public abstract class MethodExpressionHandler implements ExpressionHandler<TypeMirror,ExecutableElement,VariableElement,VariableElement>
{
    private static final Set<ExecutableElement> RadianArgs = new HashSet<>();
    private static final Set<ExecutableElement> RadianReturns = new HashSet<>();
    static
    {
        RadianArgs.add(El.getMethod(Math.class, "sin", double.class));
        RadianArgs.add(El.getMethod(Math.class, "cos", double.class));
        RadianArgs.add(El.getMethod(Math.class, "tan", double.class));
        RadianReturns.add(El.getMethod(Math.class, "asin", double.class));
        RadianReturns.add(El.getMethod(Math.class, "acos", double.class));
        RadianReturns.add(El.getMethod(Math.class, "atan", double.class));
    }
    protected ExecutableElement method;
    protected MethodCompiler mc;
    protected TypeMirror type;
    private TypeMirror safe;

    private enum VarType {LocalVariable, Field, Getter, Enum };
    protected TypeMirror variableType;
    protected TypeMirror componentType;
    private Map<TypeMirror,Integer> ranking = new HashMap<>();

    public MethodExpressionHandler(ExecutableElement method, MethodCompiler methodCompiler, TypeMirror type)
    {
        checkType(type);
        this.method = method;
        this.mc = methodCompiler;
        this.type = type;
        
        ranking.put(Typ.Int, 1);
        ranking.put(Typ.Long, 2);
        ranking.put(Typ.Float, 3);
        ranking.put(Typ.Double, 4);
    }
    
    private void checkType(TypeMirror type)
    {
        switch (type.getKind())
        {
            case INT:
            case SHORT:
            case CHAR:
            case LONG:
            case FLOAT:
            case DOUBLE:
                break;
            default:
                if (!Typ.isSubtype(type, Typ.getTypeFor(Number.class)))
                {
                    throw new IllegalArgumentException(type+" not suitable for math expr");
                }
        }
    }
    @Override
    public ExecutableElement findMethod(String funcName, int args) throws Exception
    {
        TypeMirror[] params = new TypeMirror[args];
        TypeMirror typ = getType();
        Arrays.fill(params, typ);
        List<TypeElement> classList = new ArrayList<>();
        ExecutableElement result = null;
        int match = Integer.MAX_VALUE;
        for (TypeMirror t :Typ.directSupertypes(typ))
        {
            DeclaredType dt = (DeclaredType) t;
            classList.add((TypeElement)dt.asElement());
        }
        classList.add(El.getTypeElement(Math.class.getCanonicalName()));
        classList.add(El.getTypeElement(MoreMath.class.getCanonicalName()));
        MethodIterator mi = new MethodIterator(funcName, args, classList);
        while (mi.hasNext())
        {
            ExecutableElement mtd = mi.next();
            int ma = getMatch(mtd);
            if (ma == 0)
            {
                return mtd;
            }
            if (ma < match)
            {
                result = mtd;
                match = ma;
            }
        }
        return result;
    }

    private int getMatch(ExecutableElement method)
    {
        int res = getMatch(method.getReturnType());
        if (res < 0)
        {
            return res;
        }
        for (VariableElement v : method.getParameters())
        {
            int m = getMatch(v.asType());
            if (m < 0)
            {
                return m;
            }
            res += m;
        }
        return res;
    }
    private int getMatch(TypeMirror t)
    {
        return ranking.get(t) - ranking.get(type);
    }
    public TypeMirror getType()
    {
        return type;
    }

    /**
     * Set index parsing mode. In true mode we are parsing inside bracket where 
     * index type must be int.
     * @param b 
     */
    @Override
    public void setIndex(boolean b)
    {
        if (b)
        {
            assert safe == null;
            safe = type;
            type = Typ.Int;
        }
        else
        {
            assert type.getKind() == TypeKind.INT;
            assert safe != null;
            type = safe;
            safe = null;
        }
    }

    @Override
    public void loadVariable(String identifier) throws Exception
    {
        VarType varType = null;
        if (mc.hasLocalVariable(identifier))
        {
            loadLocalVariable(identifier);
            variableType = mc.getLocalType(identifier);
            varType = VarType.LocalVariable;
        }
        TypeElement cls = (TypeElement) method.getEnclosingElement();
        VariableElement field = El.getField(cls, identifier);
        if (field != null)
        {
            if (varType != null)
            {
                throw new IllegalArgumentException(identifier+" is ambiguous. Field and "+varType+" matches");
            }
            variableType = field.asType();
            loadField(field);
            varType = VarType.Field;
        }        
        String getter = "get"+identifier.substring(0, 1).toUpperCase()+identifier.substring(1);
        ExecutableElement getMethod = El.getMethod(cls, getter);
        if (getMethod != null)
        {
            if (varType != null)
            {
                throw new IllegalArgumentException(identifier+" is ambiguous. Getter and "+varType+" matches");
            }
            variableType = getMethod.getReturnType();
            invoke(getMethod);
            varType = VarType.Getter;
        }        
        int ordinal = findEnum(cls, identifier);
        if (ordinal >= 0)
        {
            if (varType != null)
            {
                throw new IllegalArgumentException(identifier+" is ambiguous. Enum and "+varType+" matches");
            }
            variableType = Typ.Int;
            number(String.valueOf(ordinal));
            varType = VarType.Enum;
        }
        if (varType == null)
        {
            throw new IllegalArgumentException("argument "+identifier+" not found");
        }
        if (variableType.getKind() == TypeKind.ARRAY)
        {
            ArrayType at = (ArrayType) variableType;
            componentType = at.getComponentType();
        }
    }

    private int findEnum(TypeElement cls, String name)
    {
        for (TypeElement cc : ElementFilter.typesIn(El.getAllMembers(cls)))
        {
            if (cc.getKind() == ElementKind.ENUM)
            {
                for (VariableElement ve : ElementFilter.fieldsIn(cc.getEnclosedElements()))
                {
                    if (ve.getKind() == ElementKind.ENUM_CONSTANT)
                    {
                        if (name.contentEquals(ve.getSimpleName()))
                        {
                            return (int) ve.getConstantValue();
                        }
                    }
                }
            }
        }
        return -1; 
    }
    
    @Override
    public void invoke(ExecutableElement method) throws Exception
    {
        mc.invoke(method);
    }

    public void loadLocalVariable(String name) throws Exception
    {
        mc.tload(name);
    }

    @Override
    public void loadField(VariableElement field) throws Exception
    {
        mc.getField(field);
    }

    @Override
    public boolean isRadianArgs(ExecutableElement method)
    {
        return RadianArgs.contains(method);
    }

    @Override
    public boolean isRadianReturn(ExecutableElement method)
    {
        return RadianReturns.contains(method);
    }

    @Override
    public List<? extends VariableElement> getParameters(ExecutableElement method)
    {
        return method.getParameters();
    }

    @Override
    public TypeMirror getReturnType(ExecutableElement method)
    {
        return method.getReturnType();
    }

    @Override
    public TypeMirror asType(VariableElement variable)
    {
        return variable.asType();
    }

    @Override
    public ExecutableElement getMethod(Class<?> cls, String name, Class<?>... parameters)
    {
        return El.getMethod(cls, name, parameters);
    }

    @Override
    public VariableElement getField(Class<?> cls, String name)
    {
        return El.getField(cls, name);
    }

    private class MethodIterator implements Iterator<ExecutableElement> 
    {
        private String name;
        private int argCount;
        private Iterator<TypeElement> iterator;
        private List<? extends ExecutableElement> methods;
        private int index;
        private ExecutableElement next;

        public MethodIterator(String name, int argCount, List<TypeElement> list)
        {
            this.name = name;
            this.argCount = argCount;
            this.iterator = list.iterator();
            next = getNext();
        }

        @Override
        public boolean hasNext()
        {
            return next != null;
        }

        @Override
        public ExecutableElement next()
        {
            ExecutableElement res = next;
            next = getNext();
            return res;
        }
        
        private ExecutableElement getNext()
        {
            ExecutableElement m = getNext2();
            while (m != null)
            {
                if (
                        name.contentEquals(m.getSimpleName()) &&
                        m.getParameters().size() == argCount
                        )
                {
                    return m;
                }
                m = getNext2();
            }
            return null;
        }
        private ExecutableElement getNext2()
        {
            while ((methods != null && index < methods.size()) || iterator.hasNext())
            {
                if (methods != null && index < methods.size())
                {
                    return methods.get(index++);
                }
                else
                {
                    TypeElement cls = iterator.next();
                    methods = ElementFilter.methodsIn(cls.getEnclosedElements());
                    index = 0;
                }
            }
            return null;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
