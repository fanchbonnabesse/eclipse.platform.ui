/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.ide.IIDEActionConstants;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.actions.BuildSetMenu;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.internal.ide.actions.QuickMenuAction;
import org.eclipse.ui.internal.ide.actions.RetargetActionWithDefault;
import org.eclipse.ui.internal.util.StatusLineContributionItem;

/**
 * Adds actions to a workbench window.
 */
public final class WorkbenchActionBuilder extends ActionBarAdvisor {
    private final IWorkbenchWindow window;

    // generic actions
    private IWorkbenchAction closeAction;

    private IWorkbenchAction closeAllAction;

    private IWorkbenchAction closeAllSavedAction;

    private IWorkbenchAction saveAction;

    private IWorkbenchAction saveAllAction;

    private IWorkbenchAction helpContentsAction;

    private IWorkbenchAction aboutAction;

    private IWorkbenchAction openPreferencesAction;

    private IWorkbenchAction saveAsAction;

    private IWorkbenchAction hideShowEditorAction;

    private IWorkbenchAction savePerspectiveAction;

    private IWorkbenchAction resetPerspectiveAction;

    private IWorkbenchAction editActionSetAction;

    private IWorkbenchAction closePerspAction;

    private IWorkbenchAction lockToolBarAction;

    private IWorkbenchAction closeAllPerspsAction;

    private IWorkbenchAction showViewMenuAction;

    private IWorkbenchAction showPartPaneMenuAction;

    private IWorkbenchAction nextPartAction;

    private IWorkbenchAction prevPartAction;

    private IWorkbenchAction nextEditorAction;

    private IWorkbenchAction prevEditorAction;

    private IWorkbenchAction nextPerspectiveAction;

    private IWorkbenchAction prevPerspectiveAction;

    private IWorkbenchAction activateEditorAction;

    private IWorkbenchAction maximizePartAction;
    
    private IWorkbenchAction minimizePartAction;

    private IWorkbenchAction workbenchEditorsAction;

    private IWorkbenchAction workbookEditorsAction;

    private IWorkbenchAction backwardHistoryAction;

    private IWorkbenchAction forwardHistoryAction;

    // generic retarget actions
    private IWorkbenchAction undoAction;

    private IWorkbenchAction redoAction;

    private IWorkbenchAction cutAction;

    private IWorkbenchAction copyAction;

    private IWorkbenchAction pasteAction;

    private IWorkbenchAction deleteAction;

    private IWorkbenchAction selectAllAction;

    private IWorkbenchAction findAction;

    private IWorkbenchAction printAction;

    private IWorkbenchAction revertAction;

    private IWorkbenchAction refreshAction;

    private IWorkbenchAction propertiesAction;

    private IWorkbenchAction quitAction;

    private IWorkbenchAction moveAction;

    private IWorkbenchAction renameAction;

    private IWorkbenchAction goIntoAction;

    private IWorkbenchAction backAction;

    private IWorkbenchAction forwardAction;

    private IWorkbenchAction upAction;

    private IWorkbenchAction nextAction;

    private IWorkbenchAction previousAction;

    // IDE-specific actions
    private IWorkbenchAction openWorkspaceAction;

    private IWorkbenchAction projectPropertyDialogAction;

    private IWorkbenchAction newWizardAction;

    private IWorkbenchAction newWizardDropDownAction;

    private IWorkbenchAction importResourcesAction;

    private IWorkbenchAction exportResourcesAction;

    IWorkbenchAction buildAllAction; // Incremental workspace build

    private IWorkbenchAction cleanAction;

    private IWorkbenchAction toggleAutoBuildAction;

    MenuManager buildWorkingSetMenu;

    private IWorkbenchAction quickStartAction;

    private IWorkbenchAction tipsAndTricksAction;

    private QuickMenuAction showInQuickMenu;

    private QuickMenuAction newQuickMenu;

    private IWorkbenchAction introAction;

    // IDE-specific retarget actions
    private IWorkbenchAction addBookmarkAction;

    private IWorkbenchAction addTaskAction;

    IWorkbenchAction buildProjectAction;

    private IWorkbenchAction openProjectAction;

    private IWorkbenchAction closeProjectAction;

    // contribution items
    // @issue should obtain from ContributionItemFactory
    private NewWizardMenu newWizardMenu;

    private IContributionItem pinEditorContributionItem;

    // @issue class is workbench internal
    private StatusLineContributionItem statusLineItem;

    private Preferences.IPropertyChangeListener prefListener;

    // listener for the "close editors automatically"
    // preference change
    private IPropertyChangeListener propPrefListener;

    private IPageListener pageListener;

    private IPerspectiveListener perspectiveListener;

    private IResourceChangeListener resourceListener;
    
    /**
     * Indicates if the action builder has been disposed
     */
    private boolean isDisposed = false;
    
    /**
     * Constructs a new action builder which contributes actions
     * to the given window.
     * 
     * @param configurer the action bar configurer for the window
     */
    public WorkbenchActionBuilder(IActionBarConfigurer configurer) {
        super(configurer);
        window = configurer.getWindowConfigurer().getWindow();
    }

    /**
     * Returns the window to which this action builder is contributing.
     */
    private IWorkbenchWindow getWindow() {
        return window;
    }

