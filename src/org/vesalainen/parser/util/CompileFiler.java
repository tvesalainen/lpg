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

package org.vesalainen.parser.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Objects;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

/**
 * @author Timo Vesalainen
 */
public class CompileFiler implements Filer
{
    private File classDir;
    private File srcDir;

    public CompileFiler(File classDir, File srcDir)
    {
        this.classDir = Objects.requireNonNull(classDir);
        this.srcDir = Objects.requireNonNull(srcDir);
    }

    @Override
    public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements) throws IOException
    {
        File f = new File(srcDir, name.toString());
        return new JavaFileObject(f.toURI(), Kind.SOURCE);
    }

    @Override
    public JavaFileObject createClassFile(CharSequence name, Element... originatingElements) throws IOException
    {
        File f = new File(classDir, name.toString());
        return new JavaFileObject(f.toURI(), Kind.CLASS);
    }

    @Override
    public FileObject createResource(Location location, CharSequence pkg, CharSequence relativeName, Element... originatingElements) throws IOException
    {
        String name = pkg.toString().replace('.', File.separatorChar)+File.separatorChar+relativeName.toString();
        if (StandardLocation.CLASS_OUTPUT.equals(location))
        {
            File f = new File(classDir, name.toString());
            return new JavaFileObject(f.toURI(), Kind.OTHER);
        }
        if (StandardLocation.SOURCE_OUTPUT.equals(location))
        {
            File f = new File(srcDir, name.toString());
            return new JavaFileObject(f.toURI(), Kind.OTHER);
        }
        throw new IllegalArgumentException(location+" not supported");
    }

    @Override
    public FileObject getResource(Location location, CharSequence pkg, CharSequence relativeName) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public class JavaFileObject extends SimpleJavaFileObject
    {

        private JavaFileObject(URI uri, Kind kind)
        {
            super(uri, kind);
        }

        @Override
        public OutputStream openOutputStream() throws IOException
        {
            File file = new File(uri);
            File parent = file.getParentFile();
            if (parent != null)
            {
                parent.mkdirs();
            }
            return new FileOutputStream(file);
        }
        
    }
}
