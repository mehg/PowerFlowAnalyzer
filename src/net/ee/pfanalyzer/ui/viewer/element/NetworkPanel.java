package net.ee.pfanalyzer.ui.viewer.element;

import net.ee.pfanalyzer.model.AbstractNetworkElement;
import net.ee.pfanalyzer.model.Branch;
import net.ee.pfanalyzer.model.Bus;
import net.ee.pfanalyzer.model.Network;
import net.ee.pfanalyzer.model.data.NetworkParameter;
import net.ee.pfanalyzer.model.util.ModelDBUtils;
import net.ee.pfanalyzer.ui.parameter.ParameterMasterNetwork;
import net.ee.pfanalyzer.ui.util.Group;

public class NetworkPanel extends AbstractElementPanel {

	private Network data;
	
	public NetworkPanel(ElementViewer viewer, Network data) {
		super(viewer);
		this.data = data;
		setParameterMaster(new ParameterMasterNetwork(data, false));
		setShowNetworkParameters(true);
		setTitle("Network Overview");
		updateNetwork();
	}
	
	public void updateNetwork() {
		removeAllElements();
		// add network parameters
		Group globalParameters = new Group("Global Network Parameters");
		for (NetworkParameter parameter : data.getParameterList()) {
			if(ModelDBUtils.isInternalScriptParameter(parameter.getID()))
				continue;
			NetworkParameter paramDef = ModelDBUtils.getParameterDefinition(
					data.getGlobalParameterClass(), parameter.getID());
			if(paramDef == null)
				paramDef = ModelDBUtils.findChildParameterDefinition(
						data.getScriptParameterClass(), parameter.getID());
			if(paramDef == null)
				paramDef = parameter; // fallback if parameter not defined in db
			addParameter(paramDef, parameter, globalParameters);
		}
		if(globalParameters.getComponentCount() > 0)
			addElementGroup(globalParameters);
		
		// add element groups
		addBusElements(data.getBusses());
		addElements(data.getElements("generator"), "Generators");
		addElements(data.getElements("load"), "Loads");
//		addElements(data.getElements("branch.transformer"), "Transformers");
		addBranchElements(data.getBranches(), data.getCombinedBusses());
		
		Group group = new Group("Bus Nodes (no location data available)");
		for (Bus bus: data.getBusses()) {
			boolean added = false;
			for (int i = 0; i < data.getCombinedBusCount(); i++) {
				if(data.getCombinedBus(i).contains(bus)) {
					added = true;
					break;
				}
			}
			if(added == false)
				group.addElementLink(bus, AbstractNetworkElement.DISPLAY_DEFAULT);
		}
		if(group.getComponentCount() > 0)
			addElementGroup(group);
		
		group = new Group("Branches (no location data available)");
		for (Branch branch: data.getBranches()) {
			boolean added = false;
			for (int i = 0; i < data.getCombinedBranchCount(); i++) {
				if(data.getCombinedBranch(i).contains(branch)) {
					added = true;
					break;
				}
			}
			if(added == false)
				group.addElementLink(branch, AbstractNetworkElement.DISPLAY_NAME);
		}
		if(group.getComponentCount() > 0)
			addElementGroup(group);
			
		finishLayout();
	}
}
//class LabelSorter implements Comparator<CombinedNetworkElement<?>> {
//	@Override
//	public int compare(CombinedNetworkElement<?> c1, CombinedNetworkElement<?> c2) {
//		return c1.getLabel().compareTo(c2.getLabel());
//	}
//}