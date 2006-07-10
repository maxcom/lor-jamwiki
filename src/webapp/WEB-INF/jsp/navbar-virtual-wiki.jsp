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
<f:message key="virtualwiki.${virtualWiki}.name" var="wikiname"/>
<%-- FIXME: clean this up --%>
<%
if (WikiBase.getVirtualWikiCount() > 1) {
%>
<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_ALLOW_VWIKI_LIST) && WikiBase.getVirtualWikiCount() > 1) {
%>
<a href="<jamwiki:link value="Special:VirtualWikiList" />">
<%
}
%>
<f:message key="common.wiki"/>
<%
if (Environment.getBooleanValue(Environment.PROP_TOPIC_ALLOW_VWIKI_LIST) && WikiBase.getVirtualWikiCount() > 1) {
%>
</a>
<%
}
%>
:
<jamwiki:link value="StartingPoints"><c:out value="${virtualWiki}"/></jamwiki:link> :
<%
}
%>