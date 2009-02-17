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
<c:if test="${!badinput}">
	<c:if test="${empty diffs}"><div class="message"><fmt:message key="diff.nochange" /></div></c:if>
	<c:if test="${!empty diffs}">
<div id="diff">
		<c:set var="previousLineNumber" value="-10" />
		<c:forEach items="${diffs}" var="diff">
			<c:if test="${diff.lineNumber > (previousLineNumber + 1)}">
<div class="diff-line"><fmt:message key="diff.line" /> <c:out value="${diff.lineNumber}" />:</div>
			</c:if>
<div class="diff-entry">
			<c:if test="${!empty diff.oldLine && diff.change}">
	<div class="diff-indicator">-</div>
	<div class="diff-delete"><c:out value="${diff.oldLine}" />&#160;</div>
			</c:if>
			<c:if test="${empty diff.oldLine || !diff.change}">
	<div class="diff-indicator">&#160;</div>
	<div class="diff-unchanged"><c:out value="${diff.oldLine}" />&#160;</div>
			</c:if>
			<c:if test="${!empty diff.newLine && diff.change}">
	<div class="diff-indicator">+</div>
	<div class="diff-add"><c:out value="${diff.newLine}" />&#160;</div>
			</c:if>
			<c:if test="${empty diff.newLine || !diff.change}">
	<div class="diff-indicator">&#160;</div>
	<div class="diff-unchanged"><c:out value="${diff.newLine}" />&#160;</div>
			</c:if>
	<div class="clear"></div>
</div>
			<c:set var="previousLineNumber" value="${diff.lineNumber}" />
		</c:forEach>
</div>
	</c:if>
</c:if>
<c:if test="${badinput=='true'}">
	<fmt:message key="diff.badinput" />
</c:if>
