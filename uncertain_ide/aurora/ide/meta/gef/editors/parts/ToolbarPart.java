package aurora.ide.meta.gef.editors.parts;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import aurora.ide.meta.gef.editors.figures.ToolbarFigure;
import aurora.ide.meta.gef.editors.policies.DiagramLayoutEditPolicy;
import aurora.ide.meta.gef.editors.policies.NodeDirectEditPolicy;
import aurora.ide.meta.gef.editors.policies.NodeEditPolicy;

public class ToolbarPart extends ContainerPart {

	@Override
	protected IFigure createFigure() {
		Figure figure = new ToolbarFigure();

		return figure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new DiagramLayoutEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
				new NodeDirectEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new NodeEditPolicy());
	}

	protected void refreshVisuals() {
//		BOX model = (BOX) getModel();
//		BoxFigure figure = (ToolbarFigure) getFigure();
//		figure.setLabelWidth(model.getLabelWidth());
//		figure.setType(model.getType());
		
		super.refreshVisuals();

	}

}
