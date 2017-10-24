%% Initialisation Script for MatpowerGUI
% Technische Universitšt Berlin 
% Fachgebiet Energieversorgungsnetze und Integration Erneuerbarer Energien (SENSE)
% Chair of Sustainable Electric Networks and Sources of Energy

% Markus Gronau

function PowerFlowAnalyzer_MAIN()

if ispfviewerrunning == false
	pfviewer = net.ee.pfanalyzer.PowerFlowAnalyzer(net.ee.pfanalyzer.PowerFlowAnalyzer.MATLAB_ENVIRONMENT);
    pfviewer.setWorkingDirectory(pwd); % direct connection to viewer is allowed here
%     clear pfviewer; % remove variable from workspace
else
% 	disp('Matpower User Interface is already running');
	showpfviewer();
end

end