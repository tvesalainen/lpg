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
package org.vesalainen.parser;

import java.io.PrintStream;
import javax.lang.model.type.TypeKind;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TraceHelper
{

    public static void printLaBuffer(PrintStream out, int[] tokenBuffer, int[] lengthBuffer, int start, int end)
    {
        out.println("Token Length");
        for (int ii=start;ii<end;ii++)
        {
            int token = tokenBuffer[ii % tokenBuffer.length];
            int length = lengthBuffer[ii % lengthBuffer.length];
            out.printf("% 4d % 4d\n", token, length);
        }
    }
    public static void printStacks(PrintStream out, int[] stateStack, int[] typeStack, Object[] valueStack, int sp)
    {
        out.println("State Value               Token");
        for (int ii=sp;ii>=0;ii--)
        {
            int state = stateStack[ii];
            String value = getValue(typeStack, valueStack, ii);
            out.printf("% 4d %s\n", state, value);
        }
    }
    private static String getValue(int[] typeStack, Object[] valueStack, int sp)
    {
        int type = typeStack[sp];
        TypeKind ot = TypeKind.values()[type];
        int index = 0;
        for (int jj = 0; jj <= sp; jj++)
        {
            if (typeStack[jj] == type)
            {
                index++;
            }
        }
        switch (ot)
        {
            case BOOLEAN:
            {
                boolean[] s = (boolean[]) valueStack[ot.ordinal()];
                return "boolean("+s[sp]+")";
            }
            case BYTE:
            {
                byte[] s = (byte[]) valueStack[ot.ordinal()];
                return "byte("+s[sp]+")";
            }
            case CHAR:
            {
                char[] s = (char[]) valueStack[ot.ordinal()];
                return "char("+s[sp]+")";
            }
            case DOUBLE:
            {
                double[] s = (double[]) valueStack[ot.ordinal()];
                return "double("+ s[sp]+")";
            }
            case FLOAT:
            {
                float[] s = (float[]) valueStack[ot.ordinal()];
                return "float("+s[sp]+")";
            }
            case INT:
            {
                int[] s = (int[]) valueStack[ot.ordinal()];
                return "int("+s[sp]+")";
            }
            case LONG:
            {
                long[] s = (long[]) valueStack[ot.ordinal()];
                return "long("+s[sp]+")";
            }
            case DECLARED:
            {
                Object[] s = (Object[]) valueStack[ot.ordinal()];
                return s[sp]+"";
            }
            case SHORT:
            {
                short[] s = (short[]) valueStack[ot.ordinal()];
                return "short("+s[sp]+")";
            }
            case VOID:
            {
                return "void";
            }
            default:
                return "???";
        }
    }
}
