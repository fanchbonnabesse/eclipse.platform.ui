/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import org.eclipse.ui.IMemento;

final class Contributor implements Comparable {

	final static String ELEMENT = "contributor";
	private final static String ATTRIBUTE_VALUE = "value";
	
	static Contributor create(String value) {
		return new Contributor(value);
	}

	static Contributor read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		return Contributor.create(memento.getString(ATTRIBUTE_VALUE));
	}

	private String value;
	
	private Contributor(String value) {
		super();
		this.value = value;	
	}
	
	String getValue() {
		return value;	
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Contributor))
			throw new ClassCastException();
			
		return Util.compare(value, ((Contributor) object).value);			
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Contributor))
			return false;
		
		String value = ((Contributor) object).value;		
		return this.value == null ? value == null : this.value.equals(value);
	}
	
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}

	void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		memento.putString(ATTRIBUTE_VALUE, value);
	}	
}
