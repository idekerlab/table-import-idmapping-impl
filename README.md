# table-import-idmapping-impl
A preliminary table-import-impl which allows id mapping

How to use id mapper for table import:
* go to: path/to/git/table-import-idmapping-impl/table-import-impl
* % mvn clean install
* Launch Cytoscape from developer console
* Then, File|Import|Table|File... and select a CSV file example with Uniprot identifiers
* In the preview table, right click on a column header and the Id Mapping sub-menu should appear below "List Delimiter" button


  
* TODO Id mapper need to be packaged better!
* TODO Problems with adding column to preview table persists.

 
* Id mapper for existing tables is here: https://github.com/cytoscape/idmap-impl 

