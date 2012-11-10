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
package org.vesalainen.regex.ant;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}
import org.vesalainen.regex.Regex;
import org.vesalainen.regex.Regex.Option;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * @author tkv
 */
public class RegexBuilder extends Task
{
    private Text expression = new Text();
    private String className;
    private File srcdir;
    private File destdir;
    private Option[] options = new Option[] {};
    private boolean debug;
    private boolean state;

    public Object createExpression()
    {
        return expression;
    }
    public void setClassName(String className)
    {
        this.className = className;
    }

    public void setOptions(Option... options)
    {
        this.options = options;
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    public void setPrintStates(boolean state)
    {
        this.state = state;
    }

    public void setDestdir(File destdir)
    {
        this.destdir = destdir;
        log(destdir.getPath());
    }

    public void setSrcdir(File srcdir)
    {
        this.srcdir = srcdir;
        log(srcdir.getPath());
    }

    public
    @Override
    void execute() throws BuildException
    {
        try
        {
            String exp = expression.getExpression();
            log("compiling regex '"+exp+"' -> "+className);
            Regex.saveAs(exp, destdir, srcdir, className, options);
        }
        catch (NoSuchMethodException ex)
        {
            log(ex, Project.MSG_ERR);
            throw new BuildException(expression+" fails", ex, getLocation());
        }
        catch (NoSuchFieldException ex)
        {
            log(ex, Project.MSG_ERR);
            throw new BuildException(expression+" fails", ex, getLocation());
        }
        catch (IOException ex)
        {
            log(ex, Project.MSG_ERR);
            throw new BuildException(expression+" fails", ex, getLocation());
        }
        catch (InstantiationException ex)
        {
            log(ex, Project.MSG_ERR);
            throw new BuildException(expression+" fails", ex, getLocation());
        }
        catch (IllegalAccessException ex)
        {
            log(ex, Project.MSG_ERR);
            throw new BuildException(expression+" fails", ex, getLocation());
        }
    }

    public class Text extends Task
    {
        private boolean regex;
        private StringBuilder text = new StringBuilder();

        public void setRegex(boolean regex)
        {
            this.regex = regex;
        }

        public String getExpression() throws IOException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException
        {
            if (regex)
            {
                return text.toString();
            }
            else
            {
                return Regex.escape(text.toString());
            }
        }

        public void addText(String raw)
        {
            String s = getProject().replaceProperties(raw.trim());
            text.append(s);
        }

    }
}
