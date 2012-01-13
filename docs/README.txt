==============================
   ModuleInducer notes
   Author: Oksana Korol
   Date: January 13, 2012
==============================

---------------------
Other documentation:
---------------------

- The articles, data from which was used in ModuleInducer, are located in this folder
- More information of ModuleInducer design can be found in Oksana's Master's thesis, located in this folder in Korol_Oksana_2011_thesis.pdf
- Shorter information about ModuleInducer can be found in the article CIBCB_2012_Article_Korol_Turcotte.pdf, also in this folder

--------------------------
Some quirks to be aware of:
--------------------------

I. How to execute ModuleInducer (MI)

Web interface of MI is located in a separate project, called ModuleInducerWeb. It consists of one index.xhtml file, located in WebContent. To execute it in Eclipse, right-click on index.xhtml -> Run As -> Run on Server. If the server is properly set up in Eclipse, this operation will start the server (if it wasn't running before) and display the web interface (in internal Eclipse browser) or you can find it running in any browser on http://localhost:8080/ModuleInducerWeb/ (for default Glassfish configuration). NOTE: there is a quirk with system properties, associated with running ModuleInducerWeb. Read section II for more info.

Not all the MI functionality is exposed in web interface. For example, human data from the article by Palii et al. uses .bed files that are then mapped to hg18 human genome. This could only be executed by modifying MainEngine.java in ca.uottawa.okorol.bioinf.ModuleInducer package and running it as a java application (i.e. in Eclipse, right-click on the class -> Run As -> Java Application).

II. System properties

ModuleInducer uses ResourceBundle called SystemVariables to de-couple different deployment/development environments (i.e. machines, on which ModuleInducer runs). All path information to different scripts and programs, invoked in ModuleInducer, is collected in separate files, one for each environment. Each file contains a set of properties (variables) that is separated into 2 groups:
	1) set of common properties, like script names or a name of a result file, outputed by ModuleInducer
	2) system specific paths, like path to where swi prolog is installed
	
The property files are located in ca.uottawa.okorol.bioinf.ModuleInducer.properties package. Below is the list of all property files and description of the environments they were created for:
- SystemVariables_Mac_MT.properties - Marcel's machine (turcotte-1). NOTE: this file has not been tested for a while, so it might need to be modified.
- SystemVariables_Mac_OK.properties - turcotte-2 (my former machine in SITE 5004) in okoro103 account. NOTE: as of today (13.01.2012) this is a working development environment.
- SystemVariables_MacMini_induce.properties - MacMini, deployment machine.
- SystemVariables_MacMini_OK.properties - my MacMini.
- SystemVariables_Win_OK.properties - my Windows laptop (doesn't work for Dreme).

When MI is run directly (i.e. executing MainEngine, as described above), appropriate file is picked up automatically. BUT, for some reason, when running web interface (ModuleInducerWeb), the files are not picked up and the default SystemVariables.properties is used. Therefore, when running ModuleInducerWeb on a particular machine, SystemVariables.properties contents has to be replaced by the contents of an appropriate properties environment. For example, when running ModuleInducerWeb on turcotte-2 development machine, copy the contents of SystemVariables_Mac_OK.properties and overwrite the contents of SystemVariables.properties with it.


------------------------------
ModuleInducer deployment info:
------------------------------

* The app is running at: http://turcotte-4.eecs.uottawa.ca:8080/ModuleInducerWeb/index.xhtml
* To deploy a new war: http://turcotte-4.eecs.uottawa.ca:8080/manager/
* Backups of previously deployed wars: on turcotte-4 (deployment machine) in /Users/induce/ModuleInducerWars/
* Production deployment machine: turcotte-4.eecs.uottawa.ca (M's mini)
* Server: tomcat; installed: /usr/local/appserver/apache-tomcat/7.0.11/
* War deployed at: /usr/local/appserver/apache-tomcat/7.0.11/webapps/ModuleInducerWeb/
* Directory with requested jobs  (needs to be cleaned time after time): /usr/local/appserver/apache-tomcat/7.0.11/webapps/ModuleInducerWeb/work/
* Logs: /usr/local/appserver/apache-tomcat/7.0.11/logs/catalina.out

---------------------
Deployment checklist:
---------------------

- Backup old work directories (located in /usr/local/appserver/apache-tomcat/7.0.11/webapps/ModuleInducerWeb/work/)
- Copy the old war to the backup dir
- Eclipse: change Last updated date on the IU (it is done manually)
- Eclipse: Copy the contents of SystemVariables_MacMini_induce.properties to SystemVariables.properties
- Eclipse: Export war (Select ModuleInducerWeb project -> righ-click -> Export -> War file -> in the pop-up window, the Web project should be ModuleInducerWeb; select full path with the name of the new war, click Finish)
- Go to tomcat manager; undeploy old war; deploy new
- Check if the app is running
- Copy back the old work directories

