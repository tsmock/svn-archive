function box_change(ob) {
  ob.form.submit();
}

function box_click(boxname, subboxname) {
  var ob;

  call_hooks("list_request", boxname, subboxname);

  if(!subboxname)
    ob=document.getElementsByName(boxname);
  else
    ob=document.getElementsByName(boxname+"|"+subboxname);

  ob=ob[0];
  ob.checked=!ob.checked;

  box_change(ob);
}

function list_entry(ob) {
//  if(sizeof($app)>0)
//    $app=" (".implode(", ", $app).")";
//  else
//    $app="";

  return "<li class='listitem' id='list_"+ob.getAttribute("id")+"'><element id='"+ob.getAttribute("id")+"'><a href='#"+ob.getAttribute("id")+"' onMouseOver='set_highlight([\""+ob.getAttribute("id")+"\"])' onMouseOut='unset_highlight()'>"+ob.getAttribute("name")+"</a></element></li>\n";
}
