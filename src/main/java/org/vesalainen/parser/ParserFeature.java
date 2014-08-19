/*
 * Copyright (C) 2014 Timo Vesalainen
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

import java.util.EnumSet;
import org.vesalainen.parser.annotation.ParseMethod;

/**
 * 
 * @author Timo Vesalainen
 */
public enum ParserFeature
{
     /**
     * Convert input to upper-case
     */
    UpperCase,
    /**
     * Convert input to lower-case
     */
    LowerCase,
    /**
     * goto and jrs instructions are replaced with goto_w and jsr_w.
     */
    WideIndex,
    /**
     * Reducer methods are not used in parsing.
     */
    SyntaxOnly,
    /**
     * Use OffsetLocatorException when syntax error.
     * @see org.vesalainen.parser.util.OffsetLocatorException
     */
    UseOffsetLocatorException,
    /**
     * Use include feature.
     * @see org.vesalainen.parser.util.InputReader#include
     */
    UseInclude,
    /**
     * Use insert feature.
     * @see org.vesalainen.parser.util.InputReader#insert
     */
    UsePushback,
    /**
     * Used when charset must be changed during parsing. Ex. XML.
     */
    UseModifiableCharset,
    /**
     * Used to auto-close parser input after parsing.
     */
    UseAutoClose,
    /**
     * Use direct buffer if possible
     * @see java.nio.ByteBuffer#allocateDirect(int) 
     */
    UseDirectBuffer,
    /**
     * Use when parser class implements UseChecksum interface. UseChecksum is updated
 with parsed data.
     * @see java.util.zip.Checksum
     */
    UseChecksum;
    /**
     * Returns EnumSet constructed from @ParseMethod
     * @param pm
     * @return 
     */
    @SuppressWarnings("deprecated")
    public static EnumSet<ParserFeature> get(ParseMethod pm)
    {
        EnumSet<ParserFeature> set = EnumSet.noneOf(ParserFeature.class);
        if (pm.lower())
        {
            set.add(LowerCase);
        }
        if (pm.upper())
        {
            set.add(UpperCase);
        }
        if (pm.wideIndex())
        {
            set.add(WideIndex);
        }
        if (pm.syntaxOnly())
        {
            set.add(SyntaxOnly);
        }
        if (pm.useOffsetLocatorException())
        {
            set.add(UseOffsetLocatorException);
        }
        for (ParserFeature pf : pm.features())
        {
            set.add(pf);
        }
        return set;
    }
}
