function [ data ] = import_datasource_excel( input_file, table_name, ...
    data_index_column, data_index_row, data_values_row )
%PFA_READ_EXCEL Summary of this function goes here
%   Detailed explanation goes here

MAIN_COLUMN = 'A';

EXCEL_COLUMN_NAMES = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

[num, txt] = xlsread_fast(input_file, table_name, strcat(MAIN_COLUMN, ':', MAIN_COLUMN));
rows = length(txt) - (data_values_row - 1) + length(num);
% fprintf('Data rows: %i\n', rows);

[~, parameters] = xlsread_fast(input_file, table_name, strcat(num2str(data_index_row), ':', num2str(data_index_row)));

for column = 1:length(parameters)
    range = char(strcat(EXCEL_COLUMN_NAMES(column), num2str(data_values_row), ...
        ':', EXCEL_COLUMN_NAMES(column), num2str(data_values_row - 1 + rows)));
    field_name = char(parameters(column));
    % skip columns with empty parameter
    if isempty(field_name)
        continue;
    end
    [num, txt] = xlsread_fast(input_file, table_name, range);
    if isempty(num)
        data.(field_name) = txt; % cell2mat(txt);
    elseif isempty(txt)
        data.(field_name) = num;
    else
        warning('%s: %s: Mixed data for parameter "%s" (range %s)', ...
            input_file, table_name, field_name, range);
    end
    if length(data.(field_name)) ~= rows
        warning('%s: %s: Missing data for parameter "%s" (range %s)', ...
            input_file, table_name, field_name, range);
    end
end

end

