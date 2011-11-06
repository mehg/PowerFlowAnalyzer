package net.ee.pfanalyzer.ui;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import net.ee.pfanalyzer.model.AbstractNetworkElement;
import net.ee.pfanalyzer.model.Network;
import net.ee.pfanalyzer.model.PowerFlowCase;
import net.ee.pfanalyzer.model.data.DataViewerData;
import net.ee.pfanalyzer.model.diagram.DiagramSheetProperties;
import net.ee.pfanalyzer.model.util.ModelDBUtils;
import net.ee.pfanalyzer.ui.dataviewer.DataViewerConfiguration;
import net.ee.pfanalyzer.ui.dataviewer.DataViewerContainer;
import net.ee.pfanalyzer.ui.dataviewer.DataViewerDialog;
import net.ee.pfanalyzer.ui.dataviewer.INetworkDataViewer;
import net.ee.pfanalyzer.ui.dataviewer.SelectViewerDialog;
import net.ee.pfanalyzer.ui.diagram.PowerFlowDiagram;
import net.ee.pfanalyzer.ui.map.NetworkViewer;
import net.ee.pfanalyzer.ui.model.ElementPanelController;
import net.ee.pfanalyzer.ui.table.DataTable;
import net.ee.pfanalyzer.ui.util.ClosableTabbedPane;
import net.ee.pfanalyzer.ui.util.IActionUpdater;
import net.ee.pfanalyzer.ui.util.TabListener;

public class PowerFlowViewer extends JPanel implements INetworkElementSelectionListener {

	private PowerFlowCase powerFlowCase;
	private Network network;
	
	private NetworkElementSelectionManager selectionManager;
	private ViewerTabbedPane bottomViewers, leftViewers;
	private List<ViewerFrame> viewerFrames = new ArrayList<ViewerFrame>();
	private JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private ElementPanelController panelController;

	private List<IActionUpdater> actionUpdater = new ArrayList<IActionUpdater>();
	private JLabel networkDescriptionLabel = new JLabel();
	
	public PowerFlowViewer(PowerFlowCase caze, Network network) {
		super(new BorderLayout());
		this.powerFlowCase = caze;
		this.network = network;
		selectionManager = new NetworkElementSelectionManager();
		panelController = new ElementPanelController(getNetwork());
		JPanel dataPanel = new JPanel(new BorderLayout());
		dataPanel.add(panelController, BorderLayout.CENTER);
		bottomViewers = new ViewerTabbedPane();
		leftViewers = new ViewerTabbedPane();
		horizontalSplitPane.setLeftComponent(leftViewers.getComponent());
		horizontalSplitPane.setRightComponent(dataPanel);
		horizontalSplitPane.setContinuousLayout(true);
		horizontalSplitPane.setOneTouchExpandable(true);
		horizontalSplitPane.setDividerLocation(300);
		horizontalSplitPane.setDividerSize(10);
		verticalSplitPane.setTopComponent(horizontalSplitPane);
		verticalSplitPane.setContinuousLayout(true);
		verticalSplitPane.setOneTouchExpandable(true);
		verticalSplitPane.setDividerSize(10);
		add(networkDescriptionLabel, BorderLayout.NORTH);
		add(verticalSplitPane, BorderLayout.CENTER);
		
		networkDescriptionLabel.setText(network.getDescription());
		networkDescriptionLabel.setFont(networkDescriptionLabel.getFont().deriveFont(14f));
		
		// add data viewers
		for (DataViewerData viewerData : getPowerFlowCase().getDataViewerData()) {
			addDataViewer(new DataViewerConfiguration(viewerData), false);
		}

		getNetwork().addNetworkChangeListener(panelController);
		addNetworkElementSelectionListener(panelController);
		addNetworkElementSelectionListener(this);
	}
	
	public void dispose() {
		getNetwork().removeNetworkChangeListener(panelController);
		removeNetworkElementSelectionListener(panelController);
		removeNetworkElementSelectionListener(this);
		leftViewers.dispose();
		bottomViewers.dispose();
		for (ViewerFrame frame : viewerFrames) {
			frame.closeFrame(false);
		}
		viewerFrames.clear();
	}
	
