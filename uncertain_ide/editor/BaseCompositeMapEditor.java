package editor;

import helpers.ApplicationException;
import helpers.AuroraConstant;
import helpers.AuroraResourceUtil;
import helpers.DialogUtil;
import ide.AuroraPlugin;
import ide.AuroraProjectNature;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.xml.sax.SAXException;

import editor.textpage.TextPage;


import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;


public abstract class BaseCompositeMapEditor extends FormEditor {

	protected CompositeMapPage mainViewerPage ;
	protected TextPage textPage = new TextPage(this);
	private boolean dirty = false;
	private File file;
	protected int mainViewerIndex ;
	protected int textPageIndex;
	public BaseCompositeMapEditor() {
		super();
		this.mainViewerPage = initMainViewerPage();
	}
	public abstract CompositeMapPage initMainViewerPage();
	
	protected void addPages() {
		try {
			mainViewerIndex = addPage(mainViewerPage);
			textPageIndex = addPage(textPage,getEditorInput());
			setPageText(textPageIndex, TextPage.textPageTitle);
		} catch (PartInitException e) {
			DialogUtil.showExceptionMessageBox(e);
		}
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (!(input instanceof IFileEditorInput))
			throw new PartInitException(
					"Invalid Input: Must be IFileEditorInput");
		setSite(site);
		setInput(input);
		AuroraPlugin.getWorkspace().addResourceChangeListener(
				new InputFileListener(this));
		IFile ifile = ((IFileEditorInput) input).getFile();
		file = new File(AuroraResourceUtil.getIfileLocalPath(ifile));
		String fileName = file.getName();
		setPartName(fileName);
		// todo delete
		autoAddAuroraNatue(ifile);
		
	}
	private void autoAddAuroraNatue(IFile file){
		if(file.getName().toLowerCase().endsWith("."+AuroraConstant.BMFileExtension)||file.getName().toLowerCase().endsWith("."+AuroraConstant.ScreenFileExtension)){
			IProject project = file.getProject();
			try {
				if(!AuroraProjectNature.hasAuroraNature(project)){
					AuroraProjectNature.addAuroraNature(project);
				}
			} catch (CoreException e) {
				DialogUtil.showExceptionMessageBox(e);
			}
		}
	}

	public void doSave(IProgressMonitor monitor) {
		int currentPage = getCurrentPage();
		if(currentPage == textPageIndex){
			try {
				sycMainViewerPageWithTextPage();
			} catch (ApplicationException e) {
				DialogUtil.showExceptionMessageBox(e);
				return;
			}
		}else if(currentPage == mainViewerIndex){
			//ifile.refreshLocal will cause textChanged event,so prevent it;
			textPage.setSyc(true);
		}
		mainViewerPage.doSave(monitor);
		setDirty(false);
	}

	public void doSaveAs() {
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		super.editorDirtyStateChanged();
	}

	public boolean isDirty() {
		return dirty;
	}

	public File getFile() {
		return file;
	}
	public void editorDirtyStateChanged() {
		if(!dirty)
			setDirty(true);
	}
	
	protected void pageChange(int newPageIndex) {
		int currentPage = getCurrentPage();
		if(currentPage==textPageIndex){
			try {
				//setActivePage will call pageChage(),we should prevent dead lock.
				if(textPage.isIgnorceSycOnce()){
					textPage.setIgnorceSycOnce(false);
					return;
				}
				if(textPage.isModify()){
					sycMainViewerPageWithTextPage();
					textPage.setModify(false);
				}
			} catch (Exception e) {
				textPage.setIgnorceSycOnce(true);
				setActivePage(textPageIndex);
				DialogUtil.showExceptionMessageBox(e);
			}
		}
		super.pageChange(newPageIndex);
		if(currentPage==mainViewerIndex){
			sycTextPageWithMainViewerPage();
		} 
	}
	private boolean sycMainViewerPageWithTextPage() throws ApplicationException{
		CompositeLoader loader = AuroraResourceUtil.getCompsiteLoader();
		CompositeMap cm;
		try {
			cm = loader.loadFromString(textPage.getContent(),"UTF-8");
		} catch (IOException e) {
			throw new ApplicationException("文件路径错误",e);
		} catch (SAXException e) {
			throw new ApplicationException("文件解析错误",e);
		}
		mainViewerPage.setContent(cm);
		return true;
	}
	private boolean sycTextPageWithMainViewerPage(){
		textPage.refresh(mainViewerPage.getFullContent());
		return true;
	}

}