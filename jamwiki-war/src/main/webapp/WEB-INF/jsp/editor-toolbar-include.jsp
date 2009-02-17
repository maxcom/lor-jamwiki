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
addButton('../images/button_bold.png','<fmt:message key="edit.button.bold"/>','\'\'\'','\'\'\'','<fmt:message key="edit.button.bold.text"/>');
addButton('../images/button_italic.png','<fmt:message key="edit.button.italic"/>','\'\'','\'\'','<fmt:message key="edit.button.italic.text"/>');
addButton('../images/button_underline.png','<fmt:message key="edit.button.underline"/>','<u>','</u>','<fmt:message key="edit.button.underline.text"/>');
addButton('../images/button_link.png','<fmt:message key="edit.button.internal.link"/>','[[',']]','<fmt:message key="edit.button.internal.link.text"/>');
addButton('../images/button_extlink.png','<fmt:message key="edit.button.external.link"/>','[',']','<fmt:message key="edit.button.external.link.text"/>');
addButton('../images/button_headline.png','<fmt:message key="edit.button.head2"/>','\n== ',' ==\n','<fmt:message key="edit.button.head2.text"/>');
addButton('../images/button_image.png','<fmt:message key="edit.button.image"/>','[[Image:',']]','<fmt:message key="edit.button.image.text"/>');
addButton('../images/button_nowiki.png','<fmt:message key="edit.button.nowiki"/>','<nowiki>','</nowiki>','<fmt:message key="edit.button.nowiki.text"/>');
addButton('../images/button_sig.png','<fmt:message key="edit.button.signature"/>','--~~~~','','');
addButton('../images/button_hr.png','<fmt:message key="edit.button.line"/>','\n----\n','','');
document.writeln("</div>");
/*]]>*/ 
</script>
