<%--

  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the latest version of the GNU Lesser General
  Public License as published by the Free Software Foundation;

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program (LICENSE.txt); if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

--%>

<script type="text/javascript">
/*<![CDATA[*/
document.writeln("<div id='toolbar'>");
addButton('../images/button_bold.png','<f:message key="edit.button.bold"/>','\'\'\'','\'\'\'','<f:message key="edit.button.bold.text"/>');
addButton('../images/button_italic.png','<f:message key="edit.button.italic"/>','\'\'','\'\'','<f:message key="edit.button.italic.text"/>');
addButton('../images/button_underline.png','<f:message key="edit.button.underline"/>','<u>','</u>','<f:message key="edit.button.underline.text"/>');
addButton('../images/button_link.png','<f:message key="edit.button.internal.link"/>','[[',']]','<f:message key="edit.button.internal.link.text"/>');
addButton('../images/button_extlink.png','<f:message key="edit.button.external.link"/>','[',']','<f:message key="edit.button.external.link.text"/>');
addButton('../images/button_headline.png','<f:message key="edit.button.head2"/>','\n== ',' ==\n','<f:message key="edit.button.head2.text"/>');
addButton('../images/button_image.png','<f:message key="edit.button.image"/>','[[Image:',']]','<f:message key="edit.button.image.text"/>');
addButton('../images/button_nowiki.png','<f:message key="edit.button.nowiki"/>','<nowiki>','</nowiki>','<f:message key="edit.button.nowiki.text"/>');
addButton('../images/button_sig.png','<f:message key="edit.button.signature"/>','--~~~~','','');
addButton('../images/button_hr.png','<f:message key="edit.button.line"/>','\n----\n','','');
document.writeln("</div>");
/*]]>*/ 
</script>
