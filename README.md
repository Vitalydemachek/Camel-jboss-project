# Camel-jboss-project
Integration with accounting systems

Integration point №1 

Method to take project's code and tasks's number.
Method name – GetProjectList;
Method tipe – Get.
Method is for task directory to be updated in SAP app.

incoming param:
1. name: ProjectCode*, tipe: STRING, data desc: code of project;

return param:
1. name: ProjectCode*, tipe: STRING, data desc: code of project;
2. name: NumberOfTask*, tipe: STRING, data desc: task number referred to business trips expenses limits on current project;
3. name: Status*, tipe: NUMBER, data desc: responce codes 200 – OK, 400 – Bad Request, 503 - Service Unavailable;
* is mandatory

request message:
{
"ProjectCode":"210A"
}

responce message:
{

"ProjectCode" : "210A",
"NumberOfTask": "15S",
"Status" : 200

}
