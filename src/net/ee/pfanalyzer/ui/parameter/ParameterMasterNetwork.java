package net.ee.pfanalyzer.ui.parameter;

import net.ee.pfanalyzer.model.Network;
import net.ee.pfanalyzer.model.NetworkChangeEvent;
import net.ee.pfanalyzer.model.data.NetworkParameter;
import net.ee.pfanalyzer.model.util.ParameterSupport;

public class ParameterMasterNetwork implements IParameterMasterElement {

	private Network network;
	private boolean parametersAreRequired;
	
	public ParameterMasterNetwork(Network network, boolean parametersAreRequired) {
		this.network = network;
		this.parametersAreRequired = parametersAreRequired;
	}
	
	public Network getNetwork() {
		return network;
	}
	
	@Override
	public boolean isRequired(String parameterID) {
		return parametersAreRequired;
	}
	
	@Override
	public NetworkParameter getParameter(String parameterID, boolean create) {
		return network.getParameter(parameterID, create);
	}
	
	@Override
	public boolean hasParameterDefinition(String parameterID) {
		return false;// networks never contain parameter definitions
	}

	@Override
	public NetworkParameter getParameterValue(String parameterID) {
		return network.getParameterValue(parameterID);
	}
	
	@Override
	public NetworkParameter getOwnParameter(String parameterID) {
		return network.getOwnParameter(parameterID);
	}
	
	@Override
	public void removeOwnParameter(String parameterID) {
//		network.removeOwnParameter(parameterID);
	}
	
	@Override
	public void fireValueChanged(String parameterID, String oldValue, String newValue) {
		NetworkChangeEvent event = new NetworkChangeEvent(network, parameterID, oldValue, newValue);
		network.fireNetworkElementChanged(event);
	}

	@Override
	public ParameterSupport getParameterSupport() {
		return network;
	}
}