	public void addViewer() {
		SelectViewerDialog dialog1 = new SelectViewerDialog(SwingUtilities.getWindowAncestor(this));
		dialog1.showDialog(-1, -1);
		if(dialog1.isCancelPressed())
			return;
		DataViewerConfiguration configuration = new DataViewerConfiguration(dialog1.getSelectedViewer());
		DataViewerDialog dialog2 = new DataViewerDialog(SwingUtilities.getWindowAncestor(this), 
				"Add viewer", configuration, getPowerFlowCase(), true);
		dialog2.showDialog(-1, -1);
		if(dialog2.isCancelPressed())
			return;
		addDataViewer(configuration, true);
		getPowerFlowCase().getDataViewerData().add(configuration.getData());
	}
	
	private void addDataViewer(DataViewerConfiguration viewerData, boolean selectTab) {
		INetworkDataViewer viewer = null;
		if("viewer.table.type_filter".equals(viewerData.getModelID()))
			viewer = new DataTable(viewerData);
//		else if("viewer.diagram.bar".equals(viewerData.getModelID()))
//			viewer = new PowerFlowDiagram(viewerData);
		else if("viewer.network.map".equals(viewerData.getModelID()))
			viewer = new NetworkViewer(getNetwork(), viewerData);
		if(viewer == null)
			return;
		viewer.setData(getNetwork());
		viewer.refresh();
		ViewerTabbedPane tabPane = getViewerTab(viewer);
		if(tabPane == null) {
			ViewerFrame frame = new ViewerFrame(viewer);
			viewerFrames.add(frame);
//			frame.toFront();
		} else {
			tabPane.addViewer(viewer);
			if(selectTab)
				tabPane.selectLastTab();
			if(verticalSplitPane.getBottomComponent() == null)
				verticalSplitPane.setBottomComponent(bottomViewers.getComponent());
			if(bottomViewers.getComponent().getHeight() == 0) {
				verticalSplitPane.setDividerLocation(400);
			}
		}
		addNetworkElementSelectionListener(viewer);
		getNetwork().addNetworkChangeListener(viewer);
	}
	
	private ViewerTabbedPane getViewerTab(INetworkDataViewer viewer) {
		String position = viewer.getViewerConfiguration().getTextParameter("POSITION", "bottom");
		ViewerTabbedPane tabPane;
		if(position.equals("bottom"))
			tabPane = bottomViewers;
		else if(position.equals("left"))
			tabPane = leftViewers;
		else if(position.equals("free"))
			tabPane = null;
		else
			tabPane = bottomViewers;
		return tabPane;
	}
	
//	private void addDiagram(String label, String elementID, String parameterID) {
//		PowerFlowDiagram diagram = new PowerFlowDiagram(elementID, parameterID);
//		diagram.setTitle(label);
//		diagram.setData(getNetwork());
//		diagram.refresh();
//		viewers.add(diagram);
//		dataTabs.addTab(label, new JScrollPane(diagram.getComponent()));
//		getNetwork().addNetworkChangeListener(diagram);
//	}
	
//	public void addDiagramSheet(DiagramSheetProperties props) {
//		DiagramSheet sheet = new DiagramSheet(props);
//		diagrams.add(sheet);
//		diagramTabs.addTab(props.getTitle(), new JScrollPane(sheet));
//		diagramTabs.selectLastTab();
//	}
	
//	public void setCurrentDiagramSheet(DiagramSheetProperties props) {
//		if(getCurrentSheet() == null)
//			return;
//		getCurrentSheet().setProperties(props);
//		diagramTabs.setTitleAt(diagramTabs.getSelectedIndex(), props.getTitle());
//	}
//	
//	public DiagramSheetProperties getCurrentDiagramSheetProperties() {
//		if(getCurrentSheet() == null)
//			return null;
//		return getCurrentSheet().getProperties();
//	}
	
//	private DiagramSheet getCurrentSheet() {
//		if(diagramTabs.getSelectedIndex() == -1)
//			return null;
//		return diagrams.get(diagramTabs.getSelectedIndex());
//	}
//	
//	public boolean hasDiagramSheet() {
//		return diagramTabs.hasTabs();
//	}
	
	public PowerFlowCase getPowerFlowCase() {
		return powerFlowCase;
	}
	
	public Network getNetwork() {
		return network;
	}

