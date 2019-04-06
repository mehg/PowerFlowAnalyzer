%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright 2019 Markus Gronau
% 
% This file is part of PowerFlowAnalyzer.
% 
% Licensed under the Apache License, Version 2.0 (the "License");
% you may not use this file except in compliance with the License.
% You may obtain a copy of the License at
% 
%     http://www.apache.org/licenses/LICENSE-2.0
% 
% Unless required by applicable law or agreed to in writing, software
% distributed under the License is distributed on an "AS IS" BASIS,
% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
% See the License for the specific language governing permissions and
% limitations under the License.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function [ parameter ] = createparameter( scenario, parameter_ID, label, parameterType )
%CREATEPARAMETER Summary of this function goes here
%   Detailed explanation goes here

% check if parameter 'parameterType' was set
% and use default value if necessary
if (exist('parameterType','var') == false)
   parameterType = 'list'; % default value
end

% create parameter
parameter = net.ee.pfanalyzer.model.scenario.ScenarioParameter(parameter_ID, label, parameterType);
scenario.addParameter(parameter);

end

