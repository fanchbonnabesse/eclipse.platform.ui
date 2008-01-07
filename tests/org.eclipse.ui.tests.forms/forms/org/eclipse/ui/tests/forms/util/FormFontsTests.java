/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 
package org.eclipse.ui.tests.forms.util;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.forms.widgets.FormFonts;

public class FormFontsTests extends TestCase {
	public void testSingleton() {
		Display display = Display.getCurrent();
		FormFonts instance = FormFonts.getInstance();
		// ensure the singleton is returning the same instance
		Assert.assertTrue("getInstance() returned a different FormFonts instance", instance.equals(FormFonts.getInstance()));
		Font boldSystemFont = instance.getBoldFont(display, display.getSystemFont());
		instance.markFinished(boldSystemFont);
		// ensure the singleton is returning the same instance after creating and disposing one gradient
		Assert.assertTrue("getInstance() returned a different FormFonts instance after creation and disposal of one bold font", instance.equals(FormFonts.getInstance()));
	}
	
	public void testDisposeOne() {
		Display display = Display.getCurrent();
		Font boldSystemFont = FormFonts.getInstance().getBoldFont(display, display.getSystemFont());
		FormFonts.getInstance().markFinished(boldSystemFont);
		// ensure that getting a single gradient and marking it as finished disposed it
		Assert.assertTrue("markFinished(...) did not dispose a font after a single getBoldFont()", boldSystemFont.isDisposed());
	}
	
	public void testMultipleInstances() {
		Display display = Display.getCurrent();
		Font boldSystemFont = FormFonts.getInstance().getBoldFont(display, display.getSystemFont());
		int count;
		// ensure that the same image is returned for many calls with the same parameter
		for (count = 1; count < 20; count ++)
			Assert.assertEquals("getBoldFont(...) returned a different font for the same params on iteration "+count,
					boldSystemFont, FormFonts.getInstance().getBoldFont(display, display.getSystemFont()));
		for ( ;count > 0; count--) {
			FormFonts.getInstance().markFinished(boldSystemFont);
			if (count != 1)
				// ensure that the gradient is not disposed early
				Assert.assertFalse("markFinished(...) disposed a shared font early on iteration "+count,boldSystemFont.isDisposed());
			else
				// ensure that the gradient is disposed on the last markFinished
				Assert.assertTrue("markFinished(...) did not dispose a shared font on the last call",boldSystemFont.isDisposed());
		}
	}
	
	public void testMultipleFonts() {
		Display display = Display.getCurrent();
		Font systemFont = display.getSystemFont();
		Font bannerFont = JFaceResources.getBannerFont();
		if (systemFont.equals(bannerFont)) return;  // Skip test in unlikely event these are the same font
		Font boldSystemFont = FormFonts.getInstance().getBoldFont(display, systemFont);
		Font boldBannerFont = FormFonts.getInstance().getBoldFont(display, bannerFont);
		assertFalse(boldSystemFont.equals(boldBannerFont));
		FormFonts.getInstance().markFinished(boldSystemFont);
		assertTrue(boldSystemFont.isDisposed());
		assertFalse(boldBannerFont.isDisposed());
		FormFonts.getInstance().markFinished(boldBannerFont);
		assertTrue(boldBannerFont.isDisposed());
	}
	
	public void testDisposeUnknown() {
		Display display = Display.getCurrent();
		Font system = new Font(display, display.getSystemFont().getFontData());
		FormFonts.getInstance().markFinished(system);
		Assert.assertTrue("markFinished(...) did not dispose of an unknown font", system.isDisposed());
	}
}