	public ElementPanelController getPanelController() {
		return panelController;
	}
	
	public void updateNetworkDescription() {
		networkDescriptionLabel.setText(network.getDescription());
	}
	
	public NetworkElementSelectionManager getSelectionManager() {
		return selectionManager;
	}

	@Override
	public void selectionChanged(Object data) {
		if(data == null)
			return;
		// show tab which contains the new selection
		if(data instanceof AbstractNetworkElement) {
			if(leftViewers.selectViewer((AbstractNetworkElement) data))
				return;
			bottomViewers.selectViewer((AbstractNetworkElement) data);
		}
	}
	
	public void addNetworkElementSelectionListener(INetworkElementSelectionListener listener) {
		getSelectionManager().addNetworkElementSelectionListener(listener);
	}
	
	public void removeNetworkElementSelectionListener(INetworkElementSelectionListener listener) {
		getSelectionManager().removeNetworkElementSelectionListener(listener);
	}

	public void addActionUpdateListener(IActionUpdater listener) {
		actionUpdater.add(listener);
		getSelectionManager().addActionUpdateListener(listener);
	}
	
	public void removeActionUpdateListener(IActionUpdater listener) {
		actionUpdater.remove(listener);
		getSelectionManager().removeActionUpdateListener(listener);
	}
	
	private void fireActionUpdate() {
		for (IActionUpdater listener : actionUpdater) {
			listener.updateActions();
		}
	}

	public void updateTabTitles() {
		leftViewers.updateTabs("left");
		bottomViewers.updateTabs("bottom");
		updateFrames();
		fireActionUpdate();
	}
	
	private void updateFrames() {
		boolean updateAgain = false;
		for (ViewerFrame frame : viewerFrames) {
			if(frame.updateFrame() == false) {
				updateAgain = true;
				break;
			}
		}
		if(updateAgain)
			updateFrames();
	}
	
	private void removeViewer(INetworkDataViewer viewer, boolean removeFromCase) {
		removeNetworkElementSelectionListener(viewer);
		getNetwork().removeNetworkChangeListener(viewer);
		if(removeFromCase)
			getPowerFlowCase().getDataViewerData().remove(viewer.getViewerConfiguration().getData());
	}
	
	class ViewerTabbedPane extends ClosableTabbedPane {
		private List<INetworkDataViewer> viewers = new ArrayList<INetworkDataViewer>();
		ViewerTabbedPane() {
			setTabListener(new TabListener() {
				@Override
				public boolean tabClosing(int tabIndex) {
					int choice = JOptionPane.showConfirmDialog(PowerFlowViewer.this, 
							"Do you want to remove this viewer?", "Close viewer", JOptionPane.YES_NO_OPTION);
					return choice == JOptionPane.YES_OPTION;
				}
				@Override
				public void tabClosed(int tabIndex) {
					INetworkDataViewer viewer = viewers.get(tabIndex);
					viewers.remove(tabIndex);
					removeViewer(viewer, true);
					fireActionUpdate();
				}
				@Override
				public void tabOpened(int tabIndex) {
				}
			});
		}
		
		private boolean selectViewer(AbstractNetworkElement element) {
			String modelID = element.getModelID();
			if(modelID == null || modelID.isEmpty())
				return false;
			for (int i = 0; i < viewers.size(); i++) {
				if(modelID.startsWith(viewers.get(i).getViewerConfiguration().getElementFilter())) {
					setSelectedIndex(i);
					return true;
				}
			}
			return false;
		}
		
		private void addViewer(INetworkDataViewer viewer) {
			viewers.add(viewer);
			addTab(viewer.getViewerConfiguration().getTitle(), 
					new DataViewerContainer(viewer, PowerFlowViewer.this));
			
		}
		
		public void dispose() {
			for (INetworkDataViewer viewer : viewers) {
				viewer.dispose();
				removeViewer(viewer, false);
			}
		}
		
		public void updateTabs(String tabPosition) {
			boolean updateAgain = false;
			// update tab titles for viewers
			for (int i = 0; i < viewers.size(); i++) {
				DataViewerConfiguration conf = viewers.get(i).getViewerConfiguration();
				String position = conf.getTextParameter("POSITION", "bottom");
				// check if position is still correct
				if(position.equals(tabPosition)) {
					setTitleAt(i, conf.getTitle());
				} else {
					removeViewer(viewers.get(i), false);
					viewers.remove(i);
					getTabbedPane().remove(i);
					addDataViewer(conf, true);
					updateAgain = true;
					break;
				}
			}
			if(updateAgain)
				updateTabs(tabPosition);
		}
	}
	
