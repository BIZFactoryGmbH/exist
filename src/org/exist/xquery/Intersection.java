/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-06 Wolfgang M. Meier
 *  wolfgang@exist-db.org
 *  http://exist.sourceforge.net
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id$
 */
package org.exist.xquery;

import java.util.Set;
import java.util.TreeSet;

import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;

/**
 * @author Wolfgang Meier (wolfgang@exist-db.org)
 */
public class Intersection extends CombiningExpression {
	
	/**
	 * @param context
	 */
	public Intersection(XQueryContext context, PathExpr left, PathExpr right) {
		super(context, left, right);
	}
    
	/* (non-Javadoc)
	 * @see org.exist.xquery.Expression#eval(org.exist.dom.DocumentSet, org.exist.xquery.value.Sequence, org.exist.xquery.value.Item)
	 */
	public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {
        if (context.getProfiler().isEnabled()) {
            context.getProfiler().start(this);       
            context.getProfiler().message(this, Profiler.DEPENDENCIES, "DEPENDENCIES", Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT SEQUENCE", contextSequence);
            if (contextItem != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT ITEM", contextItem.toSequence());
        }
        
        Sequence result;
		Sequence lval = left.eval(contextSequence, contextItem);        	
		Sequence rval = right.eval(contextSequence, contextItem);
        lval.removeDuplicates();
        rval.removeDuplicates();        
        
		if(!(Type.subTypeOf(lval.getItemType(), Type.NODE) && Type.subTypeOf(rval.getItemType(), Type.NODE)))
			throw new XPathException(getASTNode(), "intersect operand is not a node sequence");

        if(lval.getLength() == 0 )
            result = Sequence.EMPTY_SEQUENCE;         
        if(rval.getLength() == 0)
            result = Sequence.EMPTY_SEQUENCE;        
        if (lval.isPersistentSet() && rval.isPersistentSet())
            result = lval.toNodeSet().intersection(rval.toNodeSet());
        else {
            result = new ValueSequence();
            Set set = new TreeSet();
            for (SequenceIterator i = lval.unorderedIterator(); i.hasNext(); )
                set.add(i.nextItem());
            for (SequenceIterator i = rval.unorderedIterator(); i.hasNext(); ) {
                Item next = i.nextItem();
                if (set.contains(next))
                    result.add(next);
            }
            result.removeDuplicates();            
        }
        
        if (context.getProfiler().isEnabled())           
            context.getProfiler().end(this, "", result);  
        
        return result;
	}
	
	/* (non-Javadoc)
     * @see org.exist.xquery.Expression#dump(org.exist.xquery.util.ExpressionDumper)
     */
    public void dump(ExpressionDumper dumper) {
        left.dump(dumper);
        dumper.display(" intersect ");
        right.dump(dumper);
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(left.toString());
        result.append(" intersect ");
        result.append(right.toString());
        return result.toString();
    }    
}
