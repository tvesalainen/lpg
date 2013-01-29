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
package org.vesalainen.parser.ant;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}
import org.vesalainen.parser.ParserCompiler;
import org.vesalainen.parser.annotation.GrammarDef;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.vesalainen.bcc.BulkCompiler;
import org.vesalainen.grammar.Grammar;
import org.vesalainen.parser.annotation.GenClassname;

/**
 * ParserBuilder creates a parser class from given grammar.
 * @author tkv
 */
public class ParserBuilder extends Task
{

    private List<Class<?>> classes = new ArrayList<>();
    private Grammar grammar;
    private String language;
    private String region;
    private String script;
    private String variant;

    public ParserBuilder()
    {
        BulkCompiler.reset();
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    public void setVariant(String variant)
    {
        this.variant = variant;
    }

    public void setGrammar(String grammarClassname)
    {
        try
        {
            Class<? extends Grammar> grammarClass = (Class<? extends Grammar>) Class.forName(grammarClassname);
            grammar = grammarClass.newInstance();
        }
        catch (InstantiationException ex)
        {
            throw new BuildException(grammarClassname + " couldn't be instantiated", ex, getLocation());
        }
        catch (IllegalAccessException ex)
        {
            throw new BuildException(grammarClassname + " couldn't be accessed", ex, getLocation());
        }
        catch (ClassNotFoundException ex)
        {
            throw new BuildException(grammarClassname + " not found", ex, getLocation());
        }
    }
    /**
     * @deprecated 
     * @param debug 
     */
    public void setDebug(boolean debug)
    {
        log("debug is deprecated!");
    }

    public void setDestdir(File destdir)
    {
        BulkCompiler.setClasses(destdir);
        log(destdir.getPath());
        log("using "+destdir.getPath()+" for generated byte code");
    }

    public void setSrcdir(File srcdir)
    {
        BulkCompiler.setSrc(srcdir);
        log("using "+srcdir.getPath()+" for generated source");
    }

    public void setSrc(String className)
    {
        String[] cs = className.split("[, \t\r\n]+");
        for (String name : cs)
        {
            try
            {
                classes.add(Class.forName(name));
                log(name);
            }
            catch (ClassNotFoundException ex)
            {
                throw new BuildException(name + " not found", ex, getLocation());
            }
        }
    }

    public
    @Override
    void execute() throws BuildException
    {
        Locale defaultLocale = Locale.getDefault();
        try
        {
            if (language != null)
            {
                Locale.Builder builder = new Locale.Builder();
                builder.setLanguage(language);
                builder.setRegion(region);
                builder.setScript(script);
                builder.setVariant(variant);
                Locale.setDefault(builder.build());
            }
            for (Class<?> clazz : classes)
            {
                if (clazz.isAnnotationPresent(GrammarDef.class)
                        || grammar != null)
                {
                    compileParser(clazz);
                }
                else
                {
                    throw new BuildException(clazz + " not valid for compilation", getLocation());
                }
            }
        }
        finally
        {
            Locale.setDefault(defaultLocale);
        }
    }

    private void compileParser(Class<?> parser)
    {
        GenClassname genClassname = parser.getAnnotation(GenClassname.class);
        if (genClassname == null)
        {
            throw new BuildException("@GenClassname not set in "+parser, getLocation());
        }
        String classname = genClassname.value();
        if (!needsCompiling(parser, classname))
        {
            log(classname+" is uptodate");
            return;
        }
        try
        {
            log("compiling " + parser);
            ParserCompiler c = null;
            if (grammar != null)
            {
                log("Compiling parser " + classname);
                c = new ParserCompiler(parser, grammar);
                BulkCompiler.compile(c);
            }
            else
            {
                log("Compiling parser " + classname);
                c = new ParserCompiler(parser);
                BulkCompiler.compile(c);
            }
            log("Saving parser " + classname + " in " + BulkCompiler.getClasses());
        }

        catch (ReflectiveOperationException | IOException ex)
        {
            log(ex, Project.MSG_ERR);
            throw new BuildException(parser + " fails", ex, getLocation());
        }        

    }
    private boolean needsCompiling(Class<?> superClass, String dstClass)
    {
        String superClassname = superClass.getName().replace('.', '/')+".class";
        String dstClassname = dstClass.replace('.', '/')+".class";
        File destdir = BulkCompiler.getClasses();
        File superFile = new File(destdir, superClassname);
        File dstFile = new File(destdir, dstClassname);
        return !superFile.exists() || !dstFile.exists() || dstFile.lastModified() < superFile.lastModified();
    }
}
