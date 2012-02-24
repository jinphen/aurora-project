package aurora.ide.meta.gef.editors.source.gen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.undo.CreateFileOperation;
import org.eclipse.ui.ide.undo.ResourceDescription;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import aurora.ide.meta.exception.ResourceNotFoundException;
import aurora.ide.meta.exception.TemplateNotBindedException;
import aurora.ide.meta.gef.FileFinder;
import aurora.ide.meta.gef.editors.models.ViewDiagram;
import aurora.ide.meta.gef.editors.models.io.ModelIOManager;
import aurora.ide.meta.project.AuroraMetaProject;
import aurora.ide.search.core.Message;
import aurora.ide.search.core.Util;
import aurora.ide.search.ui.MessageFormater;

public class ProjectGenerator {
	private IProject project;
	private boolean isOverlap;
	private int fNumberOfFilesToScan;
	private IFile fCurrentFile;
	private int fNumberOfScannedFiles;
	private Shell shell;
	private IProject auroraProject;
	private IFolder screenFolder;
	private IFolder auroraWebFolder;
	private String errorMessage;

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public ProjectGenerator(IProject project, boolean isOverlap, Shell shell) {
		super();
		this.project = project;
		this.isOverlap = isOverlap;
		this.shell = shell;
	}

	public boolean isOverlap() {
		return isOverlap;
	}

	public void setOverlap(boolean isOverlap) {
		this.isOverlap = isOverlap;
	}

	public void go(final IProgressMonitor monitor)
			throws InvocationTargetException {

		boolean validate = validate();
		if (validate == false)
			return;

		FileFinder fileFinder = new FileFinder();
		try {
			project.accept(fileFinder);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		List<IResource> files = fileFinder.getResult();
		fNumberOfFilesToScan = files.size();
		Job monitorUpdateJob = new Job("source generator") {
			private int fLastNumberOfScannedFiles = 0;

			public IStatus run(final IProgressMonitor inner) {
				while (!inner.isCanceled()) {
					final IFile file = fCurrentFile;
					if (file != null) {
						updateMonitor(monitor, file);
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						return Status.OK_STATUS;
					}
				}
				return Status.OK_STATUS;
			}

			private void updateMonitor(final IProgressMonitor monitor,
					final IFile file) {
				String fileName = file.getName();
				final Object[] args = { fileName,
						new Integer(fNumberOfScannedFiles),
						new Integer(fNumberOfFilesToScan) };
				monitor.subTask(MessageFormater.format(Message._scanning, args));
				int steps = fNumberOfScannedFiles - fLastNumberOfScannedFiles;
				monitor.worked(steps);
				fLastNumberOfScannedFiles += steps;
			}

		};

		monitor.beginTask("生成代码 ", files.size());
		monitorUpdateJob.setSystem(true);
		monitorUpdateJob.schedule();
		try {
			if (files != null) {
				for (int i = 0; i < files.size(); i++) {
					if (monitor.isCanceled())
						return;
					fCurrentFile = (IFile) files.get(i);
					fNumberOfScannedFiles++;
					processFile(fCurrentFile, monitor);
				}
			}
		} finally {
			monitorUpdateJob.cancel();
			monitor.done();
		}

	}

	public boolean validate() {
		auroraProject = this.getAuroraProject();
		screenFolder = this.getScreenFolder();
		auroraWebFolder = this.getAuroraWebFolder();
		if (auroraProject == null) {
			errorMessage = "未找到关联的Aurora工程";
			return false;
		}
		if (auroraWebFolder == null) {
			errorMessage = "未找到关联Aurora工程的web目录";
			return false;
		}
		if (screenFolder == null) {
			errorMessage = "未找到关联的原型文件存放的目录";
			return false;
		}
		return true;
	}

	private ViewDiagram loadFile(IFile file) {
		ViewDiagram diagram = null;
		InputStream is = null;
		try {
			is = file.getContents(false);

			CompositeLoader parser = new CompositeLoader();
			CompositeMap rootMap = parser.loadFromStream(is);
			ModelIOManager mim = ModelIOManager.getNewInstance();
			diagram = mim.fromCompositeMap(rootMap);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return diagram;
	}

	private void processFile(IFile fCurrentFile, IProgressMonitor monitor)
			throws InvocationTargetException {
		ViewDiagram loadFile = this.loadFile(fCurrentFile);
		ScreenGenerator sg = new ScreenGenerator(project);
		try {

			String genFile = sg.genFile(loadFile);
			InputStream is = new ByteArrayInputStream(genFile.getBytes());
			CreateFileOperation cfo = new CreateFileOperation(
					getNewFile(fCurrentFile), null, is, "create file.") {
				@Override
				protected void setResourceDescriptions(
						ResourceDescription[] descriptions) {
					super.setResourceDescriptions(descriptions);
				}

				public IStatus computeExecutionStatus(IProgressMonitor monitor) {
					IStatus status = super.computeExecutionStatus(monitor);
					if (status.isOK()) {
						// Overwrite is not allowed when we are creating a new
						// file
						status = computeCreateStatus(true);
					}
					return status;
				}
			};

			try {
				cfo.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(shell));
			} catch (ExecutionException e) {
				throw new InvocationTargetException(e);
			}

		} catch (TemplateNotBindedException e) {
		}
	}

	private IProject getAuroraProject() {
		AuroraMetaProject amp = new AuroraMetaProject(project);
		try {
			return amp.getAuroraProject();
		} catch (ResourceNotFoundException e) {
		}
		return null;
	}

	private IFolder getScreenFolder() {
		AuroraMetaProject amp = new AuroraMetaProject(project);
		try {
			return amp.getScreenFolder();
		} catch (ResourceNotFoundException e) {
		}
		return null;
	}

	private IFolder getAuroraWebFolder() {
		IContainer findWebInf = Util.findWebInf(auroraProject);
		return (IFolder) (findWebInf == null ? null : findWebInf.getParent());
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	private IFile getNewFile(IFile file) {
		IPath makeRelativeTo = file.getProjectRelativePath().makeRelativeTo(
				screenFolder.getProjectRelativePath());
		makeRelativeTo =makeRelativeTo.removeFileExtension();
		makeRelativeTo =makeRelativeTo.addFileExtension("screen");
		return auroraWebFolder.getFile(makeRelativeTo);
	}
}
