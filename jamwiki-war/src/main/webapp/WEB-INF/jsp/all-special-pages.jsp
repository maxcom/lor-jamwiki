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
<%@ page errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div id="special">

<h3><f:message key="specialpages.heading.allusers" /></h3>

<ul>
<li><jamwiki:link value="Special:TopicsAdmin"><f:message key="specialpages.caption.topicsadmin" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Categories"><f:message key="specialpages.caption.categories" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Filelist"><f:message key="specialpages.caption.filelist" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Imagelist"><f:message key="specialpages.caption.imagelist" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Allpages"><f:message key="specialpages.caption.allpages" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Listusers"><f:message key="specialpages.caption.listusers" /></jamwiki:link></li>
<li><jamwiki:link value="Special:OrphanedPages"><f:message key="specialpages.caption.orphanedpages" /></jamwiki:link></li>
<li><jamwiki:link value="Special:RecentChanges"><f:message key="specialpages.caption.recentchanges" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Search"><f:message key="specialpages.caption.search" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Upload"><f:message key="specialpages.caption.upload" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Login"><f:message key="specialpages.caption.login" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Logout"><f:message key="specialpages.caption.logout" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Account"><f:message key="specialpages.caption.account" /></jamwiki:link></li>
</ul>

<h3><f:message key="specialpages.heading.administrative" /></h3>

<ul>
<li><jamwiki:link value="Special:Admin"><f:message key="specialpages.caption.admin" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Maintenance"><f:message key="specialpages.caption.maintenance" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Roles"><f:message key="specialpages.caption.roles" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Translation"><f:message key="specialpages.caption.translation" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Setup"><f:message key="specialpages.caption.setup" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Upgrade"><f:message key="specialpages.caption.upgrade" /></jamwiki:link></li>
</ul>

</div>