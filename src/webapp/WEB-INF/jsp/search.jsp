<form name="searchForm" method="post" action="<jmwiki:link value="Special:Search" />">
<f:message key="search.for"/><input type="text" name="text" value="<c:out value="${text}" />">  <input type="submit" name="Submit" value="<f:message key="search.search"/>">
<p>&nbsp;</p>
<f:message key="search.hints"/>
<input type="hidden" name="action" value="<%= WikiServlet.ACTION_SEARCH %>"/>
</form>
<p>&nbsp;</p>
<font size="-1"><i>search powered by</i></font> <a href="http://jakarta.apache.org/lucene"><img src="../images/lucene_green_100.gif" alt="Lucene" border="0" /></a>
<script language="JavaScript">document.searchForm.text.focus();</script>