	public static String getFrameTitle(DataViewerConfiguration viewerConfiguration, Network network) {
		return viewerConfiguration.getTitle() + " - " + network.getDisplayName();
	}
	
	class ViewerFrame extends JFrame {
		
		private INetworkDataViewer viewer;
		
		ViewerFrame(INetworkDataViewer viewer) {
			super(getFrameTitle(viewer.getViewerConfiguration(), getNetwork()));
			this.viewer = viewer;
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					int choice = JOptionPane.showConfirmDialog(ViewerFrame.this, 
							"Do you want to remove this viewer?", "Close viewer", JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.YES_OPTION) {
						closeFrame(true);
					}
				}
				@Override
				public void windowClosed(WindowEvent e) {
				}
			});
			addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					ViewerFrame.this.viewer.getViewerConfiguration().setParameter(
							ModelDBUtils.WIDTH_PARAMETER, getWidth());
					ViewerFrame.this.viewer.getViewerConfiguration().setParameter(
							ModelDBUtils.HEIGHT_PARAMETER, getHeight());
				}
			});
			
			getContentPane().add(new DataViewerContainer(viewer, PowerFlowViewer.this));
			pack();
			int width = viewer.getViewerConfiguration().getIntParameter(ModelDBUtils.WIDTH_PARAMETER, 300);
			int height = viewer.getViewerConfiguration().getIntParameter(ModelDBUtils.HEIGHT_PARAMETER, 300);
			setSize(width, height);
			setVisible(true);
		}
		
		private void closeFrame(boolean removeFromCase) {
			viewer.dispose();
			removeViewer(viewer, removeFromCase);
			if(removeFromCase)
				viewerFrames.remove(ViewerFrame.this);
			setVisible(false);
			dispose();
			fireActionUpdate();
		}
		
		public boolean updateFrame() {
			DataViewerConfiguration conf = viewer.getViewerConfiguration();
			String position = conf.getTextParameter("POSITION", "bottom");
			// check if position is "free"
			if(position.equals("free")) {
				setTitle(getFrameTitle(conf, getNetwork()));
				return true;
			} else {
				closeFrame(false);
				viewerFrames.remove(this);
				addDataViewer(conf, true);
				return false;
			}
		}
		
		PowerFlowViewer getPowerFlowViewer() {
			return PowerFlowViewer.this;
		}
	}
	
	class DiagramSheet extends Box {

		private DiagramSheetProperties sheetProps;
		private List<PowerFlowDiagram> diagrams = new ArrayList<PowerFlowDiagram>();
		
		public DiagramSheet(DiagramSheetProperties props) {
			super(BoxLayout.Y_AXIS);
			setProperties(props);
		}
		
		public void setProperties(DiagramSheetProperties props) {
//			for (PowerFlowDiagram diagram : diagrams)
//				diagram.setNetworkElementSelectionListener(null);
			diagrams.clear();
			removeAll();
			sheetProps = props;
//			for (int i = 0; i < props.getBusDataFieldsCount(); i++) {
//				if(props.hasBusDataDiagram(i))
//					addDiagram(new BusDataDiagram(network, i));
//			}
//			for (int i = 0; i < props.getBranchDataFieldsCount(); i++) {
//				if(props.hasBranchDataDiagram(i))
//					addDiagram(new BranchDataDiagram(network, i));
//			}
//			for (int i = 0; i < props.getGeneratorDataFieldsCount(); i++) {
//				if(props.hasGeneratorDataDiagram(i))
//					addDiagram(new GeneratorDataDiagram(network, i));
//			}
			revalidate();
		}
		
		public void addDiagram(PowerFlowDiagram diagram) {
			diagrams.add(diagram);
			add(diagram);
//			diagram.setNetworkElementSelectionListener(PowerFlowViewer.this);
		}
		
		public DiagramSheetProperties getProperties() {
			return sheetProps;
		}
	}
}
