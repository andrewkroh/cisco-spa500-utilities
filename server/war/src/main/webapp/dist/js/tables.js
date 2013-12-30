$(document).ready(function(){
    $("table").tablesorter();
   
    $.ajax({
        type: "GET",
        dataType: "json",
        url: "/phones/rest/app/directory/get",
        success: function( resp ){
            for(var i = 0; i < resp.directoryEntries.length; i++)
            {
                var tableRow = 
                    "<tr>" +
                      "<td>" + resp.directoryEntries[i].name + "</td>" +
                      "<td>" + resp.directoryEntries[i].telephoneNumber + "</td>" +
                    "</tr>";
                $('#directory-table tbody').append(tableRow);
            }

            $("table").trigger("update");
        }
    });
});
