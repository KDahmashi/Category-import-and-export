<%@include file="init.jsp"%>
<portlet:resourceURL var="exportURL" ></portlet:resourceURL>
<portlet:actionURL var="importURL" >
   <portlet:param name="name" value="import"/>
</portlet:actionURL>
<liferay-ui:panel id="export" title="Export..." collapsible="<%=true %>" extended="<%= true %>" persistState="<%= false %>">
<form  method="post" name="searchGroupForm" id="searchGroupForm" >
		   <input type="button" title="Export" id="exportBT" value="Export" onclick="search()"/>
</form>
</liferay-ui:panel>

<liferay-ui:panel id="import" title="Import..." collapsible="true" extended="<%= true %>" persistState="<%= false %>">
<form  method="post" name="importForm" id="importForm" enctype="multipart/form-data" >
		<div id="errorMSG" style="display: none; color: red; "><liferay-ui:message key="Please select (XML)file only..."/> </div>
		<input type="file" id="file" name="file" accept="text/xml"/> 
	    <input type="button" title="Import" id="importBT" value="Import" onclick="importFile()"/>
</form>
</liferay-ui:panel>

<script type="text/javascript">
function importFile(){
	document.getElementById("errorMSG").style.display = "none";
	var value = document.importForm.file.value;
	if(value!="" && value.substr(value.lastIndexOf('.'))==".xml"){
		document.importForm.action ='<%=importURL%>';
    	document.importForm.submit();
	} else{
		 document.getElementById("errorMSG").style.display = "block";
	}
}
function search() {
   document.searchGroupForm.action ='<%=exportURL%>';
   document.searchGroupForm.submit();
} 
</script>