    /**
     * Hooks listeners on the preference store and the window's page, perspective and selection services.
     */
    private void hookListeners() {

        pageListener = new IPageListener() {
            public void pageActivated(IWorkbenchPage page) {
                enableActions(page.getPerspective() != null);
            }

            public void pageClosed(IWorkbenchPage page) {
                IWorkbenchPage pg = getWindow().getActivePage();
                enableActions(pg != null && pg.getPerspective() != null);
            }

            public void pageOpened(IWorkbenchPage page) {
                // set default build handler -- can't be done until the shell is available
                IAction buildHandler = new BuildAction(page.getWorkbenchWindow().getShell(), IncrementalProjectBuilder.INCREMENTAL_BUILD);
            	((RetargetActionWithDefault)buildProjectAction).setDefaultHandler(buildHandler);
            }
        };
        getWindow().addPageListener(pageListener);

        perspectiveListener = new IPerspectiveListener() {
            public void perspectiveActivated(IWorkbenchPage page,
                    IPerspectiveDescriptor perspective) {
                enableActions(true);
            }

            public void perspectiveChanged(IWorkbenchPage page,
                    IPerspectiveDescriptor perspective, String changeId) {
                // do nothing
            }
        };
        getWindow().addPerspectiveListener(perspectiveListener);

        prefListener = new Preferences.IPropertyChangeListener() {
            public void propertyChange(Preferences.PropertyChangeEvent event) {
                if (event.getProperty().equals(
                        ResourcesPlugin.PREF_AUTO_BUILDING)) {
                   	updateBuildActions();
                }
            }
        };
        ResourcesPlugin.getPlugin().getPluginPreferences()
                .addPropertyChangeListener(prefListener);

        // listener for the "close editors automatically"
        // preference change
        propPrefListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(
                        IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
                    if (window.getShell() != null
                            && !window.getShell().isDisposed()) {
                        // this property change notification could be from a non-ui thread
                        window.getShell().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                updatePinActionToolbar();
                            }
                        });
                    }
                }
            }
        };
        /*
         * In order to ensure that the pin action toolbar sets its size 
         * correctly, the pin action should set its visiblity before we call updatePinActionToolbar().
         * 
         * In other words we always want the PinActionContributionItem to be notified before the 
         * WorkbenchActionBuilder.
         */
        WorkbenchPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(propPrefListener);
        //listen for project description changes, which can affect enablement of build actions
        resourceListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				IResourceDelta delta = event.getDelta();
				if (delta == null)
					return;
				IResourceDelta[] projectDeltas = delta.getAffectedChildren();
				for (int i = 0; i < projectDeltas.length; i++) {
					int kind = projectDeltas[i].getKind();
					//affected by projects being opened/closed or description changes
					boolean changed = (projectDeltas[i].getFlags() & (IResourceDelta.DESCRIPTION | IResourceDelta.OPEN)) != 0;
					if (kind != IResourceDelta.CHANGED || changed) {
						updateBuildActions();
						return;
					}
				}
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
    }

    /**
     * Enables the menu items dependent on an active
     * page and perspective.
     * Note, the show view action already does its own 
     * listening so no need to do it here.
     */
    private void enableActions(boolean value) {
        hideShowEditorAction.setEnabled(value);
        savePerspectiveAction.setEnabled(value);
        lockToolBarAction.setEnabled(value);
        resetPerspectiveAction.setEnabled(value);
        editActionSetAction.setEnabled(value);
        closePerspAction.setEnabled(value);
        closeAllPerspsAction.setEnabled(value);
//        newWizardMenu.setEnabled(value);
        newWizardDropDownAction.setEnabled(value);
        // Bug 53560.  "Ctrl+N" shouldn't work if the menus are all disabled.
        newWizardAction.setEnabled(value);
        importResourcesAction.setEnabled(value);
        exportResourcesAction.setEnabled(value);
    }

    public void fillActionBars(int flags) {
        super.fillActionBars(flags);
        if ((flags & FILL_PROXY) == 0) {
            updateBuildActions();
            hookListeners();
        }
    }

    /**
     * Fills the coolbar with the workbench actions.
     */
    protected void fillCoolBar(ICoolBarManager coolBar) {

        { // Set up the context Menu
            IMenuManager popUpMenu = new MenuManager();
            popUpMenu.add(new ActionContributionItem(lockToolBarAction));
            popUpMenu.add(new ActionContributionItem(editActionSetAction));
            coolBar.setContextMenuManager(popUpMenu);
        }
        coolBar.add(new GroupMarker(IIDEActionConstants.GROUP_FILE));
        { // File Group
            IToolBarManager fileToolBar = new ToolBarManager(coolBar
                    .getStyle());
            fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
            fileToolBar.add(newWizardDropDownAction);
            fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
            fileToolBar.add(new GroupMarker(
                    IWorkbenchActionConstants.SAVE_GROUP));
            fileToolBar.add(saveAction);
            fileToolBar
                    .add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
            fileToolBar.add(printAction);
            fileToolBar
                    .add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));

            fileToolBar
                    .add(new Separator(IWorkbenchActionConstants.BUILD_GROUP));
            fileToolBar
                    .add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
            fileToolBar.add(new Separator(
                    IWorkbenchActionConstants.MB_ADDITIONS));

            // Add to the cool bar manager
            coolBar.add(new ToolBarContributionItem(fileToolBar,
                    IWorkbenchActionConstants.TOOLBAR_FILE));
        }

        coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

        coolBar.add(new GroupMarker(IIDEActionConstants.GROUP_NAV));
        { // Navigate group
            IToolBarManager navToolBar = new ToolBarManager(coolBar
                    .getStyle());
            navToolBar.add(new Separator(
                    IWorkbenchActionConstants.HISTORY_GROUP));
            navToolBar
                    .add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
            navToolBar.add(backwardHistoryAction);
            navToolBar.add(forwardHistoryAction);
            navToolBar.add(new Separator(IWorkbenchActionConstants.PIN_GROUP));
            navToolBar.add(pinEditorContributionItem);

            // Add to the cool bar manager
            coolBar.add(new ToolBarContributionItem(navToolBar,
                    IWorkbenchActionConstants.TOOLBAR_NAVIGATE));
        }

        coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));

    }

    /**
     * Fills the menu bar with the workbench actions.
     */
    protected void fillMenuBar(IMenuManager menuBar) {
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createNavigateMenu());
        menuBar.add(createProjectMenu());
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(createWindowMenu());
        menuBar.add(createHelpMenu());
    }

    /**
     * Creates and returns the File menu.
     */
    private MenuManager createFileMenu() {
        MenuManager menu = new MenuManager(IDEWorkbenchMessages
                .getString("Workbench.file"), IWorkbenchActionConstants.M_FILE); //$NON-NLS-1$
        menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
        {
            // create the New submenu, using the same id for it as the New action
            String newText = IDEWorkbenchMessages.getString("Workbench.new"); //$NON-NLS-1$
            String newId = ActionFactory.NEW.getId();
            MenuManager newMenu = new MenuManager(newText, newId) {
                public String getMenuText() {
                    String result = super.getMenuText();
                    if (newQuickMenu == null)
                        return result;
                    String shortCut = newQuickMenu.getShortCutString();
                    if (shortCut == null)
                        return result;
                    return result + "\t" + shortCut; //$NON-NLS-1$
                }
            };
            newMenu.add(new Separator(newId));
            this.newWizardMenu = new NewWizardMenu(getWindow());
            newMenu.add(this.newWizardMenu);
            newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            menu.add(newMenu);
        }

        menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
        menu.add(new Separator());

        menu.add(closeAction);
        menu.add(closeAllAction);
        //		menu.add(closeAllSavedAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
        menu.add(new Separator());
        menu.add(saveAction);
        menu.add(saveAsAction);
        menu.add(saveAllAction);

        menu.add(revertAction);
        menu.add(new Separator());
        menu.add(moveAction);
        menu.add(renameAction);
        menu.add(refreshAction);

        menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
        menu.add(new Separator());
        menu.add(printAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
        menu.add(new Separator());
        menu.add(openWorkspaceAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
        menu.add(new Separator());
        menu.add(importResourcesAction);
        menu.add(exportResourcesAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        menu.add(new Separator());
        menu.add(propertiesAction);

        menu.add(ContributionItemFactory.REOPEN_EDITORS.create(getWindow()));
        menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
        menu.add(new Separator());
        menu.add(quitAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
        return menu;
    }

    /**
     * Creates and returns the Edit menu.
     */
    private MenuManager createEditMenu() {
        MenuManager menu = new MenuManager(IDEWorkbenchMessages
                .getString("Workbench.edit"), IWorkbenchActionConstants.M_EDIT); //$NON-NLS-1$
        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

        menu.add(undoAction);
        menu.add(redoAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
        menu.add(new Separator());

        menu.add(cutAction);
        menu.add(copyAction);
        menu.add(pasteAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
        menu.add(new Separator());

        menu.add(deleteAction);
        menu.add(selectAllAction);
        menu.add(new Separator());

        menu.add(findAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
        menu.add(new Separator());

        menu.add(addBookmarkAction);
        menu.add(addTaskAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));

        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        return menu;
    }

    /**
     * Creates and returns the Navigate menu.
     */
    private MenuManager createNavigateMenu() {
        MenuManager menu = new MenuManager(
                IDEWorkbenchMessages.getString("Workbench.navigate"), IWorkbenchActionConstants.M_NAVIGATE); //$NON-NLS-1$
        menu.add(new GroupMarker(IWorkbenchActionConstants.NAV_START));
        menu.add(goIntoAction);

        MenuManager goToSubMenu = new MenuManager(IDEWorkbenchMessages
                .getString("Workbench.goTo"), IWorkbenchActionConstants.GO_TO); //$NON-NLS-1$
        menu.add(goToSubMenu);
        goToSubMenu.add(backAction);
        goToSubMenu.add(forwardAction);
        goToSubMenu.add(upAction);
        goToSubMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        menu.add(new Separator(IWorkbenchActionConstants.OPEN_EXT));
        for (int i = 2; i < 5; ++i) {
            menu.add(new Separator(IWorkbenchActionConstants.OPEN_EXT + i));
        }
        menu.add(new Separator(IWorkbenchActionConstants.SHOW_EXT));
        {

            MenuManager showInSubMenu = new MenuManager(IDEWorkbenchMessages
                    .getString("Workbench.showIn"), "showIn") { //$NON-NLS-1$ //$NON-NLS-2$
                public String getMenuText() {
                    String result = super.getMenuText();
                    if (showInQuickMenu == null)
                        return null;
                    String shortCut = showInQuickMenu.getShortCutString();
                    if (shortCut == null)
                        return result;
                    return result + "\t" + shortCut; //$NON-NLS-1$
                }
            };
            showInSubMenu.add(ContributionItemFactory.VIEWS_SHOW_IN
                    .create(getWindow()));
            menu.add(showInSubMenu);
        }
        for (int i = 2; i < 5; ++i) {
            menu.add(new Separator(IWorkbenchActionConstants.SHOW_EXT + i));
        }
        menu.add(new Separator());
        menu.add(nextAction);
        menu.add(previousAction);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new GroupMarker(IWorkbenchActionConstants.NAV_END));

        //TBD: Location of this actions
        menu.add(new Separator());
        menu.add(backwardHistoryAction);
        menu.add(forwardHistoryAction);
        return menu;
    }

    /**
     * Creates and returns the Project menu.
     */
    private MenuManager createProjectMenu() {
        MenuManager menu = new MenuManager(
                IDEWorkbenchMessages.getString("Workbench.project"), IWorkbenchActionConstants.M_PROJECT); //$NON-NLS-1$
        menu.add(new Separator(IWorkbenchActionConstants.PROJ_START));

        menu.add(openProjectAction);
        menu.add(closeProjectAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
        menu.add(new Separator());
        menu.add(buildAllAction);
        menu.add(buildProjectAction);
        addWorkingSetBuildActions(menu);
        menu.add(cleanAction);
        menu.add(toggleAutoBuildAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
        menu.add(new Separator());

        menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new GroupMarker(IWorkbenchActionConstants.PROJ_END));
        menu.add(new Separator());
        menu.add(projectPropertyDialogAction);
        return menu;
    }

    /**
     * Creates and returns the Window menu.
     */
    private MenuManager createWindowMenu() {
        MenuManager menu = new MenuManager(
                IDEWorkbenchMessages.getString("Workbench.window"), IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$

        IWorkbenchAction action = ActionFactory.OPEN_NEW_WINDOW
                .create(getWindow());
        action.setText(IDEWorkbenchMessages
                .getString("Workbench.openNewWindow")); //$NON-NLS-1$
        menu.add(action);
        menu.add(new Separator());
        addPerspectiveActions(menu);
        menu.add(new Separator());
        addKeyboardShortcuts(menu);
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "end")); //$NON-NLS-1$
        menu.add(openPreferencesAction);

        menu.add(ContributionItemFactory.OPEN_WINDOWS.create(getWindow()));
        return menu;
    }

    /**
     * Adds the perspective actions to the specified menu.
     */
    private void addPerspectiveActions(MenuManager menu) {
        {
            String openText = IDEWorkbenchMessages
                    .getString("Workbench.openPerspective"); //$NON-NLS-1$
            MenuManager changePerspMenuMgr = new MenuManager(openText,
                    "openPerspective"); //$NON-NLS-1$
            IContributionItem changePerspMenuItem = ContributionItemFactory.PERSPECTIVES_SHORTLIST
                    .create(getWindow());
            changePerspMenuMgr.add(changePerspMenuItem);
            menu.add(changePerspMenuMgr);
        }
        {
            MenuManager showViewMenuMgr = new MenuManager(IDEWorkbenchMessages
                    .getString("Workbench.showView"), "showView"); //$NON-NLS-1$ //$NON-NLS-2$
            IContributionItem showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST
                    .create(getWindow());
            showViewMenuMgr.add(showViewMenu);
            menu.add(showViewMenuMgr);
        }
        menu.add(new Separator());
        menu.add(editActionSetAction);
        menu.add(savePerspectiveAction);
        menu.add(resetPerspectiveAction);
        menu.add(closePerspAction);
        menu.add(closeAllPerspsAction);
    }

    /**
     * Adds the keyboard navigation submenu to the specified menu.
     */
    private void addWorkingSetBuildActions(MenuManager menu) {
        buildWorkingSetMenu = new MenuManager(IDEWorkbenchMessages
                .getString("Workbench.buildSet")); //$NON-NLS-1$
        IContributionItem workingSetBuilds = new BuildSetMenu(window,
                getActionBarConfigurer());
        buildWorkingSetMenu.add(workingSetBuilds);
        menu.add(buildWorkingSetMenu);
    }

    /**
     * Adds the keyboard navigation submenu to the specified menu.
     */
    private void addKeyboardShortcuts(MenuManager menu) {
        MenuManager subMenu = new MenuManager(IDEWorkbenchMessages
                .getString("Workbench.shortcuts"), "shortcuts"); //$NON-NLS-1$ //$NON-NLS-2$
        menu.add(subMenu);
        subMenu.add(showPartPaneMenuAction);
        subMenu.add(showViewMenuAction);
        subMenu.add(new Separator());
        subMenu.add(maximizePartAction);
        subMenu.add(minimizePartAction);
        subMenu.add(new Separator());
        subMenu.add(activateEditorAction);
        subMenu.add(nextEditorAction);
        subMenu.add(prevEditorAction);
        subMenu.add(workbookEditorsAction);
        subMenu.add(new Separator());
        subMenu.add(nextPartAction);
        subMenu.add(prevPartAction);
        subMenu.add(new Separator());
        subMenu.add(nextPerspectiveAction);
        subMenu.add(prevPerspectiveAction);
    }

    /**
	 * Creates and returns the Help menu.
	 */
	private MenuManager createHelpMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages
				.getString("Workbench.help"), IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.intro"); //$NON-NLS-1$
		// See if a welcome or intro page is specified
		if (introAction != null)
			menu.add(introAction);
		else if (quickStartAction != null)
			menu.add(quickStartAction);
		menu.add(new GroupMarker("group.intro.ext")); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.main"); //$NON-NLS-1$
		menu.add(helpContentsAction);

		// See if a tips and tricks page is specified
		if (tipsAndTricksAction != null)
			menu.add(tipsAndTricksAction);
		// HELP_START should really be the first item, but it was after
		// quickStartAction and tipsAndTricksAction in 2.1.
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new GroupMarker("group.main.ext")); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.tutorials"); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.tools"); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.updates"); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		addSeparatorOrGroupMarker(menu, IWorkbenchActionConstants.MB_ADDITIONS);
		// about should always be at the bottom
		menu.add(new Separator("group.about")); //$NON-NLS-1$
		menu.add(aboutAction);
		menu.add(new GroupMarker("group.about.ext")); //$NON-NLS-1$

        /*		
         final IMutableContextActivationService contextActivationServiceA = ContextActivationServiceFactory.getMutableContextActivationService();
         contextActivationServiceA.setActiveContextIds(new HashSet(Collections.singletonList("A")));

         final IMutableContextActivationService contextActivationServiceB = ContextActivationServiceFactory.getMutableContextActivationService();
         contextActivationServiceB.setActiveContextIds(new HashSet(Collections.singletonList("B")));				
         
         menu.add(new Separator());
         
         menu.add(new Action("Add context A to the workbench") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchContextSupport workbenchContextSupport = (IWorkbenchContextSupport) workbench.getContextSupport();
         workbenchContextSupport.getCompoundContextActivationService().addContextActivationService(contextActivationServiceA);
         }
         });

         menu.add(new Action("Remove context A from the workbench") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchContextSupport workbenchContextSupport = (IWorkbenchContextSupport) workbench.getContextSupport();
         workbenchContextSupport.getCompoundContextActivationService().removeContextActivationService(contextActivationServiceA);
         }
         });
         
         menu.add(new Action("Add context B to the workbench") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchContextSupport workbenchContextSupport = (IWorkbenchContextSupport) workbench.getContextSupport();
         workbenchContextSupport.getCompoundContextActivationService().addContextActivationService(contextActivationServiceB);
         }
         });

         menu.add(new Action("Remove context B from the workbench") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchContextSupport workbenchContextSupport = (IWorkbenchContextSupport) workbench.getContextSupport();
         workbenchContextSupport.getCompoundContextActivationService().removeContextActivationService(contextActivationServiceB);
         }
         });
         
         menu.add(new Separator());
         
         menu.add(new Action("Add context A to the workbench page") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

         if (workbenchWindow != null) {
         IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
         
         if (workbenchPage != null) {					
         IWorkbenchPageContextSupport workbenchPageContextSupport = (IWorkbenchPageContextSupport) workbenchPage.getContextSupport();
         workbenchPageContextSupport.getCompoundContextActivationService().addContextActivationService(contextActivationServiceA);
         }
         }
         }
         });
         
         menu.add(new Action("Remove context A from the workbench page") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

         if (workbenchWindow != null) {
         IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
         
         if (workbenchPage != null) {					
         IWorkbenchPageContextSupport workbenchPageContextSupport = (IWorkbenchPageContextSupport) workbenchPage.getContextSupport();
         workbenchPageContextSupport.getCompoundContextActivationService().removeContextActivationService(contextActivationServiceA);
         }
         }
         }
         });		
         
         menu.add(new Action("Add context B to the workbench page") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

         if (workbenchWindow != null) {
         IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
         
         if (workbenchPage != null) {					
         IWorkbenchPageContextSupport workbenchPageContextSupport = (IWorkbenchPageContextSupport) workbenchPage.getContextSupport();
         workbenchPageContextSupport.getCompoundContextActivationService().addContextActivationService(contextActivationServiceB);
         }
         }
         }
         });

         menu.add(new Action("Remove context B from the workbench page") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

         if (workbenchWindow != null) {
         IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
         
         if (workbenchPage != null) {					
         IWorkbenchPageContextSupport workbenchPageContextSupport = (IWorkbenchPageContextSupport) workbenchPage.getContextSupport();
         workbenchPageContextSupport.getCompoundContextActivationService().removeContextActivationService(contextActivationServiceB);
         }
         }
         }
         });
         
         IHandler handlerA = new IHandler() {
         public void execute() {
         }
         
         public void execute(Event event) {
         }
         
         public boolean isEnabled() {
         return false;
         }
         };
         
         IHandler handlerB = new IHandler() {
         public void execute() {
         }
         
         public void execute(Event event) {
         }
         
         public boolean isEnabled() {
         return false;
         }
         };		
         
         final IMutableCommandHandlerService commandHandlerServiceA = CommandHandlerServiceFactory.getMutableCommandHandlerService();
         commandHandlerServiceA.setHandlersByCommandId(new HashMap(Collections.singletonMap("command", handlerA)));

         final IMutableCommandHandlerService commandHandlerServiceB = CommandHandlerServiceFactory.getMutableCommandHandlerService();
         commandHandlerServiceB.setHandlersByCommandId(new HashMap(Collections.singletonMap("command", handlerB)));				
         
         menu.add(new Separator());
         
         menu.add(new Action("Add handler A to the workbench") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchCommandSupport workbenchCommandSupport = (IWorkbenchCommandSupport) workbench.getCommandSupport();
         workbenchCommandSupport.getCompoundCommandHandlerService().addCommandHandlerService(commandHandlerServiceA);
         }
         });

         menu.add(new Action("Remove handler A from the workbench") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchCommandSupport workbenchCommandSupport = (IWorkbenchCommandSupport) workbench.getCommandSupport();
         workbenchCommandSupport.getCompoundCommandHandlerService().removeCommandHandlerService(commandHandlerServiceA);
         }
         });
         
         menu.add(new Action("Add handler B to the workbench") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchCommandSupport workbenchCommandSupport = (IWorkbenchCommandSupport) workbench.getCommandSupport();
         workbenchCommandSupport.getCompoundCommandHandlerService().addCommandHandlerService(commandHandlerServiceB);
         }
         });

         menu.add(new Action("Remove handler B from the workbench") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchCommandSupport workbenchCommandSupport = (IWorkbenchCommandSupport) workbench.getCommandSupport();
         workbenchCommandSupport.getCompoundCommandHandlerService().removeCommandHandlerService(commandHandlerServiceB);
         }
         });

         menu.add(new Separator());
         
         menu.add(new Action("Add handler A to the workbench page") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

         if (workbenchWindow != null) {
         IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
         
         if (workbenchPage != null) {					
         IWorkbenchPageCommandSupport workbenchPageCommandSupport = (IWorkbenchPageCommandSupport) workbenchPage.getCommandSupport();
         workbenchPageCommandSupport.getCompoundCommandHandlerService().addCommandHandlerService(commandHandlerServiceA);
         }
         }
         }
         });

         menu.add(new Action("Remove handler A from the workbench page") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

         if (workbenchWindow != null) {
         IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
         
         if (workbenchPage != null) {					
         IWorkbenchPageCommandSupport workbenchPageCommandSupport = (IWorkbenchPageCommandSupport) workbenchPage.getCommandSupport();
         workbenchPageCommandSupport.getCompoundCommandHandlerService().removeCommandHandlerService(commandHandlerServiceA);
         }
         }
         }
         });
         
         menu.add(new Action("Add handler B to the workbench page") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

         if (workbenchWindow != null) {
         IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
         
         if (workbenchPage != null) {					
         IWorkbenchPageCommandSupport workbenchPageCommandSupport = (IWorkbenchPageCommandSupport) workbenchPage.getCommandSupport();
         workbenchPageCommandSupport.getCompoundCommandHandlerService().addCommandHandlerService(commandHandlerServiceB);
         }
         }
         }
         });

         menu.add(new Action("Remove handler B from the workbench page") {
         public void run() {
         IWorkbench workbench = PlatformUI.getWorkbench();
         IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

         if (workbenchWindow != null) {
         IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
         
         if (workbenchPage != null) {					
         IWorkbenchPageCommandSupport workbenchPageCommandSupport = (IWorkbenchPageCommandSupport) workbenchPage.getCommandSupport();
         workbenchPageCommandSupport.getCompoundCommandHandlerService().removeCommandHandlerService(commandHandlerServiceB);
         }
         }
         }
         });
         */

        return menu;
    }

    /**
	 * Adds a <code>GroupMarker</code> or <code>Separator</code> to a menu.
	 * The test for whether a separator should be added is done by checking for
	 * the existence of a preference matching the string
	 * useSeparator.MENUID.GROUPID that is set to <code>true</code>.
	 * 
	 * @param menu
	 *            the menu to add to
	 * @param groupId
	 *            the group id for the added separator or group marker
	 */
	private void addSeparatorOrGroupMarker(MenuManager menu, String groupId) {
		String prefId = "useSeparator." + menu.getId() + "." + groupId; //$NON-NLS-1$ //$NON-NLS-2$
		boolean addExtraSeparators = IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore().getBoolean(prefId);
		if (addExtraSeparators) {
			menu.add(new Separator(groupId));
		} else {
			menu.add(new GroupMarker(groupId));
		}
	}
    
    /**
     * Disposes any resources and unhooks any listeners that are no longer needed.
     * Called when the window is closed.
     */
    public void dispose() {
        if (isDisposed)
            return;
    	isDisposed = true;
        getActionBarConfigurer().getStatusLineManager().remove(statusLineItem);
        if (pageListener != null) {
            window.removePageListener(pageListener);
            pageListener = null;
        }
        if (perspectiveListener != null) {
            window.removePerspectiveListener(perspectiveListener);
            perspectiveListener = null;
        }
        if (prefListener != null) {
            ResourcesPlugin.getPlugin().getPluginPreferences()
                    .removePropertyChangeListener(prefListener);
            prefListener = null;
        }
        if (propPrefListener != null) {
            WorkbenchPlugin.getDefault().getPreferenceStore()
                    .removePropertyChangeListener(propPrefListener);
            propPrefListener = null;
        }
        if (resourceListener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
            resourceListener = null;
        }

        pinEditorContributionItem.dispose();
        showInQuickMenu.dispose();
        newQuickMenu.dispose();
        
        // null out actions to make leak debugging easier
        closeAction = null;
        closeAllAction = null;
        closeAllSavedAction = null;
        saveAction = null;
        saveAllAction = null;
        helpContentsAction = null;
        aboutAction = null;
        openPreferencesAction = null;
        saveAsAction = null;
        hideShowEditorAction = null;
        savePerspectiveAction = null;
        resetPerspectiveAction = null;
        editActionSetAction = null;
        closePerspAction = null;
        lockToolBarAction = null;
        closeAllPerspsAction = null;
        showViewMenuAction = null;
        showPartPaneMenuAction = null;
        nextPartAction = null;
        prevPartAction = null;
        nextEditorAction = null;
        prevEditorAction = null;
        nextPerspectiveAction = null;
        prevPerspectiveAction = null;
        activateEditorAction = null;
        maximizePartAction = null;
        minimizePartAction = null;
        workbenchEditorsAction = null;
        workbookEditorsAction = null;
        backwardHistoryAction = null;
        forwardHistoryAction = null;
        undoAction = null;
        redoAction = null;
        cutAction = null;
        copyAction = null;
        pasteAction = null;
        deleteAction = null;
        selectAllAction = null;
        findAction = null;
        printAction = null;
        revertAction = null;
        refreshAction = null;
        propertiesAction = null;
        quitAction = null;
        moveAction = null;
        renameAction = null;
        goIntoAction = null;
        backAction = null;
        forwardAction = null;
        upAction = null;
        nextAction = null;
        previousAction = null;
        openWorkspaceAction = null;
        projectPropertyDialogAction = null;
        newWizardAction = null;
        newWizardDropDownAction = null;
        importResourcesAction = null;
        exportResourcesAction = null;
        buildAllAction = null;
        cleanAction = null;
        toggleAutoBuildAction = null;
        buildWorkingSetMenu = null;
        quickStartAction = null;
        tipsAndTricksAction = null;
        showInQuickMenu = null;
        newQuickMenu = null;
        addBookmarkAction = null;
        addTaskAction = null;
        buildProjectAction = null;
        openProjectAction = null;
        closeProjectAction = null;
        newWizardMenu = null;
        pinEditorContributionItem = null;
        statusLineItem = null;
        prefListener = null;
        propPrefListener = null;
        introAction = null;
        
        super.dispose();
    }

    void updateModeLine(final String text) {
        statusLineItem.setText(text);
    }

    /**
     * Returns true if the menu with the given ID should
     * be considered as an OLE container menu. Container menus
     * are preserved in OLE menu merging.
     */
    public boolean isApplicationMenu(String menuId) {
        if (menuId.equals(IWorkbenchActionConstants.M_FILE))
            return true;
        if (menuId.equals(IWorkbenchActionConstants.M_WINDOW))
            return true;
        return false;
    }

    /**
     * Return whether or not given id matches the id of the coolitems that
     * the workbench creates.
     */
    public boolean isWorkbenchCoolItemId(String id) {
        if (IWorkbenchActionConstants.TOOLBAR_FILE.equalsIgnoreCase(id))
            return true;
        if (IWorkbenchActionConstants.TOOLBAR_NAVIGATE.equalsIgnoreCase(id))
            return true;
        return false;
    }

    /**
     * Fills the status line with the workbench contribution items.
     */
    protected void fillStatusLine(IStatusLineManager statusLine) {
        statusLine.add(statusLineItem);
    }

    /**
     * Creates actions (and contribution items) for the menu bar, toolbar and status line.
     */
    protected void makeActions(IWorkbenchWindow window) {

        // @issue should obtain from ConfigurationItemFactory
        statusLineItem = new StatusLineContributionItem("ModeContributionItem"); //$NON-NLS-1$

        newWizardAction = ActionFactory.NEW.create(getWindow());
        register(newWizardAction);

        newWizardDropDownAction = IDEActionFactory.NEW_WIZARD_DROP_DOWN
                .create(getWindow());
        register(newWizardDropDownAction);

        importResourcesAction = ActionFactory.IMPORT.create(getWindow());
        register(importResourcesAction);

        exportResourcesAction = ActionFactory.EXPORT.create(getWindow());
        register(exportResourcesAction);

        buildAllAction = IDEActionFactory.BUILD.create(getWindow());
        register(buildAllAction);

        cleanAction = IDEActionFactory.BUILD_CLEAN.create(getWindow());
        register(cleanAction);

        toggleAutoBuildAction = IDEActionFactory.BUILD_AUTOMATICALLY
                .create(getWindow());
        register(toggleAutoBuildAction);

        saveAction = ActionFactory.SAVE.create(getWindow());
        register(saveAction);

        saveAsAction = ActionFactory.SAVE_AS.create(getWindow());
        register(saveAsAction);

        saveAllAction = ActionFactory.SAVE_ALL.create(getWindow());
        register(saveAllAction);

        undoAction = ActionFactory.UNDO.create(getWindow());
        register(undoAction);

        redoAction = ActionFactory.REDO.create(getWindow());
        register(redoAction);

        cutAction = ActionFactory.CUT.create(getWindow());
        register(cutAction);

        copyAction = ActionFactory.COPY.create(getWindow());
        register(copyAction);

        pasteAction = ActionFactory.PASTE.create(getWindow());
        register(pasteAction);

        printAction = ActionFactory.PRINT.create(getWindow());
        register(printAction);

        selectAllAction = ActionFactory.SELECT_ALL.create(getWindow());
        register(selectAllAction);

        findAction = ActionFactory.FIND.create(getWindow());
        register(findAction);

        closeAction = ActionFactory.CLOSE.create(getWindow());
        register(closeAction);

        closeAllAction = ActionFactory.CLOSE_ALL.create(getWindow());
        register(closeAllAction);

        closeAllSavedAction = ActionFactory.CLOSE_ALL_SAVED.create(getWindow());
        register(closeAllSavedAction);

        helpContentsAction = ActionFactory.HELP_CONTENTS.create(getWindow());
        register(helpContentsAction);

        aboutAction = ActionFactory.ABOUT.create(getWindow());
        aboutAction
                .setImageDescriptor(IDEInternalWorkbenchImages
                        .getImageDescriptor(IDEInternalWorkbenchImages.IMG_OBJS_DEFAULT_PROD));
        register(aboutAction);

        openPreferencesAction = ActionFactory.PREFERENCES.create(getWindow());
        register(openPreferencesAction);

        addBookmarkAction = IDEActionFactory.BOOKMARK.create(getWindow());
        register(addBookmarkAction);

        addTaskAction = IDEActionFactory.ADD_TASK.create(getWindow());
        register(addTaskAction);

        deleteAction = ActionFactory.DELETE.create(getWindow());
        register(deleteAction);

        AboutInfo[] infos = IDEWorkbenchPlugin.getDefault().getFeatureInfos();
        // See if a welcome page is specified
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getWelcomePageURL() != null) {
                quickStartAction = IDEActionFactory.QUICK_START
                        .create(getWindow());
                register(quickStartAction);
                break;
            }
        }
        // See if a tips and tricks page is specified
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getTipsAndTricksHref() != null) {
                tipsAndTricksAction = IDEActionFactory.TIPS_AND_TRICKS
                        .create(getWindow());
                register(tipsAndTricksAction);
                break;
            }
        }

        // Actions for invisible accelerators
        showViewMenuAction = ActionFactory.SHOW_VIEW_MENU.create(getWindow());
        register(showViewMenuAction);

        showPartPaneMenuAction = ActionFactory.SHOW_PART_PANE_MENU
                .create(getWindow());
        register(showPartPaneMenuAction);

        nextEditorAction = ActionFactory.NEXT_EDITOR.create(getWindow());
        register(nextEditorAction);
        prevEditorAction = ActionFactory.PREVIOUS_EDITOR.create(getWindow());
        register(prevEditorAction);
        ActionFactory.linkCycleActionPair(nextEditorAction, prevEditorAction);

        nextPartAction = ActionFactory.NEXT_PART.create(getWindow());
        register(nextPartAction);
        prevPartAction = ActionFactory.PREVIOUS_PART.create(getWindow());
        register(prevPartAction);
        ActionFactory.linkCycleActionPair(nextPartAction, prevPartAction);

        nextPerspectiveAction = ActionFactory.NEXT_PERSPECTIVE
                .create(getWindow());
        register(nextPerspectiveAction);
        prevPerspectiveAction = ActionFactory.PREVIOUS_PERSPECTIVE
                .create(getWindow());
        register(prevPerspectiveAction);
        ActionFactory.linkCycleActionPair(nextPerspectiveAction,
                prevPerspectiveAction);

        activateEditorAction = ActionFactory.ACTIVATE_EDITOR
                .create(getWindow());
        register(activateEditorAction);

        maximizePartAction = ActionFactory.MAXIMIZE.create(getWindow());
        register(maximizePartAction);

		minimizePartAction = ActionFactory.MINIMIZE.create(getWindow());
		register(minimizePartAction);
        
        workbenchEditorsAction = ActionFactory.SHOW_OPEN_EDITORS
                .create(getWindow());
        register(workbenchEditorsAction);

        workbookEditorsAction = ActionFactory.SHOW_WORKBOOK_EDITORS
                .create(getWindow());
        register(workbookEditorsAction);

        hideShowEditorAction = ActionFactory.SHOW_EDITOR.create(getWindow());
        register(hideShowEditorAction);
        savePerspectiveAction = ActionFactory.SAVE_PERSPECTIVE
                .create(getWindow());
        register(savePerspectiveAction);
        editActionSetAction = ActionFactory.EDIT_ACTION_SETS
                .create(getWindow());
        register(editActionSetAction);
        lockToolBarAction = ActionFactory.LOCK_TOOL_BAR.create(getWindow());
        register(lockToolBarAction);
        resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE
                .create(getWindow());
        register(resetPerspectiveAction);
        closePerspAction = ActionFactory.CLOSE_PERSPECTIVE.create(getWindow());
        register(closePerspAction);
        closeAllPerspsAction = ActionFactory.CLOSE_ALL_PERSPECTIVES
                .create(getWindow());
        register(closeAllPerspsAction);

        forwardHistoryAction = ActionFactory.FORWARD_HISTORY
                .create(getWindow());
        register(forwardHistoryAction);

        backwardHistoryAction = ActionFactory.BACKWARD_HISTORY
                .create(getWindow());
        register(backwardHistoryAction);

        revertAction = ActionFactory.REVERT.create(getWindow());
        register(revertAction);

        refreshAction = ActionFactory.REFRESH.create(getWindow());
        register(refreshAction);

        propertiesAction = ActionFactory.PROPERTIES.create(getWindow());
        register(propertiesAction);

        quitAction = ActionFactory.QUIT.create(getWindow());
        register(quitAction);

        moveAction = ActionFactory.MOVE.create(getWindow());
        register(moveAction);

        renameAction = ActionFactory.RENAME.create(getWindow());
        register(renameAction);

        goIntoAction = ActionFactory.GO_INTO.create(getWindow());
        register(goIntoAction);

        backAction = ActionFactory.BACK.create(getWindow());
        register(backAction);

        forwardAction = ActionFactory.FORWARD.create(getWindow());
        register(forwardAction);

        upAction = ActionFactory.UP.create(getWindow());
        register(upAction);

        nextAction = ActionFactory.NEXT.create(getWindow());
        nextAction
                .setImageDescriptor(IDEInternalWorkbenchImages
                        .getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_NEXT_NAV));
        register(nextAction);

        previousAction = ActionFactory.PREVIOUS.create(getWindow());
        previousAction
                .setImageDescriptor(IDEInternalWorkbenchImages
                        .getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_PREVIOUS_NAV));
        register(previousAction);

        buildProjectAction = IDEActionFactory.BUILD_PROJECT.create(getWindow());
        register(buildProjectAction);

        openProjectAction = IDEActionFactory.OPEN_PROJECT.create(getWindow());
        register(openProjectAction);

        closeProjectAction = IDEActionFactory.CLOSE_PROJECT.create(getWindow());
        register(closeProjectAction);

        openWorkspaceAction = IDEActionFactory.OPEN_WORKSPACE
                .create(getWindow());
        register(openWorkspaceAction);

        projectPropertyDialogAction = IDEActionFactory.OPEN_PROJECT_PROPERTIES
                .create(getWindow());
        register(projectPropertyDialogAction);

        if (getWindow().getWorkbench().getIntroManager().hasIntro()) {
            introAction = ActionFactory.INTRO.create(window);
            register(introAction);
        }

        String showInQuickMenuId = "org.eclipse.ui.navigate.showInQuickMenu"; //$NON-NLS-1$
        showInQuickMenu = new QuickMenuAction(showInQuickMenuId) {
            protected void fillMenu(IMenuManager menu) {
                menu.add(ContributionItemFactory.VIEWS_SHOW_IN
                        .create(getWindow()));
            }
        };
        register(showInQuickMenu);

        final String newQuickMenuId = "org.eclipse.ui.file.newQuickMenu"; //$NON-NLS-1$
        newQuickMenu = new QuickMenuAction(newQuickMenuId) {
            protected void fillMenu(IMenuManager menu) {
                menu.add(new NewWizardMenu(getWindow()));
            }
        };
        register(newQuickMenu);

        pinEditorContributionItem = ContributionItemFactory.PIN_EDITOR
                .create(getWindow());
    }

    /**
     * Update the build actions on the toolbar and menu bar based on the 
     * current state of autobuild.  This method can be called from any thread.
     */
    void updateBuildActions() {
        // this can be triggered by property or resource change notifications
        Runnable update = new Runnable() {
            public void run() {
                if (isDisposed)
                	return;
		    	IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IProject[] projects = workspace.getRoot().getProjects();
		    	boolean enabled = BuildUtilities.isEnabled(projects, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		        //update menu bar actions in project menu
		        buildAllAction.setEnabled(enabled);
		        buildProjectAction.setEnabled(enabled);
		        toggleAutoBuildAction.setChecked(workspace.isAutoBuilding());
		        cleanAction.setEnabled(BuildUtilities.isEnabled(projects, IncrementalProjectBuilder.CLEAN_BUILD));
		
		        //update the cool bar build button
		        ICoolBarManager coolBarManager = getActionBarConfigurer()
		                .getCoolBarManager();
		        IContributionItem cbItem = coolBarManager
		                .find(IWorkbenchActionConstants.TOOLBAR_FILE);
		        if (!(cbItem instanceof ToolBarContributionItem)) {
		            // This should not happen
		            IDEWorkbenchPlugin.log("File toolbar contribution item is missing"); //$NON-NLS-1$
		            return;
		        }
		        ToolBarContributionItem toolBarItem = (ToolBarContributionItem) cbItem;
		        IToolBarManager toolBarManager = toolBarItem.getToolBarManager();
		        if (toolBarManager == null) {
		            // error if this happens, file toolbar assumed to always exist
		            IDEWorkbenchPlugin.log("File toolbar is missing"); //$NON-NLS-1$
		            return;
		        }
		        //add the build button if build actions are enabled, and remove it otherwise
		        boolean found = toolBarManager.find(buildAllAction.getId()) != null;
		        if (enabled && !found) {
		            toolBarManager.appendToGroup(IWorkbenchActionConstants.BUILD_GROUP,
		                    buildAllAction);
		            toolBarManager.update(false);
		            toolBarItem.update(ICoolBarManager.SIZE);
		        } else if (buildAllAction != null && found && !enabled) {
		            toolBarManager.remove(buildAllAction.getId());
		            toolBarManager.update(false);
		            toolBarItem.update(ICoolBarManager.SIZE);
		        }
            }
        };
        //run the update immediately if we are in the UI thread
        if (Display.getCurrent() != null) {
        	update.run();
        } else {
	        //dispatch the update to the UI thread
	        Shell shell = window.getShell();
	        if (shell != null && !shell.isDisposed())
	        	shell.getDisplay().asyncExec(update);
        }
    }

	/**
     * Update the pin action's tool bar
     */
    void updatePinActionToolbar() {

        ICoolBarManager coolBarManager = getActionBarConfigurer()
                .getCoolBarManager();
        IContributionItem cbItem = coolBarManager
                .find(IWorkbenchActionConstants.TOOLBAR_NAVIGATE);
        if (!(cbItem instanceof ToolBarContributionItem)) {
            // This should not happen
            IDEWorkbenchPlugin
                    .log("Navigation toolbar contribution item is missing"); //$NON-NLS-1$
            return;
        }
        ToolBarContributionItem toolBarItem = (ToolBarContributionItem) cbItem;
        IToolBarManager toolBarManager = toolBarItem.getToolBarManager();
        if (toolBarManager == null) {
            // error if this happens, navigation toolbar assumed to always exist
            IDEWorkbenchPlugin.log("Navigate toolbar is missing"); //$NON-NLS-1$
            return;
        }

        toolBarManager.update(false);
        toolBarItem.update(ICoolBarManager.SIZE);
    }
}