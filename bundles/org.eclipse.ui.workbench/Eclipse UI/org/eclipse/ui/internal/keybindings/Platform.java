/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IMemento;

public final class Platform {
	
	public final static String ELEMENT = "platform";		

	public static Platform create() {
		return new Platform(Path.create());
	}
	
	public static Platform create(Path path)
		throws IllegalArgumentException {
		return new Platform(path);
	}

	public static Platform read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		return Platform.create(Path.read(memento.getChild(Path.ELEMENT)));
	}

	public static Platform system() {
		List pathItems = new ArrayList();				
		String platform = SWT.getPlatform();
		
		if (platform != null) {
			pathItems.add(PathItem.create(platform));
		}
			
		return Platform.create(Path.create(pathItems));
	}	
	
	private Path path;
	
	private Platform(Path path)
		throws IllegalArgumentException {
		super();
		
		if (path == null)
			throw new IllegalArgumentException();
		
		this.path = path;	
	}
	
	public Path getPath() {
		return path;	
	}
	
	public int match(Platform platform)
		throws IllegalArgumentException {
		if (platform == null)
			throw new IllegalArgumentException();
		
		return (path.match(platform.path));
	}
		
	public int compareTo(Object object) {
		if (!(object instanceof Platform))
			throw new ClassCastException();
			
		return path.compareTo(((Platform) object).path); 
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Platform))
			return false;
		
		return path.equals(((Platform) object).path);		
	}

	public void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		path.write(memento.createChild(Path.ELEMENT));
	}
}
