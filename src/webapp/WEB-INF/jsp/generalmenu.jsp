<%@page import="
    org.jmwiki.Environment,
    org.jmwiki.WikiBase,
    org.jmwiki.servlets.WikiServlet,
    org.jmwiki.utils.Encryption,
    org.jmwiki.utils.Utilities
" %>

<div class="menu">
<form method="POST" action="Wiki">
<table style="width: 100%; border: 0px solid;">
<tr>
    <td class="menu" align=left>
        | <span class="menuinactive"><f:message key="menu.editpage"/></span>
        | <span class="menuinactive"><f:message key="menu.attach"/></span>
        | <a href="<jmwiki:link value="Special:RecentChanges" />"><f:message key="generalmenu.recentchanges"/></a>
        | <a href='<%= JSPUtils.encodeURL(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC)) %>'><%= Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) %></a>
        | <a href="<jmwiki:link value="Special:Search" />"><f:message key="generalmenu.search"/></a>
        | <span class="menuinactive"><f:message key="menu.printablepage"/></span>
        |
    </td>
    <td class="menu" align=right>
        <input type="hidden" name="action" value="<%= WikiServlet.ACTION_MENU_JUMP %>"/>
        <input name="text" size="20"/>
        <input type="submit" name="search" value='<f:message key="generalmenu.search"/>'/>
        <input type="submit" name="jumpto" value='<f:message key="generalmenu.jumpto"/>'/>
        &nbsp;
    </td>
</tr>
</table>
</form>
</div>
