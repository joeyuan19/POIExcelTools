Version History
===============

* 0.5.2 -- **Features Added**
	* Additions to POI Extension
		* Tools for copying sheets
			* Methods for copying a section of one Worksheet onto another
			  By coordinates with optional offset
		* Cell and Row retrieval methods
		* Workbook opening
			* CSV handling
			* TDV handling
	* Additions to File tools
		* Regex file searching
		* Rudimentary File Detection _needs improvement_
	* Helper Class created
		* created to supply helper methods where needed
		* contains a comparison of string dates
		* contains regex searching for dates
		* contains parsers for .csv and .tdv files
* 0.5.01 -- **Bug fixes**
* 0.5.0 -- **Initial Saved Version**
	* Tools for file manipulation
		* Path joining
		* File opening
		* File locating
		* Extension assertion
	* Tools for POI manipulation of Excel Files including
		* Column Name and Row conversion
		* Cell information copying
		* Searching
		* Sheet cell type checking
		* Index validation
		* Row/Column formula refactoring
		* Cell information
		* Saving Workbook
