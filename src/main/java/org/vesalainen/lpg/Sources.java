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
package org.vesalainen.lpg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author tkv
 */
public class Sources
{

    private Map<Act, StackElement> configs = new HashMap<Act, StackElement>();
    private Set<StackElement> stackSeen = new HashSet<StackElement>();
    private List<Act> list = new ArrayList<Act>();

    void addConfigs(Act act, StackElement configRoot)
    {
        if (configRoot != null)
        {
            if (!configs.containsKey(act))
            {
                list.add(0, act);
            }
            configs.put(act,
                    StackElement.unionConfigSets(configs.get(act), configRoot));
        }
    }

    public StackElement getConfigs(Act act)
    {
        return configs.get(act);
    }
    
    public List<Act> getActList()
    {
        return list;
    }

    public void clearStackSeen()
    {
        stackSeen.clear();
    }

    boolean stackWasSeen(StackElement stack)
    {
        if (stackSeen.contains(stack))
        {
            return true;
        }
        stackSeen.add(stack);
        return false;
    }
}
