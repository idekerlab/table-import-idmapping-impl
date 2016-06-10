# table-import-idmapping-impl
A preliminary table-import-impl which allows id mapping

How to use id mapper for table import:
* go to:
* path/to/git/table-import-idmapping-impl/table-import-impl
* % mvn clean install
* Launch Cytoscape from developer console
* then, File|Import|Table|File...
* select a CSV file example with Uniprot identifiers
* in the preview table, right click on a column header 
* Id Mapping sub-menu should appear below "List Delimiter" button.
  
  
* TODO Id mapper need to be packaged better!
* TODO Problems with adding column to preview table persists.
 
* Id mapper is here: https://github.com/cytoscape/idmap-impl 

