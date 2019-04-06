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
function [ value ] = pfa_param_boolean( ...
    network_element, parameter_name, default_value )
%PFA_PARAM_BOOL Summary of this function goes here
%   Detailed explanation goes here

jBoolean = network_element.getBooleanParameter(parameter_name);
if not(isempty(jBoolean))
    value = jBoolean.booleanValue();
else
    value = default_value;
end

end

