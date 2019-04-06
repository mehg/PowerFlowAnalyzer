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
function [ pfa_element ] = pfa_create_element_bus( pfa_network )
%PFA_CREATE_BUS Summary of this function goes here
%   Detailed explanation goes here

pfa_element = net.ee.pfanalyzer.model.Bus(pfa_network, 0);
% pfa_element = net.ee.pfanalyzer.model.NetworkElement(pfa_network);

end

